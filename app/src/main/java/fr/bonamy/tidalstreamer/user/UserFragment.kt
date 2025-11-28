package fr.bonamy.tidalstreamer.user

import androidx.lifecycle.lifecycleScope
import fr.bonamy.tidalstreamer.R
import fr.bonamy.tidalstreamer.api.UserClient
import fr.bonamy.tidalstreamer.utils.BrowserFragment
import kotlinx.coroutines.launch

class UserFragment : BrowserFragment() {

  override fun title(): String {
    return getString(R.string.browse_user_title)
  }

  override fun loadRows() {

    // init
    val rowsAdapter = initRowsAdapter(4)
    val userClient = UserClient(requireContext())
    adapter = rowsAdapter

    // User's personal library only

    viewLifecycleOwner.lifecycleScope.launch {
      loadRow(rowsAdapter, userClient.fetchPlaylists(), 0, "Your playlists")
    }

    viewLifecycleOwner.lifecycleScope.launch {
      loadRow(rowsAdapter, userClient.fetchFavoriteAlbums(), 1, "Your albums")
    }

    viewLifecycleOwner.lifecycleScope.launch {
      loadRow(rowsAdapter, userClient.fetchFavoriteArtists(), 2, "Your artists")
    }

    viewLifecycleOwner.lifecycleScope.launch {
      loadRow(rowsAdapter, userClient.fetchFavoriteTracks(), 3, "Your tracks")
    }

  }

}