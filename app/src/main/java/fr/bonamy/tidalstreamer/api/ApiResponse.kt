package fr.bonamy.tidalstreamer.api

import fr.bonamy.tidalstreamer.models.Track

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

data class ItemTrack(
	var type: String = "track",
	var item: Track? = null,
)
