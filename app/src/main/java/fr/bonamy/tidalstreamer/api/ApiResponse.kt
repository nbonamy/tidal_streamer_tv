package fr.bonamy.tidalstreamer.api

data class ApiResponse<out T>(
	val status: String? = null,
	val result: T? = null,
)

data class Page<out T>(
	val limit: Int? = null,
	val offset: Int? = null,
	val totalNumberOfItems: Int? = null,
	val items: List<T>? = null,
)

data class ApiResponsePage<out T>(
	val status: String? = null,
	val result: Page<T>? = null,
)
