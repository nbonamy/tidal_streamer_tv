package fr.bonamy.tidalstreamer.models

enum class RadioType {
  ARTIST,
  TRACK
}

data class Radio(
  var type: RadioType,
  var id: String? = null,
  var title: String? = null,
  var image: String? = null,
  var numberOfTracks: Int = 0,
) : Collection() {

  override fun title(): String {
    return title ?: ""
  }

  override fun subtitle(): String {
    return ""
  }

  override fun imageUrl(): String {
    val dimensions = when (type) {
      RadioType.ARTIST -> "750x750"
      RadioType.TRACK -> "640x640"
    }
    return image?.replace("-", "/")
      ?.let { "https://resources.tidal.com/images/$it/$dimensions.jpg" } ?: ""
  }

  override fun toString(): String {
    return "Radio{" +
        "type=" + type +
        ", id=" + id +
        ", title='" + title + '\'' +
        ", image='" + image + '\'' +
        ", numberOfTracks='" + numberOfTracks + '\'' +
        '}'
  }

}
