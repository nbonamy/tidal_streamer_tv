package fr.bonamy.tidalstreamer.utils

import android.app.Activity
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
import fr.bonamy.tidalstreamer.models.Radio
import fr.bonamy.tidalstreamer.models.RadioType
import fr.bonamy.tidalstreamer.models.Track
import kotlinx.coroutines.launch


class ItemLongClickedListener(private val mActivity: FragmentActivity, private val mActivityToFinish: Activity? = null) {

  fun onItemLongClicked(item: Any, cardView: ImageCardView?) {
    if (item is Collection) {
      mActivity.lifecycleScope.launch {
        onCollectionLongClicked(item, cardView)
      }
    }
    if (item is Artist) {
      onArtistLongClicked(item, cardView)
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
      val artists = collection.allArtists()
      artists.forEach { artist ->
        if (artist.name != null) {
          if (artists.size == 1) {
            menuItems.add(mActivity.getString(R.string.go_to_artist))
          } else {
            menuItems.add(mActivity.getString(R.string.go_to_prefix) + " " + artist.name!!)
          }
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

  private fun onArtistLongClicked(artist: Artist, cardView: ImageCardView?) {
    // populate menu
    val menuItems: MutableList<String> = ArrayList()
    menuItems.add(mActivity.getString(R.string.go_to_artist))
    menuItems.add(mActivity.getString(R.string.go_to_artist_radio))
    menuItems.add(mActivity.getString(R.string.go_to_artist_info))

    // show the dialog
    val builder: AlertDialog.Builder = AlertDialog.Builder(mActivity)
    builder.setItems(menuItems.toTypedArray()) { _: DialogInterface?, which: Int ->

      val menuChosen = menuItems[which]
      if (isGoToArtist(menuChosen)) {
        goToArtist(artist)
      } else if (isArtistRadio(menuChosen)) {
        goToArtistRadio(artist, cardView)
      } else if (isGoToArtistInfo(menuChosen)) {
        goToArtistInfo(artist)
      }
    }
    builder.show()
  }

  fun onTrackLongClicked(track: Track, cardView: ImageCardView?, navigationOnly: Boolean = false) {

    // populate menu
    val menuItems: MutableList<String> = ArrayList()
    if (!navigationOnly) {
      menuItems.add(mActivity.getString(R.string.play_now))
      menuItems.add(mActivity.getString(R.string.play_next))
      menuItems.add(mActivity.getString(R.string.play_after))
    }

    // album
    if (track.album != null) {
      menuItems.add(mActivity.getString(R.string.go_to_album))
    }

    // artist
    val artists = track.allArtists()
    artists.forEach {
      if (it.name != null) {
        if (artists.size == 1) {
          menuItems.add(mActivity.getString(R.string.go_to_artist))
        } else {
          menuItems.add(mActivity.getString(R.string.go_to_prefix) + " " + it.name!!)
        }
      }
    }

    // track radio
    if (!navigationOnly) {
      menuItems.add(mActivity.getString(R.string.go_to_track_radio))
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
        goToArtist(artists[0])
      } else if (isGoToArtistPrefix(menuChosen)) {
        val artistName = menuChosen.substring(mActivity.getString(R.string.go_to_prefix).length + 1)
        val artist = artists.find { it.name == artistName }
        goToArtist(artist!!)
      } else if (isTrackRadio(menuChosen)) {
        goToTrackRadio(track, cardView)
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

  private fun isArtistRadio(menuChosen: String): Boolean {
    return menuChosen.equals(mActivity.getString(R.string.go_to_artist_radio), ignoreCase = true)
  }

  private fun isGoToArtistInfo(menuChosen: String): Boolean {
    return menuChosen.equals(mActivity.getString(R.string.go_to_artist_info), ignoreCase = true)
  }

  private fun isTrackRadio(menuChosen: String): Boolean {
    return menuChosen.equals(mActivity.getString(R.string.go_to_track_radio), ignoreCase = true)
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

    // base intent
    val intent = Intent(mActivity, CollectionActivity::class.java)
    intent.putExtra(CollectionActivity.COLLECTION, collection)

    // if no card view
    if (cardView == null) {
      mActivity.startActivity(intent)
      mActivityToFinish?.finish()
      return
    }

    // else add transition info
    val bundle = ActivityOptionsCompat.makeSceneTransitionAnimation(
      mActivity,
      cardView.mainImageView,
      CollectionActivity.SHARED_ELEMENT_NAME
    ).toBundle()
    mActivity.startActivity(intent, bundle)
    mActivityToFinish?.finish()

  }

  private fun goToArtist(artist: Artist) {
    val intent = Intent(mActivity, ArtistActivity::class.java)
    intent.putExtra(ArtistActivity.ARTIST, artist)
    mActivity.startActivity(intent)
    mActivityToFinish?.finish()
  }

  fun goToArtistRadio(artist: Artist, cardView: ImageCardView?) {
    mActivity.lifecycleScope.launch {
      val metadataClient = MetadataClient()
      when (val result = metadataClient.fetchArtistRadio(artist.id!!)) {
        is ApiResult.Success -> {
          val radio = Radio(
            type = RadioType.ARTIST,
            id = artist.id,
            title = artist.name,
            image = artist.picture,
          )
          radio.tracks = result.data
          radio.numberOfTracks = result.data.size
          goToCollection(radio, cardView)
        }

        is ApiResult.Error -> {
          Log.e(TAG, "Error fetching artist radio: ${result.exception}")
        }
      }
    }
  }

  fun goToArtistInfo(artist: Artist) {
    mActivity.lifecycleScope.launch {
      val metadataClient = MetadataClient()
      when (val result = metadataClient.fetchArtistInfo(artist.id!!)) {
        is ApiResult.Success -> {
          // show an alert showing result.text
          val builder: AlertDialog.Builder = AlertDialog.Builder(mActivity)
          builder.setTitle(artist.name)
          builder.setMessage(result.data.getPlainText())
          builder.show()
        }

        is ApiResult.Error -> {
          Log.e(TAG, "Error fetching artist radio: ${result.exception}")
        }
      }
    }
  }

  private fun goToTrackRadio(track: Track, cardView: ImageCardView?) {
    mActivity.lifecycleScope.launch {
      val metadataClient = MetadataClient()
      when (val result = metadataClient.fetchTrackRadio(track.id!!)) {
        is ApiResult.Success -> {
          val radio = Radio(
            type = RadioType.TRACK,
            id = track.id,
            title = track.title,
            image = track.album?.cover,
          )
          radio.tracks = result.data
          radio.numberOfTracks = result.data.size
          goToCollection(radio, cardView)
        }

        is ApiResult.Error -> {
          Log.e(TAG, "Error fetching artist radio: ${result.exception}")
        }
      }
    }
  }

  companion object {
    private const val TAG = "TrackLongClickListener"
  }
}
