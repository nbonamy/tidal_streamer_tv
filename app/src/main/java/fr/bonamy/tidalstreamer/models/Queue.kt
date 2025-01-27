package fr.bonamy.tidalstreamer.models

import java.io.Serializable

data class Device(
	var uuid: String? = null,
	var name: String? = null,
) : Serializable

data class Queue(
	var id: String? = null,
	var device: Device? = null,
) : Serializable
