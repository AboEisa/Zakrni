package com.example.zakrni.clean.ui.viewmodels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
                Log.d("QuranViewModel", "Starting to load verses for surah $surahNumber")
                _isLoading.value = true
                _error.value = null

                // Clear previous verses first
                _verses.value = emptyList()

                // Load verses
                val verses = withTimeoutOrNull(15_000L) {
                    getQuranUseCase.getQuranVerses(surahNumber)
                }

                if (verses == null) {
                    throw Exception("Request timed out after 15 seconds")
                }

                Log.d("QuranViewModel", "Successfully loaded ${verses.size} verses for surah $surahNumber")
                _verses.value = verses

                // Load surah info from cached list
                val cachedSurahs = _surahs.value
                val surah = cachedSurahs?.find { it.number == surahNumber }

                if (surah != null) {
                    // Update the surah with the actual verses
                    val updatedSurah = surah.copy(ayahs = verses)
                    _currentSurah.value = updatedSurah
                    Log.d("QuranViewModel", "Updated surah info: ${surah.name} with ${verses.size} verses")
                } else {
                    Log.w("QuranViewModel", "Surah $surahNumber not found in cached list")
                    // Try to get it directly
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
                Log.e("QuranViewModel", "Coroutine cancelled for surah $surahNumber", e)
                _error.value = "Operation cancelled"
            } catch (e: Exception) {
                Log.e("QuranViewModel", "Error loading verses for surah $surahNumber: ${e.message}", e)
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

                val surahsList = withTimeoutOrNull(15_000L) {
                    getQuranUseCase.getAllSurahs()
                }

                if (surahsList == null) {
                    throw Exception("Request timed out after 15 seconds")
                }

                Log.d("QuranViewModel", "Loaded ${surahsList.size} surahs")
                _surahs.value = surahsList

                if (surahsList.isEmpty()) {
                    _error.value = "No surahs found"
                }
            } catch (e: CancellationException) {
                Log.e("QuranViewModel", "Coroutine cancelled for surah list", e)
                _error.value = "Operation cancelled"
            } catch (e: Exception) {
                Log.e("QuranViewModel", "Error loading surahs: ${e.message}", e)
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

    fun clearVerses() {
        _verses.value = emptyList()
        _currentSurah.value = null
    }
}