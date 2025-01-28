package fr.bonamy.tidalstreamer.api

import fr.bonamy.tidalstreamer.models.Album
import fr.bonamy.tidalstreamer.models.Mix
import fr.bonamy.tidalstreamer.models.Track
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

	suspend fun fetchArtistAlbums(artistId: String): ApiResult<List<Album>> = withContext(Dispatchers.IO) {
		try {
			val response = apiService.getArtistAlbums(artistId)
			if (response.isSuccessful && response.body()!!.status == "ok") {
				ApiResult.Success(response.body()!!.result!!.items!!)
			} else {
				ApiResult.Error(Throwable("Error: ${response.code()}"))
			}
		} catch (e: Exception) {
			ApiResult.Error(e)
		}
	}

	suspend fun fetchArtistSingles(artistId: String): ApiResult<List<Album>> = withContext(Dispatchers.IO) {
		try {
			val response = apiService.getArtistSingles(artistId)
			if (response.isSuccessful && response.body()!!.status == "ok") {
				ApiResult.Success(response.body()!!.result!!.items!!)
			} else {
				ApiResult.Error(Throwable("Error: ${response.code()}"))
			}
		} catch (e: Exception) {
			ApiResult.Error(e)
		}
	}

	suspend fun fetchArtistCompilations(artistId: String): ApiResult<List<Album>> = withContext(Dispatchers.IO) {
		try {
			val response = apiService.getArtistCompilations(artistId)
			if (response.isSuccessful && response.body()!!.status == "ok") {
				ApiResult.Success(response.body()!!.result!!.items!!)
			} else {
				ApiResult.Error(Throwable("Error: ${response.code()}"))
			}
		} catch (e: Exception) {
			ApiResult.Error(e)
		}
	}

	suspend fun fetchAlbumTracks(albumId: String): ApiResult<List<Track>> = withContext(Dispatchers.IO) {
		try {
			val response = apiService.getAlbumTracks(albumId)
			if (response.isSuccessful && response.body()!!.status == "ok") {
				ApiResult.Success(response.body()!!.result!!.items!!.map { it.item!! })
			} else {
				ApiResult.Error(Throwable("Error: ${response.code()}"))
			}
		} catch (e: Exception) {
			ApiResult.Error(e)
		}
	}

	suspend fun fetchMixTracks(mixId: String): ApiResult<List<Track>> = withContext(Dispatchers.IO) {
		try {
			val response = apiService.getMixTracks(mixId)
			fetchResponse(response)
		} catch (e: Exception) {
			ApiResult.Error(e)
		}
	}

	private val apiService: MetadataService by lazy {
		ApiRetrofitClient.instance.create(MetadataService::class.java)
	}

}

