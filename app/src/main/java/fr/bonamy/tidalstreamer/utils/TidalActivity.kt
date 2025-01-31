package fr.bonamy.tidalstreamer.utils

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.KeyEvent
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import fr.bonamy.tidalstreamer.api.ApiResult
import fr.bonamy.tidalstreamer.api.StreamingClient
import fr.bonamy.tidalstreamer.playback.PlaybackActivity
import fr.bonamy.tidalstreamer.user.UserActivity
import kotlinx.coroutines.launch

abstract class TidalActivity : FragmentActivity() {

  private lateinit var apiClient: StreamingClient
  private val handler = Handler(Looper.getMainLooper())

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
//    if (savedInstanceState == null) {
//      if (hasMiniPlayback()) {
//        supportFragmentManager.beginTransaction()
//          .replace(R.id.playback_fragment, MiniPlaybackFragment())
//          .commitNow()
//      }
//    }
    apiClient = StreamingClient()
  }

  open fun hasMiniPlayback(): Boolean {
    return true
  }

  override fun onResume() {
    super.onResume()
    schedulePlaybackTask()
  }

  override fun onPause() {
    super.onPause()
    cancelPlaybackTask()
  }

  override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {

    // reschedule playback task
    schedulePlaybackTask()

    // playback
    if (keyCode == KeyEvent.KEYCODE_INFO || keyCode == KeyEvent.KEYCODE_I) {
      startPlaybackActivity()
      return true
    }

    // user collection
    if (keyCode == KeyEvent.KEYCODE_LAST_CHANNEL || keyCode == KeyEvent.KEYCODE_J) {
      val intent = Intent(this, UserActivity::class.java)
      startActivity(intent)
      return true
    }

    // toggle play/pause
    if (keyCode == KeyEvent.KEYCODE_MEDIA_PAUSE || keyCode == KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE || keyCode == KeyEvent.KEYCODE_P) {
      lifecycleScope.launch {
        when (val status = apiClient.status()) {
          is ApiResult.Success -> {
            if (status.data.state == "PAUSED") {
              apiClient.stop()
            } else {
              apiClient.pause()
            }
          }

          else -> {}
        }
      }
      return true
    }

    // play
    if (keyCode == KeyEvent.KEYCODE_MEDIA_PLAY || keyCode == KeyEvent.KEYCODE_O) {
      lifecycleScope.launch {
        apiClient.play()
      }
      return true
    }

    // next track
    if (keyCode == KeyEvent.KEYCODE_MEDIA_FAST_FORWARD || keyCode == KeyEvent.KEYCODE_RIGHT_BRACKET) {
      lifecycleScope.launch {
        apiClient.next()
      }
      return true
    }

    // prev track
    if (keyCode == KeyEvent.KEYCODE_MEDIA_REWIND || keyCode == KeyEvent.KEYCODE_LEFT_BRACKET) {
      lifecycleScope.launch {
        apiClient.previous()
      }
      return true
    }

    // volume up
    if (keyCode == KeyEvent.KEYCODE_VOLUME_UP || keyCode == KeyEvent.KEYCODE_CHANNEL_UP || keyCode == KeyEvent.KEYCODE_EQUALS) {
      lifecycleScope.launch {
        apiClient.volumeUp()
      }
      return true
    }

    // volume down
    if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN || keyCode == KeyEvent.KEYCODE_CHANNEL_DOWN || keyCode == KeyEvent.KEYCODE_MINUS) {
      lifecycleScope.launch {
        apiClient.volumeDown()
      }
      return true
    }

    // default
    return super.onKeyDown(keyCode, event)
  }

  private fun cancelPlaybackTask() {
    handler.removeCallbacks(playbackTask)
  }

  private fun schedulePlaybackTask() {
    cancelPlaybackTask()
    handler.postDelayed(playbackTask, REFRESH_INTERVAL)
  }

  private val playbackTask = object : Runnable {
    override fun run() {
      lifecycleScope.launch {
        when (val status = apiClient.status()) {
          is ApiResult.Success -> {
            if (status.data.state == "PLAYING") {
              startPlaybackActivity()
            }
          }

          is ApiResult.Error -> {
          }
        }
      }

      handler.postDelayed(this, REFRESH_INTERVAL)
    }
  }

  private fun startPlaybackActivity() {
    val intent = Intent(this, PlaybackActivity::class.java)
    startActivity(intent)
  }

  companion object {
    //private const val TAG = "PlaybackFragment"
    private const val REFRESH_INTERVAL = 15000L
  }

}