package fr.bonamy.tidalstreamer.search

import android.graphics.drawable.Drawable
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.leanback.widget.ImageCardView
import com.bumptech.glide.Glide
import fr.bonamy.tidalstreamer.R
import fr.bonamy.tidalstreamer.models.Track
import fr.bonamy.tidalstreamer.utils.CardPresenter
import fr.bonamy.tidalstreamer.utils.ItemLongClickedListener

class TrackCardPresenter(private var mTrackPlayback: TrackPlayback, private var mLongClickedListener: ItemLongClickedListener) : CardPresenter() {

  enum class TrackPlayback {
    SINGLE,
    ALL,
  }

  private var mDefaultCardImage: Drawable? = null

  fun getTrackPlayback(): TrackPlayback {
    return mTrackPlayback
  }

  override fun onCreateViewHolder(parent: ViewGroup): ViewHolder {
    mDefaultCardImage = ContextCompat.getDrawable(parent.context, R.drawable.album)
    return super.onCreateViewHolder(parent)
  }

  override fun onBindViewHolder(viewHolder: ViewHolder, item: Any) {
    val track = item as Track
    val cardView = viewHolder.view as ImageCardView
    viewHolder.view.setOnLongClickListener {
      mLongClickedListener.onItemLongClicked(track, cardView)
      true
    }

    //Log.d(TAG, "onBindViewHolder")
    cardView.titleText = track.title
    cardView.contentText = track.artist?.name ?: ""
    cardView.setMainImageDimensions(CARD_WIDTH, CARD_HEIGHT)
    Glide.with(viewHolder.view.context)
      .load(track.imageUrl())
      .centerCrop()
      .error(mDefaultCardImage)
      .into(cardView.mainImageView)
  }

}