package fr.bonamy.tidalstreamer.playback

import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import androidx.fragment.app.commitNow
import fr.bonamy.tidalstreamer.R
import fr.bonamy.tidalstreamer.queue.QueueFragment
import fr.bonamy.tidalstreamer.utils.RemoteKey
import fr.bonamy.tidalstreamer.utils.TidalActivity

enum class PlaybackLayout {
  NO_LYRICS,
  LYRICS
}

enum class PlaybackScreenMode {
  PLAYBACK,
  QUEUE,
  LYRICS
}

class PlaybackActivity : TidalActivity() {

  override fun hasMiniPlayback(): Boolean {
    return false
  }

  override fun canSwitchToPlayback(): Boolean {
    return currentMode == PlaybackScreenMode.PLAYBACK
  }

  override fun closeOnGoToKey(): Boolean {
    return true
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    window.addFlags(FLAG_KEEP_SCREEN_ON)
    setContentView(R.layout.activity_playback)
    if (savedInstanceState == null) {
      switchMode(modeFromIntent(intent), immediate = true)
    }
  }

  override fun onNewIntent(intent: Intent) {
    super.onNewIntent(intent)
    setIntent(intent)
    switchMode(modeFromIntent(intent))
  }

  override fun onBackPressed() {
    super.onBackPressed()
  }

  override fun dispatchKeyEvent(event: KeyEvent): Boolean {
    val fragment = supportFragmentManager.findFragmentById(R.id.playback_fragment)
    if (fragment is QueueFragment && fragment.dispatchKeyEvent(event)) {
      return true
    }

    return super.dispatchKeyEvent(event)
  }

  override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {

    if (RemoteKey.isCaptions(keyCode)) {
      switchMode(if (currentMode == PlaybackScreenMode.LYRICS) PlaybackScreenMode.PLAYBACK else PlaybackScreenMode.LYRICS)
      return true
    }

    if (RemoteKey.isAudioMenu(keyCode)) {
      showCurrentTrackMenu()
      return true
    }

    if (RemoteKey.isQueue(keyCode)) {
      switchMode(if (currentMode == PlaybackScreenMode.QUEUE) PlaybackScreenMode.PLAYBACK else PlaybackScreenMode.QUEUE)
      return true
    }

    if (RemoteKey.isDisplay(keyCode)) {
      switchMode(PlaybackScreenMode.PLAYBACK)
      return true
    }

    // we might need the fragment
    val fragment = supportFragmentManager.findFragmentById(R.id.playback_fragment)

    // toggle favorite
    if (RemoteKey.isFavorite(keyCode)) {
      (fragment as? FullPlaybackFragment)?.toggleFavorite()
      return true
    }

    if (fragment is QueueFragment && fragment.onKeyDown(keyCode, event)) {
      return true
    }

    // lyrics scrolling: send event to fragment
    if (fragment is FullPlaybackFragment && (keyCode == KeyEvent.KEYCODE_DPAD_UP || keyCode == KeyEvent.KEYCODE_DPAD_DOWN)) {
      return fragment.onKeyDown(keyCode, event)
    }

    // default
    return super.onKeyDown(keyCode, event)
  }

  override fun onKeyUp(keyCode: Int, event: KeyEvent?): Boolean {
    val fragment = supportFragmentManager.findFragmentById(R.id.playback_fragment)
    if (fragment is QueueFragment && fragment.onKeyUp(keyCode)) {
      return true
    }

    return super.onKeyUp(keyCode, event)
  }

  private fun switchMode(mode: PlaybackScreenMode, immediate: Boolean = false) {
    if (!immediate) {
      supportFragmentManager.executePendingTransactions()
    }

    val currentFragment = supportFragmentManager.findFragmentById(R.id.playback_fragment)
    val previousMode = if (currentFragment is QueueFragment) PlaybackScreenMode.QUEUE else currentMode
    currentMode = mode
    currentLayout = if (mode == PlaybackScreenMode.LYRICS) PlaybackLayout.LYRICS else PlaybackLayout.NO_LYRICS

    val status = when (currentFragment) {
      is FullPlaybackFragment -> currentFragment.latestStatus()
      is QueueFragment -> currentFragment.latestStatus()
      else -> null
    }
    val nextFragment: Fragment = when (mode) {
      PlaybackScreenMode.PLAYBACK -> FullPlaybackFragment(PlaybackLayout.NO_LYRICS, status)
      PlaybackScreenMode.LYRICS -> FullPlaybackFragment(PlaybackLayout.LYRICS, status)
      PlaybackScreenMode.QUEUE -> QueueFragment(status)
    }

    if (immediate) {
      supportFragmentManager.commitNow {
        replace(R.id.playback_fragment, nextFragment)
      }
    } else {
      supportFragmentManager.commit {
        setReorderingAllowed(true)
        if (shouldUsePlaybackSharedElements(previousMode, mode, currentFragment)) {
          addPlaybackSharedElement(R.id.title)
          addPlaybackSharedElement(R.id.artist)
          addPlaybackSharedElement(R.id.album_art)
          addPlaybackSharedElement(R.id.progress)
        }
        replace(R.id.playback_fragment, nextFragment)
      }
    }
  }

  private fun shouldUsePlaybackSharedElements(
    previousMode: PlaybackScreenMode,
    nextMode: PlaybackScreenMode,
    currentFragment: Fragment?
  ): Boolean {
    return (currentFragment is FullPlaybackFragment || currentFragment is QueueFragment) &&
      previousMode != nextMode &&
      (previousMode == PlaybackScreenMode.PLAYBACK || nextMode == PlaybackScreenMode.PLAYBACK)
  }

  private fun androidx.fragment.app.FragmentTransaction.addPlaybackSharedElement(viewId: Int) {
    val view = findViewById<View>(viewId) ?: return
    val transitionName = view.transitionName ?: return
    addSharedElement(view, transitionName)
  }

  private fun modeFromIntent(intent: Intent?): PlaybackScreenMode {
    val modeName = intent?.getStringExtra(EXTRA_MODE) ?: PlaybackScreenMode.PLAYBACK.name
    return PlaybackScreenMode.entries.firstOrNull { it.name == modeName } ?: PlaybackScreenMode.PLAYBACK
  }

  companion object {
    const val EXTRA_MODE = "fr.bonamy.tidalstreamer.playback.MODE"
    var currentLayout = PlaybackLayout.NO_LYRICS
    var currentMode = PlaybackScreenMode.PLAYBACK
  }

}
