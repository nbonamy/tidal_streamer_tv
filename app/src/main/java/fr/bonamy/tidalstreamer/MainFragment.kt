package fr.bonamy.tidalstreamer

import androidx.lifecycle.lifecycleScope
import fr.bonamy.tidalstreamer.api.UserClient
import fr.bonamy.tidalstreamer.utils.BrowserFragment
import kotlinx.coroutines.launch

class MainFragment : BrowserFragment() {

  override fun title(): String {
    return getString(R.string.browse_title)
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
        userClient.fetchShortcuts(),
        ROWS_TITLE,
        0,
      )
    }

    viewLifecycleOwner.lifecycleScope.launch {
      loadRow(
        rowsAdapter,
        userClient.fetchNewAlbums(),
        ROWS_TITLE,
        1,
      )
    }

    viewLifecycleOwner.lifecycleScope.launch {
      loadRow(
        rowsAdapter,
        userClient.fetchRecentAlbums(),
        ROWS_TITLE,
        2,
      )
    }

    viewLifecycleOwner.lifecycleScope.launch {
      loadRow(
        rowsAdapter,
        userClient.fetchRecommendedAlbums(),
        ROWS_TITLE,
        3,
      )
    }

    viewLifecycleOwner.lifecycleScope.launch {
      loadRow(
        rowsAdapter,
        userClient.fetchMixes(),
        ROWS_TITLE,
        4,
      )
    }

    viewLifecycleOwner.lifecycleScope.launch {
      loadRow(
        rowsAdapter,
        userClient.fetchRecentArtists(),
        ROWS_TITLE,
        5,
      )
    }

  }

  companion object {
//    private const val TAG = "MainFragment"
    private val ROWS_TITLE = arrayOf(
      "",
      "Suggested new albums for you",
      "Recently played",
      "Albums you'll enjoy",
      "Custom mixes",
      "Your favorite artists",
    )
  }
}