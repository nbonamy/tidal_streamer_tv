package fr.bonamy.tidalstreamer.auth

import android.app.AlertDialog
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.TextView
import androidx.leanback.app.VerticalGridSupportFragment
import androidx.leanback.widget.ArrayObjectAdapter
import androidx.leanback.widget.OnItemViewClickedListener
import androidx.leanback.widget.Presenter
import androidx.leanback.widget.Row
import androidx.leanback.widget.RowPresenter
import androidx.leanback.widget.VerticalGridPresenter
import androidx.lifecycle.lifecycleScope
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import fr.bonamy.tidalstreamer.MainActivity
import fr.bonamy.tidalstreamer.R
import fr.bonamy.tidalstreamer.api.ApiResult
import fr.bonamy.tidalstreamer.api.ApiRetrofitClient
import fr.bonamy.tidalstreamer.api.AuthClient
import fr.bonamy.tidalstreamer.models.User
import fr.bonamy.tidalstreamer.utils.Configuration
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class AuthFragment : VerticalGridSupportFragment() {

  private lateinit var authClient: AuthClient
  private lateinit var configuration: Configuration
  private var pollingJob: Job? = null

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setupUI()
    authClient = AuthClient(requireContext())
    configuration = Configuration(requireContext())
    loadUsers()
  }

  private fun setupUI() {
    title = "Select User"
    val gridPresenter = VerticalGridPresenter()
    gridPresenter.numberOfColumns = 3
    setGridPresenter(gridPresenter)

    setOnItemViewClickedListener(ItemViewClickedListener())
  }

  private fun loadUsers() {
    lifecycleScope.launch {
      when (val result = authClient.fetchUsers()) {
        is ApiResult.Success -> {
          val adapter = ArrayObjectAdapter(UserCardPresenter())

          // Add existing users
          result.data.forEach { user ->
            adapter.add(user)
          }

          // Add "new user" option
          adapter.add(UserCardPresenter.NEW_USER_MARKER)

          setAdapter(adapter)
        }

        is ApiResult.Error -> {
          Log.e(TAG, "Error loading users: ${result.exception}")
          // Show only "new user" option if we can't load users
          val adapter = ArrayObjectAdapter(UserCardPresenter())
          adapter.add(UserCardPresenter.NEW_USER_MARKER)
          setAdapter(adapter)
        }
      }
    }
  }

  private inner class ItemViewClickedListener : OnItemViewClickedListener {
    override fun onItemClicked(
      itemViewHolder: Presenter.ViewHolder,
      item: Any,
      rowViewHolder: RowPresenter.ViewHolder?,
      row: Row?
    ) {
      if (item is User) {
        selectUser(item)
      } else if (item is String && item == UserCardPresenter.NEW_USER_MARKER) {
        createNewUser()
      }
    }
  }

  private fun selectUser(user: User) {
    // Save user ID and update API client
    user.id?.let { userId ->
      configuration.setUserId(userId)
      ApiRetrofitClient.setUserId(userId)

      // Return to main activity
      val intent = Intent(requireContext(), MainActivity::class.java)
      intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
      startActivity(intent)
      requireActivity().finish()
    }
  }

  private fun createNewUser() {
    lifecycleScope.launch {
      when (val result = authClient.createUser()) {
        is ApiResult.Success -> {
          val authResponse = result.data
          val flowId = authResponse.flowId
          if (flowId != null) {
            showQRCodeDialog(authResponse.verificationUri, authResponse.userCode, flowId)
            startPollingAuthStatus(flowId)
          } else {
            showErrorDialog("Invalid authentication response - missing flowId")
          }
        }

        is ApiResult.Error -> {
          Log.e(TAG, "Error creating user: ${result.exception}")
          showErrorDialog("Failed to initiate authentication. Please try again.")
        }
      }
    }
  }

  private fun startPollingAuthStatus(flowId: String) {
    pollingJob?.cancel()
    pollingJob = lifecycleScope.launch {
      var attempts = 0
      val maxAttempts = 60 // 5 minutes with 5 second intervals

      while (attempts < maxAttempts) {
        delay(5000) // Poll every 5 seconds
        attempts++

        when (val result = authClient.checkAuthStatus(flowId)) {
          is ApiResult.Success -> {
            val status = result.data
            when (status.status) {
              "completed" -> {
                pollingJob?.cancel()
                status.user?.let { onAuthCompleted(it) }
                return@launch
              }

              "failed" -> {
                pollingJob?.cancel()
                showErrorDialog(status.error ?: "Authentication failed")
                return@launch
              }

              "pending" -> {
                // Continue polling
                Log.d(TAG, "Auth still pending, attempt $attempts/$maxAttempts")
              }
            }
          }

          is ApiResult.Error -> {
            Log.e(TAG, "Error checking auth status: ${result.exception}")
            // Continue polling on error
          }
        }
      }

      // Timeout after max attempts
      pollingJob?.cancel()
      showErrorDialog("Authentication timed out. Please try again.")
    }
  }

  private fun onAuthCompleted(user: User) {
    // Save the new user and refresh the list
    user.id?.let { userId ->
      configuration.setUserId(userId)
      ApiRetrofitClient.setUserId(userId)

      // Return to main activity
      val intent = Intent(requireContext(), MainActivity::class.java)
      intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
      startActivity(intent)
      requireActivity().finish()
    }
  }

  override fun onDestroy() {
    super.onDestroy()
    pollingJob?.cancel()
  }

  private fun showQRCodeDialog(verificationUri: String?, userCode: String?, @Suppress("UNUSED_PARAMETER") flowId: String) {
    if (verificationUri == null || userCode == null) {
      showErrorDialog("Invalid authentication response")
      return
    }

    val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_qr_code, null)
    val qrCodeImage = dialogView.findViewById<ImageView>(R.id.qr_code_image)
    val codeText = dialogView.findViewById<TextView>(R.id.verification_code)
    val urlText = dialogView.findViewById<TextView>(R.id.verification_url)

    // Generate QR code
    try {
      val qrBitmap = generateQRCode(verificationUri)
      qrCodeImage.setImageBitmap(qrBitmap)
    } catch (e: Exception) {
      Log.e(TAG, "Error generating QR code", e)
    }

    codeText.text = "Code: $userCode"
    urlText.text = "https://$verificationUri"

    AlertDialog.Builder(requireContext())
      .setTitle("Scan to Login")
      .setView(dialogView)
      .setMessage("Scan the QR code or visit $verificationUri\n\nWaiting for authentication...")
      .setPositiveButton("Cancel") { dialog, _ ->
        pollingJob?.cancel()
        dialog.dismiss()
        loadUsers()
      }
      .setCancelable(false)
      .show()
  }

  private fun showErrorDialog(message: String) {
    AlertDialog.Builder(requireContext())
      .setTitle("Error")
      .setMessage(message)
      .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
      .show()
  }

  private fun generateQRCode(text: String, size: Int = 512): Bitmap {
    val writer = QRCodeWriter()
    val bitMatrix = writer.encode(text, BarcodeFormat.QR_CODE, size, size)
    val width = bitMatrix.width
    val height = bitMatrix.height
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)

    for (x in 0 until width) {
      for (y in 0 until height) {
        bitmap.setPixel(x, y, if (bitMatrix[x, y]) android.graphics.Color.BLACK else android.graphics.Color.WHITE)
      }
    }

    return bitmap
  }

  companion object {
    private const val TAG = "AuthFragment"
  }

}
