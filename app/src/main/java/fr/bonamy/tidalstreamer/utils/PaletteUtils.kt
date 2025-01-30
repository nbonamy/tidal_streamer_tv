package fr.bonamy.tidalstreamer.utils

import android.graphics.Color
import androidx.palette.graphics.Palette

class PaletteUtils(private val mPalette: Palette) {

  fun getContentBgColor(): Int {
    val swatch = swatch()
    return swatch?.rgb ?: 0
  }

  fun getTitleBgColor(): Int {
    val swatch = swatch() ?: return 0

    val color = swatch.rgb
    if (color == 0) {
      return 0
    }

    val hsv = FloatArray(3)
    Color.colorToHSV(color, hsv)
    hsv[2] *= 0.7f // value component
    return Color.HSVToColor(hsv)

  }


  private fun swatch(): Palette.Swatch? {
    if (mPalette.darkVibrantSwatch != null) {
      return mPalette.darkVibrantSwatch
    }
    if (mPalette.darkMutedSwatch != null) {
      return mPalette.darkMutedSwatch
    }
    if (mPalette.vibrantSwatch != null) {
      return mPalette.vibrantSwatch
    }
    if (mPalette.mutedSwatch != null) {
      return mPalette.mutedSwatch
    }
    return null
  }

}