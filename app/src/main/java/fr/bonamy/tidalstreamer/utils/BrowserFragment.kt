package fr.bonamy.tidalstreamer.utils

import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.DisplayMetrics
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityOptionsCompat
import androidx.core.content.ContextCompat
import androidx.leanback.app.BackgroundManager
import androidx.leanback.app.BrowseSupportFragment
import androidx.leanback.widget.ImageCardView
import androidx.leanback.widget.OnItemViewClickedListener
import androidx.leanback.widget.OnItemViewSelectedListener
import androidx.leanback.widget.Presenter
import androidx.leanback.widget.Row
import androidx.leanback.widget.RowPresenter
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
import fr.bonamy.tidalstreamer.R
import fr.bonamy.tidalstreamer.api.ApiResult
import fr.bonamy.tidalstreamer.api.StreamingClient
import fr.bonamy.tidalstreamer.collection.CollectionActivity
import fr.bonamy.tidalstreamer.models.Album
import fr.bonamy.tidalstreamer.models.Artist
import fr.bonamy.tidalstreamer.models.ImageRepresentation
import fr.bonamy.tidalstreamer.models.Track
import fr.bonamy.tidalstreamer.search.SearchActivity
import kotlinx.coroutines.launch
import java.util.Timer
import java.util.TimerTask


abstract class BrowserFragment : BrowseSupportFragment() {

	abstract open fun title(): String
	abstract open fun loadRows()

	open fun searchEnabled(): Boolean {
		return true
	}

	open fun updateBackground(): Boolean {
		return false
	}

	private val mHandler = Handler(Looper.myLooper()!!)
	private lateinit var mBackgroundManager: BackgroundManager
	private var mDefaultBackground: Drawable? = null
	private lateinit var mMetrics: DisplayMetrics
	private var mBackgroundTimer: Timer? = null
	private var mBackgroundUri: String? = null

	override fun onActivityCreated(savedInstanceState: Bundle?) {
		super.onActivityCreated(savedInstanceState)
		prepareBackgroundManager()
		setupUIElements()
		loadRows()
		setupEventListeners()
	}

	override fun onDestroy() {
		super.onDestroy()
		Log.d(TAG, "onDestroy: " + mBackgroundTimer?.toString())
		mBackgroundTimer?.cancel()
	}

	private fun prepareBackgroundManager() {
		mBackgroundManager = BackgroundManager.getInstance(activity)
		mBackgroundManager.attach(activity!!.window)
		mBackgroundManager.color = ContextCompat.getColor(context!!, R.color.default_background)
		//mDefaultBackground = ContextCompat.getDrawable(context!!, R.drawable.default_background)
		mMetrics = DisplayMetrics()
		activity!!.windowManager.defaultDisplay.getMetrics(mMetrics)
	}

	private fun setupUIElements() {
		title = title()
		headersState = HEADERS_ENABLED
		isHeadersTransitionOnBackEnabled = false
		brandColor = ContextCompat.getColor(context!!, R.color.fastlane_background)
		searchAffordanceColor = ContextCompat.getColor(context!!, R.color.search_opaque)
	}

	private fun setupEventListeners() {

		onItemViewClickedListener = ItemViewClickedListener()
		if (updateBackground()) {
			onItemViewSelectedListener = ItemViewSelectedListener()
		}

		if (searchEnabled()) {
			setOnSearchClickedListener {
				val intent = Intent(context!!, SearchActivity::class.java)
				startActivity(intent)
			}
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

	private inner class ItemViewSelectedListener : OnItemViewSelectedListener {
		override fun onItemSelected(
			itemViewHolder: Presenter.ViewHolder?, item: Any?,
			rowViewHolder: RowPresenter.ViewHolder, row: Row
		) {
			if (item is ImageRepresentation && item.imageUrl() != "") {
				mBackgroundUri = item.imageUrl()
				startBackgroundTimer()
			}
		}
	}


	private fun updateBackground(uri: String?) {
		val width = mMetrics.widthPixels
		val height = mMetrics.heightPixels
		Glide.with(context!!)
			.load(uri)
			.centerCrop()
			.error(mDefaultBackground)
			.into<SimpleTarget<Drawable>>(
				object : SimpleTarget<Drawable>(width, height) {
					override fun onResourceReady(
						drawable: Drawable,
						transition: Transition<in Drawable>?
					) {
						mBackgroundManager.drawable = drawable
					}
				})
		mBackgroundTimer?.cancel()
	}

	private fun startBackgroundTimer() {
		mBackgroundTimer?.cancel()
		mBackgroundTimer = Timer()
		mBackgroundTimer?.schedule(UpdateBackgroundTask(), BACKGROUND_UPDATE_DELAY.toLong())
	}

	private inner class UpdateBackgroundTask : TimerTask() {

		override fun run() {
			mHandler.post { updateBackground(mBackgroundUri) }
		}
	}

	companion object {
		private const val TAG = "BrowserFragment"
		private val BACKGROUND_UPDATE_DELAY = 300
	}

}
