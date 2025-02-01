package fr.bonamy.tidalstreamer.playback

import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import androidx.activity.viewModels
import androidx.fragment.app.commit
import androidx.fragment.app.commitNow
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import fr.bonamy.tidalstreamer.R
import fr.bonamy.tidalstreamer.utils.TidalActivity

class PlaybackKeyEventViewModel : ViewModel() {
  private val _keyEvent: MutableLiveData<Int> = MutableLiveData(-1)
  val keyEvent: LiveData<Int> get() = _keyEvent
  fun setEvent(code: Int) {
    _keyEvent.value = code
  }
}

enum class PlaybackLayout {
  NO_LYRICS,
  LYRICS
}

class PlaybackActivity : TidalActivity() {

  private val viewModel: PlaybackKeyEventViewModel by viewModels()

  override fun hasMiniPlayback(): Boolean {
    return false
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_playback)
    if (savedInstanceState == null) {
      supportFragmentManager.commitNow {
        replace(R.id.playback_fragment, FullPlaybackFragment(currentLayout, null))
      }
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

    // lyrics scrolling: send event to fragment
    if (keyCode == KeyEvent.KEYCODE_DPAD_UP || keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
      viewModel.setEvent(keyCode)
      return true
    }

    // default
    return super.onKeyDown(keyCode, event)
  }

  companion object {
    private const val TAG = "PlaybackActivity"
    var currentLayout = PlaybackLayout.NO_LYRICS
  }

}
