package fr.bonamy.tidalstreamer

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityOptionsCompat
import androidx.core.content.ContextCompat
import androidx.leanback.app.DetailsSupportFragment
import androidx.leanback.app.DetailsSupportFragmentBackgroundController
import androidx.leanback.widget.Action
import androidx.leanback.widget.ArrayObjectAdapter
import androidx.leanback.widget.ClassPresenterSelector
import androidx.leanback.widget.DetailsOverviewRow
import androidx.leanback.widget.FullWidthDetailsOverviewRowPresenter
import androidx.leanback.widget.FullWidthDetailsOverviewSharedElementHelper
import androidx.leanback.widget.ImageCardView
import androidx.leanback.widget.OnActionClickedListener
import androidx.leanback.widget.OnItemViewClickedListener
import androidx.leanback.widget.Presenter
import androidx.leanback.widget.Row
import androidx.leanback.widget.RowPresenter
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
import fr.bonamy.tidalstreamer.api.ApiResult
import fr.bonamy.tidalstreamer.api.StreamingClient
import fr.bonamy.tidalstreamer.models.Album
import fr.bonamy.tidalstreamer.models.Collection
import fr.bonamy.tidalstreamer.models.Mix
import kotlinx.coroutines.launch

/**
 * A wrapper fragment for leanback details screens.
 * It shows a detailed view of video and its metadata plus related videos.
 */
class CollectionDetailsFragment : DetailsSupportFragment() {

	private var mSelectedCollection: Collection? = null

	private lateinit var mDetailsBackground: DetailsSupportFragmentBackgroundController
	private lateinit var mPresenterSelector: ClassPresenterSelector
	private lateinit var mAdapter: ArrayObjectAdapter

	override fun onCreate(savedInstanceState: Bundle?) {
		Log.d(TAG, "onCreate DetailsFragment")
		super.onCreate(savedInstanceState)

		mDetailsBackground = DetailsSupportFragmentBackgroundController(this)

		mSelectedCollection = activity!!.intent.getSerializableExtra(DetailsActivity.COLLECTION) as Collection
		if (mSelectedCollection != null) {
			mPresenterSelector = ClassPresenterSelector()
			mAdapter = ArrayObjectAdapter(mPresenterSelector)
			setupDetailsOverviewRow()
			setupDetailsOverviewRowPresenter()
			setupRelatedAlbumListRow()
			adapter = mAdapter
			initializeBackground(mSelectedCollection)
			//onItemViewClickedListener = ItemViewClickedListener()
		} else {
			val intent = Intent(context!!, MainActivity::class.java)
			startActivity(intent)
		}
	}

	private fun initializeBackground(collection: Collection?) {
		mDetailsBackground.enableParallax()
		Glide.with(context!!)
			.asBitmap()
			.centerCrop()
			.error(R.drawable.default_background)
			.load(collection?.imageUrl())
			.into<SimpleTarget<Bitmap>>(object : SimpleTarget<Bitmap>() {
				override fun onResourceReady(
					bitmap: Bitmap,
					transition: Transition<in Bitmap>?
				) {
					mDetailsBackground.coverBitmap = bitmap
					mAdapter.notifyArrayItemRangeChanged(0, mAdapter.size())
				}
			})
	}

	private fun setupDetailsOverviewRow() {
		Log.d(TAG, "doInBackground: " + mSelectedCollection?.toString())
		val row = DetailsOverviewRow(mSelectedCollection)
		row.imageDrawable = ContextCompat.getDrawable(context!!, R.drawable.default_background)
		val width = convertDpToPixel(context!!, DETAIL_THUMB_WIDTH)
		val height = convertDpToPixel(context!!, DETAIL_THUMB_HEIGHT)
		Glide.with(context!!)
			.load(mSelectedCollection?.imageUrl())
			.centerCrop()
			.error(R.drawable.default_background)
			.into<SimpleTarget<Drawable>>(object : SimpleTarget<Drawable>(width, height) {
				override fun onResourceReady(
					drawable: Drawable,
					transition: Transition<in Drawable>?
				) {
					Log.d(TAG, "details overview card image url ready: " + drawable)
					row.imageDrawable = drawable
					mAdapter.notifyArrayItemRangeChanged(0, mAdapter.size())
				}
			})

		val actionAdapter = ArrayObjectAdapter()

		actionAdapter.add(
			Action(
				ACTION_PLAY_NOW,
				resources.getString(R.string.play_now),
			)
		)
		actionAdapter.add(
			Action(
				ACTION_PLAY_NEXT,
				resources.getString(R.string.play_next),
			)
		)
		actionAdapter.add(
			Action(
				ACTION_QUEUE,
				resources.getString(R.string.play_after),
			)
		)
		row.actionsAdapter = actionAdapter

		mAdapter.add(row)
	}

	private fun setupDetailsOverviewRowPresenter() {
		// Set detail background.
		val detailsPresenter = FullWidthDetailsOverviewRowPresenter(DetailsDescriptionPresenter())
		detailsPresenter.backgroundColor =
			ContextCompat.getColor(context!!, R.color.selected_background)

		// Hook up transition element.
		val sharedElementHelper = FullWidthDetailsOverviewSharedElementHelper()
		sharedElementHelper.setSharedElementEnterTransition(
			activity, DetailsActivity.SHARED_ELEMENT_NAME
		)
		detailsPresenter.setListener(sharedElementHelper)
		detailsPresenter.isParticipatingEntranceTransition = true

		detailsPresenter.onActionClickedListener = OnActionClickedListener { action ->

			if (action.id == ACTION_PLAY_NOW) {
				lifecycleScope.launch {
					val apiClient = StreamingClient()

					if (mSelectedCollection is Album) {
						when (val result = apiClient.playAlbum((mSelectedCollection as Album)!!.id!!)) {
							is ApiResult.Success -> {}
							is ApiResult.Error -> {
								Log.e(TAG, "Error playing albums: ${result.exception}")
							}
						}
					} else if (mSelectedCollection is Mix) {
						when (val result = apiClient.playMix((mSelectedCollection as Mix)!!.id!!)) {
							is ApiResult.Success -> {}
							is ApiResult.Error -> {
								Log.e(TAG, "Error playing collection: ${result.exception}")
							}
						}
					}
				}
				return@OnActionClickedListener
			}

//			if (action.id == ACTION_WATCH_TRAILER) {
//				val intent = Intent(context!!, PlaybackActivity::class.java)
//				intent.putExtra(DetailsActivity.MOVIE, mSelectedMovie)
//				startActivity(intent)
//			} else {
				Toast.makeText(context!!, action.toString(), Toast.LENGTH_SHORT).show()
//			}
		}
		mPresenterSelector.addClassPresenter(DetailsOverviewRow::class.java, detailsPresenter)
	}

	private fun setupRelatedAlbumListRow() {
//		val subcategories = arrayOf(getString(R.string.related_movies))
//		val list = MovieList.list
//
//		Collections.shuffle(list)
//		val listRowAdapter = ArrayObjectAdapter(CardPresenter())
//		for (j in 0 until NUM_COLS) {
//			listRowAdapter.add(list[j % 5])
//		}
//
//		val header = HeaderItem(0, subcategories[0])
//		mAdapter.add(ListRow(header, listRowAdapter))
//		mPresenterSelector.addClassPresenter(ListRow::class.java, ListRowPresenter())
	}

	private fun convertDpToPixel(context: Context, dp: Int): Int {
		val density = context.applicationContext.resources.displayMetrics.density
		return Math.round(dp.toFloat() * density)
	}

	private inner class ItemViewClickedListener : OnItemViewClickedListener {
		override fun onItemClicked(
			itemViewHolder: Presenter.ViewHolder?,
			item: Any?,
			rowViewHolder: RowPresenter.ViewHolder,
			row: Row
		) {
			if (item is Album) {
				Log.d(TAG, "Item: " + item.toString())
				val intent = Intent(context!!, DetailsActivity::class.java)
				intent.putExtra(resources.getString(R.string.collection), mSelectedCollection)

				val bundle =
					ActivityOptionsCompat.makeSceneTransitionAnimation(
						activity!!,
						(itemViewHolder?.view as ImageCardView).mainImageView,
						DetailsActivity.SHARED_ELEMENT_NAME
					)
						.toBundle()
				startActivity(intent, bundle)
			}
		}
	}

	companion object {
		private val TAG = "VideoDetailsFragment"

		private val ACTION_PLAY_NOW = 1L
		private val ACTION_PLAY_NEXT = 2L
		private val ACTION_QUEUE = 3L

		private val DETAIL_THUMB_WIDTH = 274
		private val DETAIL_THUMB_HEIGHT = 274

		private val NUM_COLS = 10
	}
}