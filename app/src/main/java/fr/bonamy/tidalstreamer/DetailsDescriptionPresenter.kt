package fr.bonamy.tidalstreamer

import androidx.leanback.widget.AbstractDetailsDescriptionPresenter
import fr.bonamy.tidalstreamer.models.Album

class DetailsDescriptionPresenter : AbstractDetailsDescriptionPresenter() {

	override fun onBindDescription(
		viewHolder: AbstractDetailsDescriptionPresenter.ViewHolder,
		item: Any
	) {
		val album = item as Album

		viewHolder.title.text = album.title
		viewHolder.subtitle.text = album.mainArtist()?.name ?: ""
		//viewHolder.body.text = album.description
	}
}