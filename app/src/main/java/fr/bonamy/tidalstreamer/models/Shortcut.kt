package fr.bonamy.tidalstreamer.models

data class Shortcut(
  var type: String? = null,
  var id: String? = null,
  var uuid: String? = null,
  var title: String? = null,
  var description: String? = null,
  var artists: List<Artist>? = null,
  var cover: String? = null,
  var image: String? = null,
  var squareImage: String? = null,
) {

  fun toCollection(): Collection? {

    if (type == "ALBUM") {
      return Album(
        id = id,
        title = title,
        artists = artists,
        cover = cover,
      )
    }

    if (type == "PLAYLIST" || type == "EDITORIAL") {
      return Playlist(
        uuid = uuid,
        title = title,
        image = image,
        squareImage = squareImage,
      )
    }

    return null

  }

}
