package fr.bonamy.tidalstreamer.models

import java.io.Serializable

data class Album(
	var id: Long = 0,
	var title: String? = null,
	var artists: List<Artist>? = null,
	var cover: String? = null,
	var releaseDate: String? = null,
	var numberOfVolumes: Int = 0,
	var numberOfTracks: Int = 0,
) : Serializable {

	override fun toString(): String {
		return "Album{" +
						"id=" + id +
						", title='" + title + '\'' +
						", cover='" + cover + '\'' +
						", releaseDate='" + releaseDate + '\'' +
						'}'
	}

	fun mainArtist(): Artist? {
		val mainArtist = artists?.find { it.main == true }
		return mainArtist ?: artists?.firstOrNull()
	}

	fun coverUrl(): String? {
		return cover?.replace("-", "/")?.let { "https://resources.tidal.com/images/$it/640x640.jpg" }
	}

	companion object {
		internal const val serialVersionUID = 727566175075960653L
	}
}
