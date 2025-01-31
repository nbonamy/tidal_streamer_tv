package fr.bonamy.tidalstreamer.models

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
