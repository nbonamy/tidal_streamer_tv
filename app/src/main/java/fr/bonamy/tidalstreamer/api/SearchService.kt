package fr.bonamy.tidalstreamer.api

import fr.bonamy.tidalstreamer.models.Album
import fr.bonamy.tidalstreamer.models.Artist
import fr.bonamy.tidalstreamer.models.Track
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface SearchService {

	@GET("search/album")
	suspend fun searchAlbums(@Query("query") query: String): Response<ApiResponsePage<Album>>

	@GET("search/track")
	suspend fun searchTracks(@Query("query") query: String): Response<ApiResponsePage<Track>>

	@GET("search/artist")
	suspend fun searchArtists(@Query("query") query: String): Response<ApiResponsePage<Artist>>

}
