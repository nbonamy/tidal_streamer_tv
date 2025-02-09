package fr.bonamy.tidalstreamer.playback

import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
import androidx.fragment.app.commit
import androidx.fragment.app.commitNow
import fr.bonamy.tidalstreamer.R
import fr.bonamy.tidalstreamer.utils.TidalActivity

enum class PlaybackLayout {
  NO_LYRICS,
  LYRICS
}

class PlaybackActivity : TidalActivity() {

  override fun hasMiniPlayback(): Boolean {
    return false
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    window.addFlags(FLAG_KEEP_SCREEN_ON)
    setContentView(R.layout.activity_playback)
    if (savedInstanceState == null) {
      supportFragmentManager.commitNow {
        replace(R.id.playback_fragment, FullPlaybackFragment(currentLayout, null))
      }
    }
  }

  override fun onBackPressed() {
    if (currentLayout == PlaybackLayout.LYRICS) {
      onKeyDown(KeyEvent.KEYCODE_DPAD_CENTER, null)
    } else {
      super.onBackPressed()
    }
  }

  override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {

    // toggle lyrics
    if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER || keyCode == KeyEvent.KEYCODE_CAPTIONS || keyCode == KeyEvent.KEYCODE_C) {
      currentLayout =
        if (currentLayout == PlaybackLayout.NO_LYRICS) PlaybackLayout.LYRICS
        else PlaybackLayout.NO_LYRICS
      val currentFragment = supportFragmentManager.findFragmentById(R.id.playback_fragment) as FullPlaybackFragment
      supportFragmentManager.commit {
        setReorderingAllowed(true)
        addSharedElement(findViewById(R.id.title), findViewById<View>(R.id.title).transitionName)
        addSharedElement(findViewById(R.id.artist), findViewById<View>(R.id.artist).transitionName)
        addSharedElement(findViewById(R.id.album_art), findViewById<View>(R.id.album_art).transitionName)
        addSharedElement(findViewById(R.id.progress), findViewById<View>(R.id.progress).transitionName)
        replace(R.id.playback_fragment, FullPlaybackFragment(currentLayout, currentFragment.latestStatus()))
      }
      return true
    }

    // we might need the fragment
    val fragment = supportFragmentManager.findFragmentById(R.id.playback_fragment) as FullPlaybackFragment

    // lyrics scrolling: send event to fragment
    if (keyCode == KeyEvent.KEYCODE_DPAD_UP || keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
      return fragment.onKeyDown(keyCode, event)
    }

    // jump for menu
    if (keyCode == KeyEvent.KEYCODE_LAST_CHANNEL || keyCode == KeyEvent.KEYCODE_J) {
      return fragment.onKeyDown(keyCode, event)
    }

    // default
    return super.onKeyDown(keyCode, event)
  }

  companion object {
    var currentLayout = PlaybackLayout.NO_LYRICS
  }

}
