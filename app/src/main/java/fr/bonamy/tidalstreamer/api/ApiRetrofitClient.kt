package fr.bonamy.tidalstreamer.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiRetrofitClient {

	private const val BASE_URL = "http://192.168.1.2:5002/"

	val instance: Retrofit by lazy {
		Retrofit.Builder()
			.baseUrl(BASE_URL)
			.addConverterFactory(GsonConverterFactory.create())
			.build()
	}

}
