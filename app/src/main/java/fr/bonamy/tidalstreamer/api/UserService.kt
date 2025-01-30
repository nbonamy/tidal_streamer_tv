package fr.bonamy.tidalstreamer.api

import fr.bonamy.tidalstreamer.models.Album
import fr.bonamy.tidalstreamer.models.Artist
import fr.bonamy.tidalstreamer.models.Mix
import fr.bonamy.tidalstreamer.models.Playlist
import retrofit2.Response
import retrofit2.http.GET

interface UserService {

  @GET("/user/shortcuts")
  suspend fun getShortcuts(): Response<ApiResponse<List<Album>>>

  @GET("/user/mixes")
  suspend fun getMixes(): Response<ApiResponse<List<Mix>>>

  @GET("/user/albums")
  suspend fun getFavoriteAlbums(): Response<ApiResponsePage<ItemAlbum>>

  @GET("/user/artists")
  suspend fun getFavoriteArtists(): Response<ApiResponsePage<ItemArtist>>

  @GET("/user/tracks")
  suspend fun getFavoriteTracks(): Response<ApiResponsePage<ItemTrack>>

  @GET("/user/playlists")
  suspend fun getPlaylists(): Response<ApiResponsePage<Playlist>>

  @GET("/user/new/albums")
  suspend fun getNewAlbums(): Response<ApiResponse<List<Album>>>

  @GET("/user/recent/albums")
  suspend fun getRecentAlbums(): Response<ApiResponse<List<Album>>>

  @GET("/user/recent/artists")
  suspend fun getRecentArtists(): Response<ApiResponse<List<Artist>>>

  @GET("/user/recommended/albums")
  suspend fun getRecommendedAlbums(): Response<ApiResponse<List<Album>>>

}
