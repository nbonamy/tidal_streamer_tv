package fr.bonamy.tidalstreamer.artist

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.leanback.widget.SearchOrbView
import com.bumptech.glide.Glide
import fr.bonamy.tidalstreamer.R
import fr.bonamy.tidalstreamer.models.Artist
import fr.bonamy.tidalstreamer.search.SearchActivity
import fr.bonamy.tidalstreamer.utils.FocusableButton
import fr.bonamy.tidalstreamer.utils.ItemLongClickedListener


class ArtistTitleFragment : Fragment() {

  private lateinit var mArtist: Artist

  override fun onCreate(savedInstanceState: Bundle?) {
    mArtist =
      requireActivity().intent.getSerializableExtra(ArtistActivity.ARTIST) as Artist
    super.onCreate(savedInstanceState)
  }

  override fun onCreateView(
    inflater: LayoutInflater, container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    val view = inflater.inflate(R.layout.fragment_artist_title, container, false)

    // title
    val title = view.findViewById<TextView>(R.id.title)
    title.text = mArtist.name

    // description
    var description = view.findViewById<TextView>(R.id.description)
    description.text = mArtist.artistRoles?.map { it.category }?.joinToString(", ")

    // thumbnail
    val image = view.findViewById<ImageView>(R.id.image)
    Glide.with(requireContext())
      .load(mArtist.imageUrl())
      .into(image)

    // orb
    val orb = view.findViewById<SearchOrbView>(R.id.orb)
    orb.orbColors = SearchOrbView.Colors(ContextCompat.getColor(requireContext(), R.color.search_opaque))
    orb.setOnOrbClickedListener {
      val intent = Intent(requireContext(), SearchActivity::class.java)
      startActivity(intent)
    }

    // radio button
    val radio = view.findViewById<FocusableButton>(R.id.radio)
    radio.setOnClickListener {
      val longClickedListener = ItemLongClickedListener(requireActivity())
      longClickedListener.goToArtistRadio(mArtist, null)
    }

    // info button
    val info = view.findViewById<FocusableButton>(R.id.info)
    info.setOnClickListener {
      val longClickedListener = ItemLongClickedListener(requireActivity())
      longClickedListener.goToArtistInfo(mArtist)
    }


    // done
    return view
  }

}