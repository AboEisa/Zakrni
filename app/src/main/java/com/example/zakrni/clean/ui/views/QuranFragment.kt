package com.example.zakrni.clean.views

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.media3.exoplayer.SimpleExoPlayer
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.zakrni.R
import com.example.zakrni.clean.ui.adapters.QuranAdapter
import com.example.zakrni.clean.viewmodels.QuranViewModel
import com.example.zakrni.databinding.FragmentQuranBinding
import dagger.hilt.android.AndroidEntryPoint

@UnstableApi
@AndroidEntryPoint
class QuranFragment : Fragment() {
    private lateinit var binding: FragmentQuranBinding
    private lateinit var viewModel: QuranViewModel
    private lateinit var adapter: QuranAdapter
    private var exoPlayer: SimpleExoPlayer? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentQuranBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this).get(QuranViewModel::class.java)
        adapter = QuranAdapter()
        binding.quranVersesRecycler.adapter = adapter
        binding.quranVersesRecycler.layoutManager = LinearLayoutManager(context)

        // Load Surah 1 (Al-Fatiha) as an example
        viewModel.loadQuranVerses(1)

        viewModel.verses.observe(viewLifecycleOwner) { verses ->
            binding.surahHeader.text = viewModel.surahName.value ?: "سُورَةُ ٱلْفَاتِحَةِ"
            adapter.submitList(verses)
            if (verses.isNotEmpty()) {
                val audioUrl = verses.first().audioUrl ?: verses.first().editions?.get("ar.alafasy")
                setupExoPlayer(audioUrl ?: "")
            }
        }

        viewModel.isPlaying.observe(viewLifecycleOwner) { isPlaying ->
            updatePlayButton(isPlaying)
        }

        binding.playButton.setOnClickListener {
            viewModel.togglePlay()
        }
    }

    private fun setupExoPlayer(audioUrl: String) {
        exoPlayer = SimpleExoPlayer.Builder(requireContext()).build()
        if (audioUrl.isNotEmpty()) {
            val mediaItem = MediaItem.fromUri(audioUrl)
            exoPlayer?.setMediaItem(mediaItem)
            exoPlayer?.prepare()
        }
        viewModel.isPlaying.observe(viewLifecycleOwner) { isPlaying ->
            if (isPlaying) exoPlayer?.play() else exoPlayer?.pause()
            updatePlayButton(isPlaying)
        }
    }

    private fun updatePlayButton(isPlaying: Boolean?) {
        binding.playButton.setImageResource(if (isPlaying == true) R.drawable.ic_pause else R.drawable.ic_play)
    }

    override fun onDestroy() {
        super.onDestroy()
        exoPlayer?.release()
        exoPlayer = null
    }
}