package fr.bonamy.tidalstreamer.search

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognizerIntent
import android.util.Log
import android.view.View
import androidx.core.content.ContextCompat
import androidx.leanback.app.BackgroundManager
import androidx.leanback.app.SearchSupportFragment
import androidx.leanback.widget.ArrayObjectAdapter
import androidx.leanback.widget.ClassPresenterSelector
import androidx.leanback.widget.HeaderItem
import androidx.leanback.widget.ListRow
import androidx.leanback.widget.ListRowPresenter
import androidx.leanback.widget.ObjectAdapter
import androidx.leanback.widget.SearchBar
import androidx.lifecycle.lifecycleScope
import fr.bonamy.tidalstreamer.R
import fr.bonamy.tidalstreamer.api.ApiResult
import fr.bonamy.tidalstreamer.api.SearchClient
import fr.bonamy.tidalstreamer.artist.ArtistCardPresenter
import fr.bonamy.tidalstreamer.collection.CollectionCardPresenter
import fr.bonamy.tidalstreamer.models.Artist
import fr.bonamy.tidalstreamer.models.Collection
import fr.bonamy.tidalstreamer.models.Track
import fr.bonamy.tidalstreamer.utils.Configuration
import fr.bonamy.tidalstreamer.utils.ItemClickedListener
import fr.bonamy.tidalstreamer.utils.ItemLongClickedListener
import fr.bonamy.tidalstreamer.utils.PresenterFlags
import kotlinx.coroutines.launch

enum class SearchState {
  RECENT_SEARCHES,
  SEARCH_RESULTS
}

class SearchFragment : SearchSupportFragment(), SearchSupportFragment.SearchResultProvider, OnRecentSearchClickListener {

  private lateinit var mConfiguration: Configuration
  private lateinit var mBackgroundManager: BackgroundManager
  private lateinit var mRowsAdapter: ArrayObjectAdapter
  private var mLastQuery: String = ""
  private val mHandler = Handler(Looper.getMainLooper())
  private var mSearchRunnable: Runnable? = null
  private var mState: SearchState = SearchState.RECENT_SEARCHES

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    prepareBackgroundManager()
    mConfiguration = Configuration(requireContext())
    mRowsAdapter = ArrayObjectAdapter(ListRowPresenter())
    setSearchResultProvider(this)

//		if (!AndroidUtils.hasPermission(requireContext(), Manifest.permission.RECORD_AUDIO)) {
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

    // handle recent search click and result item click
    val itemClickedListener = ItemClickedListener(requireActivity())
    setOnItemViewClickedListener { itemViewHolder, item, rowViewHolder, row ->
      if (item is RecentSearch) {
        query(item.query)
        focusResults()
      } else {
        itemClickedListener.onItemClicked(itemViewHolder, item, rowViewHolder, row)
      }
    }

  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    //this is overriding the default searchResultProvider, because of a bug in it
    view.findViewById<SearchBar>(androidx.leanback.R.id.lb_search_bar).setSearchBarListener(object : SearchBar.SearchBarListener {
      override fun onSearchQueryChange(query: String?) {
        onQueryTextChange(query)
        if (query.isNullOrEmpty()) {
          showRecentSearches()
        }
      }

      override fun onSearchQuerySubmit(query: String?) {
        onQueryTextSubmit(query)
      }

      override fun onKeyboardDismiss(query: String?) {
        mConfiguration.addRecentSearch(query)
        focusResults()
      }
    })

    showRecentSearches()
  }

  fun onBackPressed() {
    if (mState == SearchState.SEARCH_RESULTS) {
      showRecentSearches()
    } else {
      requireActivity().finish()
    }
  }

  private fun prepareBackgroundManager() {
    mBackgroundManager = BackgroundManager.getInstance(activity)
    mBackgroundManager.attach(requireActivity().window)
    mBackgroundManager.color = ContextCompat.getColor(requireContext(), R.color.default_background)
  }

  @Deprecated("OK Boomer")
  override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    super.onActivityResult(requestCode, resultCode, data)
    if (requestCode == REQUEST_SPEECH && resultCode == Activity.RESULT_OK) {
      val results = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
      val spokenText = results?.get(0) ?: ""
      // Handle the spoken text
      Log.d(TAG, "Spoken text: $spokenText")
    }
  }

  fun focusResults() {
    this@SearchFragment.mHandler.postDelayed({
      requireView().findViewById<View>(androidx.leanback.R.id.container_list).requestFocus()
    }, 200)
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
    if (query == null) return false
    mConfiguration.addRecentSearch(query)
    query(query)
    return true
  }

  private fun query(query: String) {
    if (query == mLastQuery) {
      return
    }

    if (query.length < 2) {
      return
    }

    val searchClient = SearchClient(requireContext())

    mRowsAdapter.clear()
    mRowsAdapter.add(ListRow(ArrayObjectAdapter(ListRowPresenter())))
    mRowsAdapter.add(ListRow(ArrayObjectAdapter(ListRowPresenter())))
    mRowsAdapter.add(ListRow(ArrayObjectAdapter(ListRowPresenter())))

    // Create the presenter selector
    val presenter = ClassPresenterSelector()
    val itemLongClickedListener = ItemLongClickedListener(requireActivity())
    presenter.addClassPresenter(Collection::class.java, CollectionCardPresenter(PresenterFlags.NONE, itemLongClickedListener))
    presenter.addClassPresenter(Artist::class.java, ArtistCardPresenter(PresenterFlags.NONE, itemLongClickedListener))
    presenter.addClassPresenter(Track::class.java, TrackCardPresenter(PresenterFlags.NONE, itemLongClickedListener))

    viewLifecycleOwner.lifecycleScope.launch {
      when (val result = searchClient.searchAlbums(query)) {
        is ApiResult.Success -> {
          val listRowAdapter = ArrayObjectAdapter(presenter)
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

    viewLifecycleOwner.lifecycleScope.launch {
      when (val result = searchClient.searchArtists(query)) {
        is ApiResult.Success -> {
          val listRowAdapter = ArrayObjectAdapter(presenter)
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

    viewLifecycleOwner.lifecycleScope.launch {
      when (val result = searchClient.searchTracks(query)) {
        is ApiResult.Success -> {
          val listRowAdapter = ArrayObjectAdapter(presenter)
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

    mState = SearchState.SEARCH_RESULTS

  }

  private fun showRecentSearches() {

    var chars = 0
    var firstRow = true
    mRowsAdapter.clear()
    val recentSearches = mConfiguration.loadRecentSearches()
    var listRowAdapter = ArrayObjectAdapter(RecentSearchPresenter(this))
    val header = HeaderItem("Recent Searches")

    for (search in recentSearches.reversed()) {

      val length = 20.coerceAtMost(search.length)
      if (chars + length > RECENT_SEARCH_ROW_SIZE) {
        if (mRowsAdapter.size() == 0) {
          mRowsAdapter.add(ListRow(header, listRowAdapter))
        } else {
          mRowsAdapter.add(ListRow(listRowAdapter))
        }

        // reset
        listRowAdapter = ArrayObjectAdapter(RecentSearchPresenter(this))
        firstRow = false
        chars = 0
      }

      // add it
      listRowAdapter.add(RecentSearch(search, firstRow))
      chars += length
    }

    if (listRowAdapter.size() > 0) {
      if (mRowsAdapter.size() == 0) {
        mRowsAdapter.add(ListRow(header, listRowAdapter))
      } else {
        mRowsAdapter.add(ListRow(listRowAdapter))
      }
    }

    mRowsAdapter.notifyArrayItemRangeChanged(0, mRowsAdapter.size())
    mState = SearchState.RECENT_SEARCHES

  }

  override fun onLongClick(recentSearch: RecentSearch) {
    mConfiguration.removeRecentSearch(recentSearch.query)
    showRecentSearches()
  }

  companion object {
    private const val TAG = "SearchFragment"
    private const val REQUEST_SPEECH = 1
    private const val SEARCH_TEMPO = 500L
    private const val RECENT_SEARCH_ROW_SIZE = 90
  }

}
