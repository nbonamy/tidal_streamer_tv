package fr.bonamy.tidalstreamer.utils

import fr.bonamy.tidalstreamer.api.ApiResult

data class RowDefinition(
  val title: String,
  val fetcher: suspend () -> ApiResult<List<*>>,
  val flags: PresenterFlags = PresenterFlags.NONE
)
