package fr.bonamy.tidalstreamer.auth

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.leanback.widget.ImageCardView
import fr.bonamy.tidalstreamer.R
import fr.bonamy.tidalstreamer.models.User
import fr.bonamy.tidalstreamer.utils.CardPresenter
import fr.bonamy.tidalstreamer.utils.PresenterFlags

class UserCardPresenter : CardPresenter(PresenterFlags.NONE) {

  private var mDefaultCardImage: Drawable? = null

  override fun onCreateViewHolder(parent: ViewGroup): ViewHolder {
    mDefaultCardImage = ContextCompat.getDrawable(parent.context, R.drawable.album)
    return super.onCreateViewHolder(parent)
  }

  override fun onBindViewHolder(viewHolder: ViewHolder, item: Any) {
    val cardView = viewHolder.view as ImageCardView

    if (item is User) {
      cardView.titleText = item.displayName()
      cardView.contentText = item.id.toString()

      // Use colored background for user badge
      cardView.setMainImageDimensions(CARD_WIDTH, CARD_HEIGHT)
      val color = generateColorForUser(item.id ?: 0)
      cardView.mainImageView.setImageDrawable(ColorDrawable(color))
    } else if (item is String && item == NEW_USER_MARKER) {
      cardView.titleText = "Add New User"
      cardView.contentText = "Connect a new TIDAL account"

      // Use a different color for the "add new user" card
      cardView.setMainImageDimensions(CARD_WIDTH, CARD_HEIGHT)
      cardView.mainImageView.setImageDrawable(ColorDrawable(Color.parseColor("#1E90FF")))
    }
  }

  private fun generateColorForUser(userId: Int): Int {
    val colors = listOf(
      "#FF6B6B", "#4ECDC4", "#45B7D1", "#FFA07A", "#98D8C8",
      "#F7DC6F", "#BB8FCE", "#85C1E2", "#F8B88B", "#7FB3D5"
    )
    return Color.parseColor(colors[userId % colors.size])
  }

  companion object {
    const val NEW_USER_MARKER = "__NEW_USER__"
  }

}
