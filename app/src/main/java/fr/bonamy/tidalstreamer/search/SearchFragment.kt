package fr.bonamy.tidalstreamer.search

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognizerIntent
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityOptionsCompat
import androidx.leanback.app.SearchSupportFragment
import androidx.leanback.widget.ArrayObjectAdapter
import androidx.leanback.widget.HeaderItem
import androidx.leanback.widget.ImageCardView
import androidx.leanback.widget.ListRow
import androidx.leanback.widget.ListRowPresenter
import androidx.leanback.widget.ObjectAdapter
import androidx.leanback.widget.OnItemViewClickedListener
import androidx.leanback.widget.Presenter
import androidx.leanback.widget.Row
import androidx.leanback.widget.RowPresenter
import androidx.lifecycle.lifecycleScope
import fr.bonamy.tidalstreamer.api.ApiResult
import fr.bonamy.tidalstreamer.api.SearchClient
import fr.bonamy.tidalstreamer.api.StreamingClient
import fr.bonamy.tidalstreamer.artist.ArtistCardPresenter
import fr.bonamy.tidalstreamer.collection.CollectionActivity
import fr.bonamy.tidalstreamer.collection.CollectionCardPresenter
import fr.bonamy.tidalstreamer.models.Album
import fr.bonamy.tidalstreamer.models.Artist
import fr.bonamy.tidalstreamer.models.Track
import kotlinx.coroutines.launch


class SearchFragment : SearchSupportFragment(), SearchSupportFragment.SearchResultProvider, TrackCardClickListener {

	private lateinit var mRowsAdapter: ArrayObjectAdapter
	private var mLastQuery: String = ""
	private val mHandler = Handler(Looper.getMainLooper())
	private var mSearchRunnable: Runnable? = null

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		mRowsAdapter = ArrayObjectAdapter(ListRowPresenter())
		setSearchResultProvider(this)
		setOnItemViewClickedListener(ItemViewClickedListener())

//		if (!AndroidUtils.hasPermission(context!!, Manifest.permission.RECORD_AUDIO)) {
//			// SpeechRecognitionCallback is not required and if not provided recognition will be handled
//			// using internal speech recognizer, in which case you must have RECORD_AUDIO permission
//			setSpeechRecognitionCallback {
//				Log.v(TAG, "recognizeSpeech")
//				try {
//					startActivityForResult(recognizerIntent, REQUEST_SPEECH)
//				} catch (e: ActivityNotFoundException) {
//					Log.e(TAG, "Cannot find activity for speech recognizer", e)
//				}
//			}
//		}
	}

	override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
		super.onActivityResult(requestCode, resultCode, data)
		if (requestCode == REQUEST_SPEECH && resultCode == Activity.RESULT_OK) {
			val results = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
			val spokenText = results?.get(0) ?: ""
			// Handle the spoken text
			Log.d(TAG, "Spoken text: $spokenText")
		}
	}

	override fun getResultsAdapter(): ObjectAdapter {
		Log.d(TAG, "getResultsAdapter")
		Log.d(TAG, mRowsAdapter.toString())
		return mRowsAdapter
	}

	override fun onQueryTextChange(newQuery: String?): Boolean {
		mSearchRunnable?.let { mHandler.removeCallbacks(it) }
		mSearchRunnable = Runnable { query(newQuery!!) }
		mHandler.postDelayed(mSearchRunnable!!, SEARCH_TEMPO)
		return true
	}

	override fun onQueryTextSubmit(query: String?): Boolean {
		query(query!!)
		return true
	}

	private fun query(query: String) {
		if (query == mLastQuery) {
			return
		}

		if (query.length < 2) {
			return
		}

		val apiClient = SearchClient()

		mRowsAdapter.clear()
		mRowsAdapter.add(ListRow(ArrayObjectAdapter(ListRowPresenter())))
		mRowsAdapter.add(ListRow(ArrayObjectAdapter(ListRowPresenter())))
		mRowsAdapter.add(ListRow(ArrayObjectAdapter(ListRowPresenter())))

		lifecycleScope.launch {
			when (val result = apiClient.searchAlbums(query)) {
				is ApiResult.Success -> {
					val listRowAdapter = ArrayObjectAdapter(CollectionCardPresenter())
					listRowAdapter.addAll(0, result.data)
					val header = HeaderItem("Albums")
					mRowsAdapter.replace(0, ListRow(header, listRowAdapter))
					mRowsAdapter.notifyArrayItemRangeChanged(0, mRowsAdapter.size())
					mLastQuery = query
				}

				is ApiResult.Error -> {
					Log.e(TAG, "Error playing collection: ${result.exception}")
				}
			}
		}

		lifecycleScope.launch {
			when (val result = apiClient.searchArtists(query)) {
				is ApiResult.Success -> {
					val listRowAdapter = ArrayObjectAdapter(ArtistCardPresenter())
					listRowAdapter.addAll(0, result.data)
					val header = HeaderItem("Artists")
					mRowsAdapter.replace(1, ListRow(header, listRowAdapter))
					mRowsAdapter.notifyArrayItemRangeChanged(0, mRowsAdapter.size())
					mLastQuery = query
				}

				is ApiResult.Error -> {
					Log.e(TAG, "Error playing collection: ${result.exception}")
				}
			}
		}

		lifecycleScope.launch {
			when (val result = apiClient.searchTracks(query)) {
				is ApiResult.Success -> {
					val listRowAdapter = ArrayObjectAdapter(TrackCardPresenter(this@SearchFragment))
					listRowAdapter.addAll(0, result.data)
					val header = HeaderItem("Tracks")
					mRowsAdapter.replace(2, ListRow(header, listRowAdapter))
					mRowsAdapter.notifyArrayItemRangeChanged(0, mRowsAdapter.size())
					mLastQuery = query
				}

				is ApiResult.Error -> {
					Log.e(TAG, "Error playing collection: ${result.exception}")
				}
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

	override fun onTrackLongClicked(track: Track, cardView: ImageCardView) {
		val intent = Intent(context!!, CollectionActivity::class.java)
		intent.putExtra(CollectionActivity.COLLECTION, track.album)
		val bundle = ActivityOptionsCompat.makeSceneTransitionAnimation(
			activity!!,
			cardView.mainImageView,
			CollectionActivity.SHARED_ELEMENT_NAME
		)
			.toBundle()
		startActivity(intent, bundle)
		return
	}

	companion object {
		private const val TAG = "SearchFragment"
		private const val REQUEST_SPEECH = 1
		private const val SEARCH_TEMPO = 500L
	}

}
