package com.example.zakrni.clean.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.zakrni.R
import com.example.zakrni.databinding.FragmentSectionBinding

class DhikrFragment : Fragment() {

    private var _binding: FragmentSectionBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSectionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        binding.sectionIcon.setImageResource(R.drawable.ic_dhikr)
        binding.sectionTitle.text = "التسبيح الذكر"
        binding.sectionDescription.text = "أذكار وتسابيح للتقرب إلى الله عز وجل"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}