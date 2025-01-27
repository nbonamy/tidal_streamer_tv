package fr.bonamy.tidalstreamer.utils

import android.os.Bundle
import android.view.KeyEvent
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import fr.bonamy.tidalstreamer.api.ApiResult
import fr.bonamy.tidalstreamer.api.StreamingClient
import kotlinx.coroutines.launch

abstract class TidalActivity : FragmentActivity() {

	private lateinit var apiClient: StreamingClient

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		apiClient = StreamingClient()
	}

	override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {

		if (keyCode == KeyEvent.KEYCODE_MEDIA_PAUSE || keyCode == KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE || keyCode == KeyEvent.KEYCODE_P) {
			lifecycleScope.launch {
				when (val status = apiClient.status()) {
					is ApiResult.Success -> {
						if (status.data.state == "PAUSED") {
							apiClient.stop()
						} else {
							apiClient.pause()
						}
					}

					else -> {}
				}
			}
			return true
		}

		if (keyCode == KeyEvent.KEYCODE_MEDIA_PLAY || keyCode == KeyEvent.KEYCODE_O) {
			lifecycleScope.launch {
				apiClient.play()
			}
			return true
		}

		if (keyCode == KeyEvent.KEYCODE_MEDIA_FAST_FORWARD || keyCode == KeyEvent.KEYCODE_RIGHT_BRACKET) {
			lifecycleScope.launch {
				apiClient.next()
			}
			return true
		}

		if (keyCode == KeyEvent.KEYCODE_MEDIA_REWIND || keyCode == KeyEvent.KEYCODE_LEFT_BRACKET) {
			lifecycleScope.launch {
				apiClient.previous()
			}
			return true
		}

		if (keyCode == KeyEvent.KEYCODE_VOLUME_UP || keyCode == KeyEvent.KEYCODE_CHANNEL_UP || keyCode == KeyEvent.KEYCODE_EQUALS) {
			lifecycleScope.launch {
				apiClient.volumeUp()
			}
			return true
		}

		if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN || keyCode == KeyEvent.KEYCODE_CHANNEL_DOWN || keyCode == KeyEvent.KEYCODE_MINUS) {
			lifecycleScope.launch {
				apiClient.volumeDown()
			}
			return true
		}

		return super.onKeyDown(keyCode, event)
	}

}