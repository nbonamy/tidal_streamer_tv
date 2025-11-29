package fr.bonamy.tidalstreamer

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.leanback.widget.ListRow
import androidx.leanback.widget.OnItemViewSelectedListener
import androidx.leanback.widget.Presenter
import androidx.leanback.widget.Row
import androidx.leanback.widget.RowPresenter
import fr.bonamy.tidalstreamer.api.UserClient
import fr.bonamy.tidalstreamer.utils.BrowserFragment
import fr.bonamy.tidalstreamer.utils.RowDefinition

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

    val rows = listOf(
      RowDefinition("Shortcuts", { userClient.fetchShortcuts() }),
      RowDefinition("Suggested new albums", { userClient.fetchNewAlbums() }),
      RowDefinition("Recommended new tracks", { userClient.fetchNewTracks() }),
      RowDefinition("Albums you'll enjoy", { userClient.fetchRecommendedAlbums() }),
      RowDefinition("Popular playlists", { userClient.fetchPopularPlaylists() }),
      RowDefinition("Spotlighted tracks", { userClient.fetchSpotlightedTracks() }),
      RowDefinition("Uploads for you", { userClient.fetchUploadsTracks() }),
      RowDefinition("Your listening history", { userClient.fetchHistoryMixes() }),
      RowDefinition("Your favorite artists", { userClient.fetchRecentArtists() }),
      RowDefinition("Custom mixes", { userClient.fetchDailyMixes() }),
      RowDefinition("Essentials to explore", { userClient.fetchEssentialPlaylists() }),
      RowDefinition("Personal radio stations", { userClient.fetchRadioMixes() }),
      RowDefinition("Just updated", { userClient.fetchUpdatedPlaylists() }),
      RowDefinition("User playlists you'll love", { userClient.fetchRecommendedPlaylists() }),
      RowDefinition("Your forgotten favorites", { userClient.fetchForgottenAlbums() }),
    )

    loadRowsFromDefinitions(rows)
  }

}