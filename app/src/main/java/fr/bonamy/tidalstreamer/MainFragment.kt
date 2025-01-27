package fr.bonamy.tidalstreamer

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.DisplayMetrics
import android.util.Log
import android.view.Gravity
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityOptionsCompat
import androidx.core.content.ContextCompat
import androidx.leanback.app.BackgroundManager
import androidx.leanback.app.BrowseSupportFragment
import androidx.leanback.widget.ArrayObjectAdapter
import androidx.leanback.widget.HeaderItem
import androidx.leanback.widget.ImageCardView
import androidx.leanback.widget.ListRow
import androidx.leanback.widget.ListRowPresenter
import androidx.leanback.widget.OnItemViewClickedListener
import androidx.leanback.widget.OnItemViewSelectedListener
import androidx.leanback.widget.Presenter
import androidx.leanback.widget.Row
import androidx.leanback.widget.RowPresenter
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
import fr.bonamy.tidalstreamer.api.ApiResult
import fr.bonamy.tidalstreamer.api.MetadataClient
import fr.bonamy.tidalstreamer.models.Album
import fr.bonamy.tidalstreamer.models.Collection
import kotlinx.coroutines.launch
import java.util.Timer
import java.util.TimerTask

/**
 * Loads a grid of cards with movies to browse.
 */
class MainFragment : BrowseSupportFragment() {

	private val mHandler = Handler(Looper.myLooper()!!)
	private lateinit var mBackgroundManager: BackgroundManager
	private var mDefaultBackground: Drawable? = null
	private lateinit var mMetrics: DisplayMetrics
	private var mBackgroundTimer: Timer? = null
	private var mBackgroundUri: String? = null

	override fun onActivityCreated(savedInstanceState: Bundle?) {
		Log.i(TAG, "onCreate")
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
		title = getString(R.string.browse_title)
		// over title
		headersState = BrowseSupportFragment.HEADERS_ENABLED
		isHeadersTransitionOnBackEnabled = true

		// set fastLane (or headers) background color
		brandColor = ContextCompat.getColor(context!!, R.color.fastlane_background)
		// set search icon color
		searchAffordanceColor = ContextCompat.getColor(context!!, R.color.search_opaque)
	}

	private fun loadRows() {

		val rowsAdapter = ArrayObjectAdapter(ListRowPresenter())
		val cardPresenter = CollectionPresenter()

		// add NUM_ROWS placeholders
		for (i in 0..NUM_ROWS-1) {
			val listRowAdapter = ArrayObjectAdapter(cardPresenter)
			rowsAdapter.add(ListRow(HeaderItem(ROWS_TITLE[i]), listRowAdapter))
		}

		lifecycleScope.launch {
			val apiClient = MetadataClient()
			when (val result = apiClient.fetchShortcuts()) {

				is ApiResult.Success -> {
					var itemAdapter = ArrayObjectAdapter(cardPresenter)
					result.data.forEach { shortcut ->
						if (shortcut.title != "" && shortcut.mainArtist() != null) {
							itemAdapter.add(shortcut)
						}
					}
					val header = HeaderItem(ROWS_TITLE[0])
					rowsAdapter.replace(0, ListRow(header, itemAdapter))
				}

				is ApiResult.Error -> {
					// Handle the error here
					Log.e(TAG, "Error fetching recent albums: ${result.exception}")
				}
			}
		}

		lifecycleScope.launch {
			val apiClient = MetadataClient()
			when (val result = apiClient.fetchNewAlbums()) {

				is ApiResult.Success -> {
					var itemAdapter = ArrayObjectAdapter(cardPresenter)
					result.data.forEach { album ->
						itemAdapter.add(album)
					}
					val header = HeaderItem(ROWS_TITLE[1])
					rowsAdapter.replace(1, ListRow(header, itemAdapter))
				}

				is ApiResult.Error -> {
					// Handle the error here
					Log.e(TAG, "Error fetching recent albums: ${result.exception}")
				}
			}
		}


		lifecycleScope.launch {
			val apiClient = MetadataClient()
			when (val result = apiClient.fetchRecentAlbums()) {

				is ApiResult.Success -> {
					var itemAdapter = ArrayObjectAdapter(cardPresenter)
					result.data.forEach { album ->
						itemAdapter.add(album)
					}
					val header = HeaderItem(ROWS_TITLE[2])
					rowsAdapter.replace(2, ListRow(header, itemAdapter))
				}

				is ApiResult.Error -> {
					// Handle the error here
					Log.e(TAG, "Error fetching recent albums: ${result.exception}")
				}
			}
		}

		lifecycleScope.launch {
			val apiClient = MetadataClient()
			when (val result = apiClient.fetchRecommendedAlbums()) {

				is ApiResult.Success -> {
					var itemAdapter = ArrayObjectAdapter(cardPresenter)
					result.data.forEach { album ->
						itemAdapter.add(album)
					}
					val header = HeaderItem(ROWS_TITLE[3])
					rowsAdapter.replace(3, ListRow(header, itemAdapter))
				}

				is ApiResult.Error -> {
					// Handle the error here
					Log.e(TAG, "Error fetching recommended albums: ${result.exception}")
				}
			}
		}

		lifecycleScope.launch {
			val apiClient = MetadataClient()
			when (val result = apiClient.fetchMixes()) {

				is ApiResult.Success -> {
					var itemAdapter = ArrayObjectAdapter(cardPresenter)
					result.data.forEach { mix ->
						if (mix.type == "DISCOVERY_MIX") {
							itemAdapter.add(0, mix)
						} else if (mix.type == "NEW_RELEASE_MIX") {
							itemAdapter.add(1, mix)
						} else {
							itemAdapter.add(mix)
						}
					}
					val header = HeaderItem(ROWS_TITLE[4])
					rowsAdapter.replace(4, ListRow(header, itemAdapter))
				}

				is ApiResult.Error -> {
					// Handle the error here
					Log.e(TAG, "Error fetching recommended albums: ${result.exception}")
				}
			}
		}


		adapter = rowsAdapter
	}

	private fun setupEventListeners() {
		setOnSearchClickedListener {
			Toast.makeText(context!!, "Implement your own in-app search", Toast.LENGTH_LONG)
				.show()
		}

		onItemViewClickedListener = ItemViewClickedListener()
		onItemViewSelectedListener = ItemViewSelectedListener()
	}

	private inner class ItemViewClickedListener : OnItemViewClickedListener {
		override fun onItemClicked(
			itemViewHolder: Presenter.ViewHolder,
			item: Any,
			rowViewHolder: RowPresenter.ViewHolder,
			row: Row
		) {

			if (item is Collection) {
				Log.d(TAG, "Item: " + item.toString())
				val intent = Intent(context!!, DetailsActivity::class.java)
				intent.putExtra(DetailsActivity.COLLECTION, item)

				val bundle = ActivityOptionsCompat.makeSceneTransitionAnimation(
					activity!!,
					(itemViewHolder.view as ImageCardView).mainImageView,
					DetailsActivity.SHARED_ELEMENT_NAME
				)
					.toBundle()
				startActivity(intent, bundle)
			} else if (item is String) {
				if (item.contains(getString(R.string.error_fragment))) {
					val intent = Intent(context!!, BrowseErrorActivity::class.java)
					startActivity(intent)
				} else {
					Toast.makeText(context!!, item, Toast.LENGTH_SHORT).show()
				}
			}
		}
	}

	private inner class ItemViewSelectedListener : OnItemViewSelectedListener {
		override fun onItemSelected(
			itemViewHolder: Presenter.ViewHolder?, item: Any?,
			rowViewHolder: RowPresenter.ViewHolder, row: Row
		) {
			if (item is Album) {
				mBackgroundUri = item.imageUrl()
				//startBackgroundTimer()
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

	private inner class GridItemPresenter : Presenter() {
		override fun onCreateViewHolder(parent: ViewGroup): Presenter.ViewHolder {
			val view = TextView(parent.context)
			view.layoutParams = ViewGroup.LayoutParams(GRID_ITEM_WIDTH, GRID_ITEM_HEIGHT)
			view.isFocusable = true
			view.isFocusableInTouchMode = true
			view.setBackgroundColor(ContextCompat.getColor(context!!, R.color.default_background))
			view.setTextColor(Color.WHITE)
			view.gravity = Gravity.CENTER
			return Presenter.ViewHolder(view)
		}

		override fun onBindViewHolder(viewHolder: Presenter.ViewHolder, item: Any) {
			(viewHolder.view as TextView).text = item as String
		}

		override fun onUnbindViewHolder(viewHolder: Presenter.ViewHolder) {}
	}

	companion object {
		private val TAG = "MainFragment"

		private val BACKGROUND_UPDATE_DELAY = 300
		private val GRID_ITEM_WIDTH = 200
		private val GRID_ITEM_HEIGHT = 200
		private val NUM_ROWS = 5
		private val NUM_COLS = 15
		private val ROWS_TITLE = arrayOf(
			"Shortcuts",
			"Suggested new albums for you",
			"Recently played",
			"Albums you'll enjoy",
			"Custom mixes"
		)
	}
}