package fr.bonamy.tidalstreamer

import android.os.Bundle
import androidx.fragment.app.FragmentActivity

/**
 * Details activity class that loads [DetailsFragment] class.
 */
class DetailsActivity : FragmentActivity() {

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_details)
		if (savedInstanceState == null) {
			supportFragmentManager.beginTransaction()
				.replace(R.id.details_fragment, DetailsFragment())
				.commitNow()
		}
	}

	companion object {
		const val SHARED_ELEMENT_NAME = "hero"
		const val COLLECTION = "Collection"
	}
}