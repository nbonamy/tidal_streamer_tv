package fr.bonamy.tidalstreamer.api

import fr.bonamy.tidalstreamer.models.Album
import fr.bonamy.tidalstreamer.models.Artist
import fr.bonamy.tidalstreamer.models.Mix
import fr.bonamy.tidalstreamer.models.Playlist
import fr.bonamy.tidalstreamer.models.Shortcut
import fr.bonamy.tidalstreamer.models.Track
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface UserService {

  @GET("/user/shortcuts")
  suspend fun getShortcuts(): Response<ApiResponse<List<Shortcut>>>

  @GET("/user/mixes/daily")
  suspend fun getDailyMixes(): Response<ApiResponse<List<Mix>>>

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

//  @GET("/user/recent/albums")
//  suspend fun getRecentAlbums(): Response<ApiResponse<List<Album>>>

  @GET("/user/recent/artists")
  suspend fun getRecentArtists(): Response<ApiResponse<List<Artist>>>

  @GET("/user/recommended/albums")
  suspend fun getRecommendedAlbums(): Response<ApiResponse<List<Album>>>

  @GET("/user/forgotten/albums")
  suspend fun getForgottenAlbums(): Response<ApiResponse<List<Album>>>

  @GET("/user/mixes/history")
  suspend fun getHistoryMixes(): Response<ApiResponse<List<Mix>>>

  @GET("/user/playlists/essential")
  suspend fun getEssentialPlaylists(): Response<ApiResponse<List<Playlist>>>

  @GET("/user/mixes/radio")
  suspend fun getRadioMixes(): Response<ApiResponse<List<Mix>>>

  @GET("/user/tracks/spotlighted")
  suspend fun getSpotlightedTracks(): Response<ApiResponse<List<Track>>>

  @GET("/user/tracks/uploads")
  suspend fun getUploadsTracks(): Response<ApiResponse<List<Track>>>

  @GET("/user/new/tracks")
  suspend fun getNewTracks(): Response<ApiResponse<List<Track>>>

  @GET("/user/playlists/popular")
  suspend fun getPopularPlaylists(): Response<ApiResponse<List<Playlist>>>

  @GET("/user/playlists/updated")
  suspend fun getUpdatedPlaylists(): Response<ApiResponse<List<Playlist>>>

  @GET("/user/playlists/recommended")
  suspend fun getRecommendedPlaylists(): Response<ApiResponse<List<Playlist>>>

  @GET("/user/tracks/{trackId}/favorite")
  suspend fun isTrackFavorite(@Path("trackId") trackId: String): Response<ApiResponse<FavoriteResponse>>

  @POST("/user/tracks/{trackId}/favorite/toggle")
  suspend fun toggleTrackFavorite(@Path("trackId") trackId: String): Response<ApiResponse<FavoriteResponse>>

}

data class FavoriteResponse(
  val favorite: Boolean
)
