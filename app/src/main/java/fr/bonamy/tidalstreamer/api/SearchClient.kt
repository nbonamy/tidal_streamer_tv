package fr.bonamy.tidalstreamer.api

import android.content.Context
import fr.bonamy.tidalstreamer.models.Album
import fr.bonamy.tidalstreamer.models.Artist
import fr.bonamy.tidalstreamer.models.Track
import fr.bonamy.tidalstreamer.utils.Configuration
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SearchClient(mContext: Context) : ApiClient() {

  suspend fun searchAlbums(query: String): ApiResult<List<Album>?> = withContext(Dispatchers.IO) {
    try {
      val response = apiService.searchAlbums(query)
      if (response.isSuccessful && response.body()!!.status == "ok") {
        ApiResult.Success(response.body()!!.result!!.items)
      } else {
        ApiResult.Error(Throwable("Error: ${response.code()}"))
      }
    } catch (e: Exception) {
      ApiResult.Error(e)
    }
  }

  suspend fun searchTracks(query: String): ApiResult<List<Track>?> = withContext(Dispatchers.IO) {
    try {
      val response = apiService.searchTracks(query)
      if (response.isSuccessful && response.body()!!.status == "ok") {
        ApiResult.Success(response.body()!!.result!!.items)
      } else {
        ApiResult.Error(Throwable("Error: ${response.code()}"))
      }
    } catch (e: Exception) {
      ApiResult.Error(e)
    }
  }

  suspend fun searchArtists(query: String): ApiResult<List<Artist>?> = withContext(Dispatchers.IO) {
    try {
      val response = apiService.searchArtists(query)
      if (response.isSuccessful && response.body()!!.status == "ok") {
        ApiResult.Success(response.body()!!.result!!.items)
      } else {
        ApiResult.Error(Throwable("Error: ${response.code()}"))
      }
    } catch (e: Exception) {
      ApiResult.Error(e)
    }
  }

  private val apiService: SearchService by lazy {
    val configuration = Configuration(mContext)
    ApiRetrofitClient.instance(configuration.getHttpBaseUrl()).create(SearchService::class.java)
  }

}

