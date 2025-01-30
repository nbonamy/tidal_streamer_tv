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
import java.util.Locale

interface OnTrackClickListener {
  fun onTrackClick(track: Track)
  fun onTrackLongClick(track: Track)
}

class DetailsPresenter(private val mCollection: Collection, private val mListener: OnTrackClickListener) : Presenter() {

  private fun onCreateView(parent: ViewGroup): View {
    return LayoutInflater.from(parent.context)
      .inflate(R.layout.layout_details, parent, false)
  }

  override fun onCreateViewHolder(parent: ViewGroup?): Presenter.ViewHolder {
    val view: View = onCreateView(parent!!)
    val vh = ViewHolder(view)
    vh.tracks.layoutManager = LinearLayoutManager(parent.context)
    vh.tracks.adapter = TrackAdapter(mCollection, listOf(), mListener)
    return vh
  }

  @SuppressLint("SetTextI18n")
  override fun onBindViewHolder(viewHolder: Presenter.ViewHolder?, item: Any?) {
    val collection = item as Collection
    val vh = viewHolder as ViewHolder
    vh.title.text = collection.title()
    vh.subtitle.text = collection.subtitle()
    if (collection is Album && collection.releaseDate != null) {
      val tokens = collection.releaseDate!!.split("-")
      vh.releaseDate.text = tokens.get(0)
    }
    if (collection.tracks != null) {
      val trackCount = collection.tracks!!.size
      val trackCountText = vh.view.getResources().getQuantityString(R.plurals.track_count, trackCount, trackCount)
      vh.trackCount.text = trackCountText
    }
    vh.trackCount.text = "${collection.tracks?.size?.toString()} tracks" ?: ""
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

  class TrackAdapter(private var mCollection: Collection, private var mList: List<Track>, private val listener: OnTrackClickListener) : RecyclerView.Adapter<TrackAdapter.ViewHolder>() {

    fun updateData(newList: List<Track>) {
      mList = newList
      notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
      val view = LayoutInflater.from(parent.context)
        .inflate(R.layout.item_track, parent, false)
      return ViewHolder(view)
    }

    // binds the list items to a view
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

      // basic
      val track = mList[position]
      holder.itemView.setOnClickListener {
        listener.onTrackClick(track)
      }
      holder.itemView.setOnLongClickListener {
        listener.onTrackLongClick(track)
        true
      }

      // index
      if (mCollection is Album) {
        holder.index.visibility = View.VISIBLE
        holder.index.text = String.format(Locale.getDefault(), "%d.", track.trackNumber)
      } else {
        holder.index.visibility = View.GONE
      }

      // title
      if ((track.artist == null && track.artists != null) || track.index != null) {
        holder.title.text = track.title + " - " + track.artists!!.map { it.name }.joinToString(", ")
      } else {
        holder.title.text = track.title
      }

      // duration
      holder.duration.text = track.durationString()

    }

    // return the number of the items in the list
    override fun getItemCount(): Int {
      return mList.size
    }

    // Holds the views for adding it to image and text
    class ViewHolder(ItemView: View) : RecyclerView.ViewHolder(ItemView) {
      val index: TextView = itemView.findViewById(R.id.index)
      val title: TextView = itemView.findViewById(R.id.title)
      val duration: TextView = itemView.findViewById(R.id.duration)
    }
  }

}