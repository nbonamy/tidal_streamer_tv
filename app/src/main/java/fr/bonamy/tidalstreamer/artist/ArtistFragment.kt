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
    val rowsAdapter = initRowsAdapter(6)
    val metadataClient = MetadataClient(requireContext())
    adapter = rowsAdapter

    // now load rows

    viewLifecycleOwner.lifecycleScope.launch {
      loadRow(
        rowsAdapter,
        metadataClient.fetchArtistTopTracks(mArtist.id!!),
        0,
        "Top tracks",
        PresenterFlags.SHOW_TRACK_ALBUM or PresenterFlags.PLAY_ALL_TRACKS
      )
    }

    viewLifecycleOwner.lifecycleScope.launch {
      loadRow(
        rowsAdapter,
        metadataClient.fetchArtistAlbums(mArtist.id!!),
        1,
        "Albums",
        PresenterFlags.SHOW_ALBUM_YEAR
      )
    }

    viewLifecycleOwner.lifecycleScope.launch {
      loadRow(
        rowsAdapter,
        metadataClient.fetchArtistLiveAlbums(mArtist.id!!),
        2,
        "Live Albums",
        PresenterFlags.SHOW_ALBUM_YEAR
      )
    }

    viewLifecycleOwner.lifecycleScope.launch {
      loadRow(
        rowsAdapter,
        metadataClient.fetchArtistSingles(mArtist.id!!),
        3,
        "EP & Singles",
        PresenterFlags.SHOW_ALBUM_YEAR
      )
    }

    viewLifecycleOwner.lifecycleScope.launch {
      loadRow(
        rowsAdapter,
        metadataClient.fetchArtistCompilations(mArtist.id!!),
        4,
        "Compilations",
      )
    }

    viewLifecycleOwner.lifecycleScope.launch {
      loadRow(
        rowsAdapter,
        metadataClient.fetchSimilarArtists(mArtist.id!!),
        5,
        "Fans also like",
      )
    }

  }

}
