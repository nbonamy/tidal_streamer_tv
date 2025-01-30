package fr.bonamy.tidalstreamer.models

interface ImageRepresentation {
  fun imageUrl(): String
  fun largeImageUrl(): String {
    return imageUrl().replace("640x640", "1280x1280")
  }
}
