package fr.bonamy.tidalstreamer.models

import java.io.Serializable

data class Artist(
	var id: Long = 0,
	var name: String? = null,
	var picture: String? = null,
	var main: Boolean? = null,
) : Serializable {

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
