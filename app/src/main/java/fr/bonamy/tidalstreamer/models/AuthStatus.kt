package fr.bonamy.tidalstreamer.models

import java.io.Serializable

data class AuthStatus(
  var status: String? = null,  // "pending", "completed", "failed"
  var user: User? = null,
  var error: String? = null,
) : Serializable
