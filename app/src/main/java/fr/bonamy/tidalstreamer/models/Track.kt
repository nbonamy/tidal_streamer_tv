package fr.bonamy.tidalstreamer.models

import java.io.Serializable
import java.util.Locale

data class Track(
  var id: String? = null,
  var title: String? = null,
  var volumeNumber: Int = 0,
  var trackNumber: Int = 0,
  var index: Int? = null,
  var duration: Int = 0,
  var copyright: String? = null,
  var audioQuality: String? = null,
  var audioModes: List<String>? = null,
  override var artist: Artist? = null,
  override var artists: List<Artist>? = null,
  var album: Album? = null,
) : Serializable, ImageRepresentation, PlayedBy {

  override fun imageUrl(): String {
    return album?.cover?.replace("-", "/")?.let { "https://resources.tidal.com/images/$it/640x640.jpg" } ?: ""
  }

  fun durationString(): String {
    val minutes = duration / 60
    val seconds = duration % 60
    return String.format(Locale.getDefault(), "%d:%02d", minutes, seconds)
  }

  override fun toString(): String {
    return "Track{" +
        "id=" + id +
        ", title='" + title + '\'' +
        ", volumeNumber=" + volumeNumber +
        ", trackNumber=" + trackNumber +
        '}'
  }

}
