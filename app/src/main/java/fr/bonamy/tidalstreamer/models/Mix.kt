package fr.bonamy.tidalstreamer.models

import android.graphics.Color
import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class TextInfo(
  var text: String? = null,
  var color: String? = null,
) : Serializable

data class MixImage(
  var size: String? = null,
  var url: String? = null,
  var width: Int? = null,
  var height: Int? = null,
) : Serializable

data class Mix(
  var id: String? = null,
  var type: String? = null,

  // Legacy fields for backwards compatibility
  var title: String? = null,
  var subTitle: String? = null,
  var thumbnail: String? = null,
  var titleColor: String? = null,

  // New TIDAL API fields
  @SerializedName("titleTextInfo")
  var titleTextInfo: TextInfo? = null,
  @SerializedName("subtitleTextInfo")
  var subtitleTextInfo: TextInfo? = null,
  @SerializedName("mixImages")
  var mixImages: List<MixImage>? = null,
) : Collection() {

  override fun title(): String {
    return titleTextInfo?.text ?: title ?: ""
  }

  override fun subtitle(): String {
    return subtitleTextInfo?.text ?: subTitle ?: ""
  }

  override fun imageUrl(): String {
    // Get MEDIUM size image (640x640) from mixImages, fallback to thumbnail
    val mediumImage = mixImages?.find { it.size == "MEDIUM" }?.url
    return mediumImage ?: thumbnail ?: ""
  }

  override fun largeImageUrl(): String {
    // Get LARGE size image (1280x1280) from mixImages, fallback to scaled image
    val largeImage = mixImages?.find { it.size == "LARGE" }?.url
    return largeImage ?: imageUrl().replace("640x640", "1280x1280")
  }

  override fun color(): Int? {
    val colorString = titleTextInfo?.color ?: titleColor
    return colorString?.let { Color.parseColor(it) }
  }

  override fun toString(): String {
    return "Mix{" +
        "id=" + id +
        ", type='" + type + '\'' +
        ", title='" + title() + '\'' +
        ", subtitle='" + subtitle() + '\'' +
        ", imageUrl='" + imageUrl() + '\'' +
        '}'
  }

}
