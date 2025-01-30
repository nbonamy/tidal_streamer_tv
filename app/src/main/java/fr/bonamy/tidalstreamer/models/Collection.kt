package fr.bonamy.tidalstreamer.models

import java.io.Serializable

abstract class Collection : Serializable, ImageRepresentation {
  abstract fun title(): String
  abstract fun subtitle(): String
  open fun color(): Int? {
    return null
  }

  var tracks: List<Track>? = null
}
