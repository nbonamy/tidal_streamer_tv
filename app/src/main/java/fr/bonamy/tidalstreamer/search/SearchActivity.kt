package fr.bonamy.tidalstreamer.search

import android.annotation.SuppressLint
import android.os.Bundle
import fr.bonamy.tidalstreamer.R
import fr.bonamy.tidalstreamer.utils.TidalActivity

class SearchActivity : TidalActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_search)
    if (savedInstanceState == null) {
      supportFragmentManager.beginTransaction()
        .replace(R.id.search_fragment, SearchFragment())
        .commitNow()
    }
  }

  @SuppressLint("MissingSuperCall")
  override fun onBackPressed() {
    (supportFragmentManager.findFragmentById(R.id.search_fragment) as SearchFragment).onBackPressed()
  }

  override fun canSwitchToPlayback(): Boolean {
    return false
  }

}