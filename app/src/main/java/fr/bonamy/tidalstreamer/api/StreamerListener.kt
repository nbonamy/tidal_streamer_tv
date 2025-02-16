package fr.bonamy.tidalstreamer.api

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.google.gson.Gson
import fr.bonamy.tidalstreamer.models.Status
import fr.bonamy.tidalstreamer.utils.Configuration
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okio.ByteString
import java.util.concurrent.TimeUnit

interface StreamerEventListener {
  fun onStatus(status: Status)
}

class StreamerListener private constructor() : WebSocketListener() {

  private lateinit var mRequest: Request
  private lateinit var mClient: OkHttpClient
  private lateinit var mWebSocket: WebSocket
  private val mListeners = mutableListOf<StreamerEventListener>()

  fun addListener(listener: StreamerEventListener) {
    mListeners.add(listener)
  }

  fun removeListener(listener: StreamerEventListener) {
    mListeners.remove(listener)
  }

  fun start(context: Context) {

    mClient = OkHttpClient.Builder()
      .readTimeout(0, TimeUnit.MILLISECONDS)
      .build()

    val configuration = Configuration(context)
    mRequest = Request.Builder()
      .url(configuration.getWsBaseUrl())
      .build()

    connect()

  }

  fun stop() {
    mWebSocket.close(1000, null)
  }

  private fun connect() {
    try {
      mWebSocket = mClient.newWebSocket(mRequest, this)
    } catch (e: Exception) {
      Log.e(TAG, "Failed to connect to WS: $e")
      reconnect()
    }
  }

  private fun reconnect(delayMillis: Long = 5000) {
    Handler(Looper.getMainLooper()).postDelayed({
      connect()
    }, delayMillis)
  }

  override fun onOpen(webSocket: WebSocket, response: Response) {
    Log.d(TAG, "WS Listener connected")
  }

  override fun onMessage(webSocket: WebSocket, text: String) {

    val status: Status?

    // decode
    try {
      status = Gson().fromJson(text, Status::class.java)
    } catch (e: Exception) {
      Log.e(TAG, "Failed to parse status: $e")
      return
    }

    // now process
    if (status != null) {
      mListeners.forEach {
        try {
          it.onStatus(status)
        } catch (e: Exception) {
          Log.e(TAG, "Failed to process status: $e")
        }
      }
    }

  }

  override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
    //Log.d(TAG, "WS MESSAGE: " + bytes.hex())
  }

  override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
    Log.d(TAG, "WS CLOSING: $code $reason")
    webSocket.close(1000, null)
  }

  override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
    Log.d(TAG, "WS CLOSE: $code $reason. Restarting...")
    reconnect()
  }

  override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
    Log.e(TAG, "WS FAILURE: $t")
    reconnect()
  }

  companion object {
    const val TAG = "StreamerListener"

    private var INSTANCE: StreamerListener? = null

    fun getInstance(): StreamerListener {
      return INSTANCE ?: synchronized(this) {
        INSTANCE ?: StreamerListener().also { INSTANCE = it }
      }
    }
  }
}
