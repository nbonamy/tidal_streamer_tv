package fr.bonamy.tidalstreamer

import android.util.Log
import androidx.leanback.widget.ArrayObjectAdapter
import androidx.leanback.widget.HeaderItem
import androidx.leanback.widget.ListRow
import androidx.leanback.widget.ListRowPresenter
import androidx.lifecycle.lifecycleScope
import fr.bonamy.tidalstreamer.api.ApiResult
import fr.bonamy.tidalstreamer.api.MetadataClient
import fr.bonamy.tidalstreamer.collection.CollectionCardPresenter
import fr.bonamy.tidalstreamer.utils.BrowserFragment
import kotlinx.coroutines.launch

class MainFragment : BrowserFragment() {

	override fun searchEnabled(): Boolean { return true }
	override fun title(): String { return "TIDAL" }

	override fun loadRows() {

		// init
		val rowsAdapter = ArrayObjectAdapter(ListRowPresenter())
		val cardPresenter = CollectionCardPresenter()

		// add placeholders
		for (i in 0..NUM_ROWS-1) {
			val listRowAdapter = ArrayObjectAdapter(cardPresenter)
			rowsAdapter.add(ListRow(HeaderItem(ROWS_TITLE[i]), listRowAdapter))
		}

		// save it as is
		adapter = rowsAdapter

		// now load rows

		lifecycleScope.launch {
			val apiClient = MetadataClient()
			when (val result = apiClient.fetchShortcuts()) {

				is ApiResult.Success -> {
					var itemAdapter = ArrayObjectAdapter(cardPresenter)
					result.data.forEach { shortcut ->
						if (shortcut.title != "" && shortcut.mainArtist() != null) {
							itemAdapter.add(shortcut)
						}
					}
					val header = HeaderItem(ROWS_TITLE[0])
					rowsAdapter.replace(0, ListRow(header, itemAdapter))
					rowsAdapter.notifyArrayItemRangeChanged(0, 1)
				}

				is ApiResult.Error -> {
					// Handle the error here
					Log.e(TAG, "Error fetching recent albums: ${result.exception}")
				}
			}
		}

		lifecycleScope.launch {
			val apiClient = MetadataClient()
			when (val result = apiClient.fetchNewAlbums()) {

				is ApiResult.Success -> {
					var itemAdapter = ArrayObjectAdapter(cardPresenter)
					result.data.forEach { album ->
						itemAdapter.add(album)
					}
					val header = HeaderItem(ROWS_TITLE[1])
					rowsAdapter.replace(1, ListRow(header, itemAdapter))
					rowsAdapter.notifyArrayItemRangeChanged(1, 1)
				}

				is ApiResult.Error -> {
					// Handle the error here
					Log.e(TAG, "Error fetching recent albums: ${result.exception}")
				}
			}
		}


		lifecycleScope.launch {
			val apiClient = MetadataClient()
			when (val result = apiClient.fetchRecentAlbums()) {

				is ApiResult.Success -> {
					var itemAdapter = ArrayObjectAdapter(cardPresenter)
					result.data.forEach { album ->
						itemAdapter.add(album)
					}
					val header = HeaderItem(ROWS_TITLE[2])
					rowsAdapter.replace(2, ListRow(header, itemAdapter))
				}

				is ApiResult.Error -> {
					// Handle the error here
					Log.e(TAG, "Error fetching recent albums: ${result.exception}")
				}
			}
		}

		lifecycleScope.launch {
			val apiClient = MetadataClient()
			when (val result = apiClient.fetchRecommendedAlbums()) {

				is ApiResult.Success -> {
					var itemAdapter = ArrayObjectAdapter(cardPresenter)
					result.data.forEach { album ->
						itemAdapter.add(album)
					}
					val header = HeaderItem(ROWS_TITLE[3])
					rowsAdapter.replace(3, ListRow(header, itemAdapter))
				}

				is ApiResult.Error -> {
					// Handle the error here
					Log.e(TAG, "Error fetching recommended albums: ${result.exception}")
				}
			}
		}

		lifecycleScope.launch {
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
					val header = HeaderItem(ROWS_TITLE[4])
					rowsAdapter.replace(4, ListRow(header, itemAdapter))
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