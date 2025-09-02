package com.example.zakrni.clean.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.zakrni.clean.domain.models.DomainSurah
import com.example.zakrni.clean.domain.usecase.GetQuranUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class QuranViewModel @Inject constructor(
    private val getQuranUseCase: GetQuranUseCase
) : ViewModel() {

    private val _surahs = MutableLiveData<List<DomainSurah>>()
    val surahs: MutableLiveData<List<DomainSurah>> get() = _surahs

    private val _isLoading = MutableLiveData<Boolean>(false)
    val isLoading: MutableLiveData<Boolean> get() = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: MutableLiveData<String?> get() = _error

    init {
        loadAllSurahs()
    }

    fun loadAllSurahs() {
        viewModelScope.launch {
            try {
                println("DEBUG: SurahListViewModel - Starting to load all surahs")
                _isLoading.value = true
                _error.value = null

                val surahs = getQuranUseCase.getAllSurahs()
                println("DEBUG: SurahListViewModel - Got ${surahs.size} surahs")

                if (surahs.isNotEmpty()) {
                    surahs.forEachIndexed { index, surah ->
                        println("DEBUG: SurahListViewModel - Surah ${index + 1}: ${surah.number} - ${surah.englishName}")
                    }
                    _surahs.value = surahs
                } else {
                    println("DEBUG: SurahListViewModel - No surahs received")
                    _surahs.value = emptyList()
                }

            } catch (e: Exception) {
                println("DEBUG: SurahListViewModel - Exception in loadAllSurahs: ${e.message}")
                e.printStackTrace()
                _error.value = "Failed to load Surahs: ${e.message}"
                _surahs.value = emptyList()
            } finally {
                _isLoading.value = false
                println("DEBUG: SurahListViewModel - Finished loading")
            }
        }
    }

    fun clearError() {
        _error.value = null
    }

    // Test method with dummy data
    fun loadTestData() {
        println("DEBUG: SurahListViewModel - Loading test data")
        val testSurahs = listOf(
            DomainSurah(
                number = 1,
                name = "سُورَةُ ٱلْفَاتِحَةِ",
                englishName = "Al-Faatiha",
                englishNameTranslation = "The Opening",
                revelationType = "Meccan",
                ayahs = emptyList()
            ),
            DomainSurah(
                number = 2,
                name = "سُورَةُ ٱلْبَقَرَةِ",
                englishName = "Al-Baqarah",
                englishNameTranslation = "The Cow",
                revelationType = "Medinan",
                ayahs = emptyList()
            ),
            DomainSurah(
                number = 3,
                name = "سُورَةُ آلِ عِمْرَانَ",
                englishName = "Ali 'Imran",
                englishNameTranslation = "Family of Imran",
                revelationType = "Medinan",
                ayahs = emptyList()
            )
        )
        _surahs.value = testSurahs
        println("DEBUG: SurahListViewModel - Test data loaded with ${testSurahs.size} surahs")
    }
}