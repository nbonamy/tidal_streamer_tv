package fr.bonamy.tidalstreamer.models

import android.graphics.Color

data class Mix(
  var id: String? = null,
  var type: String? = null,
  var title: String? = null,
  var subTitle: String? = null,
  var thumbnail: String? = null,
  var titleColor: String? = null,
) : Collection() {

  override fun title(): String {
    return title ?: ""
  }

  override fun subtitle(): String {
    return subTitle ?: ""
  }

  override fun imageUrl(): String {
    return thumbnail ?: ""
  }

  override fun largeImageUrl(): String {
    return imageUrl().replace("640x640", "1280x1280")
  }

  override fun color(): Int? {
    return titleColor?.let { Color.parseColor(it) }
  }

  override fun toString(): String {
    return "Album{" +
        "id=" + id +
        ", type='" + type + '\'' +
        ", title='" + title + '\'' +
        ", subTitle='" + subTitle + '\'' +
        ", thumbnail='" + thumbnail + '\'' +
        '}'
  }

}
