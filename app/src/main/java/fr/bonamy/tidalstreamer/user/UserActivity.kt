package fr.bonamy.tidalstreamer.user

import android.os.Bundle
import fr.bonamy.tidalstreamer.R
import fr.bonamy.tidalstreamer.utils.TidalActivity

class UserActivity : TidalActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_user)
    if (savedInstanceState == null) {
      supportFragmentManager.beginTransaction()
        .replace(R.id.user_fragment, UserFragment())
        .commitNow()
    }
  }

}