package fr.bonamy.tidalstreamer

import android.util.Log
import androidx.leanback.widget.ArrayObjectAdapter
import androidx.lifecycle.lifecycleScope
import fr.bonamy.tidalstreamer.api.ApiResult
import fr.bonamy.tidalstreamer.api.MetadataClient
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

  override fun loadRows() {

    // init
    val rowsAdapter = initRowsAdapter(NUM_ROWS)
    val cardPresenter = CollectionCardPresenter()
    adapter = rowsAdapter

    // now load rows

		viewLifecycleOwner.lifecycleScope.launch {
      val apiClient = MetadataClient()
      when (val result = apiClient.fetchShortcuts()) {

        is ApiResult.Success -> {
          var itemAdapter = ArrayObjectAdapter(cardPresenter)
          result.data.forEach { shortcut ->
            if (shortcut.title != "" && shortcut.mainArtist() != null) {
              itemAdapter.add(shortcut)
            }
          }
          updateRowsAdapter(rowsAdapter, 0, ROWS_TITLE, itemAdapter)
        }

        is ApiResult.Error -> {
          // Handle the error here
          Log.e(TAG, "Error fetching recent albums: ${result.exception}")
        }
      }
    }

		viewLifecycleOwner.lifecycleScope.launch {
      val apiClient = MetadataClient()
      when (val result = apiClient.fetchNewAlbums()) {

        is ApiResult.Success -> {
          var itemAdapter = ArrayObjectAdapter(cardPresenter)
          result.data.forEach { album ->
            itemAdapter.add(album)
          }
          updateRowsAdapter(rowsAdapter, 1, ROWS_TITLE, itemAdapter)
        }

        is ApiResult.Error -> {
          // Handle the error here
          Log.e(TAG, "Error fetching recent albums: ${result.exception}")
        }
      }
    }

		viewLifecycleOwner.lifecycleScope.launch {
      val apiClient = MetadataClient()
      when (val result = apiClient.fetchRecentAlbums()) {

        is ApiResult.Success -> {
          var itemAdapter = ArrayObjectAdapter(cardPresenter)
          result.data.forEach { album ->
            itemAdapter.add(album)
          }
          updateRowsAdapter(rowsAdapter, 2, ROWS_TITLE, itemAdapter)
        }

        is ApiResult.Error -> {
          // Handle the error here
          Log.e(TAG, "Error fetching recent albums: ${result.exception}")
        }
      }
    }

		viewLifecycleOwner.lifecycleScope.launch {
      val apiClient = MetadataClient()
      when (val result = apiClient.fetchRecommendedAlbums()) {

        is ApiResult.Success -> {
          var itemAdapter = ArrayObjectAdapter(cardPresenter)
          result.data.forEach { album ->
            itemAdapter.add(album)
          }
          updateRowsAdapter(rowsAdapter, 3, ROWS_TITLE, itemAdapter)
        }

        is ApiResult.Error -> {
          // Handle the error here
          Log.e(TAG, "Error fetching recommended albums: ${result.exception}")
        }
      }
    }

		viewLifecycleOwner.lifecycleScope.launch {
      val apiClient = MetadataClient()
      when (val result = apiClient.fetchMixes()) {

        is ApiResult.Success -> {
          var itemAdapter = ArrayObjectAdapter(cardPresenter)
          result.data.forEach { mix ->
            if (mix.type == "DISCOVERY_MIX") {
              itemAdapter.add(0, mix)
            } else if (mix.type == "NEW_RELEASE_MIX") {
              itemAdapter.add(1, mix)
            } else {
              itemAdapter.add(mix)
            }
          }
          updateRowsAdapter(rowsAdapter, 4, ROWS_TITLE, itemAdapter)
        }

        is ApiResult.Error -> {
          // Handle the error here
          Log.e(TAG, "Error fetching recommended albums: ${result.exception}")
        }
      }
    }

  }

  companion object {
    private val TAG = "MainFragment"
    private val NUM_ROWS = 5
    private val ROWS_TITLE = arrayOf(
      "Shortcuts",
      "Suggested new albums for you",
      "Recently played",
      "Albums you'll enjoy",
      "Custom mixes"
    )
  }
}