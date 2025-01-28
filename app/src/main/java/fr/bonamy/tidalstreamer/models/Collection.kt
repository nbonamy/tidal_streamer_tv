package fr.bonamy.tidalstreamer.models

import java.io.Serializable

abstract class Collection: Serializable, ImageRepresentation {
	abstract fun title(): String
	abstract fun subtitle(): String
	var tracks : List<Track>? = null
}
