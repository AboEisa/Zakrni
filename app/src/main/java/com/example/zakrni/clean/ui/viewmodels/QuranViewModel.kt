package com.example.zakrni.clean.ui.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.zakrni.clean.domain.IRepo
import com.example.zakrni.clean.domain.models.DomainAudioEdition
import com.example.zakrni.clean.domain.models.DomainAyah
import com.example.zakrni.clean.domain.models.DomainSurah
import com.example.zakrni.clean.domain.usecase.GetQuranUseCase
import com.example.zakrni.clean.ui.utils.AudioPlayerManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import javax.inject.Inject

@HiltViewModel
class QuranViewModel @Inject constructor(
    private val getQuranUseCase: GetQuranUseCase,
    private val repository: IRepo,
    val audioPlayerManager: AudioPlayerManager
) : ViewModel() {

    private val _surahs = MutableLiveData<List<DomainSurah>>()
    val surahs: LiveData<List<DomainSurah>> get() = _surahs

    private val _verses = MutableLiveData<List<DomainAyah>>()
    val verses: LiveData<List<DomainAyah>> get() = _verses

    private val _currentSurah = MutableLiveData<DomainSurah?>()
    val currentSurah: LiveData<DomainSurah?> get() = _currentSurah

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> get() = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> get() = _error

    // Audio-related properties
    private val _audioReciters = MutableLiveData<List<DomainAudioEdition>>()
    val audioReciters: LiveData<List<DomainAudioEdition>> = _audioReciters

    private val _selectedReciter = MutableLiveData<String>("ar.alafasy")
    val selectedReciter: LiveData<String> = _selectedReciter

    // Audio player state flows
    val isPlaying: StateFlow<Boolean> = audioPlayerManager.isPlaying
    val audioProgress: StateFlow<Float> = audioPlayerManager.currentProgress
    val currentPlayingSurah: StateFlow<Int> = audioPlayerManager.currentSurahNumber
    val isAudioLoading: StateFlow<Boolean> = audioPlayerManager.isLoading

    // Audio URL cache for all surahs
    private val audioUrlCache = mutableMapOf<String, String>()
    private val failedUrls = mutableSetOf<String>()

    // Track audio availability for each surah
    private val _audioAvailability = MutableLiveData<Map<Int, Boolean>>()
    val audioAvailability: LiveData<Map<Int, Boolean>> = _audioAvailability

    // Track if audio has been auto-played for a surah
    private val autoPlayedSurahs = mutableSetOf<Int>()

    init {
        loadAllSurahs()
        loadAudioReciters()
        initializeAudioAvailability()
    }

    // Initialize audio availability for all 114 surahs
    private fun initializeAudioAvailability() {
        val availability = (1..114).associateWith { true } // Assume all available initially
        _audioAvailability.value = availability
    }

    fun loadQuranVerses(surahNumber: Int) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null

                // Clear previous verses
                _verses.value = emptyList()

                val verses = withTimeoutOrNull(15_000L) {
                    getQuranUseCase.getQuranVerses(surahNumber)
                }

                if (verses == null) {
                    throw Exception("Request timed out after 15 seconds")
                }

                _verses.value = verses

                val cachedSurahs = _surahs.value
                val surah = cachedSurahs?.find { it.number == surahNumber }

                if (surah != null) {
                    val updatedSurah = surah.copy(ayahs = verses)
                    _currentSurah.value = updatedSurah
                } else {
                       val directSurah = withTimeoutOrNull(5_000L) {
                        getQuranUseCase.getSurahByNumber(surahNumber)
                    }
                    if (directSurah != null) {
                        _currentSurah.value = directSurah.copy(ayahs = verses)
                    }
                }

                if (verses.isEmpty()) {
                    _error.value = "No verses found for surah $surahNumber"
                }

            } catch (e: CancellationException) {
                _error.value = "Operation cancelled"
            } catch (e: Exception) {
                _error.value = e.message ?: "Unknown error occurred"
                _verses.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadAllSurahs() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null

                val surahsList = withTimeoutOrNull(15_000L) {
                    getQuranUseCase.getAllSurahs()
                }

                if (surahsList == null) {
                    throw Exception("Request timed out after 15 seconds")
                }

                _surahs.value = surahsList

                if (surahsList.isEmpty()) {
                    _error.value = "No surahs found"
                }
            } catch (e: CancellationException) {
                _error.value = "Operation cancelled"
            } catch (e: Exception) {
                _error.value = e.message ?: "Unknown error occurred"
                _surahs.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun loadAudioReciters(language: String = "ar") {
        viewModelScope.launch {
            try {
                val recitersResult = repository.getAudioEditions(language)
                if (recitersResult.isSuccess) {
                    val reciters = recitersResult.getOrNull()?.data ?: emptyList()
                    _audioReciters.value = reciters
                } else {
                    // Handle error silently
                }
            } catch (e: Exception) {
                // Handle error silently
            }
        }
    }

    // Main function to play audio for any surah (1-114)
    fun playSurahAudio(surahNumber: Int, autoPlay: Boolean = false) {
        if (surahNumber !in 1..114) {
            _error.value = "Invalid surah number: $surahNumber"
            return
        }

        // If we're already playing this surah and it's not auto-play, just resume
        if (isAudioPlayingForSurah(surahNumber) && !autoPlay) {
            resumeAudio()
            return
        }

        // Stop any currently playing audio first if it's a different surah
        if (currentPlayingSurah.value != surahNumber) {
            audioPlayerManager.stop()
        }

        val selectedReciterIdentifier = _selectedReciter.value ?: "ar.alafasy"
        val cacheKey = "${surahNumber}_${selectedReciterIdentifier}"

        // Check cache first
        audioUrlCache[cacheKey]?.let { cachedUrl ->
            if (!failedUrls.contains(cachedUrl)) {
                playAudioFromUrl(cachedUrl, surahNumber)
                return
            }
        }

        // Try multiple audio sources for the surah
        playAudioWithFallback(surahNumber)
    }

    private fun playAudioFromUrl(audioUrl: String, surahNumber: Int) {
        audioPlayerManager.playAudio(
            audioUrl = audioUrl,
            surahNumber = surahNumber,
            onCompletion = {
                // Remove from auto-played set when completed
                autoPlayedSurahs.remove(surahNumber)
            },
            onError = { error ->
                failedUrls.add(audioUrl)
                markSurahAudioUnavailable(surahNumber)

                // Remove from cache if it fails
                val selectedReciter = _selectedReciter.value ?: "ar.alafasy"
                val cacheKey = "${surahNumber}_${selectedReciter}"
                audioUrlCache.remove(cacheKey)

                // Try next URL
                playAudioWithFallback(surahNumber)
            }
        )
    }

    private fun playAudioWithFallback(surahNumber: Int) {
        val fallbackUrls = buildComprehensiveAudioUrls(surahNumber)
        val availableUrls = fallbackUrls.filter { !failedUrls.contains(it) }

        if (availableUrls.isEmpty()) {
            _error.value = "Audio not available for Surah $surahNumber. Please check your internet connection or try a different reciter."
            markSurahAudioUnavailable(surahNumber)
            return
        }

        tryUrls(availableUrls, surahNumber, 0)
    }

    // Comprehensive audio URL builder for all 114 surahs INCLUDING ABDUL SAMAD
    private fun buildComprehensiveAudioUrls(surahNumber: Int): List<String> {
        val formattedNumber = String.format("%03d", surahNumber)
        val selectedReciter = _selectedReciter.value ?: "ar.alafasy"

        return when (selectedReciter) {
            "ar.alafasy" -> listOf(
                "https://server8.mp3quran.net/afs/$formattedNumber.mp3",
                "https://download.quranicaudio.com/quran/mishaari_raashid_al_3afaasee/$formattedNumber.mp3",
                "https://ia903208.us.archive.org/13/items/AlafasyMurattal128kbps/$formattedNumber.mp3",
                "https://archive.org/download/AlAfasyComplete/$formattedNumber.mp3"
            )
            "ar.husary" -> listOf(
                "https://server6.mp3quran.net/husary/$formattedNumber.mp3",
                "https://download.quranicaudio.com/quran/mahmood_khaleel_al_husaree/$formattedNumber.mp3",
                "https://ia802609.us.archive.org/8/items/husarymurattalcomplete/$formattedNumber.mp3",
                "https://archive.org/download/HusaryComplete/$formattedNumber.mp3"
            )
            "ar.sudais" -> listOf(
                "https://server12.mp3quran.net/sud/$formattedNumber.mp3",
                "https://download.quranicaudio.com/quran/abdurrahmaan_as_sudays/$formattedNumber.mp3",
                "https://ia801005.us.archive.org/35/items/INDIRILIS/$formattedNumber.mp3",
                "https://archive.org/download/SudaisComplete/$formattedNumber.mp3"
            )
            "ar.ghamadi" -> listOf(
                "https://server11.mp3quran.net/sds/$formattedNumber.mp3",
                "https://download.quranicaudio.com/quran/sa3d_al_ghaamidi/$formattedNumber.mp3",
                "https://archive.org/download/GhamadiComplete/$formattedNumber.mp3"
            )
            "ar.minshawi" -> listOf(
                "https://server10.mp3quran.net/minsh/$formattedNumber.mp3",
                "https://download.quranicaudio.com/quran/mohamed_siddeeq_al-minshaawee/$formattedNumber.mp3"
            )
            "ar.tablawi" -> listOf(
                "https://server9.mp3quran.net/tblawi/$formattedNumber.mp3",
                "https://download.quranicaudio.com/quran/muhammad_jibreel/$formattedNumber.mp3"
            )
            "ar.abdulsamad" -> listOf(
                // Abdul Basit Abdul Samad URLs
                "https://server7.mp3quran.net/basit/$formattedNumber.mp3",
                "https://download.quranicaudio.com/quran/abdul_baasit_abdus_samad/$formattedNumber.mp3",
                "https://server13.mp3quran.net/basit_murattal/$formattedNumber.mp3",
                "https://ia902307.us.archive.org/8/items/abdulbasitabdulsamad/$formattedNumber.mp3",
                "https://archive.org/download/AbdulBasitComplete/$formattedNumber.mp3"
            )
            else -> listOf(
                // Default fallback to multiple reliable sources
                "https://server8.mp3quran.net/afs/$formattedNumber.mp3",
                "https://download.quranicaudio.com/quran/mishaari_raashid_al_3afaasee/$formattedNumber.mp3",
                "https://server11.mp3quran.net/sds/$formattedNumber.mp3",
                "https://server12.mp3quran.net/sud/$formattedNumber.mp3",
                "https://server6.mp3quran.net/husary/$formattedNumber.mp3",
                "https://server7.mp3quran.net/basit/$formattedNumber.mp3"
            )
        }
    }

    private fun tryUrls(urls: List<String>, surahNumber: Int, index: Int) {
        if (index >= urls.size) {
            _error.value = "Audio not available for Surah $surahNumber. Please check your internet connection or try a different reciter."
            markSurahAudioUnavailable(surahNumber)
            return
        }

        val url = urls[index]

        audioPlayerManager.playAudio(
            audioUrl = url,
            surahNumber = surahNumber,
            onCompletion = {
                // Cache successful URL
                val selectedReciter = _selectedReciter.value ?: "ar.alafasy"
                val cacheKey = "${surahNumber}_${selectedReciter}"
                audioUrlCache[cacheKey] = url
                markSurahAudioAvailable(surahNumber)
                // Remove from auto-played set when completed
                autoPlayedSurahs.remove(surahNumber)
            },
            onError = { error ->
                failedUrls.add(url)
                // Try next URL
                tryUrls(urls, surahNumber, index + 1)
            }
        )
    }

    // Mark surah audio as available/unavailable
    private fun markSurahAudioAvailable(surahNumber: Int) {
        val current = _audioAvailability.value?.toMutableMap() ?: mutableMapOf()
        current[surahNumber] = true
        _audioAvailability.value = current
    }

    private fun markSurahAudioUnavailable(surahNumber: Int) {
        val current = _audioAvailability.value?.toMutableMap() ?: mutableMapOf()
        current[surahNumber] = false
        _audioAvailability.value = current
    }

    // Get all available reciters INCLUDING ABDUL SAMAD
    fun getAvailableReciters(): List<Pair<String, String>> {
        return listOf(
            "ar.alafasy" to "Mishary Rashid Alafasy",
            "ar.husary" to "Mahmoud Khalil Al-Husary",
            "ar.sudais" to "Abdul Rahman Al-Sudais",
            "ar.ghamadi" to "Saad Al-Ghamadi",
            "ar.minshawi" to "Mohamed Al-Minshawi",
            "ar.tablawi" to "Muhammad Al-Tablawi",
            "ar.abdulsamad" to "Abdul Basit Abdul Samad"
        )
    }

    // Bulk audio preloading for popular surahs
    fun preloadPopularSurahs() {
        val popularSurahs = listOf(1, 2, 18, 36, 55, 67, 112, 113, 114)

        popularSurahs.forEach { surahNumber ->
            preloadSurahAudio(surahNumber)
        }
    }

    fun preloadSurahAudio(surahNumber: Int) {
        if (surahNumber !in 1..114) return

        val selectedReciter = _selectedReciter.value ?: "ar.alafasy"
        val cacheKey = "${surahNumber}_${selectedReciter}"

        // If not already cached, preload the first URL
        if (!audioUrlCache.containsKey(cacheKey)) {
            val urls = buildComprehensiveAudioUrls(surahNumber)
            if (urls.isNotEmpty()) {
                audioUrlCache[cacheKey] = urls.first()
            }
        }
    }

    // Check if audio is available for a specific surah
    fun isAudioAvailable(surahNumber: Int): Boolean {
        return _audioAvailability.value?.get(surahNumber) ?: true
    }

    // Get surah name by number
    fun getSurahName(surahNumber: Int): String? {
        return _surahs.value?.find { it.number == surahNumber }?.name
    }

    // Play random surah
    fun playRandomSurah() {
        val randomSurah = (1..114).random()
        playSurahAudio(randomSurah)
    }

    // Continue playing from where user left off
    fun continuePlaying() {
        val currentSurah = currentPlayingSurah.value
        if (currentSurah > 0 && !isPlaying.value) {
            resumeAudio()
        }
    }

    // Audio testing methods for fragment integration
    fun testAudioConnection() {
        // Test with known working URLs
        val testUrls = listOf(
            "https://server8.mp3quran.net/afs/001.mp3",
            "https://download.quranicaudio.com/quran/mishaari_raashid_al_3afaasee/001.mp3",
            "https://server7.mp3quran.net/basit/001.mp3"
        )

        tryUrls(testUrls, 1, 0)
    }

    // Get audio duration if available
    fun getAudioDuration(): Int {
        return audioPlayerManager.duration.value
    }

    // Seek to specific position in audio
    fun seekToPosition(position: Int) {
        audioPlayerManager.seekTo(position)
    }

    fun pauseAudio() {
        audioPlayerManager.pause()
    }

    fun resumeAudio() {
        audioPlayerManager.resume()
    }

    fun stopAudio() {
        audioPlayerManager.stop()
    }

    fun setReciter(reciterIdentifier: String) {
        val oldReciter = _selectedReciter.value
        _selectedReciter.value = reciterIdentifier

        // Clear cache when reciter changes to force reload with new reciter
        audioUrlCache.clear()
        failedUrls.clear()
        autoPlayedSurahs.clear()
        initializeAudioAvailability()
    }

    fun isPlayingSurah(surahNumber: Int): Boolean {
        return audioPlayerManager.isPlayingSurah(surahNumber)
    }

    fun isAudioPlayingForSurah(surahNumber: Int): Boolean {
        return isPlaying.value && currentPlayingSurah.value == surahNumber
    }

    fun hasAutoPlayedSurah(surahNumber: Int): Boolean {
        return autoPlayedSurahs.contains(surahNumber)
    }

    fun markSurahAsAutoPlayed(surahNumber: Int) {
        autoPlayedSurahs.add(surahNumber)
    }

    fun clearError() {
        _error.value = null
    }

    fun clearVerses() {
        _verses.value = emptyList()
        _currentSurah.value = null
    }

    fun clearAudioCache() {
        audioUrlCache.clear()
        failedUrls.clear()
        autoPlayedSurahs.clear()
        initializeAudioAvailability()
    }

    fun retryFailedAudio(surahNumber: Int) {
        val selectedReciter = _selectedReciter.value ?: "ar.alafasy"
        val cacheKey = "${surahNumber}_${selectedReciter}"

        // Remove from failed URLs and cache
        val urlsToRetry = failedUrls.filter { url ->
            url.contains(String.format("%03d", surahNumber))
        }
        failedUrls.removeAll(urlsToRetry.toSet())
        audioUrlCache.remove(cacheKey)

        playSurahAudio(surahNumber)
    }

    // Get audio system status for all surahs
    fun getAudioSystemStatus(): String {
        val availableCount = _audioAvailability.value?.values?.count { it } ?: 0
        val currentReciterName = getAvailableReciters().find { it.first == _selectedReciter.value }?.second ?: "Unknown"

        return """
            Audio System Status:
            Total Surahs: 114
            Available Audio: $availableCount
            Cache size: ${audioUrlCache.size}
            Failed URLs: ${failedUrls.size}
            Current reciter: $currentReciterName
            Is playing: ${isPlaying.value}
            Current surah: ${currentPlayingSurah.value}
            Is loading: ${isAudioLoading.value}
            Auto-played surahs: ${autoPlayedSurahs.size}
        """.trimIndent()
    }

    override fun onCleared() {
        super.onCleared()
        audioPlayerManager.release()
        audioUrlCache.clear()
        failedUrls.clear()
        autoPlayedSurahs.clear()
    }
}