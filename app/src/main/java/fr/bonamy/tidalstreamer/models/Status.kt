package fr.bonamy.tidalstreamer.models

const val STATE_STOPPED = "STOPPED"
const val STATE_PLAYING = "PLAYING"
const val STATE_PAUSED = "PAUSED"

data class StatusTrack(
  val type: String = "track",
  val mediaId: Int? = null,
  val item: Track? = null,
)

data class Status(
  val state: String? = null,
  val tracks: List<StatusTrack>? = null,
  val position: Int = -1,
  val progress: Int = -1
)
