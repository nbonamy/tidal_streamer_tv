package fr.bonamy.tidalstreamer.utils

import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.leanback.widget.ImageCardView
import androidx.leanback.widget.Presenter
import fr.bonamy.tidalstreamer.R
import kotlin.properties.Delegates

/**
 * A CardPresenter is used to generate Views and bind Objects to them on demand.
 * It contains an ImageCardView.
 */
abstract class CardPresenter : Presenter() {
  private var sSelectedBackgroundColor: Int by Delegates.notNull()
  private var sDefaultBackgroundColor: Int by Delegates.notNull()

  override fun onCreateViewHolder(parent: ViewGroup): Presenter.ViewHolder {
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
    return Presenter.ViewHolder(cardView)
  }

  abstract override fun onBindViewHolder(viewHolder: Presenter.ViewHolder, item: Any)

  override fun onUnbindViewHolder(viewHolder: Presenter.ViewHolder) {
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
    private val TAG = "CardPresenter"
    val CARD_WIDTH = 250
    val CARD_HEIGHT = 250
  }
}