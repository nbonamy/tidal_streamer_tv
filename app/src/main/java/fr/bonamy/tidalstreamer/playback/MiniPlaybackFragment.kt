package fr.bonamy.tidalstreamer.playback

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import fr.bonamy.tidalstreamer.R
import fr.bonamy.tidalstreamer.api.ApiResult
import fr.bonamy.tidalstreamer.api.StreamingClient
import fr.bonamy.tidalstreamer.models.Status
import kotlinx.coroutines.launch

class MiniPlaybackFragment : Fragment() {

  private val apiClient = StreamingClient()
  private lateinit var titleView: TextView
  private lateinit var artistView: TextView
  private lateinit var albumArtView: ImageView
  private val handler = Handler(Looper.getMainLooper())
  private var currentMediaId: String? = null

  override fun onCreateView(
    inflater: LayoutInflater, container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    val v = inflater.inflate(R.layout.fragment_mini_playback, container, false)
    titleView = v.findViewById(R.id.title)
    artistView = v.findViewById(R.id.artist)
    albumArtView = v.findViewById(R.id.album_art)

    // hide and done
    v.visibility = GONE
    return v
  }

  override fun onResume() {
    super.onResume()
    updateTask.run()
  }

  override fun onPause() {
    super.onPause()
    handler.removeCallbacks(updateTask)
  }

  private val updateTask = object : Runnable {
    override fun run() {
      viewLifecycleOwner.lifecycleScope.launch {
        when (val status = apiClient.status()) {
          is ApiResult.Success -> {
            processStatus(status.data)
          }

          is ApiResult.Error -> {
            hideSelf()
          }
        }
      }

      handler.postDelayed(this, REFRESH_INTERVAL)
    }
  }

  private fun processStatus(status: Status) {

    // basic checks
    if (status.state == "STOPPED" || status.tracks.isNullOrEmpty() || status.position < 0 || status.position >= status.tracks.size) {
      hideSelf()
      return
    }

    // get track
    val track = status.tracks[status.position].item
    if (track == null) {
      hideSelf()
      return
    }

    // are we already showing it?
    if (currentMediaId == track.id) {
      return
    }

    // all good!
    view?.visibility = View.VISIBLE
    titleView.text = track.title
    artistView.text = track.mainArtist()?.name ?: ""

    // album art
    Glide.with(this)
      .load(track.imageUrl())
      .centerCrop()
      .error(R.drawable.album)
      .into(albumArtView)

    // update
    currentMediaId = track.id
  }

  private fun hideSelf() {
    currentMediaId = null
    view?.visibility = GONE
  }

  companion object {
    //private const val TAG = "PlaybackFragment"
    private const val REFRESH_INTERVAL = 1000L
  }

}