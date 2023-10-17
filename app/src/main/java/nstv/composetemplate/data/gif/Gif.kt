package nstv.composetemplate.data.gif

import kotlinx.serialization.Serializable

@Serializable
data class GifResponse(
    val data: List<Gif>
)

@Serializable
data class Gif(
    val id: String,
    val title: String,
    val images: Images
)

@Serializable
data class Images(
    val original: Image,
    val downsized: Image? = null
)

@Serializable
data class Image(
    val url: String,
    val width: Int,
    val height: Int
)
