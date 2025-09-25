// ui/models/PresentationRadioResponse.kt
data class PresentationRadioStation(
    val id: Int,
    val name: String,
    val streamUrl: String,
    val description: String,
    val country: String,
    val language: String,
    val logoUrl: String?,
    val isLive: Boolean,
    val isPlaying: Boolean = false
)