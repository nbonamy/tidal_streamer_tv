package fr.bonamy.tidalstreamer.models

import java.io.Serializable

data class Artist(
	var id: String? = null,
	var name: String? = null,
	var picture: String? = null,
	var main: Boolean? = null,
) : Serializable {

	fun imageUrl(): String {
		return picture?.replace("-", "/")?.let { "https://resources.tidal.com/images/$it/750x750.jpg" } ?: ""
	}

	override fun toString(): String {
		return "Artist{" +
						"id=" + id +
						", name='" + name + '\'' +
						", picture='" + picture + '\'' +
						", main='" + main + '\'' +
						'}'
	}

	companion object {
		internal const val serialVersionUID = 1234567890123456789L
	}
}
