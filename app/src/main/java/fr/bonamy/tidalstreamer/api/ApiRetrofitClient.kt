package fr.bonamy.tidalstreamer.api

import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiRetrofitClient {

  private var userId: Int? = null

  fun setUserId(id: Int?) {
    userId = id
  }

  fun instance(baseUrl: String): Retrofit {
    val client = OkHttpClient.Builder()
      .addInterceptor(UserIdInterceptor())
      .build()

    return Retrofit.Builder()
      .baseUrl(baseUrl)
      .client(client)
      .addConverterFactory(GsonConverterFactory.create())
      .build()
  }

  private class UserIdInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): okhttp3.Response {
      val request = chain.request()
      val newRequest = if (userId != null) {
        request.newBuilder()
          .addHeader("x-user-id", userId.toString())
          .build()
      } else {
        request
      }
      return chain.proceed(newRequest)
    }
  }

}
