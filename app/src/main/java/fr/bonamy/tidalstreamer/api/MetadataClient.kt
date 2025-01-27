package fr.bonamy.tidalstreamer.api

import fr.bonamy.tidalstreamer.models.Album
import fr.bonamy.tidalstreamer.models.Mix
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MetadataClient: ApiClient() {

	suspend fun fetchShortcuts(): ApiResult<List<Album>> = withContext(Dispatchers.IO) {
		try {
			val response = apiService.getShortcuts()
			fetchResponse(response)
		} catch (e: Exception) {
			ApiResult.Error(e)
		}
	}

	suspend fun fetchMixes(): ApiResult<List<Mix>> = withContext(Dispatchers.IO) {
		try {
			val response = apiService.getMixes()
			fetchResponse(response)
		} catch (e: Exception) {
			ApiResult.Error(e)
		}
	}

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

	protected val apiService: MetadataService by lazy {
		ApiRetrofitClient.instance.create(MetadataService::class.java)
	}

}

