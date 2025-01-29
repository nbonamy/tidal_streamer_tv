package fr.bonamy.tidalstreamer.artist

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.leanback.widget.SearchOrbView
import androidx.leanback.widget.TitleViewAdapter
import fr.bonamy.tidalstreamer.R

class ArtistTitleView @JvmOverloads constructor(
	context: Context,
	attrs: AttributeSet? = null,
	defStyleAttr: Int = 0,
) : RelativeLayout(context, attrs, defStyleAttr), TitleViewAdapter.Provider {

	private var flags =  TitleViewAdapter.FULL_VIEW_VISIBLE
	private var mHasSearchListener = false
	private val mSearchOrbView: SearchOrbView
	private val mTextView: TextView
	private val mBadgeView: ImageView

	init {
		val inflater = LayoutInflater.from(context)
		val rootView: View = inflater.inflate(R.layout.artist_title_view, this)
		this.mBadgeView = rootView.findViewById<View>(R.id.title_badge) as ImageView
		this.mTextView = rootView.findViewById<View>(R.id.title_text) as TextView
		this.mSearchOrbView = rootView.findViewById<View>(R.id.title_orb) as SearchOrbView
		this.setClipToPadding(false)
		this.setClipChildren(false)
	}

	val mTitleViewAdapter = object : TitleViewAdapter() {
		override fun getSearchAffordanceView(): View {
			return this@ArtistTitleView.getSearchAffordanceView()
		}

		override fun setOnSearchClickedListener(listener: OnClickListener) {
			this@ArtistTitleView.setOnSearchClickedListener(listener)
		}

		override fun setAnimationEnabled(enable: Boolean) {
			this@ArtistTitleView.enableAnimation(enable)
		}

		override fun getBadgeDrawable(): Drawable {
			return this@ArtistTitleView.getBadgeDrawable()
		}

		override fun getSearchAffordanceColors(): SearchOrbView.Colors {
			return this@ArtistTitleView.getSearchAffordanceColors()
		}

		override fun getTitle(): CharSequence {
			return this@ArtistTitleView.getTitle()
		}

		override fun setBadgeDrawable(drawable: Drawable?) {
			this@ArtistTitleView.setBadgeDrawable(drawable)
		}

		override fun setSearchAffordanceColors(colors: SearchOrbView.Colors) {
			this@ArtistTitleView.setSearchAffordanceColors(colors)
		}

		override fun setTitle(titleText: CharSequence?) {
			this@ArtistTitleView.setTitle(titleText)
		}

		override fun updateComponentsVisibility(flags: Int) {
			this@ArtistTitleView.updateComponentsVisibility(flags)
		}
	}

	fun setTitle(titleText: CharSequence?) {
		this.mTextView.setText(titleText)
		updateBadgeVisibility()
	}

	fun getTitle(): CharSequence {
		return this.mTextView.getText()
	}

	fun setBadgeDrawable(drawable: Drawable?) {
		this.mBadgeView.setImageDrawable(drawable)
		updateBadgeVisibility()
	}

	fun getBadgeDrawable(): Drawable {
		return this.mBadgeView.getDrawable()
	}

	fun setOnSearchClickedListener(listener: OnClickListener?) {
		this.mHasSearchListener = listener != null
		this.mSearchOrbView.setOnOrbClickedListener(listener)
		updateSearchOrbViewVisiblity()
	}

	fun getSearchAffordanceView(): View {
		return this.mSearchOrbView
	}

	fun setSearchAffordanceColors(colors: SearchOrbView.Colors?) {
		this.mSearchOrbView.setOrbColors(colors)
	}

	fun getSearchAffordanceColors(): SearchOrbView.Colors {
		return this.mSearchOrbView.getOrbColors()
	}

	fun enableAnimation(enable: Boolean) {
		this.mSearchOrbView.enableOrbColorAnimation(enable && this.mSearchOrbView.hasFocus())
	}

	fun updateComponentsVisibility(flags: Int) {
		this.flags = flags
		if (flags and 2 == 2) {
			updateBadgeVisibility()
		} else {
			this.mBadgeView.setVisibility(GONE)
			this.mTextView.setVisibility(GONE)
		}
		updateSearchOrbViewVisiblity()
	}

	private fun updateSearchOrbViewVisiblity() {
		val visibility = if (this.mHasSearchListener && this.flags and 4 == 4) View.VISIBLE else View.INVISIBLE
		this.mSearchOrbView.setVisibility(visibility)
	}

	private fun updateBadgeVisibility() {
		val drawable: Drawable? = this.mBadgeView.getDrawable()
		if (drawable != null) {
			this.mBadgeView.setVisibility(VISIBLE)
			this.mTextView.setVisibility(VISIBLE)
		} else {
			this.mBadgeView.setVisibility(GONE)
			this.mTextView.setVisibility(VISIBLE)
		}
	}

	override fun getTitleViewAdapter(): TitleViewAdapter = mTitleViewAdapter

}