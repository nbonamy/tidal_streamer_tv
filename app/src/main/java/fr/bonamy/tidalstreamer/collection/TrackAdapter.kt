package fr.bonamy.tidalstreamer.collection

import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import es.claucookie.miniequalizerlibrary.EqualizerView
import fr.bonamy.tidalstreamer.R
import fr.bonamy.tidalstreamer.models.Album
import fr.bonamy.tidalstreamer.models.Collection
import fr.bonamy.tidalstreamer.models.PlayedBy
import fr.bonamy.tidalstreamer.models.Status
import fr.bonamy.tidalstreamer.models.Track
import java.util.Locale

class TrackAdapter(private var mCollection: Collection, private var mList: List<Track>, private val mAppearance: Appearance, private val listener: OnTrackClickListener) : RecyclerView.Adapter<TrackAdapter.ViewHolder>() {

  private var mPosition: Int = -1
  private var mSameArtists: Boolean = true

  fun updateData(newList: List<Track>) {
    mList = newList
    mSameArtists = mList.map { it.mainArtist()?.name }.distinct().size == 1
    notifyItemRangeChanged(0, mList.size)
  }

  fun updateStatus(status: Status) {

    // find the current track
    val currentTrack = status.currentTrack()
    val position = mList.indexOfFirst { it.id == currentTrack?.id }
    if (position != mPosition) {
      Handler(Looper.getMainLooper()).post {
        if (mPosition != -1) {
          notifyItemChanged(mPosition)
        }
        mPosition = position
        if (mPosition != -1) {
          notifyItemChanged(mPosition)
        }
      }
    }
  }

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
    val view = LayoutInflater.from(parent.context)
      .inflate(R.layout.item_track, parent, false)
    val vh = ViewHolder(view)
    vh.index.setTextColor(view.context.getColorStateList(getSelectorTextColor()))
    vh.title.setTextColor(view.context.getColorStateList(getSelectorTextColor()))
    vh.duration.setTextColor(view.context.getColorStateList(getSelectorTextColor()))
    return vh
  }

  private fun getSelectorTextColor(): Int {
    return if (mAppearance == Appearance.LIGHT)
      R.color.text_light_selector else
      R.color.text_dark_selector
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

    // playing
    if (mPosition == position) {
      holder.playing.visibility = View.VISIBLE
      holder.playing.animateBars()
    } else {
      holder.playing.visibility = View.INVISIBLE
      holder.playing.stopBars()
    }

    // index
    if (mCollection is Album) {
      holder.index.visibility = View.VISIBLE
      holder.index.text = String.format(Locale.getDefault(), "%d.", track.trackNumber)
    } else {
      holder.index.visibility = View.GONE
    }

    // title may contain artist name
    var showArtists = !mSameArtists
    if (showArtists && mCollection is PlayedBy) {
      if (track.artists!!.size == 1 && track.artists!![0].id == (mCollection as PlayedBy).mainArtist()?.id) {
        showArtists = false
      }
    }

    // title
    if (showArtists) {
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
  class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val playing: EqualizerView = itemView.findViewById(R.id.playing)
    val index: TextView = itemView.findViewById(R.id.index)
    val title: TextView = itemView.findViewById(R.id.title)
    val duration: TextView = itemView.findViewById(R.id.duration)
  }
}