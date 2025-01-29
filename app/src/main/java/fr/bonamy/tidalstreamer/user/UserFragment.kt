package fr.bonamy.tidalstreamer.user

import androidx.lifecycle.lifecycleScope
import fr.bonamy.tidalstreamer.R
import fr.bonamy.tidalstreamer.api.UserClient
import fr.bonamy.tidalstreamer.artist.ArtistCardPresenter
import fr.bonamy.tidalstreamer.collection.CollectionCardPresenter
import fr.bonamy.tidalstreamer.search.TrackCardPresenter
import fr.bonamy.tidalstreamer.utils.BrowserFragment
import fr.bonamy.tidalstreamer.utils.TrackLongClickListener
import kotlinx.coroutines.launch

class UserFragment : BrowserFragment() {

  override fun title(): String {
    return getString(R.string.browse_user_title)
  }

  override fun loadRows() {

    // init
    val rowsAdapter = initRowsAdapter(ROWS_TITLE.size)
    var userClient = UserClient()
    adapter = rowsAdapter

    // now load rows

    viewLifecycleOwner.lifecycleScope.launch {
      loadRow(
        rowsAdapter,
        userClient.fetchPlaylists(),
        CollectionCardPresenter(),
        ROWS_TITLE,
        0,
      )
    }

    viewLifecycleOwner.lifecycleScope.launch {
      loadRow(
        rowsAdapter,
        userClient.fetchFavoriteAlbums(),
        CollectionCardPresenter(),
        ROWS_TITLE,
        1,
      )
    }

    viewLifecycleOwner.lifecycleScope.launch {
      loadRow(
        rowsAdapter,
        userClient.fetchFavoriteArtists(),
        ArtistCardPresenter(),
        ROWS_TITLE,
        2,
      )
    }

    viewLifecycleOwner.lifecycleScope.launch {
      loadRow(
        rowsAdapter,
        userClient.fetchFavoriteTracks(),
        TrackCardPresenter(TrackCardPresenter.TrackPlayback.SINGLE, TrackLongClickListener(activity!!)),
        ROWS_TITLE,
        3,
      )
    }

  }

  companion object {
    private const val TAG = "MainFragment"
    private val ROWS_TITLE = arrayOf(
      "Your playlists",
      "Your albums",
      "Your artists",
      "Your tracks",
    )
  }
}