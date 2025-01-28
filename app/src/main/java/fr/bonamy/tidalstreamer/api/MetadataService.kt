package fr.bonamy.tidalstreamer.api

import fr.bonamy.tidalstreamer.models.Album
import fr.bonamy.tidalstreamer.models.Artist
import fr.bonamy.tidalstreamer.models.Mix
import fr.bonamy.tidalstreamer.models.Track
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface MetadataService {

	@GET("user/shortcuts")
	suspend fun getShortcuts(): Response<ApiResponse<List<Album>>>

	@GET("user/mixes")
	suspend fun getMixes(): Response<ApiResponse<List<Mix>>>

	@GET("user/artists")
	suspend fun getFavoriteArtists(): Response<ApiResponsePage<ItemArtist>>

	@GET("user/new/albums")
	suspend fun getNewAlbums(): Response<ApiResponse<List<Album>>>

	@GET("user/recent/albums")
	suspend fun getRecentAlbums(): Response<ApiResponse<List<Album>>>

	@GET("user/recommended/albums")
	suspend fun getRecommendedAlbums(): Response<ApiResponse<List<Album>>>

	@GET("info/artist/{id}/toptracks")
	suspend fun getArtistTopTracks(@Path("id") id: String): Response<ApiResponsePage<Track>>

	@GET("info/artist/{id}/albums")
	suspend fun getArtistAlbums(@Path("id") id: String): Response<ApiResponsePage<Album>>

	@GET("info/artist/{id}/singles")
	suspend fun getArtistSingles(@Path("id") id: String): Response<ApiResponsePage<Album>>

	@GET("info/artist/{id}/compilations")
	suspend fun getArtistCompilations(@Path("id") id: String): Response<ApiResponsePage<Album>>

	@GET("info/artist/{id}/similar")
	suspend fun getSimilarArtists(@Path("id") id: String): Response<ApiResponsePage<Artist>>

	@GET("info/album/{id}")
	suspend fun getAlbumTracks(@Path("id") id: String): Response<ApiResponsePage<ItemTrack>>

	@GET("info/mix/{id}/tracks")
	suspend fun getMixTracks(@Path("id") id: String): Response<ApiResponse<List<Track>>>

}
