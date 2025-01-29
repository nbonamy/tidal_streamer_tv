package fr.bonamy.tidalstreamer.artist

import android.os.Bundle
import fr.bonamy.tidalstreamer.R
import fr.bonamy.tidalstreamer.utils.TidalActivity

class ArtistActivity: TidalActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_artist)
  }

  companion object {
    const val ARTIST = "Artist"
  }

}