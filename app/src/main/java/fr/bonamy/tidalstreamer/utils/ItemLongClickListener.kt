package fr.bonamy.tidalstreamer.utils

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.util.Log
import androidx.core.app.ActivityOptionsCompat
import androidx.fragment.app.FragmentActivity
import androidx.leanback.widget.ImageCardView
import androidx.lifecycle.lifecycleScope
import fr.bonamy.tidalstreamer.R
import fr.bonamy.tidalstreamer.api.ApiResult
import fr.bonamy.tidalstreamer.api.EnqueuePosition
import fr.bonamy.tidalstreamer.api.MetadataClient
import fr.bonamy.tidalstreamer.api.StreamingClient
import fr.bonamy.tidalstreamer.artist.ArtistActivity
import fr.bonamy.tidalstreamer.collection.CollectionActivity
import fr.bonamy.tidalstreamer.models.Album
import fr.bonamy.tidalstreamer.models.Artist
import fr.bonamy.tidalstreamer.models.Collection
import fr.bonamy.tidalstreamer.models.Track
import kotlinx.coroutines.launch

class ItemLongClickedListener(private val mActivity: FragmentActivity) {

  fun onItemLongClicked(item: Any, cardView: ImageCardView?) {
    if (item is Collection) {
      mActivity.lifecycleScope.launch {
        onCollectionLongClicked(item, cardView)
      }
    }
    if (item is Artist) {
      onArtistLongClicked(item)
    }
    if (item is Track) {
      onTrackLongClicked(item, cardView)
    }
  }

  private fun onCollectionLongClicked(collection: Collection, cardView: ImageCardView?) {
    // populate menu
    val menuItems: MutableList<String> = ArrayList()
    menuItems.add(mActivity.getString(R.string.play_now))
    menuItems.add(mActivity.getString(R.string.play_next))
    menuItems.add(mActivity.getString(R.string.play_after))
    menuItems.add(mActivity.getString(R.string.go_to_album))

    // artist
    if (collection is Album) {
      collection.allArtists().forEach { artist ->
        if (artist.name != null) {
          menuItems.add(mActivity.getString(R.string.go_to_prefix) + " " + artist.name!!)
        }
      }
    }

    // show the dialog
    val builder: AlertDialog.Builder = AlertDialog.Builder(mActivity)
    builder.setItems(menuItems.toTypedArray()) { _: DialogInterface?, which: Int ->

      val apiClient = StreamingClient()

      val menuChosen = menuItems[which]
      val tracksNeeded = isPlayNow(menuChosen) || isPlayNext(menuChosen) || isPlayAfter(menuChosen)

      mActivity.lifecycleScope.launch {

        // first make sure we have tracks
        if (tracksNeeded && collection.tracks == null) {
          val metadataClient = MetadataClient()
          if (!metadataClient.fetchTracks(collection)) {
            return@launch
          }
        }

        if (isPlayNow(menuChosen)) {
          playTracks(apiClient, collection.tracks!!.toTypedArray())
        } else if (isPlayNext(menuChosen)) {
          enqueueTracks(apiClient, collection.tracks!!.toTypedArray(), EnqueuePosition.NEXT)
        } else if (isPlayAfter(menuChosen)) {
          enqueueTracks(apiClient, collection.tracks!!.toTypedArray(), EnqueuePosition.END)
        } else if (isGoToAlbum(menuChosen)) {
          goToCollection(collection, cardView)
        } else if (isGoToArtist(menuChosen)) {
          goToArtist((collection as Album).mainArtist()!!)
        } else if (isGoToArtistPrefix(menuChosen)) {
          val artistName = menuChosen.substring(mActivity.getString(R.string.go_to_prefix).length + 1)
          val artist = (collection as Album).artists!!.find { it.name == artistName }
          goToArtist(artist!!)
        }
      }
    }
    builder.show()
  }

  private fun onArtistLongClicked(artist: Artist) {
    // populate menu
    val menuItems: MutableList<String> = ArrayList()
    menuItems.add(mActivity.getString(R.string.go_to_artist))

    // show the dialog
    val builder: AlertDialog.Builder = AlertDialog.Builder(mActivity)
    builder.setItems(menuItems.toTypedArray()) { _: DialogInterface?, which: Int ->

      val menuChosen = menuItems[which]
      if (isGoToArtist(menuChosen)) {
        goToArtist(artist)
      }
    }
    builder.show()
  }

  private fun onTrackLongClicked(track: Track, cardView: ImageCardView?) {

    // populate menu
    val menuItems: MutableList<String> = ArrayList()
    menuItems.add(mActivity.getString(R.string.play_now))
    menuItems.add(mActivity.getString(R.string.play_next))
    menuItems.add(mActivity.getString(R.string.play_after))

    // album needs a cardView to transition to
    if (cardView != null && track.album != null) {
      menuItems.add(mActivity.getString(R.string.go_to_album))
    }

    // artist
    track.allArtists().forEach {
      if (it.name != null) {
        menuItems.add(mActivity.getString(R.string.go_to_prefix) + " " + it.name!!)
      }
    }

    // show the dialog
    val builder: AlertDialog.Builder = AlertDialog.Builder(mActivity)
    builder.setItems(menuItems.toTypedArray()) { _: DialogInterface?, which: Int ->

      val apiClient = StreamingClient()

      val menuChosen = menuItems[which]
      if (isPlayNow(menuChosen)) {
        playTracks(apiClient, arrayOf(track))
      } else if (isPlayNext(menuChosen)) {
        enqueueTracks(apiClient, arrayOf(track), EnqueuePosition.NEXT)
      } else if (isPlayAfter(menuChosen)) {
        enqueueTracks(apiClient, arrayOf(track), EnqueuePosition.END)
      } else if (isGoToAlbum(menuChosen)) {
        goToCollection(track.album!!, cardView)
      } else if (isGoToArtist(menuChosen)) {
        goToArtist(track.artist!!)
      } else if (isGoToArtistPrefix(menuChosen)) {
        val artistName = menuChosen.substring(mActivity.getString(R.string.go_to_prefix).length + 1)
        val artist = track.artists!!.find { it.name == artistName }
        goToArtist(artist!!)
      }
    }
    builder.show()
  }

  private fun isPlayNow(menuChosen: String): Boolean {
    return menuChosen.equals(mActivity.getString(R.string.play_now), ignoreCase = true)
  }

  private fun isPlayNext(menuChosen: String): Boolean {
    return menuChosen.equals(mActivity.getString(R.string.play_next), ignoreCase = true)
  }

  private fun isPlayAfter(menuChosen: String): Boolean {
    return menuChosen.equals(mActivity.getString(R.string.play_after), ignoreCase = true)
  }

  private fun isGoToAlbum(menuChosen: String): Boolean {
    return menuChosen.equals(mActivity.getString(R.string.go_to_album), ignoreCase = true)
  }

  private fun isGoToArtist(menuChosen: String): Boolean {
    return menuChosen.equals(mActivity.getString(R.string.go_to_artist), ignoreCase = true)
  }

  private fun isGoToArtistPrefix(menuChosen: String): Boolean {
    return menuChosen.startsWith(mActivity.getString(R.string.go_to_prefix), ignoreCase = true)
  }

  private fun playTracks(apiClient: StreamingClient, tracks: Array<Track>) {
    Log.d(TAG, "Playing tracks: ${tracks.size}")
    mActivity.lifecycleScope.launch {
      when (val result = apiClient.playTracks(tracks)) {
        is ApiResult.Success -> {}
        is ApiResult.Error -> {
          Log.e(TAG, "Error playing track: ${result.exception}")
        }
      }
    }
  }

  private fun enqueueTracks(apiClient: StreamingClient, tracks: Array<Track>, position: EnqueuePosition) {
    mActivity.lifecycleScope.launch {
      when (val result = apiClient.enqueueTracks(tracks, position)) {
        is ApiResult.Success -> {}
        is ApiResult.Error -> {
          Log.e(TAG, "Error playing track: ${result.exception}")
        }
      }
    }
  }

  private fun goToCollection(collection: Collection, cardView: ImageCardView?) {
    val intent = Intent(mActivity, CollectionActivity::class.java)
    intent.putExtra(CollectionActivity.COLLECTION, collection)
    val bundle = ActivityOptionsCompat.makeSceneTransitionAnimation(
      mActivity,
      cardView!!.mainImageView,
      CollectionActivity.SHARED_ELEMENT_NAME
    ).toBundle()
    mActivity.startActivity(intent, bundle)
  }

  private fun goToArtist(artist: Artist) {
    val intent = Intent(mActivity, ArtistActivity::class.java)
    intent.putExtra(ArtistActivity.ARTIST, artist)
    mActivity.startActivity(intent)
  }

  companion object {
    private const val TAG = "TrackLongClickListener"
  }
}
