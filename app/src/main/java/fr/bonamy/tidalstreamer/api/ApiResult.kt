package fr.bonamy.tidalstreamer.api

sealed class ApiResult<out T> {
  data class Success<T>(val data: T) : ApiResult<T>()
  data class Error(val exception: Throwable) : ApiResult<Nothing>()
}

