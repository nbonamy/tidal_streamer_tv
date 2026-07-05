package fr.bonamy.tidalstreamer.models

import com.google.gson.JsonElement

data class HomeSection(
  var id: String? = null,
  var title: String? = null,
  var type: String? = null,
  var itemCount: Int = 0,
  var hasViewAll: Boolean = false,
)

data class HomeSectionItems(
  var id: String? = null,
  var title: String? = null,
  var type: String? = null,
  var items: List<HomeSectionItem>? = null,
)

data class HomeSectionItem(
  var itemType: String? = null,
  var data: JsonElement? = null,
)
