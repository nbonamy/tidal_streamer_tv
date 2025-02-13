package fr.bonamy.tidalstreamer.models

import java.io.Serializable

const val STATE_STOPPED = "STOPPED"
const val STATE_PLAYING = "PLAYING"
const val STATE_PAUSED = "PAUSED"

data class StatusTrack(
  val type: String = "track",
  val mediaId: Int? = null,
  val item: Track? = null,
) : Serializable

data class Status(
  val state: String? = null,
  val tracks: List<StatusTrack>? = null,
  val position: Int = -1,
  val progress: Int = -1
) : Serializable {
  fun currentTrack(): Track? {
    return tracks?.getOrNull(position)?.item
  }
}
