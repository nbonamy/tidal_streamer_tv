package fr.bonamy.tidalstreamer

import androidx.lifecycle.lifecycleScope
import fr.bonamy.tidalstreamer.api.UserClient
import fr.bonamy.tidalstreamer.utils.BrowserFragment
import kotlinx.coroutines.launch

class MainFragment : BrowserFragment() {

  override fun title(): String {
    return getString(R.string.browse_main_title)
  }

  override fun loadRows() {

    // init
    val rowsAdapter = initRowsAdapter(15)
    val userClient = UserClient(requireContext())
    adapter = rowsAdapter

    // TIDAL home layout - matching order from tidal_streamer

    viewLifecycleOwner.lifecycleScope.launch {
      loadRow(rowsAdapter, userClient.fetchShortcuts(), 0, "Shortcuts")
    }

    viewLifecycleOwner.lifecycleScope.launch {
      loadRow(rowsAdapter, userClient.fetchNewAlbums(), 1, "Suggested new albums")
    }

    viewLifecycleOwner.lifecycleScope.launch {
      loadRow(rowsAdapter, userClient.fetchNewTracks(), 2, "Recommended new tracks")
    }

    viewLifecycleOwner.lifecycleScope.launch {
      loadRow(rowsAdapter, userClient.fetchRecommendedAlbums(), 3, "Albums you'll enjoy")
    }

    viewLifecycleOwner.lifecycleScope.launch {
      loadRow(rowsAdapter, userClient.fetchPopularPlaylists(), 4, "Popular playlists")
    }

    viewLifecycleOwner.lifecycleScope.launch {
      loadRow(rowsAdapter, userClient.fetchSpotlightedTracks(), 5, "Spotlighted tracks")
    }

    viewLifecycleOwner.lifecycleScope.launch {
      loadRow(rowsAdapter, userClient.fetchUploadsTracks(), 6, "Uploads for you")
    }

    viewLifecycleOwner.lifecycleScope.launch {
      loadRow(rowsAdapter, userClient.fetchHistoryMixes(), 7, "Your listening history")
    }

    viewLifecycleOwner.lifecycleScope.launch {
      loadRow(rowsAdapter, userClient.fetchRecentArtists(), 8, "Your favorite artists")
    }

    viewLifecycleOwner.lifecycleScope.launch {
      loadRow(rowsAdapter, userClient.fetchDailyMixes(), 9, "Custom mixes")
    }

    viewLifecycleOwner.lifecycleScope.launch {
      loadRow(rowsAdapter, userClient.fetchEssentialPlaylists(), 10, "Essentials to explore")
    }

    viewLifecycleOwner.lifecycleScope.launch {
      loadRow(rowsAdapter, userClient.fetchRadioMixes(), 11, "Personal radio stations")
    }

    viewLifecycleOwner.lifecycleScope.launch {
      loadRow(rowsAdapter, userClient.fetchUpdatedPlaylists(), 12, "Just updated")
    }

    viewLifecycleOwner.lifecycleScope.launch {
      loadRow(rowsAdapter, userClient.fetchRecommendedPlaylists(), 13, "User playlists you'll love")
    }

    viewLifecycleOwner.lifecycleScope.launch {
      loadRow(rowsAdapter, userClient.fetchForgottenAlbums(), 14, "Your forgotten favorites")
    }

  }

}