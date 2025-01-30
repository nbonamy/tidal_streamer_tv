package fr.bonamy.tidalstreamer.models

interface ImageRepresentation {
  fun imageUrl(): String
  fun largeImageUrl(): String {
    return imageUrl()
  }
}
