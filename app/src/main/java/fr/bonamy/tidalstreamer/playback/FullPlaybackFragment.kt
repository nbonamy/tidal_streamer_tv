package fr.bonamy.tidalstreamer.playback

import android.animation.ObjectAnimator
import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.ScrollView
import android.widget.TextView
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import fr.bonamy.tidalstreamer.R
import fr.bonamy.tidalstreamer.api.ApiResult
import fr.bonamy.tidalstreamer.api.MetadataClient
import fr.bonamy.tidalstreamer.models.Lyrics
import fr.bonamy.tidalstreamer.models.STATE_PLAYING
import fr.bonamy.tidalstreamer.models.Status
import kotlinx.coroutines.launch
import java.util.Timer
import java.util.TimerTask
import java.util.regex.Pattern
import kotlin.math.abs

class FullPlaybackFragment(private var mLayout: PlaybackLayout) : PlaybackFragmentBase() {

  class LyricsLine internal constructor(val mPosition: Int, val mWords: String)

  private var mStatus: Status? = null
  private var mLyrics: Lyrics? = null
  private var mLyricsLines = mutableListOf<LyricsLine>()
  private var mLyricsView: ViewGroup? = null
  private val mLinesViews = mutableMapOf<LyricsLine, TextView>()
  private var mCurrentLine: LyricsLine? = null
  private var mScrollingLocked = false
  private var mSyncedLyrics = false
  private var mUnlockScrollTimer: Timer? = null

  private val viewModel: PlaybackKeyEventViewModel by activityViewModels()

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

    // key events coming from activity
    observeKeyEventChanges()

    // done
    v.visibility = INVISIBLE
    return v
  }

  override fun showSelf() {
    requireView().visibility = VISIBLE
  }

  override fun hideSelf() {
    requireActivity().finish()
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
    val track = super.getTrack(status) ?: return result

    // load lyrics
    if (result == StatusProcessResult.NEW_TRACK && mLyricsView != null) {
      lifecycleScope.launch {
        val metadataClient = MetadataClient()
        when (val lyrics = metadataClient.fetchTrackLyrics(track.id!!)) {
          is ApiResult.Success -> {
            updateLyrics(lyrics.data)
            syncLyrics(status, true)
          }

          is ApiResult.Error -> {
            updateLyrics(null)
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
    syncLyrics(status, result == StatusProcessResult.NEW_TRACK)

    // done
    return result

  }

  private fun observeKeyEventChanges() {
    viewModel.keyEvent.observe(viewLifecycleOwner) {
      if (it == KeyEvent.KEYCODE_DPAD_UP || it == KeyEvent.KEYCODE_DPAD_DOWN) {
        onManualScroll()
      }
    }
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
            requireActivity().runOnUiThread { syncLyrics(mStatus!!, true) }
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

  fun syncLyrics(status: Status, forceScroll: Boolean) {

    // no sync
    if (!mSyncedLyrics) {
      if (mCurrentLine != null) {
        setLyricsLine(mLinesViews[mCurrentLine], false)
        mCurrentLine = null
      }
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
          scrollView.smoothScrollTo(0, view.top - scrollView.height / 4)
        }
      }
    }

    // store
    mCurrentLine = activeLine
  }

  private fun setLyricsLine(view: TextView?, current: Boolean) {
    if (view == null) return
    view.alpha = if (current) 1f else 0.5f
  }

  companion object {
    private val timestampPattern = Pattern.compile("^\\[(\\d\\d):(\\d\\d).(\\d\\d)]")
    private const val TIMESTAMP_LENGTH = 10
    private const val SYNCED_EMPTY_LINE = "●  ●  ●"
    private const val UNLOCK_SCROLL_DELAY = 3000L
  }

}