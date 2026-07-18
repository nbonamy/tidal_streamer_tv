package fr.bonamy.tidalstreamer.playback

import android.animation.ObjectAnimator
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.ScrollView
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import androidx.transition.TransitionInflater
import fr.bonamy.tidalstreamer.R
import fr.bonamy.tidalstreamer.api.ApiResult
import fr.bonamy.tidalstreamer.api.MetadataClient
import fr.bonamy.tidalstreamer.api.StreamerEventListener
import fr.bonamy.tidalstreamer.api.StreamerListener
import fr.bonamy.tidalstreamer.models.Lyrics
import fr.bonamy.tidalstreamer.models.STATE_PLAYING
import fr.bonamy.tidalstreamer.models.Status
import kotlinx.coroutines.launch
import java.util.Timer
import java.util.TimerTask
import java.util.regex.Pattern
import kotlin.math.abs

class FullPlaybackFragment(private var mLayout: PlaybackLayout, private var mStatus: Status?) : PlaybackFragmentBase(), StreamerEventListener {

  class LyricsLine internal constructor(val mPosition: Int, val mWords: String)

  private var mLyricsLines = mutableListOf<LyricsLine>()
  private var mLyricsView: ViewGroup? = null
  private var mLyricsLoadingView: ProgressBar? = null
  private val mLinesViews = mutableMapOf<LyricsLine, TextView>()
  private var mCurrentLine: LyricsLine? = null
  private var mScrollingLocked = false
  private var mSyncedLyrics = false
  private var mLyricsReady = false
  private var mLyricsRenderGeneration = 0
  private var mUnlockScrollTimer: Timer? = null
  private var nextTrackView: TextView? = null
  private var shortcutsView: View? = null
  private val infoHandler = Handler(Looper.getMainLooper())
  private var transientInfoVisibleUntil = 0L
  private var hasNextTrack = false
  private val hideTransientInfoTask = Runnable {
    updateTransientInfoVisibility(mStatus)
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    sharedElementEnterTransition = TransitionInflater.from(requireContext()).inflateTransition(R.transition.fragment_transition)
    sharedElementReturnTransition = TransitionInflater.from(requireContext()).inflateTransition(R.transition.fragment_transition)
  }

  override fun onCreateView(
    inflater: LayoutInflater, container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {

    // pick right layout
    val layoutId = when (mLayout) {
      PlaybackLayout.NO_LYRICS -> R.layout.fragment_full_playback_no_lyrics
      PlaybackLayout.LYRICS -> R.layout.fragment_full_playback_lyrics
    }
    val v = createView(inflater, container, layoutId) ?: return null
    mLyricsView = v.findViewById(R.id.lyrics)
    mLyricsLoadingView = v.findViewById(R.id.lyrics_loading)
    nextTrackView = v.findViewById(R.id.next_track)
    shortcutsView = v.findViewById(R.id.playback_shortcuts)
    revealTransientPlaybackInfo()

    // if we have a status
    if (mStatus != null) {
      processStatus(mStatus!!)
    }

    // done
    return v
  }

  override fun onResume() {
    super.onResume()
    StreamerListener.getInstance().addListener(this)
  }

  override fun onPause() {
    super.onPause()
    StreamerListener.getInstance().removeListener(this)
    infoHandler.removeCallbacks(hideTransientInfoTask)
  }

  fun latestStatus(): Status? {
    return mStatus
  }

  override fun showSelf() {
  }

  override fun hideSelf() {
    requireActivity().finish()
  }

  fun revealTransientPlaybackInfo() {
    if (nextTrackView == null && shortcutsView == null) return

    transientInfoVisibleUntil = SystemClock.elapsedRealtime() + TRANSIENT_INFO_VISIBLE_DURATION
    updateTransientInfoVisibility(mStatus)
    scheduleTransientInfoHide()
  }

  override fun onStatus(status: Status) {
    cancelUpdate()
    try {
      viewLifecycleOwner.lifecycleScope.launch {
        processStatus(status)
      }
    } catch (e: Exception) {
      Log.e(TAG, "Failed to process status: $e")
    }
    scheduleUpdate()
  }

  override fun processStatus(status: Status): StatusProcessResult {

    // save
    mStatus = status

    // process status
    val result = super.processStatus(status)
    if (result == StatusProcessResult.NO_TRACK) {
      return result
    }

    // we need the track
    val track = status.currentTrack() ?: return result
    updateNextTrack(status)
    updateTransientInfoVisibility(status)

    // load lyrics
    if (result == StatusProcessResult.NEW_TRACK && mLyricsView != null) {
      val requestedTrackId = track.id ?: return result
      showLyricsLoading()

      // do we already have lyrics
      if (mLyrics != null && mLyrics!!.trackId == requestedTrackId) {
        updateLyrics(mLyrics)
        syncLyrics(status, true, false)
      } else {
        lifecycleScope.launch {
          val metadataClient = MetadataClient(requireContext())
          when (val lyrics = metadataClient.fetchTrackLyrics(requestedTrackId)) {
            is ApiResult.Success -> {
              if (mStatus?.currentTrack()?.id != requestedTrackId) return@launch
              updateLyrics(lyrics.data)
              syncLyrics(mStatus ?: status, true, false)
            }

            is ApiResult.Error -> {
              if (mStatus?.currentTrack()?.id == requestedTrackId) showLyricsUnavailable()
            }
          }
        }
      }
    }

    // progress
    val progressView = view?.findViewById<ProgressBar>(R.id.progress) ?: return result
    progressView.progressDrawable = resources.getDrawable(
      if (status.state == STATE_PLAYING) R.drawable.progress_rounded_corners else
        R.drawable.progress_disabled_rounded_corners, null
    )
    val progress = status.progress
    progressView.max = track.duration * 1000
    if (abs(progressView.progress - progress) > 3000) {
      progressView.progress = progress
    } else {
      val progressAnimator = ObjectAnimator.ofInt(progressView, "progress", progress)
      progressAnimator.duration = 1000L
      progressAnimator.start()
    }

    // sync lyrics
    if (result != StatusProcessResult.NEW_TRACK && mLyricsReady) {
      syncLyrics(status, false, true)
    }

    // done
    return result

  }

  private fun updateNextTrack(status: Status) {
    val view = nextTrackView ?: return
    val nextTrack = status.tracks?.getOrNull(status.position + 1)?.item
    if (nextTrack == null) {
      hasNextTrack = false
      view.visibility = INVISIBLE
      return
    }

    hasNextTrack = true
    val title = nextTrack.title.orEmpty()
    val artist = nextTrack.mainArtist()?.name.orEmpty()
    view.text = if (artist.isBlank()) {
      getString(R.string.playback_next_track_title_only, title)
    } else {
      getString(R.string.playback_next_track, title, artist)
    }
  }

  private fun updateTransientInfoVisibility(status: Status?) {
    val transientVisible = SystemClock.elapsedRealtime() < transientInfoVisibleUntil
    shortcutsView?.visibility = if (transientVisible) VISIBLE else INVISIBLE

    val track = status?.currentTrack()
    val durationMs = (track?.duration ?: 0) * 1000
    val progress = status?.progress ?: 0
    val inEdgeWindow = durationMs > 0 &&
      progress >= 0 &&
      (progress <= NEXT_TRACK_EDGE_WINDOW || durationMs - progress <= NEXT_TRACK_EDGE_WINDOW)

    nextTrackView?.visibility = if (hasNextTrack && (inEdgeWindow || transientVisible)) VISIBLE else INVISIBLE
  }

  private fun scheduleTransientInfoHide() {
    infoHandler.removeCallbacks(hideTransientInfoTask)
    val delay = (transientInfoVisibleUntil - SystemClock.elapsedRealtime()).coerceAtLeast(0L)
    infoHandler.postDelayed(hideTransientInfoTask, delay)
  }

  @Suppress("UNUSED_PARAMETER")
  fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {

    if (keyCode == KeyEvent.KEYCODE_DPAD_UP || keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
      onManualScroll()
      return true
    }

    return false
  }

  private fun onManualScroll() {
    mScrollingLocked = true
    mUnlockScrollTimer?.cancel()
    mUnlockScrollTimer = Timer()
    mUnlockScrollTimer!!.schedule(object : TimerTask() {
      override fun run() {
        mScrollingLocked = false
        if (mStatus != null) {
          try {
            requireActivity().runOnUiThread { syncLyrics(mStatus!!, true, true) }
          } catch (e: Exception) {
            //e.printStackTrace()
          }
        }
      }
    }, UNLOCK_SCROLL_DELAY)
  }

  private fun updateLyrics(lyrics: Lyrics?) {

    // save and reset
    mLyrics = lyrics
    mLyricsLines.clear()
    mSyncedLyrics = false
    mCurrentLine = null
    mLinesViews.clear()

    // do we have a view?
    mLyricsView!!.removeAllViews()

    // if no lyrics
    if (mLyrics == null) {
      return
    }

    // parse them
    parseLyrics()
    if (mLyricsLines.isEmpty()) {
      return
    }

    // create an item per line
    val inflater = LayoutInflater.from(requireContext())
    mLyricsLines.forEach { line ->
      val lineView = inflater.inflate(R.layout.item_lyrics_line, mLyricsView, false) as TextView
      lineView.text = line.mWords
      setLyricsLine(lineView, false)
      mLyricsView!!.addView(lineView)
      mLinesViews[line] = lineView
    }

  }

  private fun showLyricsLoading() {
    mLyricsRenderGeneration++
    mLyricsReady = false
    mSyncedLyrics = false
    mCurrentLine = null
    mLyricsLines.clear()
    mLinesViews.clear()
    mLyricsView?.removeAllViews()
    view?.findViewById<ScrollView>(R.id.scroll)?.apply {
      scrollTo(0, 0)
      visibility = INVISIBLE
    }
    mLyricsLoadingView?.visibility = VISIBLE
  }

  private fun showLyricsUnavailable() {
    showLyricsLoading()
    mLyricsLoadingView?.visibility = View.GONE
  }

  private fun parseLyrics() {

    // we need some text
    if (mLyrics == null) return
    val text = mLyrics!!.subtitles ?: mLyrics!!.lyrics ?: return
    val lines: List<String> = text.split("\n")

    // synced if all lines have a timestamp
    mSyncedLyrics = true
    for (line in lines) {
      if (!timestampPattern.matcher(line).find()) {
        mSyncedLyrics = false
        break
      }
    }

    // if not synced then process each line
    // and remove the timestamp if present
    if (!mSyncedLyrics) {
      for (immutableLine in lines) {
        var line = immutableLine
        val matcher = timestampPattern.matcher(line)
        if (matcher.find()) line = line.substring(TIMESTAMP_LENGTH).trim { it <= ' ' }
        this.mLyricsLines.add(LyricsLine(0, line))
      }
      return
    }

    // else we need to process
    for (line in lines) {
      val matcher = timestampPattern.matcher(line)
      matcher.find()
      val minutes = matcher.group(1)!!.toInt()
      val seconds = matcher.group(2)!!.toInt()
      val milliseconds = matcher.group(3)!!.toInt()
      val position = (minutes * 60 + seconds) * 1000 + milliseconds
      var words = line.substring(TIMESTAMP_LENGTH).trim { it <= ' ' }
      if (words.isEmpty()) words = SYNCED_EMPTY_LINE
      mLyricsLines.add(LyricsLine(position, words))
    }

    // add empty line
    if (this.mLyricsLines[0].mPosition > 0) {
      this.mLyricsLines.add(0, LyricsLine(0, SYNCED_EMPTY_LINE))
    }
  }

  fun syncLyrics(status: Status, forceScroll: Boolean, smoothScroll: Boolean = true) {

    // no sync
    if (!mSyncedLyrics) {
      if (mCurrentLine != null) {
        setLyricsLine(mLinesViews[mCurrentLine], false)
        mCurrentLine = null
      }
      revealLyrics(mLyricsRenderGeneration)
      return
    }

    // we need the scroll view
    val scrollView = view?.findViewById<ScrollView>(R.id.scroll) ?: return

    // find current line
    var activeLine: LyricsLine? = null
    for (line in mLyricsLines) {
      if (line.mPosition > status.progress) break
      activeLine = line
    }

    // switch
    if (activeLine != mCurrentLine) {
      setLyricsLine(mLinesViews[mCurrentLine], false)
      setLyricsLine(mLinesViews[activeLine], true)
    }

    // scroll
    if (activeLine != mCurrentLine || forceScroll) {
      if (!mScrollingLocked) {
        val view = mLinesViews[activeLine]
        if (view != null) {
          val renderGeneration = mLyricsRenderGeneration
          scrollView.post {
            if (renderGeneration != mLyricsRenderGeneration) return@post
            val targetY = (view.top - scrollView.height / 4).coerceAtLeast(0)
            if (smoothScroll) {
              scrollView.smoothScrollTo(0, targetY)
            } else {
              scrollView.scrollTo(0, targetY)
              revealLyrics(renderGeneration)
            }
          }
        }
      }
    }

    // done
    mCurrentLine = activeLine
  }

  private fun revealLyrics(renderGeneration: Int) {
    if (renderGeneration != mLyricsRenderGeneration) return
    mLyricsReady = true
    mLyricsLoadingView?.visibility = View.GONE
    view?.findViewById<ScrollView>(R.id.scroll)?.visibility = VISIBLE
  }

  private fun setLyricsLine(view: TextView?, current: Boolean) {
    if (view == null) return
    view.alpha = if (current) 1f else 0.5f
  }

  companion object {
    const val TAG = "FullPlaybackFragment"
    private var mLyrics: Lyrics? = null
    private val timestampPattern = Pattern.compile("^\\[(\\d\\d):(\\d\\d).(\\d\\d)]")
    private const val TIMESTAMP_LENGTH = 10
    private const val SYNCED_EMPTY_LINE = "●  ●  ●"
    private const val UNLOCK_SCROLL_DELAY = 3000L
    private const val TRANSIENT_INFO_VISIBLE_DURATION = 5000L
    private const val NEXT_TRACK_EDGE_WINDOW = 10000
  }

}
