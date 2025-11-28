package fr.bonamy.tidalstreamer.api

import android.content.Context
import fr.bonamy.tidalstreamer.models.Album
import fr.bonamy.tidalstreamer.models.Artist
import fr.bonamy.tidalstreamer.models.Collection
import fr.bonamy.tidalstreamer.models.Mix
import fr.bonamy.tidalstreamer.models.Playlist
import fr.bonamy.tidalstreamer.models.Track
import fr.bonamy.tidalstreamer.utils.Configuration
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class UserClient(mContext: Context) : ApiClient() {


  suspend fun fetchShortcuts(): ApiResult<List<Collection>> = withContext(Dispatchers.IO) {
    try {
      val response = apiService.getShortcuts()
      if (response.isSuccessful && response.body()!!.status == "ok") {
        val collections = response.body()!!.result!!.mapNotNull { it.toCollection() }
        ApiResult.Success(collections)
      } else {
        ApiResult.Error(Throwable("Error: ${response.code()}"))
      }
    } catch (e: Exception) {
      ApiResult.Error(e)
    }
  }

  suspend fun fetchDailyMixes(): ApiResult<List<Mix>> = withContext(Dispatchers.IO) {
    try {
      val response = apiService.getDailyMixes()
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

//  suspend fun fetchRecentAlbums(): ApiResult<List<Album>> = withContext(Dispatchers.IO) {
//    try {
//      val response = apiService.getRecentAlbums()
//      fetchResponse(response)
//    } catch (e: Exception) {
//      ApiResult.Error(e)
//    }
//  }

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

  suspend fun fetchForgottenAlbums(): ApiResult<List<Album>> = withContext(Dispatchers.IO) {
    try {
      val response = apiService.getForgottenAlbums()
      fetchResponse(response)
    } catch (e: Exception) {
      ApiResult.Error(e)
    }
  }

  suspend fun fetchHistoryMixes(): ApiResult<List<Mix>> = withContext(Dispatchers.IO) {
    try {
      val response = apiService.getHistoryMixes()
      fetchResponse(response)
    } catch (e: Exception) {
      ApiResult.Error(e)
    }
  }

  suspend fun fetchEssentialPlaylists(): ApiResult<List<Playlist>> = withContext(Dispatchers.IO) {
    try {
      val response = apiService.getEssentialPlaylists()
      fetchResponse(response)
    } catch (e: Exception) {
      ApiResult.Error(e)
    }
  }

  suspend fun fetchRadioMixes(): ApiResult<List<Mix>> = withContext(Dispatchers.IO) {
    try {
      val response = apiService.getRadioMixes()
      fetchResponse(response)
    } catch (e: Exception) {
      ApiResult.Error(e)
    }
  }

  suspend fun fetchSpotlightedTracks(): ApiResult<List<Track>> = withContext(Dispatchers.IO) {
    try {
      val response = apiService.getSpotlightedTracks()
      fetchResponse(response)
    } catch (e: Exception) {
      ApiResult.Error(e)
    }
  }

  suspend fun fetchUploadsTracks(): ApiResult<List<Track>> = withContext(Dispatchers.IO) {
    try {
      val response = apiService.getUploadsTracks()
      fetchResponse(response)
    } catch (e: Exception) {
      ApiResult.Error(e)
    }
  }

  suspend fun fetchNewTracks(): ApiResult<List<Track>> = withContext(Dispatchers.IO) {
    try {
      val response = apiService.getNewTracks()
      fetchResponse(response)
    } catch (e: Exception) {
      ApiResult.Error(e)
    }
  }

  suspend fun fetchPopularPlaylists(): ApiResult<List<Playlist>> = withContext(Dispatchers.IO) {
    try {
      val response = apiService.getPopularPlaylists()
      fetchResponse(response)
    } catch (e: Exception) {
      ApiResult.Error(e)
    }
  }

  suspend fun fetchUpdatedPlaylists(): ApiResult<List<Playlist>> = withContext(Dispatchers.IO) {
    try {
      val response = apiService.getUpdatedPlaylists()
      fetchResponse(response)
    } catch (e: Exception) {
      ApiResult.Error(e)
    }
  }

  suspend fun fetchRecommendedPlaylists(): ApiResult<List<Playlist>> = withContext(Dispatchers.IO) {
    try {
      val response = apiService.getRecommendedPlaylists()
      fetchResponse(response)
    } catch (e: Exception) {
      ApiResult.Error(e)
    }
  }

  private val apiService: UserService by lazy {
    val configuration = Configuration(mContext)
    ApiRetrofitClient.instance(configuration.getHttpBaseUrl()).create(UserService::class.java)
  }

}

