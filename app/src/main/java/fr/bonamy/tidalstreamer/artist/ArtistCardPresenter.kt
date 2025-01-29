package fr.bonamy.tidalstreamer.artist

import android.graphics.drawable.Drawable
import android.view.Gravity
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.leanback.widget.ImageCardView
import com.bumptech.glide.Glide
import fr.bonamy.tidalstreamer.R
import fr.bonamy.tidalstreamer.models.Artist
import fr.bonamy.tidalstreamer.utils.CardPresenter

class ArtistCardPresenter : CardPresenter() {

	private var mDefaultCardImage: Drawable? = null

	override fun onCreateViewHolder(parent: ViewGroup): ViewHolder {
		mDefaultCardImage = ContextCompat.getDrawable(parent.context, R.drawable.artist)
		return super.onCreateViewHolder(parent)
	}

	override fun onBindViewHolder(viewHolder: ViewHolder, item: Any) {
		val artist = item as Artist
		val cardView = viewHolder.view as ImageCardView

		val titleView: TextView = viewHolder.view.findViewById(androidx.leanback.R.id.title_text)
		titleView.textAlignment = TextView.TEXT_ALIGNMENT_CENTER
		titleView.gravity = Gravity.CENTER
		titleView.minLines = 2
		titleView.maxLines = 2

		val contentView: TextView = viewHolder.view.findViewById(androidx.leanback.R.id.content_text)
		contentView.visibility = TextView.GONE

		//Log.d(TAG, "onBindViewHolder")
		cardView.titleText = artist.name
		cardView.setMainImageDimensions(CARD_WIDTH, CARD_HEIGHT)
		Glide.with(viewHolder.view.context)
			.load(artist.imageUrl())
			.circleCrop()
			.error(mDefaultCardImage)
			.into(cardView.mainImageView)
	}

}