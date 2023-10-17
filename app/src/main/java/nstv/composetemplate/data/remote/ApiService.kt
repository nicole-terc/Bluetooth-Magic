package nstv.composetemplate.data.remote

import nstv.composetemplate.data.gif.GifResponse
import retrofit2.http.GET

interface ApiService {
    @GET("gifs/trending")
    suspend fun getTrendingGifs(): GifResponse
}