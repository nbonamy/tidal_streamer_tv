package fr.bonamy.tidalstreamer.collection

import android.os.Bundle
import fr.bonamy.tidalstreamer.R
import fr.bonamy.tidalstreamer.utils.TidalActivity

class CollectionActivity : TidalActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_collection)
    if (savedInstanceState == null) {
      supportFragmentManager.beginTransaction()
        .replace(R.id.details_fragment, CollectionFragment())
        .commitNow()
    }
  }

  companion object {
    const val SHARED_ELEMENT_NAME = "hero"
    const val COLLECTION = "Collection"
  }
}