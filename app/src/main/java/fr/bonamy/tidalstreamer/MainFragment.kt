package fr.bonamy.tidalstreamer

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.leanback.widget.ListRow
import androidx.leanback.widget.OnItemViewSelectedListener
import androidx.leanback.widget.Presenter
import androidx.leanback.widget.Row
import androidx.leanback.widget.RowPresenter
import androidx.lifecycle.lifecycleScope
import fr.bonamy.tidalstreamer.api.ApiResult
import fr.bonamy.tidalstreamer.api.UserClient
import fr.bonamy.tidalstreamer.utils.BrowserFragment
import fr.bonamy.tidalstreamer.utils.RowDefinition
import kotlinx.coroutines.launch

class MainFragment : BrowserFragment() {

  private var userBadge: TextView? = null
  private var currentRowIndex = 0

  override fun title(): String {
    return getString(R.string.browse_main_title)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    // Get reference to user badge from parent activity
    userBadge = activity?.findViewById(R.id.user_badge)

    // Listen to row selection to detect scrolling
    onItemViewSelectedListener = object : OnItemViewSelectedListener {
      override fun onItemSelected(
        itemViewHolder: Presenter.ViewHolder?,
        item: Any?,
        rowViewHolder: RowPresenter.ViewHolder?,
        row: Row?
      ) {
        // Find row position
        if (row is ListRow) {
          for (i in 0 until (adapter?.size() ?: 0)) {
            if (adapter?.get(i) == row) {
              currentRowIndex = i
              // Hide badge when not on first row, show when on first row
              userBadge?.animate()
                ?.alpha(if (i == 0) 1f else 0f)
                ?.setDuration(200)
                ?.start()
              break
            }
          }
        }
      }
    }
  }

  override fun loadRows() {
    val userClient = UserClient(requireContext())

    viewLifecycleOwner.lifecycleScope.launch {
      when (val result = userClient.fetchHomeSections()) {
        is ApiResult.Success -> {
          val rows = result.data.mapNotNull { section ->
            val id = section.id ?: return@mapNotNull null
            RowDefinition(section.title ?: "", { userClient.fetchHomeSectionItems(id) })
          }
          loadRowsFromDefinitions(rows)
        }

        is ApiResult.Error -> {
          Log.e(TAG, "Error fetching home sections: ${result.exception}")
          loadRowsFromDefinitions(emptyList())
        }
      }
    }
  }

  companion object {
    private const val TAG = "MainFragment"
  }

}
