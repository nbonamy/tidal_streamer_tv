package fr.bonamy.tidalstreamer.api

import fr.bonamy.tidalstreamer.utils.Configuration
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiRetrofitClient {

  val instance: Retrofit by lazy {
    val configuration = Configuration()
    Retrofit.Builder()
      .baseUrl(configuration.getHttpBaseUrl())
      .addConverterFactory(GsonConverterFactory.create())
      .build()
  }

}
