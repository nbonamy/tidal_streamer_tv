package fr.bonamy.tidalstreamer.models

import java.io.Serializable

abstract class Collection: Serializable {
	abstract fun title(): String
	abstract fun subtitle(): String
	abstract fun imageUrl(): String
	var tracks : List<Track>? = null
}
