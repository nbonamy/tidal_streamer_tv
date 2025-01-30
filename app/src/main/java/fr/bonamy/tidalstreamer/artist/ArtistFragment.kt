package fr.bonamy.tidalstreamer.artist

import android.os.Bundle
import android.view.View
import androidx.lifecycle.lifecycleScope
import fr.bonamy.tidalstreamer.api.MetadataClient
import fr.bonamy.tidalstreamer.collection.CollectionCardPresenter
import fr.bonamy.tidalstreamer.models.Artist
import fr.bonamy.tidalstreamer.search.TrackCardPresenter
import fr.bonamy.tidalstreamer.utils.BrowserFragment
import fr.bonamy.tidalstreamer.utils.TrackLongClickListener
import kotlinx.coroutines.launch

class ArtistFragment : BrowserFragment() {

	private lateinit var mArtist: Artist

	override fun title(): String {
		return ""
	}

	override fun onCreate(savedInstanceState: Bundle?) {
		mArtist =
			activity!!.intent.getSerializableExtra(ArtistActivity.ARTIST) as Artist
		super.onCreate(savedInstanceState)
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		titleView = null
		showTitle(0)
		showTitle(false)
//		titleView = ArtistTitleView(activity!!)
//
//		Glide.with(requireContext())
//			.load(mSelectedArtist.imageUrl())
//			//.error(mDefaultCardImage)
//			.into(object : CustomTarget<Drawable>() {
//				override fun onResourceReady(
//					drawable: Drawable,
//					transition: com.bumptech.glide.request.transition.Transition<in Drawable?>?
//				) {
//					(titleView as ArtistTitleView).setBadgeDrawable(drawable)
//				}
//
//				override fun onLoadCleared(placeholder: Drawable?) {}
//			})

	}

	override fun loadRows() {

		// init
		val rowsAdapter = initRowsAdapter(ROWS_TITLE.size)
		val metadataClient = MetadataClient()
		adapter = rowsAdapter

		// now load rows

		viewLifecycleOwner.lifecycleScope.launch {
			loadRow(
				rowsAdapter,
				metadataClient.fetchArtistTopTracks(mArtist.id!!),
				TrackCardPresenter(
					TrackCardPresenter.TrackPlayback.ALL,
					TrackLongClickListener(activity!!)
				),
				ROWS_TITLE,
				0,
			)
		}

		viewLifecycleOwner.lifecycleScope.launch {
			loadRow(
				rowsAdapter,
				metadataClient.fetchArtistAlbums(mArtist.id!!),
				CollectionCardPresenter(),
				ROWS_TITLE,
				1,
			)
		}

		viewLifecycleOwner.lifecycleScope.launch {
			loadRow(
				rowsAdapter,
				metadataClient.fetchArtistSingles(mArtist.id!!),
				CollectionCardPresenter(),
				ROWS_TITLE,
				2,
			)
		}


		viewLifecycleOwner.lifecycleScope.launch {
			loadRow(
				rowsAdapter,
				metadataClient.fetchArtistCompilations(mArtist.id!!),
				CollectionCardPresenter(),
				ROWS_TITLE,
				3,
			)
		}

		viewLifecycleOwner.lifecycleScope.launch {
			loadRow(
				rowsAdapter,
				metadataClient.fetchSimilarArtists(mArtist.id!!),
				ArtistCardPresenter(),
				ROWS_TITLE,
				4,
			)
		}

	}

	companion object {
		//private const val TAG = "ArtistFragment"
		private val ROWS_TITLE = arrayOf(
			"Top tracks",
			"Albums",
			"EP & Singles",
			"Compilations",
			"Fans also like"
		)
	}

}
