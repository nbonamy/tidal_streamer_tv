package fr.bonamy.tidalstreamer.queue

import android.animation.ObjectAnimator
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.TransitionInflater
import com.bumptech.glide.Glide
import fr.bonamy.tidalstreamer.R
import fr.bonamy.tidalstreamer.api.ApiResult
import fr.bonamy.tidalstreamer.api.StreamerEventListener
import fr.bonamy.tidalstreamer.api.StreamerListener
import fr.bonamy.tidalstreamer.api.StreamingClient
import fr.bonamy.tidalstreamer.models.STATE_PLAYING
import fr.bonamy.tidalstreamer.models.STATE_STOPPED
import fr.bonamy.tidalstreamer.models.Status
import fr.bonamy.tidalstreamer.models.StatusTrack
import fr.bonamy.tidalstreamer.models.Track
import fr.bonamy.tidalstreamer.playback.PlaybackFragmentBase
import fr.bonamy.tidalstreamer.utils.TvActionDialog
import fr.bonamy.tidalstreamer.utils.TvDialogAction
import kotlinx.coroutines.launch
import kotlin.math.abs

class QueueFragment(private var initialStatus: Status?) : PlaybackFragmentBase(), QueueAdapter.Listener, StreamerEventListener {

  private lateinit var streamingClient: StreamingClient
  private lateinit var adapter: QueueAdapter
  private lateinit var queueList: RecyclerView
  private lateinit var emptyView: TextView
  private lateinit var countView: TextView
  private lateinit var progressView: ProgressBar
  private lateinit var albumArtView: ImageView
  private lateinit var titleView: TextView
  private lateinit var artistView: TextView

  private val tracks = mutableListOf<StatusTrack>()
  private var currentPosition = -1
  private var focusedPosition = -1
  private var isReorderMode = false
  private var reorderOriginalPosition = RecyclerView.NO_POSITION
  private var reorderPosition = RecyclerView.NO_POSITION
  private var centerLongPressed = false
  private var suppressNextConfirmKeyUp = false
  private var currentTrackId: String? = null
  private var serverPlayingTrackId: String? = null
  private var pendingReorderPlayingTrackId: String? = null

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    sharedElementEnterTransition = TransitionInflater.from(requireContext()).inflateTransition(R.transition.fragment_transition)
    sharedElementReturnTransition = TransitionInflater.from(requireContext()).inflateTransition(R.transition.fragment_transition)
    streamingClient = StreamingClient(requireContext())
    adapter = QueueAdapter(this)
  }

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    val view = createView(inflater, container, R.layout.fragment_queue) ?: return null

    queueList = view.findViewById(R.id.queue_list)
    emptyView = view.findViewById(R.id.queue_empty)
    countView = view.findViewById(R.id.queue_label)
    progressView = view.findViewById(R.id.progress)
    albumArtView = view.findViewById(R.id.album_art)
    titleView = view.findViewById(R.id.title)
    artistView = view.findViewById(R.id.artist)

    queueList.layoutManager = LinearLayoutManager(requireContext())
    queueList.adapter = adapter

    initialStatus?.let {
      showStatus(it, focusCurrent = true, requestedFocusPosition = RecyclerView.NO_POSITION, scrollToFocus = true)
      initialStatus = null
    }

    return view
  }

  override fun onResume() {
    super.onResume()
    StreamerListener.getInstance().addListener(this)
  }

  override fun onPause() {
    super.onPause()
    StreamerListener.getInstance().removeListener(this)
  }

  override fun showSelf() {
  }

  override fun hideSelf() {
    showEmpty()
  }

  fun latestStatus(): Status? = StreamerListener.getInstance().status

  override fun onStatus(status: Status) {
    cancelUpdate()
    try {
      viewLifecycleOwner.lifecycleScope.launch {
        updateStatus(status)
      }
    } catch (e: Exception) {
      Log.e(TAG, "Failed to process status: $e")
    }
    scheduleUpdate()
  }

  override fun processStatus(status: Status): fr.bonamy.tidalstreamer.playback.StatusProcessResult {
    if (tracks.isEmpty()) {
      showStatus(status, focusCurrent = true, requestedFocusPosition = RecyclerView.NO_POSITION, scrollToFocus = true)
    } else {
      updateStatus(status)
    }
    return fr.bonamy.tidalstreamer.playback.StatusProcessResult.SAME_TRACK
  }

  fun dispatchKeyEvent(event: KeyEvent): Boolean {
    if (isReorderMode) {
      return when (event.action) {
        KeyEvent.ACTION_DOWN -> handleReorderKey(event.keyCode)
        KeyEvent.ACTION_UP -> isReorderKey(event.keyCode)
        else -> false
      }
    }

    if (isConfirmKey(event.keyCode)) {
      return when (event.action) {
        KeyEvent.ACTION_DOWN -> {
          if (event.repeatCount > 0 && !centerLongPressed) {
            centerLongPressed = true
            showFocusedQueueItemMenu()
          }
          true
        }

        KeyEvent.ACTION_UP -> {
          if (suppressNextConfirmKeyUp) {
            suppressNextConfirmKeyUp = false
            return true
          }

          if (centerLongPressed) {
            centerLongPressed = false
          } else {
            jumpToFocusedTrack()
          }
          true
        }

        else -> false
      }
    }

    return false
  }

  @Suppress("UNUSED_PARAMETER")
  fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
    if (isReorderMode) {
      return handleReorderKey(keyCode)
    }

    return when (keyCode) {
      KeyEvent.KEYCODE_DPAD_CENTER,
      KeyEvent.KEYCODE_ENTER -> {
        jumpToFocusedTrack()
        true
      }

      KeyEvent.KEYCODE_MEDIA_RECORD,
      KeyEvent.KEYCODE_F -> {
        removeFocusedTrack()
        true
      }

      else -> false
    }
  }

  override fun onQueueItemFocused(position: Int) {
    if (!isReorderMode && position != RecyclerView.NO_POSITION) {
      focusedPosition = position
    }
  }

  override fun onQueueItemClicked(position: Int) {
    if (isReorderMode) {
      confirmReorder()
      return
    }

    if (position != RecyclerView.NO_POSITION) {
      jumpToTrack(position)
    }
  }

  override fun onQueueItemLongClicked(position: Int) {
    if (isReorderMode) return

    if (position != RecyclerView.NO_POSITION) {
      showQueueItemMenu(position)
    }
  }

  private fun refreshStatus(
    focusCurrent: Boolean = false,
    focusPosition: Int = RecyclerView.NO_POSITION,
    scrollToFocus: Boolean = true
  ) {
    lifecycleScope.launch {
      when (val result = streamingClient.status()) {
        is ApiResult.Success -> showStatus(result.data, focusCurrent, focusPosition, scrollToFocus)
        is ApiResult.Error -> showEmpty()
      }
    }
  }

  private fun showStatus(
    status: Status,
    focusCurrent: Boolean,
    requestedFocusPosition: Int,
    scrollToFocus: Boolean
  ) {
    val queueTracks = status.tracks.orEmpty()
    if (status.state == STATE_STOPPED || queueTracks.isEmpty()) {
      clearReorderMode()
      showEmpty()
      return
    }

    clearReorderMode()
    tracks.clear()
    tracks.addAll(queueTracks)
    val currentTrack = applyServerPlayback(status, queueTracks)
    focusedPosition = focusedPosition.coerceIn(0, tracks.lastIndex)

    queueList.visibility = View.VISIBLE
    emptyView.visibility = View.GONE
    updateCurrentTrack(currentTrack, tracks.size, status.progress, status.state)

    if (requestedFocusPosition in tracks.indices) {
      focusedPosition = requestedFocusPosition
      adapter.submit(tracks.toList(), currentPosition, requestedFocusPosition)
      if (scrollToFocus) {
        focusQueuePosition(requestedFocusPosition)
      }
    } else if (focusCurrent && currentPosition in tracks.indices) {
      focusedPosition = currentPosition
      adapter.submit(tracks.toList(), currentPosition, currentPosition)
      if (scrollToFocus) {
        focusQueuePosition(currentPosition)
      }
    } else if (focusedPosition in tracks.indices) {
      adapter.submit(tracks.toList(), currentPosition, focusedPosition)
      if (scrollToFocus) {
        focusQueuePosition(focusedPosition)
      }
    } else {
      adapter.submit(tracks.toList(), currentPosition)
    }
  }

  private fun showEmpty() {
    tracks.clear()
    currentPosition = -1
    focusedPosition = -1
    serverPlayingTrackId = null
    pendingReorderPlayingTrackId = null
    adapter.submit(emptyList(), -1)
    queueList.visibility = View.GONE
    emptyView.visibility = View.VISIBLE
    updateCurrentTrack(null, 0)
  }

  private fun updateCurrentTrack(
    track: Track?,
    queueSize: Int,
    progress: Int? = null,
    state: String? = null
  ) {
    if (track == null) {
      albumArtView.setImageResource(R.drawable.album)
      titleView.text = getString(R.string.queue_empty_title)
      artistView.text = ""
      countView.text = getString(R.string.queue_label)
      progressView.progress = 0
      currentTrackId = null
      return
    }

    titleView.text = track.title
    artistView.text = track.mainArtist()?.name ?: ""
    countView.text = getString(
      R.string.queue_label_position_count,
      currentPosition + 1,
      queueSize
    )

    if (currentTrackId != track.id) {
      Glide.with(this@QueueFragment)
        .load(track.imageUrl())
        .centerCrop()
        .error(R.drawable.album)
        .into(albumArtView)
      currentTrackId = track.id
    }

    updateProgress(track, progress, state)
  }

  private fun updateStatus(status: Status) {
    val queueTracks = status.tracks.orEmpty()
    if (status.state == STATE_STOPPED || queueTracks.isEmpty()) {
      showEmpty()
      return
    }

    val previousPosition = currentPosition
    val previousPlayingTrackId = serverPlayingTrackId
    val currentTrack = applyServerPlayback(status, queueTracks, updateDraftList = !isReorderMode)
    if (!isReorderMode) {
      tracks.clear()
      tracks.addAll(queueTracks)
    } else {
      currentPosition = positionOfTrack(serverPlayingTrackId)
        .takeIf { it != RecyclerView.NO_POSITION }
        ?: currentPosition
    }

    updateCurrentTrack(currentTrack, queueTracks.size, status.progress, status.state)

    if (!isReorderMode && previousPosition != currentPosition) {
      adapter.submit(
        tracks.toList(),
        currentPosition,
        focusedPosition.takeIf { it in tracks.indices } ?: RecyclerView.NO_POSITION
      )
    } else if (isReorderMode && previousPlayingTrackId != serverPlayingTrackId) {
      adapter.submit(
        tracks.toList(),
        currentPosition,
        focusedPosition.takeIf { it in tracks.indices } ?: RecyclerView.NO_POSITION,
        reorderPosition
      )
    }
  }

  private fun updateProgress(track: Track, progress: Int?, state: String?) {
    progressView.progressDrawable = requireContext().getDrawable(
      if (state == STATE_PLAYING) R.drawable.progress_rounded_corners else
        R.drawable.progress_disabled_rounded_corners
    )
    progressView.max = track.duration * 1000

    val updatedProgress = progress ?: return
    if (abs(progressView.progress - updatedProgress) > 3000) {
      progressView.progress = updatedProgress
    } else {
      val progressAnimator = ObjectAnimator.ofInt(progressView, "progress", updatedProgress)
      progressAnimator.duration = 1000L
      progressAnimator.start()
    }
  }

  private fun jumpToFocusedTrack() {
    if (focusedPosition in tracks.indices) {
      jumpToTrack(focusedPosition)
    }
  }

  private fun jumpToTrack(position: Int) {
    pendingReorderPlayingTrackId = null
    lifecycleScope.launch {
      when (streamingClient.trackSeek(position)) {
        is ApiResult.Success -> {
          focusedPosition = position
          refreshStatus(focusPosition = position, scrollToFocus = false)
        }

        is ApiResult.Error -> showActionError()
      }
    }
  }

  private fun removeFocusedTrack() {
    removeTrack(focusedPosition)
  }

  private fun removeTrack(position: Int) {
    if (position !in tracks.indices) return
    clearReorderMode()
    pendingReorderPlayingTrackId = null
    val nextFocusPosition = position.coerceAtMost((tracks.size - 2).coerceAtLeast(0))

    lifecycleScope.launch {
      when (streamingClient.dequeue(position)) {
        is ApiResult.Success -> refreshStatus(focusPosition = nextFocusPosition)
        is ApiResult.Error -> showActionError()
      }
    }
  }

  private fun isConfirmKey(keyCode: Int): Boolean {
    return keyCode == KeyEvent.KEYCODE_DPAD_CENTER || keyCode == KeyEvent.KEYCODE_ENTER
  }

  private fun handleReorderKey(keyCode: Int): Boolean {
    return when (keyCode) {
      KeyEvent.KEYCODE_DPAD_UP -> {
        moveReorderItem(-1)
        true
      }

      KeyEvent.KEYCODE_DPAD_DOWN -> {
        moveReorderItem(1)
        true
      }

      KeyEvent.KEYCODE_DPAD_CENTER,
      KeyEvent.KEYCODE_ENTER -> {
        confirmReorder()
        true
      }

      KeyEvent.KEYCODE_BACK -> {
        cancelReorder()
        true
      }

      else -> true
    }
  }

  private fun isReorderKey(keyCode: Int): Boolean {
    return keyCode == KeyEvent.KEYCODE_DPAD_UP ||
      keyCode == KeyEvent.KEYCODE_DPAD_DOWN ||
      keyCode == KeyEvent.KEYCODE_DPAD_CENTER ||
      keyCode == KeyEvent.KEYCODE_ENTER ||
      keyCode == KeyEvent.KEYCODE_BACK
  }

  private fun showQueueItemMenu(position: Int) {
    if (position !in tracks.indices) return

    focusedPosition = position
    val menuItems = mutableListOf(getString(R.string.queue_reorder))
    menuItems.add(getString(R.string.queue_remove))

    val title = tracks[position].item?.title ?: getString(R.string.queue_unavailable_track)
    TvActionDialog.showActions(
      requireContext(),
      title,
      menuItems.map { menuItem ->
        TvDialogAction(menuItem) {
          when (menuItem) {
            getString(R.string.queue_reorder) -> startReorder(position)
            getString(R.string.queue_remove) -> removeTrack(position)
          }
        }
      }
    )
  }

  private fun showFocusedQueueItemMenu() {
    if (focusedPosition in tracks.indices) {
      showQueueItemMenu(focusedPosition)
    }
  }

  private fun startReorder(position: Int) {
    if (position !in tracks.indices) return

    isReorderMode = true
    reorderOriginalPosition = position
    reorderPosition = position
    focusedPosition = position
    adapter.submit(tracks.toList(), currentPosition, position, position)
    requestVisibleQueuePositionFocus(position)
  }

  private fun moveReorderItem(delta: Int) {
    val from = reorderPosition
    val to = from + delta
    if (from !in tracks.indices || to !in tracks.indices) return

    val moved = tracks.removeAt(from)
    tracks.add(to, moved)
    reorderPosition = to
    focusedPosition = to
    currentPosition = positionOfTrack(serverPlayingTrackId)
      .takeIf { it != RecyclerView.NO_POSITION }
      ?: currentPosition
    adapter.moveItem(from, to, tracks.toList(), currentPosition, to)
    keepReorderPositionVisible(to)
  }

  private fun positionOfTrack(trackId: String?): Int {
    if (trackId == null) return RecyclerView.NO_POSITION
    return tracks.indexOfFirst { it.item?.id == trackId }
  }

  private fun positionOfTrack(trackId: String?, queueTracks: List<StatusTrack>): Int {
    if (trackId == null) return RecyclerView.NO_POSITION
    return queueTracks.indexOfFirst { it.item?.id == trackId }
  }

  private fun trackById(trackId: String?, queueTracks: List<StatusTrack>): Track? {
    if (trackId == null) return null
    return queueTracks.firstOrNull { it.item?.id == trackId }?.item
  }

  private fun applyServerPlayback(
    status: Status,
    queueTracks: List<StatusTrack>,
    updateDraftList: Boolean = true
  ): Track? {
    val reportedPlayingTrackId = status.currentTrack()?.id
    val lockedPlayingTrackId = pendingReorderPlayingTrackId
    val playingTrackId = lockedPlayingTrackId ?: reportedPlayingTrackId
    val sourceTracks = if (updateDraftList) queueTracks else tracks

    if (lockedPlayingTrackId != null && reportedPlayingTrackId == lockedPlayingTrackId) {
      pendingReorderPlayingTrackId = null
    }

    serverPlayingTrackId = playingTrackId
    currentPosition = positionOfTrack(playingTrackId, sourceTracks)
      .takeIf { it != RecyclerView.NO_POSITION }
      ?: status.position

    return trackById(playingTrackId, sourceTracks) ?: status.currentTrack()
  }

  private fun confirmReorder() {
    if (!isReorderMode || reorderOriginalPosition !in tracks.indices || reorderPosition !in tracks.indices) {
      cancelReorder()
      return
    }

    suppressNextConfirmKeyUp = true
    val fromPosition = reorderOriginalPosition
    val toPosition = reorderPosition
    pendingReorderPlayingTrackId = serverPlayingTrackId
    clearReorderMode()
    currentPosition = positionOfTrack(serverPlayingTrackId)
      .takeIf { it != RecyclerView.NO_POSITION }
      ?: currentPosition

    if (fromPosition == toPosition) {
      adapter.submit(tracks.toList(), currentPosition, toPosition)
      requestVisibleQueuePositionFocus(toPosition)
      return
    }

    lifecycleScope.launch {
      var position = fromPosition
      val step = if (toPosition > fromPosition) 1 else -1
      var failed = false

      while (position != toPosition && !failed) {
        val nextPosition = position + step
        when (streamingClient.reorderQueue(position, nextPosition)) {
          is ApiResult.Success -> position = nextPosition
          is ApiResult.Error -> failed = true
        }
      }

      if (failed) {
        showActionError()
        refreshStatus(focusPosition = fromPosition)
      } else {
        refreshStatus(focusPosition = toPosition)
      }
    }
  }

  private fun cancelReorder() {
    val focusPosition = reorderOriginalPosition
    clearReorderMode()
    refreshStatus(focusPosition = focusPosition)
  }

  private fun clearReorderMode() {
    isReorderMode = false
    reorderOriginalPosition = RecyclerView.NO_POSITION
    reorderPosition = RecyclerView.NO_POSITION
  }

  private fun showActionError() {
    Toast.makeText(requireContext(), getString(R.string.queue_action_error), Toast.LENGTH_SHORT).show()
  }

  private fun focusQueuePosition(position: Int) {
    queueList.post {
      (queueList.layoutManager as? LinearLayoutManager)?.scrollToPositionWithOffset(position, queueList.height / 3)
      queueList.post {
        queueList.findViewHolderForAdapterPosition(position)?.itemView?.requestFocus()
      }
    }
  }

  private fun requestVisibleQueuePositionFocus(position: Int) {
    queueList.post {
      queueList.findViewHolderForAdapterPosition(position)?.itemView?.requestFocus()
    }
  }

  private fun keepReorderPositionVisible(position: Int) {
    queueList.post {
      val layoutManager = queueList.layoutManager as? LinearLayoutManager ?: return@post
      val firstVisible = layoutManager.findFirstCompletelyVisibleItemPosition()
      val lastVisible = layoutManager.findLastCompletelyVisibleItemPosition()

      if (firstVisible != RecyclerView.NO_POSITION && position in firstVisible..lastVisible) {
        queueList.findViewHolderForAdapterPosition(position)?.itemView?.requestFocus()
        return@post
      }

      queueList.smoothScrollToPosition(position)
      queueList.postDelayed({
        queueList.findViewHolderForAdapterPosition(position)?.itemView?.requestFocus()
      }, REORDER_SCROLL_FOCUS_DELAY)
    }
  }

  companion object {
    private const val TAG = "QueueFragment"
    private const val REORDER_SCROLL_FOCUS_DELAY = 180L
  }
}
