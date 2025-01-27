package fr.bonamy.tidalstreamer.api

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Response

open class ApiClient {

	protected suspend fun <T> fetchResponse(response: Response<ApiResponse<T>>): ApiResult<T> = withContext(
		Dispatchers.IO) {
		if (response.isSuccessful && response.body()!!.status == "ok") {
			ApiResult.Success(response.body()!!.result!!)
		} else {
			ApiResult.Error(Throwable("Error: ${response.code()}"))
		}
	}

}