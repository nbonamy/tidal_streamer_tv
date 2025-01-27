package fr.bonamy.tidalstreamer.api

import fr.bonamy.tidalstreamer.models.Album
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

data class ApiResponse<out T>(
	val status: String? = null,
	val result: T? = null,
)

interface ApiService {

	@GET("user/new/albums")
	suspend fun getNewAlbums(): Response<ApiResponse<List<Album>>>

	@GET("user/recent/albums")
	suspend fun getRecentAlbums(): Response<ApiResponse<List<Album>>>

	@GET("user/recommended/albums")
	suspend fun getRecommendedAlbums(): Response<ApiResponse<List<Album>>>

}