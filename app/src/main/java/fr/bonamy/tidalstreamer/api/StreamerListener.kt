package fr.bonamy.tidalstreamer.api

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

class StreamerListener// Trigger shutdown of the dispatcher's executor so this process exits immediately.
  (private var mListener: StreamerEventListener) : WebSocketListener() {

  lateinit var client: OkHttpClient
  lateinit var webSocket: WebSocket

  fun start() {
    val configuration = Configuration()
    client = OkHttpClient.Builder()
      .readTimeout(0, TimeUnit.MILLISECONDS)
      .build()
    val request: Request = Request.Builder()
      .url(configuration.getWsBaseUrl())
      .build()
    webSocket = client.newWebSocket(request, this)
    client.dispatcher.executorService.shutdown()
  }

  fun stop() {
    webSocket.close(1000, null)
  }

  override fun onOpen(webSocket: WebSocket, response: Response) {
    //Log.d(TAG, "WS Listener connected")
  }

  override fun onMessage(webSocket: WebSocket, text: String) {

    var status: Status?

    // decode
    try {
      status = Gson().fromJson(text, Status::class.java)
    } catch (e: Exception) {
      Log.e(TAG, "Failed to parse status: $e")
      return
    }

    // now process
    if (status != null) {
      mListener.onStatus(status)
    }

  }

  override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
    //Log.d(TAG, "WS MESSAGE: " + bytes.hex())
  }

  override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
    webSocket.close(1000, null)
    //Log.d(TAG, "WS CLOSE: $code $reason")
  }

  override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
    Log.e(TAG, "WS FAILURE: $t")
    //t.printStackTrace()
  }

  companion object {
    const val TAG = "StreamerListener"
  }
}