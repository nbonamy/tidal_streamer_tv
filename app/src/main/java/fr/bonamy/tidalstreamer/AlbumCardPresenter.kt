package fr.bonamy.tidalstreamer

import android.graphics.drawable.Drawable
import android.util.Log
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.leanback.widget.ImageCardView
import androidx.leanback.widget.Presenter
import com.bumptech.glide.Glide
import fr.bonamy.tidalstreamer.models.Album
import kotlin.properties.Delegates

/**
 * A CardPresenter is used to generate Views and bind Objects to them on demand.
 * It contains an ImageCardView.
 */
class AlbumCardPresenter : Presenter() {
	private var mDefaultCardImage: Drawable? = null
	private var sSelectedBackgroundColor: Int by Delegates.notNull()
	private var sDefaultBackgroundColor: Int by Delegates.notNull()

	override fun onCreateViewHolder(parent: ViewGroup): Presenter.ViewHolder {
		Log.d(TAG, "onCreateViewHolder")

		sDefaultBackgroundColor = ContextCompat.getColor(parent.context, R.color.default_background)
		sSelectedBackgroundColor = ContextCompat.getColor(parent.context, R.color.selected_background)
		mDefaultCardImage = ContextCompat.getDrawable(parent.context, R.drawable.album)

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

	override fun onBindViewHolder(viewHolder: Presenter.ViewHolder, item: Any) {
		val album = item as Album
		val cardView = viewHolder.view as ImageCardView

		Log.d(TAG, "onBindViewHolder")
		cardView.titleText = album.title
		cardView.contentText = album.mainArtist()?.name ?: ""
		cardView.setMainImageDimensions(CARD_WIDTH, CARD_HEIGHT)
		Glide.with(viewHolder.view.context)
			.load(album.coverUrl())
			.centerCrop()
			.error(mDefaultCardImage)
			.into(cardView.mainImageView)
	}

	override fun onUnbindViewHolder(viewHolder: Presenter.ViewHolder) {
		Log.d(TAG, "onUnbindViewHolder")
		val cardView = viewHolder.view as ImageCardView
		// Remove references to images so that the garbage collector can free up memory
		cardView.badgeImage = null
		cardView.mainImage = null
	}

	private fun updateCardBackgroundColor(view: ImageCardView, selected: Boolean) {
		val color = if (selected) sSelectedBackgroundColor else sDefaultBackgroundColor
		// Both background colors should be set because the view"s background is temporarily visible
		// during animations.
		view.setBackgroundColor(color)
		view.setInfoAreaBackgroundColor(color)
	}

	companion object {
		private val TAG = "CardPresenter"

		private val CARD_WIDTH = 313
		private val CARD_HEIGHT = 313
	}
}