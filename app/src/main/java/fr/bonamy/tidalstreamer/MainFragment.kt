package fr.bonamy.tidalstreamer

import android.annotation.SuppressLint
import androidx.lifecycle.lifecycleScope
import fr.bonamy.tidalstreamer.api.MetadataClient
import fr.bonamy.tidalstreamer.api.UserClient
import fr.bonamy.tidalstreamer.artist.ArtistCardPresenter
import fr.bonamy.tidalstreamer.collection.CollectionCardPresenter
import fr.bonamy.tidalstreamer.utils.BrowserFragment
import kotlinx.coroutines.launch

class MainFragment : BrowserFragment() {

  override fun title(): String {
    return "TIDAL"
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
        userClient.fetchShortcuts(),
        CollectionCardPresenter(),
        ROWS_TITLE,
        0,
      )
    }

		viewLifecycleOwner.lifecycleScope.launch {
      loadRow(
        rowsAdapter,
        userClient.fetchNewAlbums(),
        CollectionCardPresenter(),
        ROWS_TITLE,
        1,
      )
    }

		viewLifecycleOwner.lifecycleScope.launch {
      loadRow(
        rowsAdapter,
        userClient.fetchRecentAlbums(),
        CollectionCardPresenter(),
        ROWS_TITLE,
        2,
      )
    }

		viewLifecycleOwner.lifecycleScope.launch {
      loadRow(
        rowsAdapter,
        userClient.fetchRecommendedAlbums(),
        CollectionCardPresenter(),
        ROWS_TITLE,
        3,
      )
    }

    viewLifecycleOwner.lifecycleScope.launch {
      loadRow(
        rowsAdapter,
        userClient.fetchMixes(),
        CollectionCardPresenter(),
        ROWS_TITLE,
        4,
      )
    }

    viewLifecycleOwner.lifecycleScope.launch {
      loadRow(
        rowsAdapter,
        userClient.fetchRecentArtists(),
        ArtistCardPresenter(),
        ROWS_TITLE,
        5,
      )
    }

  }

  companion object {
    private const val TAG = "MainFragment"
    private val ROWS_TITLE = arrayOf(
      "Shortcuts",
      "Suggested new albums for you",
      "Recently played",
      "Albums you'll enjoy",
      "Custom mixes",
      "Your favorite artists",
    )
  }
}