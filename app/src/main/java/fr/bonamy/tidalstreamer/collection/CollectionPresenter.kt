package fr.bonamy.tidalstreamer.collection

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.leanback.widget.Presenter
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import fr.bonamy.tidalstreamer.R
import fr.bonamy.tidalstreamer.models.Album
import fr.bonamy.tidalstreamer.models.Collection
import fr.bonamy.tidalstreamer.models.Track

interface OnTrackClickListener {
  fun onTrackClick(track: Track)
  fun onTrackLongClick(track: Track)
}

class CollectionPresenter(
  private val mCollection: Collection,
  private val mAppearance: Appearance,
  private val mListener: OnTrackClickListener
) : Presenter() {

  private fun onCreateView(parent: ViewGroup): View {
    return LayoutInflater.from(parent.context)
      .inflate(R.layout.fragment_collection, parent, false)
  }

  override fun onCreateViewHolder(parent: ViewGroup?): Presenter.ViewHolder {
    val view: View = onCreateView(parent!!)
    val vh = ViewHolder(view)
    vh.tracks.layoutManager = LinearLayoutManager(parent.context)
    vh.tracks.adapter = TrackAdapter(mCollection, listOf(), mAppearance, mListener)
    vh.title.setTextColor(view.context.getColor(getNormalTextColor()))
    vh.subtitle.setTextColor(view.context.getColor(getNormalTextColor()))
    vh.releaseDate.setTextColor(view.context.getColor(getNormalTextColor()))
    vh.trackCount.setTextColor(view.context.getColor(getNormalTextColor()))
    return vh
  }

  private fun getNormalTextColor(): Int {
    return if (mAppearance == Appearance.LIGHT)
      R.color.text_light_normal else
      R.color.text_dark_normal
  }

  @SuppressLint("SetTextI18n")
  override fun onBindViewHolder(viewHolder: Presenter.ViewHolder?, item: Any?) {
    val collection = item as Collection
    val vh = viewHolder as ViewHolder
    vh.title.text = collection.title()
    vh.subtitle.text = collection.subtitle()
    if (collection is Album && collection.releaseDate != null) {
      val tokens = collection.releaseDate!!.split("-")
      vh.releaseDate.text = tokens[0]
    }
    if (collection.tracks != null) {
      val trackCount = collection.tracks!!.size
      val trackCountText =
        vh.view.resources.getQuantityString(R.plurals.track_count, trackCount, trackCount)
      vh.trackCount.text = trackCountText
    } else if (collection is Album) {
      vh.trackCount.text = "${collection.numberOfTracks} tracks"
    }
    if (collection.tracks !== null) {
      vh.trackCount.text = "${collection.tracks?.size?.toString()} tracks"
    }
    (vh.tracks.adapter as TrackAdapter).updateData(collection.tracks ?: listOf())
  }

  override fun onUnbindViewHolder(viewHolder: Presenter.ViewHolder?) {
  }

  class ViewHolder(view: View) : Presenter.ViewHolder(view) {
    val title: TextView = view.findViewById<View>(R.id.details_title) as TextView
    val subtitle: TextView =
      view.findViewById<View>(R.id.details_subtitle) as TextView
    var releaseDate: TextView = view.findViewById<View>(R.id.details_1) as TextView
    var trackCount = view.findViewById<View>(R.id.details_2) as TextView
    val tracks: RecyclerView = view.findViewById<View>(R.id.details_tracks) as RecyclerView
  }

}