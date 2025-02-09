package fr.bonamy.tidalstreamer.api

import android.content.Context
import android.util.Log
import fr.bonamy.tidalstreamer.models.Album
import fr.bonamy.tidalstreamer.models.Queue
import fr.bonamy.tidalstreamer.models.Status
import fr.bonamy.tidalstreamer.models.Track
import fr.bonamy.tidalstreamer.utils.Configuration
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

enum class EnqueuePosition {
  NEXT,
  END,
}

class StreamingClient(mContext: Context) : ApiClient() {

  suspend fun status(): ApiResult<Status> = withContext(Dispatchers.IO) {
    try {
//      Log.i(TAG, "status")
      val response = apiService.status()
      if (response.isSuccessful) {
        ApiResult.Success(response.body()!!)
      } else {
        ApiResult.Error(Throwable("Error: ${response.code()}"))
      }
    } catch (e: Exception) {
      ApiResult.Error(e)
    }
  }

  suspend fun play(): ApiResult<String> = withContext(Dispatchers.IO) {
    try {
      Log.i(TAG, "play")
      val response = apiService.play()
      fetchResponse(response)
    } catch (e: Exception) {
      ApiResult.Error(e)
    }
  }

  suspend fun pause(): ApiResult<String> = withContext(Dispatchers.IO) {
    try {
      Log.i(TAG, "pause")
      val response = apiService.pause()
      fetchResponse(response)
    } catch (e: Exception) {
      ApiResult.Error(e)
    }
  }

  suspend fun stop(): ApiResult<String> = withContext(Dispatchers.IO) {
    try {
      Log.i(TAG, "stop")
      val response = apiService.stop()
      fetchResponse(response)
    } catch (e: Exception) {
      ApiResult.Error(e)
    }
  }

  suspend fun next(): ApiResult<String> = withContext(Dispatchers.IO) {
    try {
      Log.i(TAG, "next")
      val response = apiService.next()
      fetchResponse(response)
    } catch (e: Exception) {
      ApiResult.Error(e)
    }
  }

  suspend fun previous(): ApiResult<String> = withContext(Dispatchers.IO) {
    try {
      Log.i(TAG, "previous")
      val response = apiService.previous()
      fetchResponse(response)
    } catch (e: Exception) {
      ApiResult.Error(e)
    }
  }

  suspend fun seek(position: Int): ApiResult<String> = withContext(Dispatchers.IO) {
    try {
      Log.i(TAG, "seek $position")
      val response = apiService.seek(position)
      fetchResponse(response)
    } catch (e: Exception) {
      ApiResult.Error(e)
    }
  }

  suspend fun volumeUp(): ApiResult<String> = withContext(Dispatchers.IO) {
    try {
      Log.i(TAG, "volumeUp")
      val response = apiService.volumeUp()
      fetchResponse(response)
    } catch (e: Exception) {
      ApiResult.Error(e)
    }
  }

  suspend fun volumeDown(): ApiResult<String> = withContext(Dispatchers.IO) {
    try {
      Log.i(TAG, "volumeDown")
      val response = apiService.volumeDown()
      fetchResponse(response)
    } catch (e: Exception) {
      ApiResult.Error(e)
    }
  }

  suspend fun playTracks(tracks: Array<Track>, position: Int = 0): ApiResult<Queue> = withContext(Dispatchers.IO) {
    try {
      Log.i(TAG, "playTracks")
      val response = apiService.playTracks(mapOf("items" to tracks), position)
      fetchResponse(response)
    } catch (e: Exception) {
      ApiResult.Error(e)
    }
  }

  suspend fun enqueueTracks(tracks: Array<Track>, position: EnqueuePosition): ApiResult<String> = withContext(Dispatchers.IO) {
    try {
      Log.i(TAG, "enqueueTracks")
      val response = apiService.enqueue(tracks, position.name.lowercase())
      fetchResponse(response)
    } catch (e: Exception) {
      ApiResult.Error(e)
    }
  }

  suspend fun playAlbum(albumId: String, position: Int): ApiResult<Album> = withContext(Dispatchers.IO) {
    try {
      Log.i(TAG, "playAlbum")
      val response = apiService.playAlbum(albumId, position)
      fetchResponse(response)
    } catch (e: Exception) {
      ApiResult.Error(e)
    }
  }

  suspend fun playMix(mixId: String, position: Int): ApiResult<Queue> = withContext(Dispatchers.IO) {
    try {
      Log.i(TAG, "playMix")
      val response = apiService.playMix(mixId, position)
      fetchResponse(response)
    } catch (e: Exception) {
      ApiResult.Error(e)
    }
  }

  suspend fun playPlaylist(playlistId: String, position: Int): ApiResult<Queue> = withContext(Dispatchers.IO) {
    try {
      Log.i(TAG, "playPlaylist")
      val response = apiService.playPlaylist(playlistId, position)
      fetchResponse(response)
    } catch (e: Exception) {
      ApiResult.Error(e)
    }
  }

  private val apiService: StreamingService by lazy {
    val configuration = Configuration(mContext)
    ApiRetrofitClient.instance(configuration.getHttpBaseUrl()).create(StreamingService::class.java)
  }

  companion object {
    private const val TAG = "StreamingClient"
  }
}
