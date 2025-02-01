package fr.bonamy.tidalstreamer.models

interface PlayedBy {

  var artist: Artist?
  var artists: List<Artist>?

  fun mainArtist(): Artist? {
    if (artist != null) return artist
    val mainArtist = artists?.find { it.main == true }
    return mainArtist ?: artists?.firstOrNull()
  }

  fun allArtists(): List<Artist> {
    val allArtists = artists?.toMutableList() ?: mutableListOf()
    val mainArtistInArtists = allArtists.find { it.id == artist?.id }
    if (mainArtistInArtists == null) {
      artist?.let { allArtists.add(0, it) }
    }
    return allArtists
  }

}
