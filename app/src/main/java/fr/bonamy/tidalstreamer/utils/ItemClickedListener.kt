package fr.bonamy.tidalstreamer.utils

import android.content.Intent
import android.os.Handler
import android.os.Looper
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
import fr.bonamy.tidalstreamer.api.ApiResult
import fr.bonamy.tidalstreamer.api.MetadataClient
import fr.bonamy.tidalstreamer.api.StreamingClient
import fr.bonamy.tidalstreamer.artist.ArtistActivity
import fr.bonamy.tidalstreamer.collection.CollectionActivity
import fr.bonamy.tidalstreamer.models.Artist
import fr.bonamy.tidalstreamer.models.Collection
import fr.bonamy.tidalstreamer.models.Track
import fr.bonamy.tidalstreamer.search.TrackCardPresenter
import kotlinx.coroutines.launch

class ItemClickedListener(private val mActivity: FragmentActivity) : OnItemViewClickedListener {

  private var lastClickedItem: Collection? = null
  private var lastClickViewHolder: Presenter.ViewHolder? = null
  private val handler = Handler(Looper.getMainLooper())

  override fun onItemClicked(
    itemViewHolder: Presenter.ViewHolder?,
    item: Any?,
    rowViewHolder: RowPresenter.ViewHolder?,
    row: Row?
  ) {

    if (item is Collection) {
      lastClickedItem = item
      lastClickViewHolder = itemViewHolder
      singleClickTask.run()
//      handler.postDelayed(singleClickTask, DOUBLE_CLICK_TIMEOUT)
//      if (item == lastClickedItem) {
//        handler.removeCallbacks(singleClickTask)
//        handleDoubleClick(item)
//        resetClickStateInformation()
//      } else {
//        lastClickedItem = item
//        lastClickViewHolder = itemViewHolder
//        handler.postDelayed(singleClickTask, DOUBLE_CLICK_TIMEOUT)
//      }
    }

    if (item is Artist) {
      val intent = Intent(mActivity, ArtistActivity::class.java)
      intent.putExtra(ArtistActivity.ARTIST, item)
      mActivity.startActivity(intent)
      return
    }

    if (item is Track) {

      val presenter = (row as ListRow).adapter.presenterSelector.getPresenter(item) as TrackCardPresenter
      if (presenter.getTrackPlayback() == TrackCardPresenter.TrackPlayback.SINGLE) {

        mActivity.lifecycleScope.launch {
          val apiClient = StreamingClient()
          when (val result = apiClient.playTracks((arrayOf(item)))) {
            is ApiResult.Success -> {}
            is ApiResult.Error -> {
              Log.e(TAG, "Error playing track: ${result.exception}")
            }
          }
        }

      } else if (presenter.getTrackPlayback() == TrackCardPresenter.TrackPlayback.ALL) {

        val tracks = (row.adapter as ArrayObjectAdapter).unmodifiableList<Track>().toTypedArray()
        val position = tracks.indexOf(item)

        mActivity.lifecycleScope.launch {
          val apiClient = StreamingClient()
          when (val result = apiClient.playTracks(tracks, position)) {
            is ApiResult.Success -> {}
            is ApiResult.Error -> {
              Log.e(TAG, "Error playing track: ${result.exception}")
            }
          }
        }

      }



      return
    }
  }

  private val singleClickTask = Runnable {
    val intent = Intent(mActivity, CollectionActivity::class.java)
    intent.putExtra(CollectionActivity.COLLECTION, lastClickedItem)
    val bundle = ActivityOptionsCompat.makeSceneTransitionAnimation(
      mActivity,
      (lastClickViewHolder!!.view as ImageCardView).mainImageView,
      CollectionActivity.SHARED_ELEMENT_NAME
    ).toBundle()
    mActivity.startActivity(intent, bundle)
    resetClickStateInformation()
  }

  private fun handleDoubleClick(item: Collection) {

    mActivity.lifecycleScope.launch {

      // first make sure we have tracks
      if (item.tracks == null) {
        val apiClient = MetadataClient()
        if (!apiClient.fetchTracks(item)) {
          return@launch
        }
      }

      val apiClient = StreamingClient()
      when (val result = apiClient.playTracks(item.tracks!!.toTypedArray())) {
        is ApiResult.Success -> {}
        is ApiResult.Error -> {
          Log.e(TAG, "Error playing track: ${result.exception}")
        }
      }
    }
  }

  private fun resetClickStateInformation() {
    lastClickedItem = null
    lastClickViewHolder = null
  }

  companion object {
    private const val TAG = "ItemClickedListener"
    private const val DOUBLE_CLICK_TIMEOUT = 250L
  }
}
