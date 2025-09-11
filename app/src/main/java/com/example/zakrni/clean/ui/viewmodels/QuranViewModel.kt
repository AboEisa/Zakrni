package com.example.zakrni.clean.ui.viewmodels

import android.util.Log
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
import java.util.Calendar
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

    // 🆕 Daily Ayah Properties
    private val _dailyAyah = MutableLiveData<DailyAyahData?>()
    val dailyAyah: LiveData<DailyAyahData?> get() = _dailyAyah

    private val _isDailyAyahLoading = MutableLiveData(false)
    val isDailyAyahLoading: LiveData<Boolean> get() = _isDailyAyahLoading

    // 🆕 Current ayah index for navigation
    private var currentAyahIndex = 0

    // Data class for Daily Ayah
    data class DailyAyahData(
        val ayahText: String,
        val ayahNumber: Int,
        val surahName: String,
        val surahNumber: Int,
        val translation: String? = null,
        val juzNumber: Int = 1
    )

    // Short meaningful ayahs (pre-selected for daily display)
    private val shortMeaningfulAyahs = listOf(
        // Al-Fatiha - Complete
        DailyAyahData("بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ", 1, "الفاتحة", 1, "In the name of Allah, the Entirely Merciful, the Especially Merciful.", 1),
        DailyAyahData("الْحَمْدُ لِلَّهِ رَبِّ الْعَالَمِينَ", 2, "الفاتحة", 1, "All praise is due to Allah, Lord of the worlds.", 1),
        DailyAyahData("الرَّحْمَٰنِ الرَّحِيمِ", 3, "الفاتحة", 1, "The Entirely Merciful, the Especially Merciful.", 1),
        DailyAyahData("مَٰلِكِ يَوْمِ الدِّينِ", 4, "الفاتحة", 1, "Sovereign of the Day of Recompense.", 1),
        DailyAyahData("إِيَّاكَ نَعْبُدُ وَإِيَّاكَ نَسْتَعِينُ", 5, "الفاتحة", 1, "It is You we worship and You we ask for help.", 1),
        DailyAyahData("اهْدِنَا الصِّرَاطَ الْمُسْتَقِيمَ", 6, "الفاتحة", 1, "Guide us to the straight path.", 1),

        // Al-Baqarah - Selected short verses
        DailyAyahData("وَاللَّهُ عَلَىٰ كُلِّ شَيْءٍ قَدِيرٌ", 20, "البقرة", 2, "And Allah is over all things competent.", 1),
        DailyAyahData("إِنَّ اللَّهَ مَعَ الصَّابِرِينَ", 153, "البقرة", 2, "Indeed, Allah is with the patient.", 2),
        DailyAyahData("فَاذْكُرُونِي أَذْكُرْكُمْ", 152, "البقرة", 2, "So remember Me; I will remember you.", 2),

        // Ali Imran
        DailyAyahData("وَاللَّهُ خَيْرُ الرَّازِقِينَ", 37, "آل عمران", 3, "And Allah is the best of providers.", 3),
        DailyAyahData("حَسْبُنَا اللَّهُ وَنِعْمَ الْوَكِيلُ", 173, "آل عمران", 3, "Sufficient for us is Allah, and He is the best Disposer of affairs.", 4),

        // An-Nisa
        DailyAyahData("وَكَانَ اللَّهُ غَفُورًا رَّحِيمًا", 23, "النساء", 4, "And Allah is ever Forgiving and Merciful.", 5),

        // Al-Maidah
        DailyAyahData("وَتَوَكَّلُواْ عَلَى اللَّهِ إِن كُنتُم مُّؤْمِنِينَ", 23, "المائدة", 5, "And rely upon Allah, if you should be believers.", 6),

        // Al-An'am
        DailyAyahData("وَهُوَ اللَّهُ فِي السَّمَاوَاتِ وَفِي الْأَرْضِ", 3, "الأنعام", 6, "And He is Allah in the heavens and the earth.", 7),

        // Ar-Ra'd
        DailyAyahData("أَلَا بِذِكْرِ اللَّهِ تَطْمَئِنُّ الْقُلُوبُ", 28, "الرعد", 13, "Unquestionably, by the remembrance of Allah hearts are assured.", 13),

        // An-Nahl
        DailyAyahData("وَاللَّهُ أَعْلَمُ بِمَا تَفْعَلُونَ", 91, "النحل", 16, "And Allah is most knowing of what you do.", 14),

        // Al-Isra
        DailyAyahData("وَقُل رَّبِّ زِدْنِي عِلْمًا", 114, "الإسراء", 17, "And say: My Lord! Increase me in knowledge.", 15),

        // Al-Kahf
        DailyAyahData("وَاللَّهُ أَعْلَمُ بِمَا لَبِثُوا", 26, "الكهف", 18, "And Allah is most knowing of how long they remained.", 15),

        // Taha
        DailyAyahData("وَمَنْ أَعْرَضَ عَن ذِكْرِي فَإِنَّ لَهُ مَعِيشَةً ضَنكًا", 124, "طه", 20, "But whoever turns away from My remembrance - indeed, he will have a depressed life.", 16),

        // Al-Furqan
        DailyAyahData("وَتَوَكَّلْ عَلَى الْحَيِّ الَّذِي لَا يَمُوتُ", 58, "الفرقان", 25, "And rely upon the Ever-Living who does not die.", 19),

        // Luqman
        DailyAyahData("إِنَّ اللَّهَ عَلِيمٌ حَكِيمٌ", 27, "لقمان", 31, "Indeed, Allah is Knowing and Wise.", 21),

        // Az-Zumar
        DailyAyahData("أَلَيْسَ اللَّهُ بِكَافٍ عَبْدَهُ", 36, "الزمر", 39, "Is not Allah sufficient for His servant?", 24),

        // Ghafir
        DailyAyahData("فَاصْبِرْ إِنَّ وَعْدَ اللَّهِ حَقٌّ", 55, "غافر", 40, "So be patient. Indeed, the promise of Allah is truth.", 24),

        // Ash-Shura
        DailyAyahData("وَمَا أَصَابَكُم مِّن مُّصِيبَةٍ فَبِمَا كَسَبَتْ أَيْدِيكُمْ", 30, "الشورى", 42, "And whatever strikes you of disaster - it is for what your hands have earned.", 25),

        // Al-Hashr
        DailyAyahData("هُوَ اللَّهُ الَّذِي لَا إِلَٰهَ إِلَّا هُوَ", 22, "الحشر", 59, "He is Allah, other than whom there is no deity.", 28),

        // At-Talaq
        DailyAyahData("وَمَن يَتَّقِ اللَّهَ يَجْعَل لَّهُ مَخْرَجًا", 2, "الطلاق", 65, "And whoever fears Allah - He will make for him a way out.", 28),

        // Al-Mulk
        DailyAyahData("تَبَارَكَ الَّذِي بِيَدِهِ الْمُلْكُ", 1, "الملك", 67, "Blessed is He in whose hand is dominion.", 29),

        // Al-Qalam
        DailyAyahData("وَإِنَّكَ لَعَلَىٰ خُلُقٍ عَظِيمٍ", 4, "القلم", 68, "And indeed, you are of a great moral character.", 29),

        // Al-Insan
        DailyAyahData("إِنَّا هَدَيْنَاهُ السَّبِيلَ", 3, "الإنسان", 76, "Indeed, We guided him to the way.", 29),

        // An-Nazi'at
        DailyAyahData("فَأَمَّا مَن طَغَىٰ", 37, "النازعات", 79, "So as for he who transgressed.", 30),

        // At-Takwir
        DailyAyahData("وَمَا تَشَاءُونَ إِلَّا أَن يَشَاءَ اللَّهُ", 29, "التكوير", 81, "And you do not will except that Allah wills.", 30),

        // Al-Inshiqaq
        DailyAyahData("فَسَبِّحْ بِاسْمِ رَبِّكَ الْعَظِيمِ", 15, "الانشقاق", 84, "So exalt the name of your Lord, the Most Great.", 30),

        // Al-A'la
        DailyAyahData("سَبِّحِ اسْمَ رَبِّكَ الْأَعْلَى", 1, "الأعلى", 87, "Exalt the name of your Lord, the Most High.", 30),

        // Al-Fajr
        DailyAyahData("إِنَّ رَبَّكَ لَبِالْمِرْصَادِ", 14, "الفجر", 89, "Indeed, your Lord is in observation.", 30),

        // Ash-Sharh - Complete
        DailyAyahData("أَلَمْ نَشْرَحْ لَكَ صَدْرَكَ", 1, "الشرح", 94, "Did We not expand for you your breast?", 30),
        DailyAyahData("فَإِنَّ مَعَ الْعُسْرِ يُسْرًا", 5, "الشرح", 94, "For indeed, with hardship [will be] ease.", 30),
        DailyAyahData("إِنَّ مَعَ الْعُسْرِ يُسْرًا", 6, "الشرح", 94, "Indeed, with hardship [will be] ease.", 30),

        // Al-Qadr - Complete
        DailyAyahData("إِنَّا أَنزَلْنَاهُ فِي لَيْلَةِ الْقَدْرِ", 1, "القدر", 97, "Indeed, We sent the Qur'an down during the Night of Decree.", 30),
        DailyAyahData("وَمَا أَدْرَاكَ مَا لَيْلَةُ الْقَدْرِ", 2, "القدر", 97, "And what can make you know what is the Night of Decree?", 30),
        DailyAyahData("لَيْلَةُ الْقَدْرِ خَيْرٌ مِّنْ أَلْفِ شَهْرٍ", 3, "القدر", 97, "The Night of Decree is better than a thousand months.", 30),

        // Az-Zalzalah
        DailyAyahData("إِذَا زُلْزِلَتِ الْأَرْضُ زِلْزَالَهَا", 1, "الزلزلة", 99, "When the earth is shaken with its [final] earthquake.", 30),
        DailyAyahData("فَمَن يَعْمَلْ مِثْقَالَ ذَرَّةٍ خَيْرًا يَرَهُ", 7, "الزلزلة", 99, "So whoever does an atom's weight of good will see it.", 30),
        DailyAyahData("وَمَن يَعْمَلْ مِثْقَالَ ذَرَّةٍ شَرًّا يَرَهُ", 8, "الزلزلة", 99, "And whoever does an atom's weight of evil will see it.", 30),

        // Al-Asr - Complete
        DailyAyahData("وَالْعَصْرِ", 1, "العصر", 103, "By time.", 30),
        DailyAyahData("إِنَّ الْإِنسَانَ لَفِي خُسْرٍ", 2, "العصر", 103, "Indeed, mankind is in loss.", 30),
        DailyAyahData("إِلَّا الَّذِينَ آمَنُوا وَعَمِلُوا الصَّالِحَاتِ", 3, "العصر", 103, "Except for those who believe and do righteous deeds.", 30),

        // Al-Kawthar - Complete
        DailyAyahData("إِنَّا أَعْطَيْنَاكَ الْكَوْثَرَ", 1, "الكوثر", 108, "Indeed, We have granted you, [O Muhammad], al-Kawthar.", 30),
        DailyAyahData("فَصَلِّ لِرَبِّكَ وَانْحَرْ", 2, "الكوثر", 108, "So pray to your Lord and sacrifice [to Him alone].", 30),
        DailyAyahData("إِنَّ شَانِئَكَ هُوَ الْأَبْتَرُ", 3, "الكوثر", 108, "Indeed, your enemy is the one cut off.", 30),

        // Al-Kafirun - Complete
        DailyAyahData("قُلْ يَا أَيُّهَا الْكَافِرُونَ", 1, "الكافرون", 109, "Say, O disbelievers.", 30),
        DailyAyahData("لَا أَعْبُدُ مَا تَعْبُدُونَ", 2, "الكافرون", 109, "I do not worship what you worship.", 30),
        DailyAyahData("لَكُمْ دِينُكُمْ وَلِيَ دِينِ", 6, "الكافرون", 109, "For you is your religion, and for me is my religion.", 30),

        // An-Nasr - Complete
        DailyAyahData("إِذَا جَاءَ نَصْرُ اللَّهِ وَالْفَتْحُ", 1, "النصر", 110, "When the victory of Allah has come and the conquest.", 30),
        DailyAyahData("وَرَأَيْتَ النَّاسَ يَدْخُلُونَ فِي دِينِ اللَّهِ أَفْوَاجًا", 2, "النصر", 110, "And you see the people entering into the religion of Allah in multitudes.", 30),
        DailyAyahData("فَسَبِّحْ بِحَمْدِ رَبِّكَ وَاسْتَغْفِرْهُ", 3, "النصر", 110, "Then exalt [Him] with praise of your Lord and ask forgiveness of Him.", 30),

        // Al-Ikhlas - Complete
        DailyAyahData("قُلْ هُوَ اللَّهُ أَحَدٌ", 1, "الإخلاص", 112, "Say, He is Allah, [who is] One.", 30),
        DailyAyahData("اللَّهُ الصَّمَدُ", 2, "الإخلاص", 112, "Allah, the Eternal Refuge.", 30),
        DailyAyahData("لَمْ يَلِدْ وَلَمْ يُولَدْ", 3, "الإخلاص", 112, "He neither begets nor is born.", 30),
        DailyAyahData("وَلَمْ يَكُن لَّهُ كُفُوًا أَحَدٌ", 4, "الإخلاص", 112, "Nor is there to Him any equivalent.", 30),

        // Al-Falaq - Complete
        DailyAyahData("قُلْ أَعُوذُ بِرَبِّ الْفَلَقِ", 1, "الفلق", 113, "Say, I seek refuge in the Lord of daybreak.", 30),
        DailyAyahData("مِن شَرِّ مَا خَلَقَ", 2, "الفلق", 113, "From the evil of that which He created.", 30),
        DailyAyahData("وَمِن شَرِّ غَاسِقٍ إِذَا وَقَبَ", 3, "الفلق", 113, "And from the evil of darkness when it settles.", 30),
        DailyAyahData("وَمِن شَرِّ النَّفَّاثَاتِ فِي الْعُقَدِ", 4, "الفلق", 113, "And from the evil of the blowers in knots.", 30),
        DailyAyahData("وَمِن شَرِّ حَاسِدٍ إِذَا حَسَدَ", 5, "الفلق", 113, "And from the evil of an envier when he envies.", 30),

        // An-Nas - Complete
        DailyAyahData("قُلْ أَعُوذُ بِرَبِّ النَّاسِ", 1, "الناس", 114, "Say, I seek refuge in the Lord of mankind.", 30),
        DailyAyahData("مَلِكِ النَّاسِ", 2, "الناس", 114, "The Sovereign of mankind.", 30),
        DailyAyahData("إِلَٰهِ النَّاسِ", 3, "الناس", 114, "The God of mankind.", 30),
        DailyAyahData("مِن شَرِّ الْوَسْوَاسِ الْخَنَّاسِ", 4, "الناس", 114, "From the evil of the retreating whisperer.", 30),
        DailyAyahData("الَّذِي يُوَسْوِسُ فِي صُدُورِ النَّاسِ", 5, "الناس", 114, "Who whispers [evil] into the breasts of mankind.", 30),
        DailyAyahData("مِنَ الْجِنَّةِ وَالنَّاسِ", 6, "الناس", 114, "From among the jinn and mankind.", 30)
    )

    init {
        loadAllSurahs()
        loadAudioReciters()
        initializeAudioAvailability()
        loadDailyAyah() // Load daily ayah on initialization
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

    // 🆕 Load Daily Ayah
    fun loadDailyAyah() {
        viewModelScope.launch {
            try {
                _isDailyAyahLoading.value = true
                Log.d("QuranViewModel", "📖 Loading daily ayah...")

                val dailyAyah = getDailyAyah()
                _dailyAyah.value = dailyAyah

                // Set current index to the daily ayah
                currentAyahIndex = shortMeaningfulAyahs.indexOf(dailyAyah)
                if (currentAyahIndex == -1) currentAyahIndex = 0

                Log.d("QuranViewModel", "✅ Daily ayah loaded: ${dailyAyah.surahName} - آية ${dailyAyah.ayahNumber}")

            } catch (e: Exception) {
                Log.e("QuranViewModel", "❌ Error loading daily ayah: ${e.message}")
                // Set fallback ayah
                _dailyAyah.value = getDefaultAyah()
                currentAyahIndex = 0
            } finally {
                _isDailyAyahLoading.value = false
            }
        }
    }

    // 🆕 Load Random Ayah
    fun loadRandomAyah() {
        viewModelScope.launch {
            try {
                _isDailyAyahLoading.value = true
                Log.d("QuranViewModel", "🎲 Loading random ayah...")

                currentAyahIndex = (0 until shortMeaningfulAyahs.size).random()
                val randomAyah = shortMeaningfulAyahs[currentAyahIndex]
                _dailyAyah.value = randomAyah

                Log.d("QuranViewModel", "✅ Random ayah loaded: ${randomAyah.surahName} - آية ${randomAyah.ayahNumber}")

            } catch (e: Exception) {
                Log.e("QuranViewModel", "❌ Error loading random ayah: ${e.message}")
                _dailyAyah.value = getDefaultAyah()
                currentAyahIndex = 0
            } finally {
                _isDailyAyahLoading.value = false
            }
        }
    }

    // 🆕 Load Next Ayah (for swipe left)
    fun loadNextAyah() {
        viewModelScope.launch {
            try {
                _isDailyAyahLoading.value = true
                Log.d("QuranViewModel", "➡️ Loading next ayah...")

                currentAyahIndex = (currentAyahIndex + 1) % shortMeaningfulAyahs.size
                val nextAyah = shortMeaningfulAyahs[currentAyahIndex]
                _dailyAyah.value = nextAyah

                Log.d("QuranViewModel", "✅ Next ayah loaded: ${nextAyah.surahName} - آية ${nextAyah.ayahNumber}")

            } catch (e: Exception) {
                Log.e("QuranViewModel", "❌ Error loading next ayah: ${e.message}")
                _dailyAyah.value = getDefaultAyah()
            } finally {
                _isDailyAyahLoading.value = false
            }
        }
    }

    // 🆕 Load Previous Ayah (for swipe right)
    fun loadPreviousAyah() {
        viewModelScope.launch {
            try {
                _isDailyAyahLoading.value = true
                Log.d("QuranViewModel", "⬅️ Loading previous ayah...")

                currentAyahIndex = if (currentAyahIndex - 1 < 0) {
                    shortMeaningfulAyahs.size - 1
                } else {
                    currentAyahIndex - 1
                }
                val previousAyah = shortMeaningfulAyahs[currentAyahIndex]
                _dailyAyah.value = previousAyah

                Log.d("QuranViewModel", "✅ Previous ayah loaded: ${previousAyah.surahName} - آية ${previousAyah.ayahNumber}")

            } catch (e: Exception) {
                Log.e("QuranViewModel", "❌ Error loading previous ayah: ${e.message}")
                _dailyAyah.value = getDefaultAyah()
            } finally {
                _isDailyAyahLoading.value = false
            }
        }
    }

    // 🆕 Get Daily Ayah based on current date
    private fun getDailyAyah(): DailyAyahData {
        val dayOfYear = Calendar.getInstance().get(Calendar.DAY_OF_YEAR)
        val index = dayOfYear % shortMeaningfulAyahs.size
        return shortMeaningfulAyahs[index]
    }

    // 🆕 Get Random Ayah
    private fun getRandomAyah(): DailyAyahData {
        return shortMeaningfulAyahs.random()
    }

    // 🆕 Get Default Ayah (fallback)
    private fun getDefaultAyah(): DailyAyahData {
        return DailyAyahData(
            ayahText = "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ",
            ayahNumber = 1,
            surahName = "الفاتحة",
            surahNumber = 1,
            translation = "In the name of Allah, the Entirely Merciful, the Especially Merciful.",
            juzNumber = 1
        )
    }

    // 🆕 Get current ayah position info
    fun getCurrentAyahPosition(): String {
        val total = shortMeaningfulAyahs.size
        val current = currentAyahIndex + 1
        return "$current من $total"
    }

    // 🆕 Helper function to convert numbers to Arabic
    fun getNumberInArabic(number: Int): String {
        val arabicNumbers = mapOf(
            0 to "٠", 1 to "١", 2 to "٢", 3 to "٣", 4 to "٤",
            5 to "٥", 6 to "٦", 7 to "٧", 8 to "٨", 9 to "٩"
        )

        return number.toString().map { digit ->
            arabicNumbers[digit.toString().toInt()] ?: digit
        }.joinToString("")
    }

    // 🆕 Helper function to get Juz number in Arabic
    fun getJuzNumberInArabic(juzNumber: Int): String {
        val arabicJuzNames = mapOf(
            1 to "الأول", 2 to "الثاني", 3 to "الثالث", 4 to "الرابع", 5 to "الخامس",
            6 to "السادس", 7 to "السابع", 8 to "الثامن", 9 to "التاسع", 10 to "العاشر",
            11 to "الحادي عشر", 12 to "الثاني عشر", 13 to "الثالث عشر", 14 to "الرابع عشر", 15 to "الخامس عشر",
            16 to "السادس عشر", 17 to "السابع عشر", 18 to "الثامن عشر", 19 to "التاسع عشر", 20 to "العشرون",
            21 to "الحادي والعشرون", 22 to "الثاني والعشرون", 23 to "الثالث والعشرون", 24 to "الرابع والعشرون",
            25 to "الخامس والعشرون", 26 to "السادس والعشرون", 27 to "السابع والعشرون", 28 to "الثامن والعشرون",
            29 to "التاسع والعشرون", 30 to "الثلاثون"
        )

        return arabicJuzNames[juzNumber] ?: "الجزء $juzNumber"
    }

    // 🆕 Get formatted ayah source
    fun getFormattedAyahSource(dailyAyah: DailyAyahData): String {
        return "${dailyAyah.surahName} - الجزء ${getJuzNumberInArabic(dailyAyah.juzNumber)} - آية ${getNumberInArabic(dailyAyah.ayahNumber)}"
    }

    // 🆕 Additional utility functions for enhanced functionality

    // Jump to specific ayah by index
    fun jumpToAyah(index: Int) {
        if (index in 0 until shortMeaningfulAyahs.size) {
            viewModelScope.launch {
                try {
                    _isDailyAyahLoading.value = true
                    currentAyahIndex = index
                    val ayah = shortMeaningfulAyahs[currentAyahIndex]
                    _dailyAyah.value = ayah
                    Log.d("QuranViewModel", "✅ Jumped to ayah: ${ayah.surahName} - آية ${ayah.ayahNumber}")
                } catch (e: Exception) {
                    Log.e("QuranViewModel", "❌ Error jumping to ayah: ${e.message}")
                } finally {
                    _isDailyAyahLoading.value = false
                }
            }
        }
    }

    // Get total number of ayahs
    fun getTotalAyahsCount(): Int = shortMeaningfulAyahs.size

    // Get current ayah index
    fun getCurrentAyahIndex(): Int = currentAyahIndex

    // Check if it's the first ayah
    fun isFirstAyah(): Boolean = currentAyahIndex == 0

    // Check if it's the last ayah
    fun isLastAyah(): Boolean = currentAyahIndex == shortMeaningfulAyahs.size - 1

    // Get ayah by surah number (if exists in our collection)
    fun getAyahsBySurah(surahNumber: Int): List<DailyAyahData> {
        return shortMeaningfulAyahs.filter { it.surahNumber == surahNumber }
    }

    // Get unique surahs from our collection
    fun getUniqueSurahs(): List<Pair<Int, String>> {
        return shortMeaningfulAyahs
            .groupBy { it.surahNumber }
            .map { (number, ayahs) -> number to ayahs.first().surahName }
            .sortedBy { it.first }
    }

    override fun onCleared() {
        super.onCleared()
        audioPlayerManager.release()
        audioUrlCache.clear()
        failedUrls.clear()
        autoPlayedSurahs.clear()
    }
}