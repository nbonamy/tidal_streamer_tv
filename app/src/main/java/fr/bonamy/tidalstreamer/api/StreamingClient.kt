package fr.bonamy.tidalstreamer.api

import fr.bonamy.tidalstreamer.models.Album
import fr.bonamy.tidalstreamer.models.Queue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Response

class StreamingClient: ApiClient() {

	suspend fun playAlbum(albumId: String): ApiResult<Album> = withContext(Dispatchers.IO) {
		try {
			val response = apiService.playAlbum(albumId)
			fetchResponse(response)
		} catch (e: Exception) {
			ApiResult.Error(e)
		}
	}

	suspend fun playMix(mixId: String): ApiResult<Queue> = withContext(Dispatchers.IO) {
		try {
			val response = apiService.playMix(mixId)
			fetchResponse(response)
		} catch (e: Exception) {
			ApiResult.Error(e)
		}
	}

	protected val apiService: StreamingService by lazy {
		ApiRetrofitClient.instance.create(StreamingService::class.java)
	}

}
