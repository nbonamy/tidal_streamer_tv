package fr.bonamy.tidalstreamer.utils

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.KeyEvent
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import fr.bonamy.tidalstreamer.MainActivity
import fr.bonamy.tidalstreamer.R
import fr.bonamy.tidalstreamer.api.ApiResult
import fr.bonamy.tidalstreamer.api.StreamingClient
import fr.bonamy.tidalstreamer.models.STATE_PAUSED
import fr.bonamy.tidalstreamer.models.STATE_PLAYING
import fr.bonamy.tidalstreamer.models.STATE_STOPPED
import fr.bonamy.tidalstreamer.models.Status
import fr.bonamy.tidalstreamer.playback.PlaybackActivity
import fr.bonamy.tidalstreamer.user.UserActivity
import kotlinx.coroutines.launch
import kotlin.math.min

abstract class TidalActivity : FragmentActivity() {

  private lateinit var mApiClient: StreamingClient
  private val mHandler = Handler(Looper.getMainLooper())
  private var mStatus: Status? = null
  private var mSeekKey: Int = 0
  private var mIsSeeking = false

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
//    if (savedInstanceState == null) {
//      if (hasMiniPlayback()) {
//        supportFragmentManager.beginTransaction()
//          .replace(R.id.playback_fragment, MiniPlaybackFragment())
//          .commitNow()
//      }
//    }
    mApiClient = StreamingClient()
  }

  open fun hasMiniPlayback(): Boolean {
    return true
  }

  open fun canSwitchToPlayback(): Boolean {
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

    // color keys order: YELLOW, BLUE, RED, GREEN

    // reschedule playback task
    schedulePlaybackTask()

    // playback
    if (keyCode == KeyEvent.KEYCODE_INFO || keyCode == KeyEvent.KEYCODE_I) {
      startPlaybackActivity()
      return true
    }

    // back to home
    if (keyCode == KeyEvent.KEYCODE_PROG_YELLOW || keyCode == KeyEvent.KEYCODE_H) {
      val intent = Intent(this, MainActivity::class.java)
      intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
      startActivity(intent)
      return true
    }

    // user collection
    if (keyCode == KeyEvent.KEYCODE_PROG_BLUE || keyCode == KeyEvent.KEYCODE_J) {
      val intent = Intent(this, UserActivity::class.java)
      startActivity(intent)
      return true
    }

    // toggle play/pause
    if (keyCode == KeyEvent.KEYCODE_MEDIA_PAUSE || keyCode == KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE || keyCode == KeyEvent.KEYCODE_P) {
      lifecycleScope.launch {
        when (val status = mApiClient.status()) {
          is ApiResult.Success -> {
            if (status.data.state == STATE_PAUSED) {
              mApiClient.stop()
            } else {
              mApiClient.pause()
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
        mApiClient.play()
      }
      return true
    }

    // track seek or move
    if (isNextTrackKey(keyCode) || isPreviousTrackKey(keyCode)) {
      lifecycleScope.launch {
        if (mStatus == null) {
          mIsSeeking = false
          when (val result = mApiClient.status()) {
            is ApiResult.Success -> {
              mSeekKey = keyCode
              mStatus = result.data
              mHandler.postDelayed(seekTrackTask, SEEK_INITIAL_DELAY)
            }

            is ApiResult.Error -> {}
          }
        }
      }
      return true
    }

    // volume up
    if (keyCode == KeyEvent.KEYCODE_VOLUME_UP || keyCode == KeyEvent.KEYCODE_CHANNEL_UP || keyCode == KeyEvent.KEYCODE_EQUALS) {
      lifecycleScope.launch {
        mApiClient.volumeUp()
      }
      return true
    }

    // volume down
    if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN || keyCode == KeyEvent.KEYCODE_CHANNEL_DOWN || keyCode == KeyEvent.KEYCODE_MINUS) {
      lifecycleScope.launch {
        mApiClient.volumeDown()
      }
      return true
    }

    // try fragment
    supportFragmentManager.fragments.forEach {
      if (it is BrowserFragment) {
        val rc = it.onKeyDown(keyCode, event)
        if (rc) return true
        return@forEach
      }
    }

    // default
    return super.onKeyDown(keyCode, event)
  }

  override fun onKeyUp(keyCode: Int, event: KeyEvent?): Boolean {

    // first cancel seek task
    if (isNextTrackKey(keyCode) || isPreviousTrackKey(keyCode)) {
      mHandler.removeCallbacks(seekTrackTask)
      mStatus = null
    }

    // next track
    if (isNextTrackKey(keyCode) && !mIsSeeking) {
      lifecycleScope.launch {
        mApiClient.next()
      }
      return true
    }

    // prev track
    if (isPreviousTrackKey(keyCode) && !mIsSeeking) {
      lifecycleScope.launch {
        mApiClient.previous()
      }
      return true
    }

    // default
    return super.onKeyUp(keyCode, event)
  }

  private fun isPreviousTrackKey(keyCode: Int) = keyCode == KeyEvent.KEYCODE_MEDIA_REWIND || keyCode == KeyEvent.KEYCODE_LEFT_BRACKET

  private fun isNextTrackKey(keyCode: Int) = keyCode == KeyEvent.KEYCODE_MEDIA_FAST_FORWARD || keyCode == KeyEvent.KEYCODE_RIGHT_BRACKET

  private fun cancelPlaybackTask() {
    mHandler.removeCallbacks(checkPlaybackTask)
  }

  private fun schedulePlaybackTask() {
    cancelPlaybackTask()
    mHandler.postDelayed(checkPlaybackTask, REFRESH_INTERVAL)
  }

  private val checkPlaybackTask = object : Runnable {
    override fun run() {

      if (canSwitchToPlayback()) {
        lifecycleScope.launch {
          when (val status = mApiClient.status()) {
            is ApiResult.Success -> {
              if (status.data.state == STATE_PLAYING) {
                startPlaybackActivity()
              }
            }

            is ApiResult.Error -> {
            }
          }
        }
      }

      mHandler.postDelayed(this, REFRESH_INTERVAL)
    }
  }

  private val seekTrackTask = object : Runnable {
    override fun run() {
      lifecycleScope.launch {

        // current state
        val progress = mStatus!!.progress
        val track = mStatus!!.currentTrack()

        // update is between 0 and duration minus step
        var updated = if (isNextTrackKey(mSeekKey)) progress + SEEK_STEP else progress - SEEK_STEP
        if (updated < 0) updated = 0
        if (track != null) {
          updated = min(track.duration * 1000 - SEEK_STEP, updated)
        }

        // update local status for next iteration
        mStatus = Status(
          state = mStatus!!.state,
          tracks = mStatus!!.tracks,
          position = mStatus!!.position,
          progress = updated
        )

        // do it!
        mIsSeeking = true
        mApiClient.seek(updated / 1000)
      }

      // repeat
      mHandler.postDelayed(this, SEEK_REPEAT_DELAY)
      
    }
  }

  private fun startPlaybackActivity() {
    lifecycleScope.launch {
      when (val result = mApiClient.status()) {
        is ApiResult.Success -> {
          if (result.data.state != STATE_STOPPED) {
            val intent = Intent(this@TidalActivity, PlaybackActivity::class.java)
            startActivity(intent)
          } else {
            Toast.makeText(this@TidalActivity, getString(R.string.error_no_playback), Toast.LENGTH_SHORT).show()
          }
        }

        is ApiResult.Error -> {
        }
      }
    }
  }

  companion object {
    //private const val TAG = "PlaybackFragment"
    private const val REFRESH_INTERVAL = 15000L
    private const val SEEK_INITIAL_DELAY = 250L
    private const val SEEK_REPEAT_DELAY = 100L
    private const val SEEK_STEP = 5000
  }

}