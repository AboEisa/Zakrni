package com.example.zakrni.clean.ui.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.zakrni.clean.domain.models.DomainAyah
import com.example.zakrni.clean.domain.models.DomainSurah
import com.example.zakrni.clean.domain.usecase.GetQuranUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

@HiltViewModel
class QuranViewModel @Inject constructor(
    private val getQuranUseCase: GetQuranUseCase
) : ViewModel() {

    // Surahs list
    private val _surahs = MutableLiveData<List<DomainSurah>>()
    val surahs: LiveData<List<DomainSurah>> get() = _surahs

    // Verses for selected surah
    private val _verses = MutableLiveData<List<DomainAyah>>()
    val verses: LiveData<List<DomainAyah>> get() = _verses

    // Current selected surah
    private val _currentSurah = MutableLiveData<DomainSurah?>()
    val currentSurah: LiveData<DomainSurah?> get() = _currentSurah

    // Loading / error states
    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> get() = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> get() = _error

    init {
        loadAllSurahs()
    }

    fun loadQuranVerses(surahNumber: Int) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null

                val verses = getQuranUseCase.getQuranVerses(surahNumber)
                println("DEBUG: QuranViewModel - Surah $surahNumber returned ${verses.size} verses")
                _verses.value = verses

                val surah = getQuranUseCase.getSurahByNumber(surahNumber)
                println("DEBUG: QuranViewModel - Loaded surah: ${surah?.name}")
                _currentSurah.value = surah

                if (verses.isEmpty()) {
                    _error.value = "No verses found for surah $surahNumber"
                }
            } catch (e: Exception) {
                println("DEBUG: QuranViewModel - Error loading verses: ${e.message}")
                e.printStackTrace()
                _error.value = "Failed to load verses: ${e.message}"
                _verses.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadAllSurahs() {
        viewModelScope.launch {
            try {
                println("DEBUG: QuranViewModel - Loading all surahs")
                _isLoading.value = true
                _error.value = null

                val surahsList = getQuranUseCase.getAllSurahs()
                println("DEBUG: QuranViewModel - Loaded ${surahsList.size} surahs")
                _surahs.value = surahsList

                if (surahsList.isEmpty()) {
                    _error.value = "No surahs found"
                }
            } catch (e: Exception) {
                println("DEBUG: QuranViewModel - Error loading surahs: ${e.message}")
                e.printStackTrace()
                _error.value = "Failed to load surahs: ${e.message}"
                _surahs.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearError() {
        _error.value = null
    }
}
