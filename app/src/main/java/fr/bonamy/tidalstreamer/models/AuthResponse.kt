package fr.bonamy.tidalstreamer.models

import java.io.Serializable

data class AuthResponse(
  var flowId: String? = null,
  var method: String? = null,
  var verificationUri: String? = null,
  var userCode: String? = null,
  var expiresIn: Int? = null,
) : Serializable
