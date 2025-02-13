package fr.bonamy.tidalstreamer.api

import fr.bonamy.tidalstreamer.models.Album
import fr.bonamy.tidalstreamer.models.Queue
import fr.bonamy.tidalstreamer.models.Status
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface StreamingService {

  @GET("/status")
  suspend fun status(): Response<Status>

  @POST("/play")
  suspend fun play(): Response<ApiResponse<String>>

  @POST("/pause")
  suspend fun pause(): Response<ApiResponse<String>>

  @POST("/stop")
  suspend fun stop(): Response<ApiResponse<String>>

  @POST("/next")
  suspend fun next(): Response<ApiResponse<String>>

  @POST("/prev")
  suspend fun previous(): Response<ApiResponse<String>>

  @POST("/timeseek/{position}")
  suspend fun seek(@Path("position") position: Int): Response<ApiResponse<String>>

  @POST("/volume/up")
  suspend fun volumeUp(): Response<ApiResponse<String>>

  @POST("/volume/down")
  suspend fun volumeDown(): Response<ApiResponse<String>>

  @POST("/play/tracks")
  suspend fun playTracks(@Body tracks: Any, @Query("position") position: Int): Response<ApiResponse<Queue>>

  @POST("/enqueue/{position}")
  suspend fun enqueue(@Body tracks: Any, @Path("position") position: String): Response<ApiResponse<String>>

  @GET("/play/album/{id}")
  suspend fun playAlbum(@Path("id") id: String, @Query("position") position: Int): Response<ApiResponse<Album>>

  @GET("/play/mix/{id}")
  suspend fun playMix(@Path("id") id: String, @Query("position") position: Int): Response<ApiResponse<Queue>>

  @GET("/play/playlist/{id}")
  suspend fun playPlaylist(@Path("id") id: String, @Query("position") position: Int): Response<ApiResponse<Queue>>

}