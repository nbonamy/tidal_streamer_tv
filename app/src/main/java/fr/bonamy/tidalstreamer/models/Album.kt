package fr.bonamy.tidalstreamer.models

import android.graphics.Color

data class Album(
  var id: String? = null,
  var title: String? = null,
  var cover: String? = null,
  override var artist: Artist? = null,
  override var artists: List<Artist>? = null,
  var vibrantColor: String? = null,
  var releaseDate: String? = null,
  var numberOfVolumes: Int = 0,
  var numberOfTracks: Int = 0,
  var popularity: Int? = null,
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

  override fun toString(): String {
    return "Album{" +
        "id=" + id +
        ", title='" + title + '\'' +
        ", cover='" + cover + '\'' +
        ", releaseDate='" + releaseDate + '\'' +
        '}'
  }

}
