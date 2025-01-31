package fr.bonamy.tidalstreamer.playback

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.ViewGroup
import fr.bonamy.tidalstreamer.R

class MiniPlaybackFragment : PlaybackFragmentBase() {

  override fun onCreateView(
    inflater: LayoutInflater, container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    val v = createView(inflater, container, R.layout.fragment_mini_playback) ?: return null
    v.visibility = GONE
    return v
  }

  override fun showSelf() {
    view?.visibility = View.VISIBLE
  }

  override fun hideSelf() {
    view?.visibility = GONE
  }

}