package fr.bonamy.tidalstreamer.auth

import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import fr.bonamy.tidalstreamer.R

class AuthActivity : FragmentActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_auth)
    if (savedInstanceState == null) {
      supportFragmentManager.beginTransaction()
        .replace(R.id.auth_fragment, AuthFragment())
        .commitNow()
    }
  }

}
