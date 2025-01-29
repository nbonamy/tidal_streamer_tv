package fr.bonamy.tidalstreamer.collection

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
}

class DetailsPresenter(private val mCollection: Collection, private val mListener: OnTrackClickListener) : Presenter(){

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

	override fun onBindViewHolder(viewHolder: Presenter.ViewHolder?, item: Any?) {
		val collection = item as Collection
		val vh = viewHolder as ViewHolder
		vh.title.text = collection.title()
		vh.subtitle.text = collection.subtitle()
		(vh.tracks.adapter as TrackAdapter).updateData(collection.tracks ?: listOf())
	}

	override fun onUnbindViewHolder(viewHolder: Presenter.ViewHolder?) {
	}

	class ViewHolder(view: View) : Presenter.ViewHolder(view) {
		val title: TextView = view.findViewById<View>(R.id.details_title) as TextView
		val subtitle: TextView =
			view.findViewById<View>(R.id.details_subtitle) as TextView
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

			// index
			if (mCollection is Album) {
				holder.index.visibility = View.VISIBLE
				holder.index.text = track.trackNumber.toString() + "."
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