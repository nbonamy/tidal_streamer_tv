package fr.bonamy.tidalstreamer

import androidx.leanback.widget.AbstractDetailsDescriptionPresenter
import fr.bonamy.tidalstreamer.models.Album
import fr.bonamy.tidalstreamer.models.Collection

class DetailsDescriptionPresenter : AbstractDetailsDescriptionPresenter() {

	override fun onBindDescription(
		viewHolder: AbstractDetailsDescriptionPresenter.ViewHolder,
		item: Any
	) {
		val collection = item as Collection

		viewHolder.title.text = collection.title()
		viewHolder.subtitle.text = collection.subtitle()
		//viewHolder.body.text = album.description
	}
}