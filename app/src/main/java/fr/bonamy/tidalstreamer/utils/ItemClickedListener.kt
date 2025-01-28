package fr.bonamy.tidalstreamer.utils

import android.content.Intent
import android.util.Log
import androidx.core.app.ActivityOptionsCompat
import androidx.fragment.app.FragmentActivity
import androidx.leanback.widget.ImageCardView
import androidx.leanback.widget.OnItemViewClickedListener
import androidx.leanback.widget.Presenter
import androidx.leanback.widget.Row
import androidx.leanback.widget.RowPresenter
import androidx.lifecycle.lifecycleScope
import fr.bonamy.tidalstreamer.api.ApiResult
import fr.bonamy.tidalstreamer.api.StreamingClient
import fr.bonamy.tidalstreamer.artist.ArtistActivity
import fr.bonamy.tidalstreamer.collection.CollectionActivity
import fr.bonamy.tidalstreamer.models.Artist
import fr.bonamy.tidalstreamer.models.Collection
import fr.bonamy.tidalstreamer.models.Track
import kotlinx.coroutines.launch

class ItemClickedListener(private val activity: FragmentActivity) : OnItemViewClickedListener {

  override fun onItemClicked(
    itemViewHolder: Presenter.ViewHolder?,
    item: Any?,
    rowViewHolder: RowPresenter.ViewHolder?,
    row: Row?
  ) {
    if (item is Collection) {
      val intent = Intent(activity, CollectionActivity::class.java)
      intent.putExtra(CollectionActivity.COLLECTION, item)
      val bundle = ActivityOptionsCompat.makeSceneTransitionAnimation(
        activity,
        (itemViewHolder!!.view as ImageCardView).mainImageView,
        CollectionActivity.SHARED_ELEMENT_NAME
      )
        .toBundle()
      activity.startActivity(intent, bundle)
      return
    }

    if (item is Artist) {
      val intent = Intent(activity, ArtistActivity::class.java)
      intent.putExtra(ArtistActivity.ARTIST, item)
      activity.startActivity(intent)
      return
    }

    if (item is Track) {

      activity.lifecycleScope.launch {
        val apiClient = StreamingClient()
        when (val result = apiClient.playTracks((arrayOf(item)))) {
          is ApiResult.Success -> {}
          is ApiResult.Error -> {
            Log.e(TAG, "Error playing track: ${result.exception}")
          }
        }
      }

      return
    }
  }

  companion object {
    private const val TAG = "ItemClickedListener"
  }
}
