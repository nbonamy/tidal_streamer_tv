package fr.bonamy.tidalstreamer.playback

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import fr.bonamy.tidalstreamer.R
import fr.bonamy.tidalstreamer.api.ApiResult
import fr.bonamy.tidalstreamer.api.StreamingClient
import fr.bonamy.tidalstreamer.api.UserClient
import fr.bonamy.tidalstreamer.models.STATE_STOPPED
import fr.bonamy.tidalstreamer.models.Status
import kotlinx.coroutines.launch

enum class StatusProcessResult {
  NO_TRACK,
  SAME_TRACK,
  NEW_TRACK,
}

abstract class PlaybackFragmentBase : Fragment() {

  abstract fun showSelf()
  abstract fun hideSelf()

  private lateinit var apiClient: StreamingClient
  private lateinit var userClient: UserClient
  private lateinit var titleView: TextView
  private lateinit var artistView: TextView
  private lateinit var albumArtView: ImageView
  private var heartView: ImageView? = null
  private var favoriteHintView: TextView? = null
  private var favoriteLoadingView: View? = null
  private val handler = Handler(Looper.getMainLooper())
  private var currentMediaId: String? = null

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    apiClient = StreamingClient(requireContext())
    userClient = UserClient(requireContext())
  }

  protected fun createView(
    inflater: LayoutInflater, container: ViewGroup?, layoutId: Int
  ): View? {
    val v = inflater.inflate(layoutId, container, false)
    titleView = v.findViewById(R.id.title)
    artistView = v.findViewById(R.id.artist)
    albumArtView = v.findViewById(R.id.album_art)
    heartView = v.findViewById(R.id.favorite)
    favoriteHintView = v.findViewById(R.id.favorite_hint)
    favoriteLoadingView = v.findViewById(R.id.favorite_loading)
    return v
  }

  override fun onResume() {
    super.onResume()
    runUpdate()
  }

  override fun onPause() {
    super.onPause()
    cancelUpdate()
  }

  protected fun runUpdate() {
    updateTask.run()
  }

  protected fun cancelUpdate() {
    handler.removeCallbacks(updateTask)
  }

  protected fun scheduleUpdate() {
    handler.postDelayed(updateTask, REFRESH_INTERVAL)
  }

  private val updateTask = Runnable {
    lifecycleScope.launch {
      when (val status = apiClient.status()) {
        is ApiResult.Success -> {
          processStatus(status.data)
        }

        is ApiResult.Error -> {
          hideSelf()
        }
      }
    }

    scheduleUpdate()
  }

  open fun processStatus(status: Status): StatusProcessResult {

    // basic checks
    if (status.state == STATE_STOPPED || status.tracks.isNullOrEmpty() || status.position < 0 || status.position >= status.tracks.size) {
      hideSelf()
      currentMediaId = null
      return StatusProcessResult.NO_TRACK
    }

    // get track
    val track = status.currentTrack()
    if (track == null) {
      hideSelf()
      currentMediaId = null
      return StatusProcessResult.NO_TRACK
    }

    // are we already showing it?
    if (currentMediaId == track.id) {
      return StatusProcessResult.SAME_TRACK
    }

    // all good!
    showSelf()
    titleView.text = track.title
    artistView.text = track.mainArtist()?.name ?: ""

    // album art
    Glide.with(this@PlaybackFragmentBase)
      .load(track.imageUrl())
      .centerCrop()
      .error(R.drawable.album)
      .into(albumArtView)

    // check favorite status
    track.id?.let { checkFavoriteStatus(it) }

    // update
    currentMediaId = track.id
    return StatusProcessResult.NEW_TRACK

  }

  private fun checkFavoriteStatus(trackId: String) {
    heartView?.visibility = View.GONE
    favoriteLoadingView?.visibility = View.VISIBLE
    lifecycleScope.launch {
      when (val result = userClient.isTrackFavorite(trackId)) {
        is ApiResult.Success -> updateFavoriteUI(result.data)
        is ApiResult.Error -> updateFavoriteUI(false)
      }
      favoriteLoadingView?.visibility = View.GONE
      heartView?.visibility = View.VISIBLE
    }
  }

  fun toggleFavorite() {
    currentMediaId?.let { trackId ->
      heartView?.visibility = View.GONE
      favoriteLoadingView?.visibility = View.VISIBLE
      lifecycleScope.launch {
        when (val result = userClient.toggleTrackFavorite(trackId)) {
          is ApiResult.Success -> updateFavoriteUI(result.data)
          is ApiResult.Error -> { }
        }
        favoriteLoadingView?.visibility = View.GONE
        heartView?.visibility = View.VISIBLE
      }
    }
  }

  private fun updateFavoriteUI(isFavorite: Boolean) {
    if (isFavorite) {
      heartView?.setImageResource(R.drawable.ic_heart)
      favoriteHintView?.text = "press  ●  to unfavorite"
    } else {
      heartView?.setImageResource(R.drawable.ic_heart_outline)
      favoriteHintView?.text = "press  ●  to favorite"
    }
  }

  companion object {
    //private const val TAG = "PlaybackFragment"
    private const val REFRESH_INTERVAL = 1000L
  }

}