package fr.bonamy.tidalstreamer.collection

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.graphics.ColorUtils
import androidx.leanback.app.BackgroundManager
import androidx.leanback.app.DetailsSupportFragment
import androidx.leanback.app.DetailsSupportFragmentBackgroundController
import androidx.leanback.widget.Action
import androidx.leanback.widget.ArrayObjectAdapter
import androidx.leanback.widget.ClassPresenterSelector
import androidx.leanback.widget.DetailsOverviewRow
import androidx.leanback.widget.FullWidthDetailsOverviewRowPresenter
import androidx.leanback.widget.FullWidthDetailsOverviewSharedElementHelper
import androidx.leanback.widget.OnActionClickedListener
import androidx.lifecycle.lifecycleScope
import androidx.palette.graphics.Palette
import androidx.palette.graphics.Palette.PaletteAsyncListener
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import fr.bonamy.tidalstreamer.MainActivity
import fr.bonamy.tidalstreamer.R
import fr.bonamy.tidalstreamer.api.ApiResult
import fr.bonamy.tidalstreamer.api.EnqueuePosition
import fr.bonamy.tidalstreamer.api.MetadataClient
import fr.bonamy.tidalstreamer.api.StreamingClient
import fr.bonamy.tidalstreamer.artist.ArtistActivity
import fr.bonamy.tidalstreamer.models.Album
import fr.bonamy.tidalstreamer.models.Collection
import fr.bonamy.tidalstreamer.models.Mix
import fr.bonamy.tidalstreamer.models.Playlist
import fr.bonamy.tidalstreamer.models.Track
import fr.bonamy.tidalstreamer.utils.ItemLongClickedListener
import fr.bonamy.tidalstreamer.utils.PaletteUtils
import kotlinx.coroutines.launch

/**
 * A wrapper fragment for leanback details screens.
 * It shows a detailed view of video and its metadata plus related videos.
 */
class CollectionFragment : DetailsSupportFragment(), PaletteAsyncListener, OnTrackClickListener {

  private var mCollection: Collection? = null
  private lateinit var mBackgroundManager: BackgroundManager
  private lateinit var mDetailsBackground: DetailsSupportFragmentBackgroundController
  private lateinit var mDetailsPresenter: FullWidthDetailsOverviewRowPresenter
  private lateinit var mPresenterSelector: ClassPresenterSelector
  private lateinit var mAdapter: ArrayObjectAdapter

  override fun onCreate(savedInstanceState: Bundle?) {
    Log.d(TAG, "onCreate DetailsFragment")
    super.onCreate(savedInstanceState)

    mDetailsBackground = DetailsSupportFragmentBackgroundController(this)
    mBackgroundManager = BackgroundManager.getInstance(requireActivity())
    mBackgroundManager.attach(requireActivity().window)

    mCollection =
      requireActivity().intent.getSerializableExtra(CollectionActivity.COLLECTION) as Collection
    if (mCollection == null) {
      val intent = Intent(requireContext(), MainActivity::class.java)
      startActivity(intent)
    }

    mPresenterSelector = ClassPresenterSelector()
    mAdapter = ArrayObjectAdapter(mPresenterSelector)
    setupDetailsOverviewRow()
    setupDetailsOverviewRowPresenter()
    adapter = mAdapter

    if (mCollection!!.tracks == null) {

      lifecycleScope.launch {
        val apiClient = MetadataClient()
        when (mCollection) {
          is Album -> {
            when (val result = apiClient.fetchAlbumTracks((mCollection as Album).id!!)) {
              is ApiResult.Success -> {
                mCollection!!.tracks = result.data
                mAdapter.notifyArrayItemRangeChanged(0, mAdapter.size())
              }

              is ApiResult.Error -> {
                // Handle the error here
                Log.e(TAG, "Error fetching album tracks: ${result.exception}")
              }
            }
          }

          is Mix -> {
            when (val result = apiClient.fetchMixTracks((mCollection as Mix).id!!)) {
              is ApiResult.Success -> {
                mCollection!!.tracks = result.data
                mAdapter.notifyArrayItemRangeChanged(0, mAdapter.size())
              }

              is ApiResult.Error -> {
                // Handle the error here
                Log.e(TAG, "Error fetching mix tracks: ${result.exception}")
              }
            }
          }

          is Playlist -> {
            when (val result = apiClient.fetchPlaylistTracks((mCollection as Playlist).uuid!!)) {
              is ApiResult.Success -> {
                mCollection!!.tracks = result.data
                mAdapter.notifyArrayItemRangeChanged(0, mAdapter.size())
              }

              is ApiResult.Error -> {
                // Handle the error here
                Log.e(TAG, "Error fetching playlist tracks: ${result.exception}")
              }
            }
          }
        }
      }

    }

  }

  override fun onResume() {
    super.onResume()
    initializeBackground(mCollection)
  }

  private fun initializeBackground(collection: Collection?) {
    //mDetailsBackground.enableParallax()
    Glide.with(requireContext())
      .asBitmap()
      .centerCrop()
      .error(R.drawable.default_background)
      .load(collection?.largeImageUrl())
      .into(object : CustomTarget<Bitmap>() {

        override fun onResourceReady(
          bitmap: Bitmap,
          transition: Transition<in Bitmap?>?
        ) {
          mBackgroundManager.setBitmap(bitmap)
        }

        override fun onLoadCleared(placeholder: Drawable?) {

        }
      })

  }

  private fun setupDetailsOverviewRow() {
    Log.d(TAG, "doInBackground: " + mCollection?.toString())
    val row = DetailsOverviewRow(mCollection)
    row.imageDrawable = ContextCompat.getDrawable(requireContext(), R.drawable.default_background)
    val width = convertDpToPixel(requireContext(), DETAIL_THUMB_WIDTH)
    val height = convertDpToPixel(requireContext(), DETAIL_THUMB_HEIGHT)
    Glide.with(requireContext())
      .load(mCollection?.imageUrl())
      .centerCrop()
      .error(R.drawable.album)
      .into(object : CustomTarget<Drawable>(width, height) {
        override fun onResourceReady(drawable: Drawable, transition: Transition<in Drawable?>?) {
          Log.d(TAG, "details overview card image url ready: $drawable")
          row.imageDrawable = drawable
          mAdapter.notifyArrayItemRangeChanged(0, mAdapter.size())
        }

        override fun onLoadCleared(placeholder: Drawable?) {

        }
      })

    val actionAdapter = ArrayObjectAdapter()

    actionAdapter.add(Action(ACTION_PLAY_NOW, resources.getString(R.string.play_now)))
    actionAdapter.add(Action(ACTION_PLAY_NEXT, resources.getString(R.string.play_next)))
    actionAdapter.add(Action(ACTION_QUEUE, resources.getString(R.string.play_after)))

    if (mCollection is Album && (mCollection as Album).mainArtist() != null) {
      actionAdapter.add(Action(ACTION_GO_TO_ARTIST, resources.getString(R.string.go_to_artist)))
    }

    row.actionsAdapter = actionAdapter
    mAdapter.add(row)
  }

  private fun setupDetailsOverviewRowPresenter() {

    // Set detail background.
    mDetailsPresenter = FullWidthDetailsOverviewRowPresenter(DetailsPresenter(mCollection!!, this))
    mDetailsPresenter.backgroundColor = ColorUtils.setAlphaComponent(ContextCompat.getColor(requireContext(), R.color.details_background), ALPHA_VALUE)
    mDetailsPresenter.actionsBackgroundColor = ContextCompat.getColor(requireContext(), R.color.details_background)

    // Hook up transition element.
    val sharedElementHelper = FullWidthDetailsOverviewSharedElementHelper()
    sharedElementHelper.setSharedElementEnterTransition(activity, CollectionActivity.SHARED_ELEMENT_NAME)
    mDetailsPresenter.setListener(sharedElementHelper)
    mDetailsPresenter.isParticipatingEntranceTransition = true

    mDetailsPresenter.onActionClickedListener = OnActionClickedListener { action ->

      if (action.id == ACTION_PLAY_NOW) {
        playCollection()
        return@OnActionClickedListener
      }

      if (action.id == ACTION_PLAY_NEXT || action.id == ACTION_QUEUE) {
        val position = if (action.id == ACTION_PLAY_NEXT) EnqueuePosition.NEXT else EnqueuePosition.END
        queueCollection(position)
        return@OnActionClickedListener
      }

      if (action.id == ACTION_GO_TO_ARTIST) {
        Intent(requireContext(), ArtistActivity::class.java).apply {
          putExtra(ArtistActivity.ARTIST, (mCollection!! as Album).mainArtist()!!)
          startActivity(this)
        }
        return@OnActionClickedListener
      }

      // default for now
      Toast.makeText(requireContext(), action.toString(), Toast.LENGTH_SHORT).show()

    }

    mPresenterSelector.addClassPresenter(DetailsOverviewRow::class.java, mDetailsPresenter)
  }

  private fun convertDpToPixel(context: Context, dp: Int): Int {
    val density = context.applicationContext.resources.displayMetrics.density
    return Math.round(dp.toFloat() * density)
  }

  override fun onTrackClick(track: Track) {
    val index = mCollection!!.tracks!!.indexOf(track)
    playCollection(index)
  }

  override fun onTrackLongClick(track: Track) {
    ItemLongClickedListener(requireActivity()).onItemLongClicked(track, null)
  }

  private fun playCollection(index: Int = 0) {
    lifecycleScope.launch {
      val apiClient = StreamingClient()

      when (mCollection) {
        is Album -> {
          when (val result = apiClient.playAlbum((mCollection as Album).id!!, index)) {
            is ApiResult.Success -> {}
            is ApiResult.Error -> {
              Log.e(TAG, "Error playing albums: ${result.exception}")
            }
          }
        }

        is Mix -> {
          when (val result = apiClient.playMix((mCollection as Mix).id!!, index)) {
            is ApiResult.Success -> {}
            is ApiResult.Error -> {
              Log.e(TAG, "Error playing collection: ${result.exception}")
            }
          }
        }

        is Playlist -> {
          when (val result =
            apiClient.playPlaylist((mCollection as Playlist).uuid!!, index)) {
            is ApiResult.Success -> {}
            is ApiResult.Error -> {
              Log.e(TAG, "Error playing collection: ${result.exception}")
            }
          }
        }
      }
    }

  }

  private fun queueCollection(position: EnqueuePosition) {

    // we need tracks
    if (mCollection?.tracks == null) {
      return
    }

    lifecycleScope.launch {
      val apiClient = StreamingClient()
      when (val result =
        apiClient.enqueueTracks(mCollection!!.tracks!!.toTypedArray(), position)) {
        is ApiResult.Success -> {}
        is ApiResult.Error -> {
          Log.e(TAG, "Error playing albums: ${result.exception}")
        }
      }
    }
  }

  override fun onGenerated(palette: Palette?) {

    // get paletteColors
    val paletteUtils = PaletteUtils(palette!!)
    val actionsBgColor: Int = ColorUtils.setAlphaComponent(paletteUtils.getTitleBgColor(), ALPHA_VALUE)
    val contentBgColor: Int = ColorUtils.setAlphaComponent(paletteUtils.getContentBgColor(), ALPHA_VALUE)

    // background
    mDetailsPresenter.setActionsBackgroundColor(actionsBgColor)
    mDetailsPresenter.setBackgroundColor(contentBgColor)
    mAdapter.notifyArrayItemRangeChanged(0, mAdapter.size())
  }


  companion object {
    private const val TAG = "VideoDetailsFragment"

    private const val ACTION_PLAY_NOW = 1L
    private const val ACTION_PLAY_NEXT = 2L
    private const val ACTION_QUEUE = 3L
    private const val ACTION_GO_TO_ARTIST = 4L

    private const val DETAIL_THUMB_WIDTH = 274
    private const val DETAIL_THUMB_HEIGHT = 274

    private const val ALPHA_VALUE = 232

  }

}