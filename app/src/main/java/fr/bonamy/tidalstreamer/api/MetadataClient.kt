package fr.bonamy.tidalstreamer.api

import android.util.Log
import fr.bonamy.tidalstreamer.models.Album
import fr.bonamy.tidalstreamer.models.Artist
import fr.bonamy.tidalstreamer.models.Collection
import fr.bonamy.tidalstreamer.models.Lyrics
import fr.bonamy.tidalstreamer.models.Mix
import fr.bonamy.tidalstreamer.models.Playlist
import fr.bonamy.tidalstreamer.models.Track
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MetadataClient : ApiClient() {

  suspend fun fetchArtistTopTracks(artistId: String): ApiResult<List<Track>> =
    withContext(Dispatchers.IO) {
      try {
        val response = apiService.getArtistTopTracks(artistId)
        fetchPagedResponse(response)
      } catch (e: Exception) {
        ApiResult.Error(e)
      }
    }

  suspend fun fetchArtistAlbums(artistId: String): ApiResult<List<Album>> = withContext(Dispatchers.IO) {
    try {
      val response = apiService.getArtistAlbums(artistId)
      fetchPagedResponse(response)
    } catch (e: Exception) {
      ApiResult.Error(e)
    }
  }

  suspend fun fetchArtistSingles(artistId: String): ApiResult<List<Album>> = withContext(Dispatchers.IO) {
    try {
      val response = apiService.getArtistSingles(artistId)
      fetchPagedResponse(response)
    } catch (e: Exception) {
      ApiResult.Error(e)
    }
  }

  suspend fun fetchArtistCompilations(artistId: String): ApiResult<List<Album>> = withContext(Dispatchers.IO) {
    try {
      val response = apiService.getArtistCompilations(artistId)
      fetchPagedResponse(response)
    } catch (e: Exception) {
      ApiResult.Error(e)
    }
  }

  suspend fun fetchSimilarArtists(artistId: String): ApiResult<List<Artist>> = withContext(Dispatchers.IO) {
    try {
      val response = apiService.getSimilarArtists(artistId)
      fetchPagedResponse(response)
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

  suspend fun fetchPlaylistTracks(playlistId: String): ApiResult<List<Track>> = withContext(Dispatchers.IO) {
    try {
      val response = apiService.getPlaylistTracks(playlistId)
      if (response.isSuccessful && response.body()!!.status == "ok") {
        ApiResult.Success(response.body()!!.result!!.items!!.map { it.item!! })
      } else {
        ApiResult.Error(Throwable("Error: ${response.code()}"))
      }
    } catch (e: Exception) {
      ApiResult.Error(e)
    }
  }

  suspend fun fetchTrackLyrics(trackId: String): ApiResult<Lyrics> = withContext(Dispatchers.IO) {
    try {
      val response = apiService.getTrackLyrics(trackId)
      fetchResponse(response)
    } catch (e: Exception) {
      ApiResult.Error(e)
    }
  }

  suspend fun fetchTracks(collection: Collection): Boolean {

    when (collection) {
      is Album -> {
        when (val result = fetchAlbumTracks(collection.id!!)) {
          is ApiResult.Success -> {
            collection.tracks = result.data
            return true
          }

          is ApiResult.Error -> {
            // Handle the error here
            Log.e(TAG, "Error fetching album tracks: ${result.exception}")
            return false
          }
        }
      }

      is Mix -> {
        when (val result = fetchMixTracks(collection.id!!)) {
          is ApiResult.Success -> {
            collection.tracks = result.data
            return true
          }

          is ApiResult.Error -> {
            // Handle the error here
            Log.e(TAG, "Error fetching mix tracks: ${result.exception}")
            return false
          }
        }
      }

      is Playlist -> {
        when (val result = fetchPlaylistTracks(collection.uuid!!)) {
          is ApiResult.Success -> {
            collection.tracks = result.data
            return true
          }

          is ApiResult.Error -> {
            // Handle the error here
            Log.e(TAG, "Error fetching playlist tracks: ${result.exception}")
            return false
          }
        }
      }

      else -> {
        return false
      }
    }

  }

  private val apiService: MetadataService by lazy {
    ApiRetrofitClient.instance.create(MetadataService::class.java)
  }

  companion object {
    private const val TAG = "MetadataClient"
  }

}

