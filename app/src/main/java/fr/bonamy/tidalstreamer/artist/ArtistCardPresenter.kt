package fr.bonamy.tidalstreamer.artist

import android.graphics.drawable.Drawable
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.leanback.widget.ImageCardView
import androidx.leanback.widget.Presenter
import com.bumptech.glide.Glide
import fr.bonamy.tidalstreamer.R
import fr.bonamy.tidalstreamer.models.Artist
import fr.bonamy.tidalstreamer.utils.CardPresenter

class ArtistCardPresenter : CardPresenter() {

	private var mDefaultCardImage: Drawable? = null

	override fun onCreateViewHolder(parent: ViewGroup): Presenter.ViewHolder {
		mDefaultCardImage = ContextCompat.getDrawable(parent.context, R.drawable.album)
		return super.onCreateViewHolder(parent)
	}

	override fun onBindViewHolder(viewHolder: Presenter.ViewHolder, item: Any) {
		val artist = item as Artist
		val cardView = viewHolder.view as ImageCardView

		//Log.d(TAG, "onBindViewHolder")
		cardView.titleText = artist.name
		//cardView.contentText = cardable.subtitle()
		cardView.setMainImageDimensions(CARD_WIDTH, CARD_HEIGHT)
		Glide.with(viewHolder.view.context)
			.load(artist.imageUrl())
			.centerCrop()
			.error(mDefaultCardImage)
			.into(cardView.mainImageView)
	}

}