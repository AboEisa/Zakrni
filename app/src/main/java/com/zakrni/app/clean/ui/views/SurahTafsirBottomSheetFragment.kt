package com.zakrni.app.clean.ui.views

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.zakrni.app.R
import com.zakrni.app.databinding.BottomSheetSurahTafsirBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class SurahTafsirBottomSheetFragment : BottomSheetDialogFragment() {

    private var _binding: BottomSheetSurahTafsirBinding? = null
    private val binding get() = _binding!!

    override fun getTheme(): Int = R.style.ReciterBottomSheetDialogStyle

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetSurahTafsirBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val title = arguments?.getString(ARG_TAFSIR_TITLE).orEmpty()
        val tafsirText = arguments?.getString(ARG_TAFSIR_TEXT).orEmpty()
        val isArabic = arguments?.getBoolean(ARG_IS_ARABIC, true) == true

        binding.tafsirSheetTitle.text = title
        binding.tafsirSheetText.text = tafsirText
        binding.tafsirSheetText.textDirection = if (isArabic) {
            View.TEXT_DIRECTION_RTL
        } else {
            View.TEXT_DIRECTION_LTR
        }
        binding.tafsirSheetText.textAlignment = View.TEXT_ALIGNMENT_VIEW_START
        binding.closeButton.setOnClickListener { dismiss() }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val ARG_TAFSIR_TITLE = "arg_tafsir_title"
        private const val ARG_TAFSIR_TEXT = "arg_tafsir_text"
        private const val ARG_IS_ARABIC = "arg_is_arabic"

        fun newInstance(
            title: String,
            tafsirText: String,
            isArabic: Boolean
        ): SurahTafsirBottomSheetFragment {
            return SurahTafsirBottomSheetFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_TAFSIR_TITLE, title)
                    putString(ARG_TAFSIR_TEXT, tafsirText)
                    putBoolean(ARG_IS_ARABIC, isArabic)
                }
            }
        }
    }
}
