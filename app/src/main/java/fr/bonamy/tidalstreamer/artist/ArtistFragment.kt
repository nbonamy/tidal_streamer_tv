package fr.bonamy.tidalstreamer.artist

import android.os.Bundle
import androidx.leanback.widget.ImageCardView
import androidx.lifecycle.lifecycleScope
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
			loadRow(
				rowsAdapter,
				apiClient.fetchArtistTopTracks(mSelectedArtist.id!!),
				TrackCardPresenter(this@ArtistFragment),
				ROWS_TITLE,
				0,
			)
		}

		viewLifecycleOwner.lifecycleScope.launch {
			loadRow(
				rowsAdapter,
				apiClient.fetchArtistAlbums(mSelectedArtist.id!!),
				CollectionCardPresenter(),
				ROWS_TITLE,
				1,
			)
		}

		viewLifecycleOwner.lifecycleScope.launch {
			loadRow(
				rowsAdapter,
				apiClient.fetchArtistSingles(mSelectedArtist.id!!),
				CollectionCardPresenter(),
				ROWS_TITLE,
				2,
			)
		}


		viewLifecycleOwner.lifecycleScope.launch {
			loadRow(
				rowsAdapter,
				apiClient.fetchArtistCompilations(mSelectedArtist.id!!),
				CollectionCardPresenter(),
				ROWS_TITLE,
				3,
			)
		}

		viewLifecycleOwner.lifecycleScope.launch {
			loadRow(
				rowsAdapter,
				apiClient.fetchSimilarArtists(mSelectedArtist.id!!),
				ArtistCardPresenter(),
				ROWS_TITLE,
				4,
			)
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
