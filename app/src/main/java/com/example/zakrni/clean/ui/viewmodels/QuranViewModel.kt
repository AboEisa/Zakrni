package com.example.zakrni.clean.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.zakrni.clean.data.remote.IRemoteDataSource
import com.example.zakrni.clean.domain.models.DomainQuranVerseResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class QuranViewModel @Inject constructor(private val repo: IRemoteDataSource) : ViewModel() {
    private val _verses = MutableLiveData<List<DomainQuranVerseResponse>>()
    val verses: MutableLiveData<List<DomainQuranVerseResponse>> get() = _verses

    private val _isPlaying = MutableLiveData<Boolean>(false)
    val isPlaying: MutableLiveData<Boolean> get() = _isPlaying

    private val _surahName = MutableLiveData<String>("سُورَةُ ٱلْفَاتِحَةِ")
    val surahName: MutableLiveData<String> get() = _surahName

    fun loadQuranVerses(surahNumber: Int) {
        viewModelScope.launch {
            val surahList = repo.getSurahList()
            val surah = surahList.find { it.number == surahNumber }
            surah?.let { _surahName.value = "${it.name} (${it.englishName})" }
            _verses.value = repo.getQuranVerses(surahNumber)
        }
    }

    fun togglePlay() {
        _isPlaying.value = _isPlaying.value?.not()
    }
}