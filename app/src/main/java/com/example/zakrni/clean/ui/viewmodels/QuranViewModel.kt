package com.example.zakrni.clean.ui.viewmodels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.zakrni.R
import com.example.zakrni.clean.domain.models.DomainAyah
import com.example.zakrni.clean.domain.models.DomainSurah
import com.example.zakrni.clean.domain.usecase.GetQuranUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

@HiltViewModel
class QuranViewModel @Inject constructor(
    private val getQuranUseCase: GetQuranUseCase
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

    init {
        loadAllSurahs()
    }

    fun loadQuranVerses(surahNumber: Int) {
        viewModelScope.launch {
            try {
                Log.d("QuranViewModel", "Loading verses for surah $surahNumber")
                _isLoading.value = true
                _error.value = null

                val verses = withTimeoutOrNull(10_000L) {
                    getQuranUseCase.getQuranVerses(surahNumber)
                } ?: throw Exception("Request timed out")
                Log.d("QuranViewModel", "Got ${verses.size} verses")
                _verses.value = verses

                val surah = withTimeoutOrNull(10_000L) {
                    getQuranUseCase.getSurahByNumber(surahNumber)
                } ?: throw Exception("Request timed out")
                _currentSurah.value = surah
            } catch (e: CancellationException) {
                Log.e("QuranViewModel", "Coroutine cancelled for surah $surahNumber", e)
                _error.value = "Operation cancelled"
            } catch (e: Exception) {
                Log.e("QuranViewModel", "Error loading verses for surah $surahNumber", e)
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
                Log.d("QuranViewModel", "Loading all surahs")
                _isLoading.value = true
                _error.value = null

                val surahsList = withTimeoutOrNull(10_000L) {
                    getQuranUseCase.getAllSurahs()
                } ?: throw Exception("Request timed out")
                Log.d("QuranViewModel", "Loaded ${surahsList.size} surahs")
                _surahs.value = surahsList

                if (surahsList.isEmpty()) {
                    _error.value = "No surahs found"
                }
            } catch (e: CancellationException) {
                Log.e("QuranViewModel", "Coroutine cancelled for surah list", e)
                _error.value = "Operation cancelled"
            } catch (e: Exception) {
                Log.e("QuranViewModel", "Error loading surahs", e)
                _error.value = e.message ?: "Unknown error occurred"
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