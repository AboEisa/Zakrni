// ui/models/PresentationRecitersResponse.kt
data class PresentationReciter(
    val id: Int,
    val name: String,
    val nameAr: String,
    val style: String,
    val photoUrl: String?,
    val server: String,
    val rewaya: String,
    val surahCount: String,
    val availableSurahs: List<Int>
)