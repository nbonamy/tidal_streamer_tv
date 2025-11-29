package fr.bonamy.tidalstreamer.api

import fr.bonamy.tidalstreamer.models.AuthResponse
import fr.bonamy.tidalstreamer.models.AuthStatus
import fr.bonamy.tidalstreamer.models.User
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface AuthService {

  @GET("/auth/users")
  suspend fun getUsers(): Response<ApiResponse<List<User>>>

  @POST("/auth/user")
  suspend fun createUser(): Response<ApiResponse<AuthResponse>>

  @GET("/auth/status/{flowId}")
  suspend fun getAuthStatus(@Path("flowId") flowId: String): Response<ApiResponse<AuthStatus>>

}
