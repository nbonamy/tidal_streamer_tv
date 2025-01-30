package fr.bonamy.tidalstreamer

import android.os.Bundle
import fr.bonamy.tidalstreamer.utils.TidalActivity

/**
 * Loads [MainFragment].
 */
class MainActivity : TidalActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)
    if (savedInstanceState == null) {
      getSupportFragmentManager().beginTransaction()
        .replace(R.id.main_browse_fragment, MainFragment())
        .commitNow()
    }
  }

}
