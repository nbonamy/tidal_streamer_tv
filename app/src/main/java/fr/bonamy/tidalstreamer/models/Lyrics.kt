package fr.bonamy.tidalstreamer.models

data class Lyrics(
  var trackId: String? = null,
  var lyricsProvider: String? = null,
  var lyrics: String? = null,
  var subtitles: String? = null
)
