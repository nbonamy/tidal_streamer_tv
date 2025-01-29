package fr.bonamy.tidalstreamer.user

import android.os.Bundle
import fr.bonamy.tidalstreamer.R
import fr.bonamy.tidalstreamer.utils.TidalActivity

class UserActivity: TidalActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_user)
  }

  companion object {
    const val SHARED_ELEMENT_NAME = "hero"
    const val ARTIST = "Artist"
  }

}