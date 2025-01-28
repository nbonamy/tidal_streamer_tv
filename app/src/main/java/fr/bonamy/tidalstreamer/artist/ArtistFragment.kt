package fr.bonamy.tidalstreamer.artist

import android.os.Bundle
import android.util.Log
import androidx.leanback.widget.ArrayObjectAdapter
import androidx.leanback.widget.ImageCardView
import androidx.lifecycle.lifecycleScope
import fr.bonamy.tidalstreamer.api.ApiResult
import fr.bonamy.tidalstreamer.api.MetadataClient
import fr.bonamy.tidalstreamer.collection.CollectionCardPresenter
import fr.bonamy.tidalstreamer.models.Artist
import fr.bonamy.tidalstreamer.models.Track
import fr.bonamy.tidalstreamer.search.TrackCardClickListener
import fr.bonamy.tidalstreamer.search.TrackCardPresenter
import fr.bonamy.tidalstreamer.utils.BrowserFragment
import kotlinx.coroutines.launch

class ArtistFragment : BrowserFragment(), TrackCardClickListener {

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
		val rowsAdapter = initRowsAdapter(ROWS_TITLE.size)
		val apiClient = MetadataClient()
		adapter = rowsAdapter

		// now load rows

		viewLifecycleOwner.lifecycleScope.launch {
			when (val result = apiClient.fetchArtistTopTracks(mSelectedArtist.id!!)) {

				is ApiResult.Success -> {
					val itemAdapter = ArrayObjectAdapter(TrackCardPresenter(this@ArtistFragment))
					result.data.forEach { track ->
						itemAdapter.add(track)
					}
					updateRowsAdapter(rowsAdapter, 0, ROWS_TITLE, itemAdapter)
				}

				is ApiResult.Error -> {
					// Handle the error here
					Log.e(TAG, "Error fetching artist albums: ${result.exception}")
				}
			}
		}

		viewLifecycleOwner.lifecycleScope.launch {
			when (val result = apiClient.fetchArtistAlbums(mSelectedArtist.id!!)) {

				is ApiResult.Success -> {
					val itemAdapter = ArrayObjectAdapter(CollectionCardPresenter())
					result.data.forEach { shortcut ->
						if (shortcut.title != "" && shortcut.mainArtist() != null) {
							itemAdapter.add(shortcut)
						}
					}
					updateRowsAdapter(rowsAdapter, 1, ROWS_TITLE, itemAdapter)
				}

				is ApiResult.Error -> {
					// Handle the error here
					Log.e(TAG, "Error fetching artist albums: ${result.exception}")
				}
			}
		}

		viewLifecycleOwner.lifecycleScope.launch {
			when (val result = apiClient.fetchArtistSingles(mSelectedArtist.id!!)) {

				is ApiResult.Success -> {
					val itemAdapter = ArrayObjectAdapter(CollectionCardPresenter())
					result.data.forEach { album ->
						itemAdapter.add(album)
					}
					updateRowsAdapter(rowsAdapter, 2, ROWS_TITLE, itemAdapter)
				}

				is ApiResult.Error -> {
					// Handle the error here
					Log.e(TAG, "Error fetching artist singles: ${result.exception}")
				}
			}
		}


		viewLifecycleOwner.lifecycleScope.launch {
			when (val result = apiClient.fetchArtistCompilations(mSelectedArtist.id!!)) {

				is ApiResult.Success -> {
					val itemAdapter = ArrayObjectAdapter(CollectionCardPresenter())
					result.data.forEach { album ->
						itemAdapter.add(album)
					}
					updateRowsAdapter(rowsAdapter, 3, ROWS_TITLE, itemAdapter)
				}

				is ApiResult.Error -> {
					// Handle the error here
					Log.e(TAG, "Error fetching artist compilations: ${result.exception}")
				}
			}
		}

		viewLifecycleOwner.lifecycleScope.launch {
			when (val result = apiClient.fetchSimilarArtists(mSelectedArtist.id!!)) {

				is ApiResult.Success -> {
					val itemAdapter = ArrayObjectAdapter(ArtistCardPresenter())
					result.data.forEach { artist ->
						itemAdapter.add(artist)
					}
					updateRowsAdapter(rowsAdapter, 4, ROWS_TITLE, itemAdapter)
				}

				is ApiResult.Error -> {
					// Handle the error here
					Log.e(TAG, "Error fetching similar artists: ${result.exception}")
				}
			}
		}

	}

	override fun onTrackLongClicked(track: Track, cardView: ImageCardView) {
		TODO("Not yet implemented")
	}

	companion object {
		private const val TAG = "ArtistFragment"
		private val ROWS_TITLE = arrayOf(
			"Top tracks",
			"Albums",
			"EP & Singles",
			"Compilations",
			"Fans also like"
		)
	}

}
