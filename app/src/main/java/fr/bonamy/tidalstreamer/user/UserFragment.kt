package fr.bonamy.tidalstreamer.user

import androidx.lifecycle.lifecycleScope
import fr.bonamy.tidalstreamer.R
import fr.bonamy.tidalstreamer.api.UserClient
import fr.bonamy.tidalstreamer.search.TrackCardPresenter
import fr.bonamy.tidalstreamer.utils.BrowserFragment
import kotlinx.coroutines.launch

class UserFragment : BrowserFragment() {

  override fun title(): String {
    return getString(R.string.browse_user_title)
  }

  override fun loadRows() {

    // init
    val rowsAdapter = initRowsAdapter(ROWS_TITLE.size)
    val userClient = UserClient()
    adapter = rowsAdapter

    // now load rows

    viewLifecycleOwner.lifecycleScope.launch {
      loadRow(
        rowsAdapter,
        userClient.fetchPlaylists(),
        ROWS_TITLE,
        0,
      )
    }

    viewLifecycleOwner.lifecycleScope.launch {
      loadRow(
        rowsAdapter,
        userClient.fetchFavoriteAlbums(),
        ROWS_TITLE,
        1,
      )
    }

    viewLifecycleOwner.lifecycleScope.launch {
      loadRow(
        rowsAdapter,
        userClient.fetchFavoriteArtists(),
        ROWS_TITLE,
        2,
      )
    }

    viewLifecycleOwner.lifecycleScope.launch {
      loadRow(
        rowsAdapter,
        userClient.fetchFavoriteTracks(),
        ROWS_TITLE,
        3,
      )
    }

  }

  companion object {
//    private const val TAG = "UserFragment"
    private val ROWS_TITLE = arrayOf(
      "Your playlists",
      "Your albums",
      "Your artists",
      "Your tracks",
    )
  }
}