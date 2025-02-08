package fr.bonamy.tidalstreamer.utils

import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.leanback.widget.ImageCardView
import androidx.leanback.widget.Presenter
import fr.bonamy.tidalstreamer.R
import kotlin.properties.Delegates

@JvmInline
value class PresenterFlags(private val value: Int) {
  companion object {
    val NONE = PresenterFlags(0)
    val SHOW_ALBUM_YEAR = PresenterFlags(1 shl 0)  // 0b0001
    val SHOW_TRACK_ALBUM = PresenterFlags(1 shl 1)  // 0b0010
    val PLAY_ALL_TRACKS = PresenterFlags(1 shl 2)  // 0b0100
  }

  infix fun or(other: PresenterFlags) = PresenterFlags(this.value or other.value)
  infix fun and(other: PresenterFlags) = PresenterFlags(this.value and other.value)
  fun hasFlag(flag: PresenterFlags) = (this.value and flag.value) == flag.value
}


abstract class CardPresenter(protected var mFlags: PresenterFlags) : Presenter() {
  private var sSelectedBackgroundColor: Int by Delegates.notNull()
  private var sDefaultBackgroundColor: Int by Delegates.notNull()

  override fun onCreateViewHolder(parent: ViewGroup): ViewHolder {
    //Log.d(TAG, "onCreateViewHolder")

    sDefaultBackgroundColor = ContextCompat.getColor(parent.context, R.color.default_background)
    sSelectedBackgroundColor = ContextCompat.getColor(parent.context, R.color.selected_background)

    val cardView = object : ImageCardView(parent.context) {
      override fun setSelected(selected: Boolean) {
        updateCardBackgroundColor(this, selected)
        super.setSelected(selected)
      }
    }

    cardView.isFocusable = true
    cardView.isFocusableInTouchMode = true
    updateCardBackgroundColor(cardView, false)
    return ViewHolder(cardView)
  }

  abstract override fun onBindViewHolder(viewHolder: ViewHolder, item: Any)

  override fun onUnbindViewHolder(viewHolder: ViewHolder) {
    //Log.d(TAG, "onUnbindViewHolder")
    val cardView = viewHolder.view as ImageCardView
    // Remove references to images so that the garbage collector can free up memory
    cardView.badgeImage = null
    cardView.mainImage = null
  }

  private fun updateCardBackgroundColor(view: ImageCardView, selected: Boolean) {
    val color = if (selected) sSelectedBackgroundColor else sDefaultBackgroundColor
    // Both background colors should be set because the view"s background is temporarily visible
    // during animations.
    view.setBackgroundColor(sDefaultBackgroundColor)//color)
    view.setInfoAreaBackgroundColor(color)
  }

  companion object {
    //private const val TAG = "CardPresenter"
    const val CARD_WIDTH = 250
    const val CARD_HEIGHT = 250
  }
}