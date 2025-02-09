package fr.bonamy.tidalstreamer.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiRetrofitClient {

  fun instance(baseUrl: String): Retrofit {
    return Retrofit.Builder()
      .baseUrl(baseUrl)
      .addConverterFactory(GsonConverterFactory.create())
      .build()
  }

}
