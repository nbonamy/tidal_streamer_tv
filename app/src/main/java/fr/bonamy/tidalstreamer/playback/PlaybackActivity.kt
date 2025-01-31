package fr.bonamy.tidalstreamer.playback

import android.os.Bundle
import fr.bonamy.tidalstreamer.R
import fr.bonamy.tidalstreamer.utils.TidalActivity

class PlaybackActivity : TidalActivity() {

  override fun hasMiniPlayback(): Boolean {
    return false
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_playback)
    if (savedInstanceState == null) {
      supportFragmentManager.beginTransaction()
        .replace(R.id.playback_fragment, FullPlaybackFragment())
        .commitNow()
    }
  }

}
