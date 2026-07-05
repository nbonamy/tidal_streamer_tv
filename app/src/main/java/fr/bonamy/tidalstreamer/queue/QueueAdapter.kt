package fr.bonamy.tidalstreamer.queue

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import es.claucookie.miniequalizerlibrary.EqualizerView
import fr.bonamy.tidalstreamer.R
import fr.bonamy.tidalstreamer.models.StatusTrack
import java.util.Locale

class QueueAdapter(
  private val listener: Listener
) : RecyclerView.Adapter<QueueAdapter.ViewHolder>() {

  interface Listener {
    fun onQueueItemFocused(position: Int)
    fun onQueueItemClicked(position: Int)
    fun onQueueItemLongClicked(position: Int)
  }

  private var tracks: List<StatusTrack> = emptyList()
  private var currentPosition = -1
  private var reorderPosition = RecyclerView.NO_POSITION
  private var pendingFocusPosition = RecyclerView.NO_POSITION

  init {
    setHasStableIds(true)
  }

  fun submit(
    newTracks: List<StatusTrack>,
    newCurrentPosition: Int,
    focusPosition: Int = RecyclerView.NO_POSITION,
    newReorderPosition: Int = RecyclerView.NO_POSITION
  ) {
    val previousSize = tracks.size
    tracks = newTracks
    currentPosition = newCurrentPosition
    reorderPosition = newReorderPosition
    pendingFocusPosition = focusPosition
    if (previousSize == tracks.size) {
      notifyItemRangeChanged(0, tracks.size)
    } else {
      notifyDataSetChanged()
    }
  }

  fun moveItem(
    from: Int,
    to: Int,
    newTracks: List<StatusTrack>,
    newCurrentPosition: Int,
    newReorderPosition: Int
  ) {
    tracks = newTracks
    currentPosition = newCurrentPosition
    reorderPosition = newReorderPosition
    pendingFocusPosition = to
    notifyItemMoved(from, to)
    notifyItemRangeChanged(minOf(from, to), kotlin.math.abs(from - to) + 1)
  }

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
    val view = LayoutInflater.from(parent.context)
      .inflate(R.layout.item_queue_track, parent, false)
    return ViewHolder(view)
  }

  override fun onBindViewHolder(holder: ViewHolder, position: Int) {
    val track = tracks[position].item
    val isPlaying = position == currentPosition
    val isReordering = position == reorderPosition

    updateRowHeight(holder, isReordering)
    holder.index.text = String.format(Locale.getDefault(), "%02d", position + 1)
    holder.title.text = track?.title ?: holder.itemView.context.getString(R.string.queue_unavailable_track)
    holder.artist.text = track?.mainArtist()?.name ?: ""
    holder.duration.text = track?.durationString() ?: ""

    if (isPlaying) {
      holder.playing.visibility = View.VISIBLE
      holder.playing.animateBars()
    } else {
      holder.playing.visibility = View.INVISIBLE
      holder.playing.stopBars()
    }

    holder.reorderUp.visibility = if (isReordering && position > 0) View.VISIBLE else View.GONE
    holder.reorderDown.visibility = if (isReordering && position < tracks.lastIndex) View.VISIBLE else View.GONE

    holder.itemView.setOnFocusChangeListener { _, hasFocus ->
      updateFocusedTextColors(holder, hasFocus)
      if (hasFocus) {
        listener.onQueueItemFocused(holder.adapterPosition)
      }
    }
    holder.itemView.setOnClickListener {
      listener.onQueueItemClicked(holder.adapterPosition)
    }
    holder.itemView.setOnLongClickListener {
      listener.onQueueItemLongClicked(holder.adapterPosition)
      true
    }

    if (position == pendingFocusPosition) {
      holder.itemView.post {
        if (holder.adapterPosition == pendingFocusPosition) {
          holder.itemView.requestFocus()
          pendingFocusPosition = RecyclerView.NO_POSITION
        }
      }
    }

    updateFocusedTextColors(holder, holder.itemView.hasFocus())
  }

  override fun getItemCount(): Int = tracks.size

  override fun getItemId(position: Int): Long {
    val track = tracks[position]
    return track.item?.id?.stableItemId()
      ?: track.mediaId?.toLong()
      ?: RecyclerView.NO_ID
  }

  private fun updateFocusedTextColors(holder: ViewHolder, hasFocus: Boolean) {
    val color = holder.itemView.context.getColor(
      if (hasFocus) R.color.text_light_normal else R.color.text_light_faded
    )
    holder.index.setTextColor(color)
    holder.title.setTextColor(color)
    holder.artist.setTextColor(color)
    holder.duration.setTextColor(color)
  }

  private fun updateRowHeight(holder: ViewHolder, isReordering: Boolean) {
    val targetHeight = holder.itemView.context.resources.displayMetrics.density.let { density ->
      ((if (isReordering) REORDER_ROW_HEIGHT_DP else ROW_HEIGHT_DP) * density).toInt()
    }
    val layoutParams = holder.itemView.layoutParams
    if (layoutParams.height != targetHeight) {
      layoutParams.height = targetHeight
      holder.itemView.layoutParams = layoutParams
    }
  }

  class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val reorderUp: ImageView = itemView.findViewById(R.id.reorder_up)
    val reorderDown: ImageView = itemView.findViewById(R.id.reorder_down)
    val playing: EqualizerView = itemView.findViewById(R.id.playing)
    val index: TextView = itemView.findViewById(R.id.index)
    val title: TextView = itemView.findViewById(R.id.title)
    val artist: TextView = itemView.findViewById(R.id.artist)
    val duration: TextView = itemView.findViewById(R.id.duration)
  }

  companion object {
    private const val ROW_HEIGHT_DP = 60
    private const val REORDER_ROW_HEIGHT_DP = 82

    private fun String.stableItemId(): Long {
      var hash = 1125899906842597L
      forEach { char ->
        hash = 31 * hash + char.code
      }
      return hash
    }
  }
}
