package fr.bonamy.tidalstreamer.models

import java.io.Serializable

data class User(
  var id: Int? = null,
  var login: String? = null,
  var fullName: String? = null,
  var email: String? = null,
  var country: String? = null,
) : Serializable {

  fun displayName(): String {
    return fullName?.takeIf { it.isNotEmpty() } ?: email ?: login ?: "User #$id"
  }

  override fun toString(): String {
    return "User{" +
        "id=" + id +
        ", login='" + login + '\'' +
        ", email='" + email + '\'' +
        ", country='" + country + '\'' +
        '}'
  }

}
