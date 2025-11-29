package fr.bonamy.tidalstreamer

import android.content.Intent
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import fr.bonamy.tidalstreamer.api.ApiResult
import fr.bonamy.tidalstreamer.api.AuthClient
import fr.bonamy.tidalstreamer.auth.AuthActivity
import fr.bonamy.tidalstreamer.utils.Configuration
import fr.bonamy.tidalstreamer.utils.TidalActivity
import kotlinx.coroutines.launch

class MainActivity : TidalActivity() {

  private lateinit var userBadge: TextView
  private lateinit var configuration: Configuration
  private lateinit var authClient: AuthClient

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    configuration = Configuration(this)
    authClient = AuthClient(this)

    if (savedInstanceState == null) {
      supportFragmentManager.beginTransaction()
        .replace(R.id.main_browse_fragment, MainFragment())
        .commitNow()
    }

    setupUserBadge()
  }

  override fun onResume() {
    super.onResume()
    updateUserBadge()
  }

  private fun setupUserBadge() {
    userBadge = findViewById(R.id.user_badge)

    // Make badge clickable for testing purposes
    // Primary interaction is via GREEN remote button (KEYCODE_PROG_GREEN)
    userBadge.setOnClickListener {
      openAuthActivity()
    }

    // Handle D-pad center/enter key when focused
    userBadge.setOnKeyListener { _, keyCode, event ->
      if (event.action == android.view.KeyEvent.ACTION_DOWN &&
        (keyCode == android.view.KeyEvent.KEYCODE_DPAD_CENTER ||
          keyCode == android.view.KeyEvent.KEYCODE_ENTER)
      ) {
        openAuthActivity()
        true
      } else {
        false
      }
    }

    // Add focus change listener for scale animation
    userBadge.setOnFocusChangeListener { view, hasFocus ->
      if (hasFocus) {
        view.animate().scaleX(1.15f).scaleY(1.15f).setDuration(150).start()
      } else {
        view.animate().scaleX(1.0f).scaleY(1.0f).setDuration(150).start()
      }
    }

    // Request focus on first launch if no user is set
    if (configuration.getUserId() == null) {
      userBadge.requestFocus()
    }

    updateUserBadge()
  }

  private fun openAuthActivity() {
    val intent = Intent(this, AuthActivity::class.java)
    startActivity(intent)
  }

  private fun updateUserBadge() {
    val userId = configuration.getUserId()

    if (userId == null) {
      // No user selected
      userBadge.text = "?"
      setUserBadgeColor("#808080") // Gray
    } else {
      // Load user info
      lifecycleScope.launch {
        when (val result = authClient.fetchUsers()) {
          is ApiResult.Success -> {
            val user = result.data.find { it.id == userId }
            if (user != null) {
              // Show first letter of user's name or email
              val initial = user.fullName?.firstOrNull()?.uppercaseChar()
                ?: user.email?.firstOrNull()?.uppercaseChar()
                ?: user.login?.firstOrNull()?.uppercaseChar()
                ?: '?'
              userBadge.text = initial.toString()
              setUserBadgeColor(generateColorForUser(userId))
            } else {
              userBadge.text = "?"
              setUserBadgeColor("#808080")
            }
          }

          is ApiResult.Error -> {
            // Show user ID if we can't load user info
            userBadge.text = userId.toString().take(1)
            setUserBadgeColor(generateColorForUser(userId))
          }
        }
      }
    }
  }

  private fun setUserBadgeColor(@Suppress("UNUSED_PARAMETER") colorHex: String) {
    // Badge now uses a fixed color from resources
    // This method kept for potential future customization
    userBadge.setBackgroundResource(R.drawable.user_badge_background)
  }

  private fun generateColorForUser(userId: Int): String {
    val colors = listOf(
      "#FF6B6B", "#4ECDC4", "#45B7D1", "#FFA07A", "#98D8C8",
      "#F7DC6F", "#BB8FCE", "#85C1E2", "#F8B88B", "#7FB3D5"
    )
    return colors[userId % colors.size]
  }

  override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
    // Allow RIGHT or UP arrow to focus user badge only when search is focused
    if ((keyCode == KeyEvent.KEYCODE_DPAD_RIGHT || keyCode == KeyEvent.KEYCODE_DPAD_UP)) {
      val currentFocus = currentFocus
      if (currentFocus != null) {
        val resourceName = try {
          resources.getResourceEntryName(currentFocus.id)
        } catch (e: Exception) {
          "unknown"
        }

        // Check if search orb (title_orb) has focus
        if (resourceName == "title_orb" || currentFocus.javaClass.simpleName == "SearchOrbView") {
          userBadge.requestFocus()
          return true
        }
      }
    }
    return super.onKeyDown(keyCode, event)
  }

  companion object {
    private const val TAG = "MainActivity"
  }

}
