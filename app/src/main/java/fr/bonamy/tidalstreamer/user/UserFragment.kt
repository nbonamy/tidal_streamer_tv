package fr.bonamy.tidalstreamer.user

import fr.bonamy.tidalstreamer.R
import fr.bonamy.tidalstreamer.api.UserClient
import fr.bonamy.tidalstreamer.utils.BrowserFragment
import fr.bonamy.tidalstreamer.utils.RowDefinition

class UserFragment : BrowserFragment() {

  override fun title(): String {
    return getString(R.string.browse_user_title)
  }

  override fun loadRows() {
    val userClient = UserClient(requireContext())

    val rows = listOf(
      RowDefinition("Your playlists", { userClient.fetchPlaylists() }),
      RowDefinition("Your albums", { userClient.fetchFavoriteAlbums() }),
      RowDefinition("Your artists", { userClient.fetchFavoriteArtists() }),
      RowDefinition("Your tracks", { userClient.fetchFavoriteTracks() }),
    )

    loadRowsFromDefinitions(rows)
  }

}