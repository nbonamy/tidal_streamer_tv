package fr.bonamy.tidalstreamer.models

data class Playlist(
  var uuid: String? = null,
  var title: String? = null,
  var image: String? = null,
  var squareImage: String? = null,
  var lastUpdated: String? = null,
  var created: String? = null,
  var numberOfTracks: Int = 0,
) : Collection() {

	override fun title(): String {
		return title ?: ""
	}

	override fun subtitle(): String {
		if (numberOfTracks == 0) {
			return ""
		} else if (numberOfTracks == 1) {
			return "1 track"
		} else {
			return "$numberOfTracks tracks"
		}
	}

	override fun imageUrl(): String {
		return squareImage?.replace("-", "/")?.let { "https://resources.tidal.com/images/$it/640x640.jpg" } ?: ""
	}

	override fun toString(): String {
		return "Album{" +
						"uuid=" + uuid +
						", title='" + title + '\'' +
						", image='" + image + '\'' +
						", numberOfTracks='" + numberOfTracks + '\'' +
						'}'
	}

	companion object {
		internal const val serialVersionUID = 727566175075960653L
	}
}
