package fr.bonamy.tidalstreamer.api

import fr.bonamy.tidalstreamer.models.Album
import fr.bonamy.tidalstreamer.models.Artist
import fr.bonamy.tidalstreamer.models.ArtistInfo
import fr.bonamy.tidalstreamer.models.Lyrics
import fr.bonamy.tidalstreamer.models.Track
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface MetadataService {

  @GET("/info/artist/{id}")
  suspend fun getArtistInfo(@Path("id") id: String): Response<ApiResponse<ArtistInfo>>

  @GET("/info/artist/{id}/toptracks")
  suspend fun getArtistTopTracks(@Path("id") id: String): Response<ApiResponsePage<Track>>

  @GET("/info/artist/{id}/albums")
  suspend fun getArtistAlbums(@Path("id") id: String): Response<ApiResponsePage<Album>>

  @GET("/info/artist/{id}/live")
  suspend fun getArtistLiveAlbums(@Path("id") id: String): Response<ApiResponsePage<Album>>

  @GET("/info/artist/{id}/singles")
  suspend fun getArtistSingles(@Path("id") id: String): Response<ApiResponsePage<Album>>

  @GET("/info/artist/{id}/compilations")
  suspend fun getArtistCompilations(@Path("id") id: String): Response<ApiResponsePage<Album>>

  @GET("/info/artist/{id}/radio")
  suspend fun getArtistRadio(@Path("id") id: String): Response<ApiResponsePage<Track>>

  @GET("/info/artist/{id}/similar")
  suspend fun getSimilarArtists(@Path("id") id: String): Response<ApiResponsePage<Artist>>

  @GET("/info/album/{id}")
  suspend fun getAlbumTracks(@Path("id") id: String): Response<ApiResponsePage<ItemTrack>>

  @GET("/info/mix/{id}/tracks")
  suspend fun getMixTracks(@Path("id") id: String): Response<ApiResponse<List<Track>>>

  @GET("/info/playlist/{id}")
  suspend fun getPlaylistTracks(@Path("id") id: String): Response<ApiResponsePage<ItemTrack>>

  @GET("/info/track/{id}/lyrics")
  suspend fun getTrackLyrics(@Path("id") id: String): Response<ApiResponse<Lyrics>>

  @GET("/info/track/{id}/radio")
  suspend fun getTrackRadio(@Path("id") id: String): Response<ApiResponsePage<Track>>

}
