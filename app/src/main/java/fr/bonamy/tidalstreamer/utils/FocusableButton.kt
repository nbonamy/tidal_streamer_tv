package fr.bonamy.tidalstreamer.utils

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.TextView
import fr.bonamy.tidalstreamer.R

class FocusableButton : TextView {

  constructor(context: Context) : super(context) {
    init()
  }

  constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
    init()
  }

  constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
    init()
  }

  private fun init() {

    this.focusable = FOCUSABLE
    this.background = context.getDrawable(R.drawable.button_rounded_corners)
    this.setOnFocusChangeListener(View.OnFocusChangeListener { _, hasFocus ->
      if (hasFocus) {
        // run scale animation and make it bigger
        val anim = AnimationUtils.loadAnimation(context, R.anim.scale_in)
        this.startAnimation(anim)
        anim.fillAfter = true
      } else {
        // run scale animation and make it smaller
        val anim = AnimationUtils.loadAnimation(context, R.anim.scale_out)
        this.startAnimation(anim)
        anim.fillAfter = true
      }
    })
  }
}