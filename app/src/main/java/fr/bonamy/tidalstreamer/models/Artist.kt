package fr.bonamy.tidalstreamer.models

import java.io.Serializable

data class ArtistRole(
  var category: String? = null,
) : Serializable

data class Artist(
  var id: String? = null,
  var name: String? = null,
  var picture: String? = null,
  var artistTypes: List<String>? = null,
  var artistRoles: List<ArtistRole>? = null,
  var popularity: Int? = null,
  var main: Boolean? = null,
) : Serializable, ImageRepresentation {

  override fun imageUrl(): String {
    return picture?.replace("-", "/")?.let { "https://resources.tidal.com/images/$it/750x750.jpg" }
      ?: ""
  }

  override fun toString(): String {
    return "Artist{" +
        "id=" + id +
        ", name='" + name + '\'' +
        ", picture='" + picture + '\'' +
        ", main='" + main + '\'' +
        '}'
  }

}
