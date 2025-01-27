package fr.bonamy.tidalstreamer.models

import java.io.Serializable

abstract class Collection: Serializable {
	abstract fun title(): String
	abstract fun subtitle(): String
	abstract fun imageUrl(): String
	open fun tracks(): List<Track>? = null
}
