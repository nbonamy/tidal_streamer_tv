package fr.bonamy.tidalstreamer.utils

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.util.Log
import androidx.core.app.ActivityOptionsCompat
import androidx.fragment.app.FragmentActivity
import androidx.leanback.widget.ArrayObjectAdapter
import androidx.leanback.widget.ImageCardView
import androidx.leanback.widget.ListRow
import androidx.leanback.widget.OnItemViewClickedListener
import androidx.leanback.widget.Presenter
import androidx.leanback.widget.Row
import androidx.leanback.widget.RowPresenter
import androidx.lifecycle.lifecycleScope
import fr.bonamy.tidalstreamer.R
import fr.bonamy.tidalstreamer.api.ApiResult
import fr.bonamy.tidalstreamer.api.EnqueuePosition
import fr.bonamy.tidalstreamer.api.StreamingClient
import fr.bonamy.tidalstreamer.artist.ArtistActivity
import fr.bonamy.tidalstreamer.collection.CollectionActivity
import fr.bonamy.tidalstreamer.collection.CollectionFragment
import fr.bonamy.tidalstreamer.collection.CollectionFragment.Companion
import fr.bonamy.tidalstreamer.models.Artist
import fr.bonamy.tidalstreamer.models.Collection
import fr.bonamy.tidalstreamer.models.Track
import fr.bonamy.tidalstreamer.search.TrackCardPresenter
import kotlinx.coroutines.launch

interface ITrackLongClickListener {
  fun onTrackLongClicked(track: Track, cardView: ImageCardView?)
}

class TrackLongClickListener(private val mActivity: FragmentActivity) : ITrackLongClickListener {

  override fun onTrackLongClicked(track: Track, cardView: ImageCardView?) {

    // populate menu
    val menuItems: MutableList<String> = ArrayList()
    menuItems.add(mActivity.getString(R.string.play_now))
    menuItems.add(mActivity.getString(R.string.play_next))
    menuItems.add(mActivity.getString(R.string.play_after))
    if (cardView != null && track.album != null) {
      menuItems.add(mActivity.getString(R.string.go_to_album))
    }
    if (track.artist != null) {
      menuItems.add(mActivity.getString(R.string.go_to_artist))
    }

    // show the dialog
    val builder: AlertDialog.Builder = AlertDialog.Builder(mActivity)

    // menu items
    builder.setItems(menuItems.toTypedArray()) { _: DialogInterface?, which: Int ->

      val apiClient = StreamingClient()

      val menuChosen = menuItems[which]
      if (menuChosen.equals(mActivity.getString(R.string.play_now), ignoreCase = true)) {
        mActivity.lifecycleScope.launch {
          when (val result = apiClient.playTracks((arrayOf(track)))) {
            is ApiResult.Success -> {}
            is ApiResult.Error -> {
              Log.e(TAG, "Error playing track: ${result.exception}")
            }
          }
        }
      } else if (menuChosen.equals(mActivity.getString(R.string.play_next), ignoreCase = true)) {
        mActivity.lifecycleScope.launch {
          when (val result = apiClient.enqueueTracks(arrayOf(track), EnqueuePosition.NEXT)) {
            is ApiResult.Success -> {}
            is ApiResult.Error -> {
              Log.e(TAG, "Error playing track: ${result.exception}")
            }
          }
        }
      } else if (menuChosen.equals(mActivity.getString(R.string.play_after), ignoreCase = true)) {
        mActivity.lifecycleScope.launch {
          when (val result = apiClient.enqueueTracks(arrayOf(track), EnqueuePosition.END)) {
            is ApiResult.Success -> {}
            is ApiResult.Error -> {
              Log.e(TAG, "Error playing track: ${result.exception}")
            }
          }
        }
      } else if (menuChosen.equals(mActivity.getString(R.string.go_to_album), ignoreCase = true)) {
        val intent = Intent(mActivity, CollectionActivity::class.java)
        intent.putExtra(CollectionActivity.COLLECTION, track.album)
        val bundle = ActivityOptionsCompat.makeSceneTransitionAnimation(
          mActivity,
          cardView!!.mainImageView,
          CollectionActivity.SHARED_ELEMENT_NAME
        )
          .toBundle()
          mActivity.startActivity(intent, bundle)

      } else if (menuChosen.equals(mActivity.getString(R.string.go_to_artist), ignoreCase = true)) {
        val intent = Intent(mActivity, ArtistActivity::class.java)
        intent.putExtra(ArtistActivity.ARTIST, track.artist)
        mActivity.startActivity(intent)
      }
    }
    builder.show()
  }

  companion object {
    private const val TAG = "TrackLongClickListener"
  }
}
