package fr.bonamy.tidalstreamer.api

import fr.bonamy.tidalstreamer.models.Album
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Response

sealed class ApiResult<out T> {
	data class Success<T>(val data: T) : ApiResult<T>()
	data class Error(val exception: Throwable) : ApiResult<Nothing>()
}

class ApiClient {

	suspend fun fetchNewAlbums(): ApiResult<List<Album>> = withContext(Dispatchers.IO) {
		try {
			val response = apiService.getNewAlbums()
			fetchResponse(response)
		} catch (e: Exception) {
			ApiResult.Error(e)
		}
	}

	suspend fun fetchRecentAlbums(): ApiResult<List<Album>> = withContext(Dispatchers.IO) {
		try {
			val response = apiService.getRecentAlbums()
			fetchResponse(response)
		} catch (e: Exception) {
			ApiResult.Error(e)
		}
	}

	suspend fun fetchRecommendedAlbums(): ApiResult<List<Album>> = withContext(Dispatchers.IO) {
		try {
			val response = apiService.getRecommendedAlbums()
			fetchResponse(response)
		} catch (e: Exception) {
			ApiResult.Error(e)
		}
	}

	private suspend fun <T> fetchResponse(response: Response<ApiResponse<T>>): ApiResult<T> = withContext(Dispatchers.IO) {
		if (response.isSuccessful && response.body()!!.status == "ok") {
			ApiResult.Success(response.body()!!.result!!)
		} else {
			ApiResult.Error(Throwable("Error: ${response.code()}"))
		}
	}

	private val apiService: ApiService by lazy {
		ApiRetrofitClient.instance.create(ApiService::class.java)
	}

}

