package fr.bonamy.tidalstreamer.artist

import android.os.Bundle
import android.util.Log
import androidx.leanback.widget.ArrayObjectAdapter
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

	override fun onCreate(savedInstanceState: Bundle?) {
		mSelectedArtist =
			activity!!.intent.getSerializableExtra(ArtistActivity.ARTIST) as Artist
		super.onCreate(savedInstanceState)
	}

	override fun loadRows() {

		// init
		val rowsAdapter = initRowsAdapter(NUM_ROWS)
		val cardPresenter = CollectionCardPresenter()
		adapter = rowsAdapter

		// now load rows

		viewLifecycleOwner.lifecycleScope.launch {
			val apiClient = MetadataClient()
			when (val result = apiClient.fetchArtistAlbums(mSelectedArtist.id!!)) {

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
			when (val result = apiClient.fetchArtistSingles(mSelectedArtist.id!!)) {

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
			when (val result = apiClient.fetchArtistCompilations(mSelectedArtist.id!!)) {

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
