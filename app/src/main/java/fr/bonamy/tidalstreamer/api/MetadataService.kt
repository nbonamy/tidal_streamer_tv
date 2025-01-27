package fr.bonamy.tidalstreamer.api

import fr.bonamy.tidalstreamer.models.Album
import fr.bonamy.tidalstreamer.models.Mix
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface MetadataService {

	@GET("user/shortcuts")
	suspend fun getShortcuts(): Response<ApiResponse<List<Album>>>

	@GET("user/mixes")
	suspend fun getMixes(): Response<ApiResponse<List<Mix>>>

	@GET("user/new/albums")
	suspend fun getNewAlbums(): Response<ApiResponse<List<Album>>>

	@GET("user/recent/albums")
	suspend fun getRecentAlbums(): Response<ApiResponse<List<Album>>>

	@GET("user/recommended/albums")
	suspend fun getRecommendedAlbums(): Response<ApiResponse<List<Album>>>

	@GET("info/album/{id}")
	suspend fun getAlbumTracks(@Path("id") id: String): Response<ApiResponsePage<ItemTrack>>

}
