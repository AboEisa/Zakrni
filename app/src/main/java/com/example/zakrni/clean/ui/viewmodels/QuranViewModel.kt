package com.example.zakrni.clean.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.zakrni.clean.domain.models.DomainAyah
import com.example.zakrni.clean.domain.usecase.GetQuranUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class QuranViewModel @Inject constructor(
    private val getQuranUseCase: GetQuranUseCase
) : ViewModel() {

    private val _verses = MutableLiveData<List<DomainAyah>>()
    val verses: MutableLiveData<List<DomainAyah>> get() = _verses

    private val _isPlaying = MutableLiveData<Boolean>(false)
    val isPlaying: MutableLiveData<Boolean> get() = _isPlaying

    private val _surahName = MutableLiveData<String>("سُورَةُ ٱلْفَاتِحَةِ")
    val surahName: MutableLiveData<String> get() = _surahName

    private val _isLoading = MutableLiveData<Boolean>(false)
    val isLoading: MutableLiveData<Boolean> get() = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: MutableLiveData<String?> get() = _error

    fun loadQuranVerses(surahNumber: Int) {
        viewModelScope.launch {
            try {
                println("DEBUG: Starting to load verses for Surah $surahNumber")
                _isLoading.value = true
                _error.value = null

                val surah = getQuranUseCase.getSurahByNumber(surahNumber)
                println("DEBUG: Got surah info: $surah")

                surah?.let {
                    _surahName.value = "${it.name} (${it.englishName})"
                }

                val verses = getQuranUseCase.getQuranVerses(surahNumber)
                println("DEBUG: Got ${verses.size} verses")

                if (verses.isNotEmpty()) {
                    verses.forEachIndexed { index, verse ->
                        println("DEBUG: Verse $index: ${verse.text.take(50)}...")
                    }
                }

                _verses.value = verses

            } catch (e: Exception) {
                println("DEBUG: Exception in loadQuranVerses: ${e.message}")
                e.printStackTrace()
                _error.value = "Failed to load Quran verses: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun togglePlay() {
        _isPlaying.value = _isPlaying.value?.not()
    }

    fun clearError() {
        _error.value = null
    }
}