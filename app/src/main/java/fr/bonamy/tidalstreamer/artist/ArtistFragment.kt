package fr.bonamy.tidalstreamer.artist

import android.os.Bundle
import android.view.View
import androidx.lifecycle.lifecycleScope
import fr.bonamy.tidalstreamer.api.MetadataClient
import fr.bonamy.tidalstreamer.models.Artist
import fr.bonamy.tidalstreamer.utils.BrowserFragment
import fr.bonamy.tidalstreamer.utils.PresenterFlags
import kotlinx.coroutines.launch

class ArtistFragment : BrowserFragment() {

  private lateinit var mArtist: Artist

  override fun title(): String {
    return ""
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    mArtist = requireActivity().intent.getSerializableExtra(ArtistActivity.ARTIST) as Artist
    super.onCreate(savedInstanceState)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    titleView = null
    showTitle(0)
    showTitle(false)
//		titleView = ArtistTitleView(requireActivity())
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
    val metadataClient = MetadataClient(requireContext())
    adapter = rowsAdapter

    // now load rows

    viewLifecycleOwner.lifecycleScope.launch {
      loadRow(
        rowsAdapter,
        metadataClient.fetchArtistTopTracks(mArtist.id!!),
        ROWS_TITLE,
        0,
        PresenterFlags.SHOW_TRACK_ALBUM or PresenterFlags.PLAY_ALL_TRACKS
      )
    }

    viewLifecycleOwner.lifecycleScope.launch {
      loadRow(
        rowsAdapter,
        metadataClient.fetchArtistAlbums(mArtist.id!!),
        ROWS_TITLE,
        1,
        PresenterFlags.SHOW_ALBUM_YEAR
      )
    }

    viewLifecycleOwner.lifecycleScope.launch {
      loadRow(
        rowsAdapter,
        metadataClient.fetchArtistLiveAlbums(mArtist.id!!),
        ROWS_TITLE,
        2,
        PresenterFlags.SHOW_ALBUM_YEAR
      )
    }

    viewLifecycleOwner.lifecycleScope.launch {
      loadRow(
        rowsAdapter,
        metadataClient.fetchArtistSingles(mArtist.id!!),
        ROWS_TITLE,
        3,
        PresenterFlags.SHOW_ALBUM_YEAR
      )
    }


    viewLifecycleOwner.lifecycleScope.launch {
      loadRow(
        rowsAdapter,
        metadataClient.fetchArtistCompilations(mArtist.id!!),
        ROWS_TITLE,
        4,
      )
    }

    viewLifecycleOwner.lifecycleScope.launch {
      loadRow(
        rowsAdapter,
        metadataClient.fetchSimilarArtists(mArtist.id!!),
        ROWS_TITLE,
        5,
      )
    }

  }

  companion object {
    //private const val TAG = "ArtistFragment"
    private val ROWS_TITLE = arrayOf(
      "Top tracks",
      "Albums",
      "Live Albums",
      "EP & Singles",
      "Compilations",
      "Fans also like"
    )
  }

}
