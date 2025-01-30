package fr.bonamy.tidalstreamer.collection

import android.graphics.drawable.Drawable
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.leanback.widget.ImageCardView
import androidx.leanback.widget.Presenter
import com.bumptech.glide.Glide
import fr.bonamy.tidalstreamer.R
import fr.bonamy.tidalstreamer.models.Collection
import fr.bonamy.tidalstreamer.utils.CardPresenter
import fr.bonamy.tidalstreamer.utils.ItemLongClickedListener

/**
 * A CardPresenter is used to generate Views and bind Objects to them on demand.
 * It contains an ImageCardView.
 */
class CollectionCardPresenter(private var mLongClickedListener: ItemLongClickedListener) : CardPresenter() {

  private var mDefaultCardImage: Drawable? = null

  override fun onCreateViewHolder(parent: ViewGroup): Presenter.ViewHolder {
    mDefaultCardImage = ContextCompat.getDrawable(parent.context, R.drawable.album)
    return super.onCreateViewHolder(parent)
  }

  override fun onBindViewHolder(viewHolder: Presenter.ViewHolder, item: Any) {
    val collection = item as Collection
    val cardView = viewHolder.view as ImageCardView
    viewHolder.view.setOnLongClickListener {
      mLongClickedListener.onItemLongClicked(collection, cardView)
      true
    }

    //Log.d(TAG, "onBindViewHolder")
    cardView.titleText = collection.title()
    cardView.contentText = collection.subtitle()
    cardView.setMainImageDimensions(CARD_WIDTH, CARD_HEIGHT)
    Glide.with(viewHolder.view.context)
      .load(collection.imageUrl())
      .centerCrop()
      .error(mDefaultCardImage)
      .into(cardView.mainImageView)
  }

}