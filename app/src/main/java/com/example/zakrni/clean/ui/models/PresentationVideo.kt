// ui/models/PresentationVideosResponse.kt
data class PresentationVideo(
    val id: Int,
    val title: String,
    val description: String,
    val videoUrl: String,
    val thumbnailUrl: String,
    val duration: String,
    val views: String,
    val channelName: String,
    val publishedDate: String,
    val category: String,
    val formattedDate: String
)