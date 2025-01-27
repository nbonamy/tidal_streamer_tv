package fr.bonamy.tidalstreamer.models

data class Mix(
	var id: String? = null,
	var type: String? = null,
	var title: String? = null,
	var subTitle: String? = null,
	var thumbnail: String? = null,
) : Collection() {

	override fun title(): String {
		return title ?: ""
	}

	override fun subtitle(): String {
		return subTitle ?: ""
	}

	override fun imageUrl(): String {
		return thumbnail ?: ""
	}

	override fun toString(): String {
		return "Album{" +
						"id=" + id +
						", type='" + type + '\'' +
						", title='" + title + '\'' +
						", subTitle='" + subTitle + '\'' +
						", thumbnail='" + thumbnail + '\'' +
						'}'
	}

	companion object {
		internal const val serialVersionUID = 72755453695660653L
	}
}
