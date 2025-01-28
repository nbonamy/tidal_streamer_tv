package fr.bonamy.tidalstreamer.utils

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityOptionsCompat
import androidx.core.content.ContextCompat
import androidx.leanback.app.BrowseSupportFragment
import androidx.leanback.widget.ImageCardView
import androidx.leanback.widget.OnItemViewClickedListener
import androidx.leanback.widget.OnItemViewSelectedListener
import androidx.leanback.widget.Presenter
import androidx.leanback.widget.Row
import androidx.leanback.widget.RowPresenter
import androidx.lifecycle.lifecycleScope
import fr.bonamy.tidalstreamer.R
import fr.bonamy.tidalstreamer.api.ApiResult
import fr.bonamy.tidalstreamer.api.StreamingClient
import fr.bonamy.tidalstreamer.collection.CollectionActivity
import fr.bonamy.tidalstreamer.models.Album
import fr.bonamy.tidalstreamer.models.Artist
import fr.bonamy.tidalstreamer.models.Track
import fr.bonamy.tidalstreamer.search.SearchActivity
import kotlinx.coroutines.launch


abstract class BrowserFragment : BrowseSupportFragment() {

	abstract open fun searchEnabled(): Boolean
	abstract open fun title(): String
	abstract open fun loadRows()

	open fun viewSelectedListener(): OnItemViewSelectedListener? {
		return null
	}

	override fun onActivityCreated(savedInstanceState: Bundle?) {
		super.onActivityCreated(savedInstanceState)
		setupUIElements()
		loadRows()
		setupEventListeners()
	}

	private fun setupUIElements() {
		title = title()
		headersState = HEADERS_ENABLED
		isHeadersTransitionOnBackEnabled = false
		brandColor = ContextCompat.getColor(context!!, R.color.fastlane_background)
		searchAffordanceColor = ContextCompat.getColor(context!!, R.color.search_opaque)
	}

	private fun setupEventListeners() {

		if (searchEnabled()) {
			setOnSearchClickedListener {
				val intent = Intent(context!!, SearchActivity::class.java)
				startActivity(intent)
			}

		}

		onItemViewClickedListener = ItemViewClickedListener()

		if (viewSelectedListener() != null) {
			onItemViewSelectedListener = viewSelectedListener()
		}
	}

	private inner class ItemViewClickedListener : OnItemViewClickedListener {
		override fun onItemClicked(
			itemViewHolder: Presenter.ViewHolder?,
			item: Any?,
			rowViewHolder: RowPresenter.ViewHolder?,
			row: Row?
		) {
			if (item is Album) {
				val intent = Intent(context!!, CollectionActivity::class.java)
				intent.putExtra(CollectionActivity.COLLECTION, item)
				val bundle = ActivityOptionsCompat.makeSceneTransitionAnimation(
					activity!!,
					(itemViewHolder!!.view as ImageCardView).mainImageView,
					CollectionActivity.SHARED_ELEMENT_NAME
				)
					.toBundle()
				startActivity(intent, bundle)
				return
			}

			if (item is Artist) {
				Toast.makeText(context, "Clicked on artist: ${item.name}", Toast.LENGTH_SHORT).show()
				return
			}

			if (item is Track) {

				lifecycleScope.launch {
					val apiClient = StreamingClient()
					when (val result = apiClient.playTracks((arrayOf(item)))) {
						is ApiResult.Success -> {}
						is ApiResult.Error -> {
							Log.e(TAG, "Error playing track: ${result.exception}")
						}
					}
				}

				return
			}
		}

	}

	companion object {
		private const val TAG = "BrowserFragment"
	}

}
