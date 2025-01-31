package fr.bonamy.tidalstreamer.playback

import android.animation.ObjectAnimator
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import fr.bonamy.tidalstreamer.R
import fr.bonamy.tidalstreamer.models.Status

class FullPlaybackFragment : PlaybackFragmentBase() {

  private lateinit var progressView: ProgressBar

  override fun onCreateView(
    inflater: LayoutInflater, container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    val v = createView(inflater, container, R.layout.fragment_full_playback) ?: return null
    progressView = v.findViewById(R.id.progress)
    return v
  }

  override fun showSelf() {
    // nothing to do
  }

  override fun hideSelf() {
    requireActivity().finish()
  }

  override fun processStatus(status: Status): StatusProcessResult {

    val result = super.processStatus(status)
    if (result == StatusProcessResult.NO_TRACK) {
      return result
    }

    // progress
    val progress = status.progress
    val track = super.getTrack(status) ?: return result
    progressView.max = track.duration * 1000
    if (result == StatusProcessResult.NEW_TRACK) {
      progressView.progress = 0
    } else if (Math.abs(progressView.progress - progress) > 3000) {
      progressView.progress = progress
    } else {
      val progressAnimator = ObjectAnimator.ofInt(progressView, "progress", progress)
      progressAnimator.duration = 1000L
      progressAnimator.start()
    }

    // done
    return result

  }

}