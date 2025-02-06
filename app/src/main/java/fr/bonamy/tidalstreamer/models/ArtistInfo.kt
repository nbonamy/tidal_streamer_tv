package fr.bonamy.tidalstreamer.models

data class ArtistInfo(
  var source: String? = null,
  var lastUpdated: String? = null,
  var text: String? = null,
  var summary: String? = null,
) {

  fun getPlainText(): String {
    // remove [wimpLink artistId=\"25731461\"]boygenius[/wimpLink]
    // and [wimpLink albumId=\"320972781\"]the rest[/wimpLink]
    // with the inner text
    return text?.replace(Regex("\\[wimpLink [^\\]]+](.*?)\\[/wimpLink]")) {
      it.groupValues[1]
    }?.replace("<br/>", "\n\n") ?: ""
  }

}
