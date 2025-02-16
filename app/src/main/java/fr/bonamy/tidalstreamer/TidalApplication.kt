package fr.bonamy.tidalstreamer

import android.app.Application
import fr.bonamy.tidalstreamer.api.StreamerListener

class TidalApplication : Application() {

  override fun onCreate() {
    super.onCreate()
    StreamerListener.getInstance().start(this)
  }

  override fun onTerminate() {
    super.onTerminate()
    StreamerListener.getInstance().stop()
  }
}
