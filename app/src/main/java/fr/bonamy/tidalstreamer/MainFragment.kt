package fr.bonamy.tidalstreamer

import android.annotation.SuppressLint
import androidx.lifecycle.lifecycleScope
import fr.bonamy.tidalstreamer.api.MetadataClient
import fr.bonamy.tidalstreamer.artist.ArtistCardPresenter
import fr.bonamy.tidalstreamer.collection.CollectionCardPresenter
import fr.bonamy.tidalstreamer.utils.BrowserFragment
import kotlinx.coroutines.launch

class MainFragment : BrowserFragment() {

  override fun searchEnabled(): Boolean {
    return true
  }

  override fun title(): String {
    return "TIDAL"
  }

  @SuppressLint("SuspiciousIndentation")
  override fun loadRows() {

    // init
    val rowsAdapter = initRowsAdapter(ROWS_TITLE.size)
    val apiClient = MetadataClient()
    adapter = rowsAdapter

    // now load rows

    viewLifecycleOwner.lifecycleScope.launch {
      loadRow(
        rowsAdapter,
        apiClient.fetchShortcuts(),
        CollectionCardPresenter(),
        ROWS_TITLE,
        0,
      )
    }

		viewLifecycleOwner.lifecycleScope.launch {
      loadRow(
        rowsAdapter,
        apiClient.fetchNewAlbums(),
        CollectionCardPresenter(),
        ROWS_TITLE,
        1,
      )
    }

		viewLifecycleOwner.lifecycleScope.launch {
      loadRow(
        rowsAdapter,
        apiClient.fetchRecentAlbums(),
        CollectionCardPresenter(),
        ROWS_TITLE,
        2,
      )
    }

		viewLifecycleOwner.lifecycleScope.launch {
      loadRow(
        rowsAdapter,
        apiClient.fetchRecommendedAlbums(),
        CollectionCardPresenter(),
        ROWS_TITLE,
        3,
      )
    }

    viewLifecycleOwner.lifecycleScope.launch {
      loadRow(
        rowsAdapter,
        apiClient.fetchMixes(),
        CollectionCardPresenter(),
        ROWS_TITLE,
        4,
      )
    }

    viewLifecycleOwner.lifecycleScope.launch {
      loadRow(
        rowsAdapter,
        apiClient.fetchFavoriteArtists(),
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