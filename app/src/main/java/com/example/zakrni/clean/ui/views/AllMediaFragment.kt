package com.example.zakrni.clean.ui.views

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.example.zakrni.R
import com.example.zakrni.databinding.FragmentAllMediaBinding


class AllMediaFragment : Fragment() {

    private var _binding: FragmentAllMediaBinding? = null
    private val binding get() = _binding!!


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_all_media, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentAllMediaBinding.bind(view)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setupNewFeatures() {
        binding.articlesCard.setOnClickListener {
            findNavController().navigate(R.id.action_allCategoriesFragment_to_articlesFragment)
        }

        binding.audioCard.setOnClickListener {
            findNavController().navigate(R.id.action_allCategoriesFragment_to_audioFragment)
        }

        binding.videosCard.setOnClickListener {
            findNavController().navigate(R.id.action_allCategoriesFragment_to_videosFragment)
        }

        binding.radioCard.setOnClickListener {
            findNavController().navigate(R.id.action_allCategoriesFragment_to_radioFragment)
        }
    }


}