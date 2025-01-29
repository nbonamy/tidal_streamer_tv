package fr.bonamy.tidalstreamer.utils

import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.DisplayMetrics
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.leanback.app.BackgroundManager
import androidx.leanback.app.BrowseSupportFragment
import androidx.leanback.widget.ArrayObjectAdapter
import androidx.leanback.widget.HeaderItem
import androidx.leanback.widget.ListRow
import androidx.leanback.widget.ListRowPresenter
import androidx.leanback.widget.OnItemViewSelectedListener
import androidx.leanback.widget.Presenter
import androidx.leanback.widget.Row
import androidx.leanback.widget.RowPresenter
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import fr.bonamy.tidalstreamer.R
import fr.bonamy.tidalstreamer.api.ApiResult
import fr.bonamy.tidalstreamer.models.Album
import fr.bonamy.tidalstreamer.models.ImageRepresentation
import fr.bonamy.tidalstreamer.search.SearchActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Timer
import java.util.TimerTask


abstract class BrowserFragment : BrowseSupportFragment() {

	abstract fun title(): String
	abstract fun loadRows()

	open fun headersState(): Int {
		return HEADERS_DISABLED
	}

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

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setupUIElements()
		prepareBackgroundManager()
	}

	override fun onActivityCreated(savedInstanceState: Bundle?) {
		super.onActivityCreated(savedInstanceState)
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
		headersState = headersState()
		isHeadersTransitionOnBackEnabled = false
		brandColor = ContextCompat.getColor(context!!, R.color.fastlane_background)
		searchAffordanceColor = ContextCompat.getColor(context!!, R.color.search_opaque)
	}

	private fun setupEventListeners() {

		onItemViewClickedListener = ItemClickedListener(activity!!)
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

	protected fun initRowsAdapter(count: Int): ArrayObjectAdapter {
		val rowsAdapter = ArrayObjectAdapter(ListRowPresenter())
		for (i in 0..<count) {
			val listRowAdapter = ArrayObjectAdapter()
			rowsAdapter.add(ListRow(HeaderItem(""), listRowAdapter))
		}
		return rowsAdapter
	}

	suspend fun <T> loadRow(rowsAdapter: ArrayObjectAdapter, result: ApiResult<List<T>>, presenter: Presenter, titles: Array<String>, index: Int) {
		when (result) {
			is ApiResult.Success -> {
				val rowAdapter = ArrayObjectAdapter(presenter)
				result.data.forEach { item ->
					if (item is Album) {
						if (item.title == null || item.mainArtist() == null) {
							return@forEach
						}
					}
					rowAdapter.add(item)
				}
				withContext(Dispatchers.Main) {
					val header = HeaderItem(titles[index])
					rowsAdapter.replace(index, ListRow(header, rowAdapter))
					rowsAdapter.notifyArrayItemRangeChanged(index, 1)
				}
			}

			is ApiResult.Error -> {
				// Handle the error here
				Log.e(TAG, "Error fetching artist albums: ${result.exception}")
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
			.into(
				object : CustomTarget<Drawable>() {
					override fun onResourceReady(
						drawable: Drawable,
						transition: Transition<in Drawable?>?
					) {
						mBackgroundManager.drawable = drawable
					}
					override fun onLoadCleared(placeholder: Drawable?) {}
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
		private const val BACKGROUND_UPDATE_DELAY = 300
	}

}
