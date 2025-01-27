package fr.bonamy.tidalstreamer.models

import java.io.Serializable

data class Track(
	var id: String? = null,
	var title: String? = null,
	var volumeNumber: Int = 0,
	var trackNumber: Int = 0,
	var duration: Int = 0,
	var copyright: String? = null,
	var audioQuality: String? = null,
	var audioModes: List<String>? = null,
	var artist: Artist? = null,
	var artists: List<Artist>? = null,
	var album: Album? = null,
): Serializable, ImageRepresentation {

	override fun imageUrl(): String {
		return album?.cover?.replace("-", "/")?.let { "https://resources.tidal.com/images/$it/640x640.jpg" } ?: ""
	}

	fun durationString(): String {
		val minutes = duration / 60
		val seconds = duration % 60
		return String.format("%d:%02d", minutes, seconds)
	}

	override fun toString(): String {
		return "Track{" +
						"id=" + id +
						", title='" + title + '\'' +
						", volumeNumber=" + volumeNumber +
						", trackNumber=" + trackNumber +
						'}'
	}

	companion object {
		internal const val serialVersionUID = 7275654484894960653L
	}
}
