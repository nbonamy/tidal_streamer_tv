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
import fr.bonamy.tidalstreamer.api.StreamerListener
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
  private var favoriteCheckRequest = 0

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
    runFallbackUpdate()
  }

  override fun onPause() {
    super.onPause()
    cancelUpdate()
  }

  protected fun runFallbackUpdate() {
    updateTask.run()
  }

  protected fun cancelUpdate() {
    handler.removeCallbacks(updateTask)
  }

  protected fun scheduleUpdate() {
    handler.postDelayed(updateTask, FALLBACK_CHECK_INTERVAL)
  }

  private val updateTask = Runnable {
    val listener = StreamerListener.getInstance()
    if (listener.hasFreshStatus(WEBSOCKET_STATUS_MAX_AGE)) {
      listener.status?.let { processStatus(it) }
      scheduleUpdate()
      return@Runnable
    }

    lifecycleScope.launch {
      when (val status = apiClient.status()) {
        is ApiResult.Success -> processStatus(status.data)
        is ApiResult.Error -> hideSelf()
      }
      scheduleUpdate()
    }
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

    // update
    currentMediaId = track.id

    // check favorite status
    track.id?.let { trackId ->
      val favoriteState = favoriteStates[trackId]
      if (favoriteState == null) {
        checkFavoriteStatus(trackId)
      } else {
        updateFavoriteUI(favoriteState)
      }
    }
    return StatusProcessResult.NEW_TRACK

  }

  private fun checkFavoriteStatus(trackId: String) {
    val request = ++favoriteCheckRequest
    heartView?.visibility = View.GONE
    favoriteLoadingView?.visibility = View.VISIBLE
    lifecycleScope.launch {
      when (val result = userClient.isTrackFavorite(trackId)) {
        is ApiResult.Success -> {
          if (currentMediaId != trackId || request != favoriteCheckRequest) return@launch
          favoriteStates[trackId] = result.data
          updateFavoriteUI(result.data)
        }

        is ApiResult.Error -> {
          if (currentMediaId != trackId || request != favoriteCheckRequest) return@launch
        }
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
          is ApiResult.Success -> {
            favoriteStates[trackId] = result.data
            updateFavoriteUI(result.data)
          }
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
      favoriteHintView?.text = getString(R.string.playback_hint_unfavorite)
    } else {
      heartView?.setImageResource(R.drawable.ic_heart_outline)
      favoriteHintView?.text = getString(R.string.playback_hint_favorite)
    }
  }

  companion object {
    //private const val TAG = "PlaybackFragment"
    private const val FALLBACK_CHECK_INTERVAL = 3000L
    private const val WEBSOCKET_STATUS_MAX_AGE = 3500L
    private val favoriteStates = mutableMapOf<String, Boolean>()
  }

}
