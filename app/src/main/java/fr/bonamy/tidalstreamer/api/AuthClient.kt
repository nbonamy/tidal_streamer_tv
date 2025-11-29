package fr.bonamy.tidalstreamer.api

import android.content.Context
import fr.bonamy.tidalstreamer.models.AuthResponse
import fr.bonamy.tidalstreamer.models.AuthStatus
import fr.bonamy.tidalstreamer.models.User
import fr.bonamy.tidalstreamer.utils.Configuration
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AuthClient(mContext: Context) : ApiClient() {

  suspend fun fetchUsers(): ApiResult<List<User>> = withContext(Dispatchers.IO) {
    try {
      val response = apiService.getUsers()
      fetchResponse(response)
    } catch (e: Exception) {
      ApiResult.Error(e)
    }
  }

  suspend fun createUser(): ApiResult<AuthResponse> = withContext(Dispatchers.IO) {
    try {
      val response = apiService.createUser()
      fetchResponse(response)
    } catch (e: Exception) {
      ApiResult.Error(e)
    }
  }

  suspend fun checkAuthStatus(flowId: String): ApiResult<AuthStatus> = withContext(Dispatchers.IO) {
    try {
      val response = apiService.getAuthStatus(flowId)
      fetchResponse(response)
    } catch (e: Exception) {
      ApiResult.Error(e)
    }
  }

  private val apiService: AuthService by lazy {
    val configuration = Configuration(mContext)
    ApiRetrofitClient.instance(configuration.getHttpBaseUrl()).create(AuthService::class.java)
  }

}
