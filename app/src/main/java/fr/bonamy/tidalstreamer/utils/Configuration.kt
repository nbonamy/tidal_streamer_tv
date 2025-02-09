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

  fun getServerHostname(): String {
    return "192.168.1.2"
  }

  fun getHttpProtocol(): String {
    return "http://"
  }

  fun getWsProtocol(): String {
    return "ws://"
  }

  fun getHttpPort(): Int {
    return 5002
  }

  fun getWsPort(): Int {
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
    return sharedPreferences.getStringSet(KEY_RECENT_SEARCHES, emptySet())?.toList() ?: emptyList()
  }

  fun addRecentSearch(search: String?) {
    if (search == null) return
    if (search.trim().isEmpty()) return
    val searches = loadRecentSearches().toMutableList()
    searches.add(0, search)
    val editor = sharedPreferences.edit()
    editor.putStringSet(KEY_RECENT_SEARCHES, searches.toSet())
    editor.apply()
  }

  companion object {
    private const val PREFS_NAME = "tidal_streamer_tv"
    private const val KEY_RECENT_SEARCHES = "recent_searches"
  }
}
