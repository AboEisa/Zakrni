package com.zakrni.app.clean.ui.views

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.zakrni.app.R
import com.zakrni.app.clean.ui.adapters.ReciterAdapter
import com.zakrni.app.clean.ui.adapters.ReciterItem
import com.zakrni.app.clean.ui.utils.LocaleHelper
import com.zakrni.app.clean.ui.utils.ReciterCatalog
import com.zakrni.app.databinding.BottomSheetReciterSelectionBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class ReciterBottomSheetFragment : BottomSheetDialogFragment() {

    private var _binding: BottomSheetReciterSelectionBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: ReciterAdapter
    private var currentReciterIdentifier: String = "ar.alafasy"
    private var onReciterSelectedListener: ((String, String) -> Unit)? = null

    companion object {
        private const val ARG_CURRENT_RECITER = "current_reciter"

        fun newInstance(currentReciter: String): ReciterBottomSheetFragment {
            return ReciterBottomSheetFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_CURRENT_RECITER, currentReciter)
                }
            }
        }
    }

    // Supported reciters only (single source of truth).
    private val reciters = ReciterCatalog.supportedReciters.map { reciter ->
        ReciterItem(
            identifier = reciter.identifier,
            nameArabic = reciter.arabicName,
            nameEnglish = reciter.englishName
        )
    }

    override fun getTheme(): Int = R.style.ReciterBottomSheetDialogStyle

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetReciterSelectionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        currentReciterIdentifier = arguments?.getString(ARG_CURRENT_RECITER) ?: "ar.alafasy"

        setupRecyclerView()
        setupClickListeners()
        setupSearch()
        setupLocalizedLabels()
    }

    private fun setupRecyclerView() {
        adapter = ReciterAdapter { reciter ->
            val displayName = if (LocaleHelper.isArabic(requireContext())) {
                reciter.nameArabic
            } else {
                reciter.nameEnglish
            }
            onReciterSelectedListener?.invoke(reciter.identifier, displayName)
            dismiss()
        }

        binding.recitersRecycler.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@ReciterBottomSheetFragment.adapter
        }

        submitReciters(reciters)
    }

    private fun setupClickListeners() {
        binding.closeButton.setOnClickListener {
            dismiss()
        }
    }

    private fun setupSearch() {
        val searchInput = binding.searchInput ?: return
        searchInput.hint = localizedText("ابحث عن القارئ", "Search reciter")
        searchInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s?.toString().orEmpty().trim()
                if (query.isEmpty()) {
                    submitReciters(reciters)
                    return
                }

                val filtered = reciters.filter { reciter ->
                    reciter.nameArabic.contains(query, ignoreCase = true) ||
                            reciter.nameEnglish.contains(query, ignoreCase = true)
                }
                submitReciters(filtered)
            }

            override fun afterTextChanged(s: Editable?) = Unit
        })
    }

    private fun setupLocalizedLabels() {
        binding.reciterSubtitle?.text = localizedText(
            "اختر القارئ المناسب لتلاوة السورة",
            "Pick the reciter you want for this surah"
        )
    }

    private fun submitReciters(source: List<ReciterItem>) {
        val withSelection = source.map { reciter ->
            reciter.copy(isSelected = reciter.identifier == currentReciterIdentifier)
        }
        adapter.submitList(withSelection)
    }

    private fun localizedText(arabic: String, english: String): String {
        return if (LocaleHelper.isArabic(requireContext())) arabic else english
    }

    fun setOnReciterSelectedListener(listener: (String, String) -> Unit) {
        onReciterSelectedListener = listener
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
