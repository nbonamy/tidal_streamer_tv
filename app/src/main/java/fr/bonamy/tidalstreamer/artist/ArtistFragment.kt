package fr.bonamy.tidalstreamer.artist

import android.os.Bundle
import android.util.Log
import androidx.leanback.widget.ArrayObjectAdapter
import androidx.leanback.widget.HeaderItem
import androidx.leanback.widget.ListRow
import androidx.leanback.widget.ListRowPresenter
import androidx.lifecycle.lifecycleScope
import fr.bonamy.tidalstreamer.api.ApiResult
import fr.bonamy.tidalstreamer.api.MetadataClient
import fr.bonamy.tidalstreamer.collection.CollectionCardPresenter
import fr.bonamy.tidalstreamer.models.Artist
import fr.bonamy.tidalstreamer.utils.BrowserFragment
import kotlinx.coroutines.launch

class ArtistFragment : BrowserFragment() {

	private lateinit var mSelectedArtist: Artist

	override fun searchEnabled(): Boolean { return false }
	override fun title(): String { return mSelectedArtist.name!! }

	override fun onActivityCreated(savedInstanceState: Bundle?) {
		mSelectedArtist =
			activity!!.intent.getSerializableExtra(ArtistActivity.ARTIST) as Artist
		super.onActivityCreated(savedInstanceState)
	}

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
			when (val result = apiClient.fetchArtistAlbums(mSelectedArtist.id!!)) {

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
			when (val result = apiClient.fetchArtistSingles(mSelectedArtist.id!!)) {

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
			when (val result = apiClient.fetchArtistCompilations(mSelectedArtist.id!!)) {

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
	}

	companion object {
		private const val TAG = "ArtistFragment"
		private const val NUM_ROWS = 3
		private val ROWS_TITLE = arrayOf(
			"Albums",
			"EP & Singles",
			"Compilations",
		)
	}

}
