package fr.bonamy.tidalstreamer.api

import fr.bonamy.tidalstreamer.models.Album
import fr.bonamy.tidalstreamer.models.Artist
import fr.bonamy.tidalstreamer.models.Mix
import fr.bonamy.tidalstreamer.models.Playlist
import fr.bonamy.tidalstreamer.models.Track
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class UserClient : ApiClient() {

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
      if (response.isSuccessful && response.body()!!.status == "ok") {
        ApiResult.Success(response.body()!!.result!!.sortedWith { a, _ ->
          when (a.type) {
            "DISCOVERY_MIX", "NEW_RELEASE_MIX" -> -1
            else -> 1
          }
        })
      } else {
        ApiResult.Error(Throwable("Error: ${response.code()}"))
      }
    } catch (e: Exception) {
      ApiResult.Error(e)
    }
  }

  suspend fun fetchFavoriteAlbums(): ApiResult<List<Album>> = withContext(Dispatchers.IO) {
    try {
      val response = apiService.getFavoriteAlbums()
      if (response.isSuccessful && response.body()!!.status == "ok") {
        ApiResult.Success(response.body()!!.result!!.items!!.sortedByDescending { it.created }
          .map { it.item!! })
      } else {
        ApiResult.Error(Throwable("Error: ${response.code()}"))
      }
    } catch (e: Exception) {
      ApiResult.Error(e)
    }
  }

  suspend fun fetchFavoriteArtists(): ApiResult<List<Artist>> = withContext(Dispatchers.IO) {
    try {
      val response = apiService.getFavoriteArtists()
      if (response.isSuccessful && response.body()!!.status == "ok") {
        ApiResult.Success(response.body()!!.result!!.items!!.sortedByDescending { it.created }
          .map { it.item!! })
      } else {
        ApiResult.Error(Throwable("Error: ${response.code()}"))
      }
    } catch (e: Exception) {
      ApiResult.Error(e)
    }
  }

  suspend fun fetchFavoriteTracks(): ApiResult<List<Track>> = withContext(Dispatchers.IO) {
    try {
      val response = apiService.getFavoriteTracks()
      if (response.isSuccessful && response.body()!!.status == "ok") {
        ApiResult.Success(response.body()!!.result!!.items!!.sortedByDescending { it.created }
          .map { it.item!! })
      } else {
        ApiResult.Error(Throwable("Error: ${response.code()}"))
      }
    } catch (e: Exception) {
      ApiResult.Error(e)
    }
  }

  suspend fun fetchPlaylists(): ApiResult<List<Playlist>> = withContext(Dispatchers.IO) {
    try {
      val response = apiService.getPlaylists()
      if (response.isSuccessful && response.body()!!.status == "ok") {
        ApiResult.Success(response.body()!!.result!!.items!!.sortedByDescending { it.lastUpdated })
      } else {
        ApiResult.Error(Throwable("Error: ${response.code()}"))
      }
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

  suspend fun fetchRecentArtists(): ApiResult<List<Artist>> = withContext(Dispatchers.IO) {
    try {
      val response = apiService.getRecentArtists()
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

  private val apiService: UserService by lazy {
    ApiRetrofitClient.instance.create(UserService::class.java)
  }

}

