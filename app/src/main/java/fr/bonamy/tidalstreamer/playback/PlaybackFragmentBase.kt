package fr.bonamy.tidalstreamer.playback

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

  private val apiClient = StreamingClient()
  private lateinit var titleView: TextView
  private lateinit var artistView: TextView
  private lateinit var albumArtView: ImageView
  private val handler = Handler(Looper.getMainLooper())
  private var currentMediaId: String? = null

  protected fun createView(
    inflater: LayoutInflater, container: ViewGroup?, layoutId: Int
  ): View? {
    val v = inflater.inflate(layoutId, container, false)
    titleView = v.findViewById(R.id.title)
    artistView = v.findViewById(R.id.artist)
    albumArtView = v.findViewById(R.id.album_art)
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

    // update
    currentMediaId = track.id
    return StatusProcessResult.NEW_TRACK

  }

  companion object {
    //private const val TAG = "PlaybackFragment"
    private const val REFRESH_INTERVAL = 1000L
  }

}