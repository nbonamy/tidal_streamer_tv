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
import fr.bonamy.tidalstreamer.utils.PresenterFlags

class TrackCardPresenter(mFlags: PresenterFlags, private var mLongClickedListener: ItemLongClickedListener) : CardPresenter(mFlags) {

  enum class TrackPlayback {
    SINGLE,
    ALL,
  }

  private var mDefaultCardImage: Drawable? = null

  fun getTrackPlayback(): TrackPlayback {
    return if (mFlags.hasFlag(PresenterFlags.PLAY_ALL_TRACKS)) TrackPlayback.ALL else TrackPlayback.SINGLE
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

    //  content depends on flags
    if (mFlags.hasFlag(PresenterFlags.SHOW_TRACK_ALBUM)) {
      cardView.contentText = track.album?.title ?: ""
    } else {
      cardView.contentText = track.mainArtist()?.name ?: ""
    }

    // image
    cardView.setMainImageDimensions(CARD_WIDTH, CARD_HEIGHT)
    Glide.with(viewHolder.view.context)
      .load(track.imageUrl())
      .centerCrop()
      .error(mDefaultCardImage)
      .into(cardView.mainImageView)
  }

}