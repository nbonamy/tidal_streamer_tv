package fr.bonamy.tidalstreamer.api

import fr.bonamy.tidalstreamer.models.Album
import fr.bonamy.tidalstreamer.models.Queue
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface StreamingService {

	@POST("/stop")
	suspend fun stop(): Response<ApiResponse<String>>

	@GET("/play/album/{id}")
	suspend fun playAlbum(@Path("id") id: String, @Query("position") position: Int): Response<ApiResponse<Album>>

	@GET("/play/mix/{id}")
	suspend fun playMix(@Path("id") id: String, @Query("position") position: Int): Response<ApiResponse<Queue>>

}