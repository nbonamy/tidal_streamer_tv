package fr.bonamy.tidalstreamer.utils

import android.view.KeyEvent

object RemoteKey {

  fun isDisplay(keyCode: Int): Boolean {
    return keyCode == KeyEvent.KEYCODE_INFO
  }

  fun isAudioMenu(keyCode: Int): Boolean {
    return keyCode == KeyEvent.KEYCODE_MEDIA_AUDIO_TRACK || keyCode == KeyEvent.KEYCODE_A
  }

  fun isQueue(keyCode: Int): Boolean {
    return keyCode == KeyEvent.KEYCODE_LAST_CHANNEL
  }

  fun isCaptions(keyCode: Int): Boolean {
    return keyCode == KeyEvent.KEYCODE_CAPTIONS || keyCode == KeyEvent.KEYCODE_C
  }

  fun isHome(keyCode: Int): Boolean {
    return keyCode == KeyEvent.KEYCODE_PROG_YELLOW || keyCode == KeyEvent.KEYCODE_H
  }

  fun isUserCollection(keyCode: Int): Boolean {
    return keyCode == KeyEvent.KEYCODE_PROG_BLUE || keyCode == KeyEvent.KEYCODE_J
  }

  fun isUserSelection(keyCode: Int): Boolean {
    return keyCode == KeyEvent.KEYCODE_PROG_GREEN || keyCode == KeyEvent.KEYCODE_U
  }

  fun isFavorite(keyCode: Int): Boolean {
    return keyCode == KeyEvent.KEYCODE_MEDIA_RECORD || keyCode == KeyEvent.KEYCODE_F
  }
}
