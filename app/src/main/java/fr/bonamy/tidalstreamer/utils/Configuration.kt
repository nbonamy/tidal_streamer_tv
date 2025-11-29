package fr.bonamy.tidalstreamer.utils

import android.content.Context
import android.content.SharedPreferences

class Configuration(mContext: Context) {

  private val sharedPreferences: SharedPreferences =
    mContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)


  fun getHttpBaseUrl(): String {
    return "${getHttpProtocol()}${getServerHostname()}:${getHttpPort()}"
  }

  fun getWsBaseUrl(): String {
    return "${getWsProtocol()}${getServerHostname()}:${getWsPort()}"
  }

  private fun getServerHostname(): String {
    return "192.168.1.2"
  }

  private fun getHttpProtocol(): String {
    return "http://"
  }

  private fun getWsProtocol(): String {
    return "ws://"
  }

  private fun getHttpPort(): Int {
    return 5002
  }

  private fun getWsPort(): Int {
    return 5003
  }

  fun loadRecentSearches(): List<String> {
//    return listOf(
//      "bohemian rhapsody", "stairway to heaven", "hotel california", "imagine", "hey jude",
//      "smells like teen spirit", "sweet child o' mine", "billie jean", "like a rolling stone", "purple haze",
//      "yesterday", "comfortably numb", "let it be", "wish you were here", "born to run",
//      "what's going on", "good vibrations", "johnny b. goode", "no woman, no cry", "superstition",
//      "i want to hold your hand", "blowin' in the wind", "london calling", "layla", "a day in the life",
//      "heroes", "hallelujah", "all along the watchtower", "hotel california", "light my fire",
//      "gimme shelter", "one", "bohemian rhapsody", "hey jude", "imagine", "like a rolling stone",
//      "purple rain", "whole lotta love", "born to be wild", "free bird", "american pie",
//      "roxanne", "under pressure", "sweet home alabama", "paint it black", "sultans of swing",
//      "another brick in the wall", "back in black", "dream on", "knockin' on heaven's door", "piano man"
//    )
    return sharedPreferences.getString(KEY_RECENT_SEARCHES, "")?.split("///")?.filter { it.isNotEmpty() } ?: emptyList()
  }

  fun addRecentSearch(search: String?) {
    if (search == null) return
    if (search.trim().isEmpty()) return
    val searches = loadRecentSearches().toMutableList()
    searches.remove(search)
    searches.add(search)
    val editor = sharedPreferences.edit()
    editor.putString(KEY_RECENT_SEARCHES, searches.joinToString("///"))
    editor.apply()
  }

  fun removeRecentSearch(search: String?) {
    if (search == null) return
    if (search.trim().isEmpty()) return
    val searches = loadRecentSearches().toMutableList()
    searches.remove(search)
    val editor = sharedPreferences.edit()
    editor.putString(KEY_RECENT_SEARCHES, searches.joinToString("///"))
    editor.apply()
  }

  fun getUserId(): Int? {
    val userId = sharedPreferences.getInt(KEY_USER_ID, -1)
    return if (userId == -1) null else userId
  }

  fun setUserId(userId: Int) {
    val editor = sharedPreferences.edit()
    editor.putInt(KEY_USER_ID, userId)
    editor.apply()
  }

  companion object {
    private const val PREFS_NAME = "tidal_streamer_tv"
    private const val KEY_RECENT_SEARCHES = "recent_search"
    private const val KEY_USER_ID = "user_id"
  }
}
