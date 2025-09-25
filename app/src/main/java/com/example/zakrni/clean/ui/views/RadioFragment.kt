// ui/views/RadioFragment.kt
package com.example.zakrni.clean.ui.views

import android.media.MediaPlayer
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.zakrni.clean.domain.models.DomainRadioStation
import com.example.zakrni.clean.ui.adapters.RadioAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlin.getValue

@AndroidEntryPoint
class RadioFragment : Fragment() {
    private var _binding: FragmentRadioBinding? = null
    private val binding get() = _binding!!

    private val viewModel: RadioViewModel by viewModels()
    private lateinit var radioAdapter: RadioAdapter
    private var mediaPlayer: MediaPlayer? = null
    private var currentStation: DomainRadioStation? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        observeViewModel()
        viewModel.loadRadioStations()
    }

    private fun setupRecyclerView() {
        radioAdapter = RadioAdapter { station ->
            if (currentStation?.id == station.id) {
                stopRadio()
            } else {
                playRadio(station)
            }
        }

        binding.radioRecyclerView.apply {
            adapter = radioAdapter
            layoutManager = GridLayoutManager(context, 2)
        }
    }

    private fun playRadio(station: DomainRadioStation) {
        stopRadio()

        mediaPlayer = MediaPlayer().apply {
            setDataSource(station.streamUrl)
            prepareAsync()
            setOnPreparedListener {
                start()
                currentStation = station.copy(isPlaying = true)
                updateStationUI()
            }
        }
    }

    private fun stopRadio() {
        mediaPlayer?.release()
        mediaPlayer = null
        currentStation?.let {
            currentStation = it.copy(isPlaying = false)
            updateStationUI()
        }
    }

    private fun updateStationUI() {
        radioAdapter.updatePlayingStation(currentStation?.id)

    }

    override fun onDestroyView() {
        stopRadio()
        super.onDestroyView()
        _binding = null
    }
}