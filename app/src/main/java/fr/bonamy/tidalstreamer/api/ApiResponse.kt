package fr.bonamy.tidalstreamer.api

import fr.bonamy.tidalstreamer.models.Album
import fr.bonamy.tidalstreamer.models.Artist
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

data class ItemAlbum(
	var created: String? = null,
	var item: Album? = null,
)

data class ItemArtist(
	var created: String? = null,
	var item: Artist? = null,
)

data class ItemTrack(
	var type: String = "track",
	var created: String? = null,
	var item: Track? = null,
)

