package fr.bonamy.tidalstreamer.models

import android.graphics.Color

data class Album(
  var id: String? = null,
  var title: String? = null,
  var version: String? = null,
  var cover: String? = null,
  override var artist: Artist? = null,
  override var artists: List<Artist>? = null,
  var duration: Int? = null,
  var vibrantColor: String? = null,
  var releaseDate: String? = null,
  var numberOfVolumes: Int = 0,
  var numberOfTracks: Int = 0,
  var popularity: Int? = null,
  var audioQuality: String? = null,
) : Collection(), PlayedBy {

  override fun title(): String {
    return title ?: ""
  }

  override fun subtitle(): String {
    return mainArtist()?.name ?: ""
  }

  override fun imageUrl(): String {
    return cover?.replace("-", "/")?.let { "https://resources.tidal.com/images/$it/640x640.jpg" }
      ?: ""
  }

  override fun largeImageUrl(): String {
    return imageUrl().replace("640x640", "1280x1280")
  }

  override fun color(): Int? {
    return vibrantColor?.let { Color.parseColor(it) }
  }

  fun releaseYear(): String {
    return releaseDate?.split("-")?.get(0) ?: ""
  }

//  val quality: Int
//    get() {
//      return when (audioQuality) {
//        "LOW" -> 1
//        "HIGH" -> 2
//        "LOSSLESS" -> 3
//        "HI_RES" -> 4
//        "HIRES_LOSSLESS" -> 5
//        else -> 0
//      }
//    }

  override fun toString(): String {
    return "Album{" +
      "id=" + id +
      ", title='" + title + '\'' +
      ", cover='" + cover + '\'' +
      ", releaseDate='" + releaseDate + '\'' +
      '}'
  }

}
