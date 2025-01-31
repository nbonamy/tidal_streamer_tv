package fr.bonamy.tidalstreamer.playback

import android.os.Bundle
import android.view.KeyEvent
import androidx.activity.viewModels
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
      supportFragmentManager.beginTransaction()
        .replace(R.id.playback_fragment, FullPlaybackFragment(currentLayout))
        .commitNow()
    }
  }

  override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {

    // toggle lyrics
    if (keyCode == KeyEvent.KEYCODE_CAPTIONS || keyCode == KeyEvent.KEYCODE_C) {
      currentLayout = if (currentLayout == PlaybackLayout.NO_LYRICS) PlaybackLayout.LYRICS else PlaybackLayout.NO_LYRICS
      supportFragmentManager.beginTransaction()
        .replace(R.id.playback_fragment, FullPlaybackFragment(currentLayout))
        .commitNow()

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
    var currentLayout = PlaybackLayout.NO_LYRICS
  }

}
