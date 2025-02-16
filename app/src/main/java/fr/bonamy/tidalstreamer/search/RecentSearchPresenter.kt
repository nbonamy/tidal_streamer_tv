package fr.bonamy.tidalstreamer.search

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.leanback.widget.Presenter
import fr.bonamy.tidalstreamer.R

data class RecentSearch(
  var query: String,
  val isFirstRow: Boolean
)

interface OnRecentSearchClickListener {
  fun onLongClick(recentSearch: RecentSearch)
}


class RecentSearchPresenter(private var mListener: OnRecentSearchClickListener) : Presenter() {

  override fun onCreateViewHolder(parent: ViewGroup): ViewHolder {
    val view = LayoutInflater.from(parent.context).inflate(R.layout.item_recent_search, parent, false)
    return ViewHolder(view)
  }

  override fun onBindViewHolder(viewHolder: ViewHolder, item: Any) {
    val searchItem = item as RecentSearch
    val textView = viewHolder.view as TextView
    textView.text = searchItem.query
    if (searchItem.isFirstRow) {
      textView.setPadding(0, 48, textView.paddingRight, 0)
    }

    // long click
    viewHolder.view.setOnLongClickListener {
      mListener.onLongClick(searchItem)
      true
    }
  }

  override fun onUnbindViewHolder(viewHolder: ViewHolder) {
  }
}