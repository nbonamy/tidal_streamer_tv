package fr.bonamy.tidalstreamer

import fr.bonamy.tidalstreamer.api.UserClient
import fr.bonamy.tidalstreamer.utils.BrowserFragment
import fr.bonamy.tidalstreamer.utils.RowDefinition

class MainFragment : BrowserFragment() {

  override fun title(): String {
    return getString(R.string.browse_main_title)
  }

  override fun loadRows() {
    val userClient = UserClient(requireContext())

    val rows = listOf(
      RowDefinition("Shortcuts", { userClient.fetchShortcuts() }),
      RowDefinition("Suggested new albums", { userClient.fetchNewAlbums() }),
      RowDefinition("Recommended new tracks", { userClient.fetchNewTracks() }),
      RowDefinition("Albums you'll enjoy", { userClient.fetchRecommendedAlbums() }),
      RowDefinition("Popular playlists", { userClient.fetchPopularPlaylists() }),
      RowDefinition("Spotlighted tracks", { userClient.fetchSpotlightedTracks() }),
      RowDefinition("Uploads for you", { userClient.fetchUploadsTracks() }),
      RowDefinition("Your listening history", { userClient.fetchHistoryMixes() }),
      RowDefinition("Your favorite artists", { userClient.fetchRecentArtists() }),
      RowDefinition("Custom mixes", { userClient.fetchDailyMixes() }),
      RowDefinition("Essentials to explore", { userClient.fetchEssentialPlaylists() }),
      RowDefinition("Personal radio stations", { userClient.fetchRadioMixes() }),
      RowDefinition("Just updated", { userClient.fetchUpdatedPlaylists() }),
      RowDefinition("User playlists you'll love", { userClient.fetchRecommendedPlaylists() }),
      RowDefinition("Your forgotten favorites", { userClient.fetchForgottenAlbums() }),
    )

    loadRowsFromDefinitions(rows)
  }

}