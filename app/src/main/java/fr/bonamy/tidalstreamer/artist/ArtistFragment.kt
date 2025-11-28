package fr.bonamy.tidalstreamer.artist

import android.os.Bundle
import android.view.View
import fr.bonamy.tidalstreamer.api.MetadataClient
import fr.bonamy.tidalstreamer.models.Artist
import fr.bonamy.tidalstreamer.utils.BrowserFragment
import fr.bonamy.tidalstreamer.utils.PresenterFlags
import fr.bonamy.tidalstreamer.utils.RowDefinition

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
    val metadataClient = MetadataClient(requireContext())

    val rows = listOf(
      RowDefinition(
        "Top tracks",
        { metadataClient.fetchArtistTopTracks(mArtist.id!!) },
        PresenterFlags.SHOW_TRACK_ALBUM or PresenterFlags.PLAY_ALL_TRACKS
      ),
      RowDefinition(
        "Albums",
        { metadataClient.fetchArtistAlbums(mArtist.id!!) },
        PresenterFlags.SHOW_ALBUM_YEAR
      ),
      RowDefinition(
        "Live Albums",
        { metadataClient.fetchArtistLiveAlbums(mArtist.id!!) },
        PresenterFlags.SHOW_ALBUM_YEAR
      ),
      RowDefinition(
        "EP & Singles",
        { metadataClient.fetchArtistSingles(mArtist.id!!) },
        PresenterFlags.SHOW_ALBUM_YEAR
      ),
      RowDefinition("Compilations", { metadataClient.fetchArtistCompilations(mArtist.id!!) }),
      RowDefinition("Fans also like", { metadataClient.fetchSimilarArtists(mArtist.id!!) }),
    )

    loadRowsFromDefinitions(rows)
  }

}
