// ui/views/RecitersFragment.kt
package com.example.zakrni.clean.ui.views

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.zakrni.R
import androidx.navigation.fragment.findNavController
import com.example.zakrni.clean.domain.models.DomainReciter
import com.example.zakrni.clean.ui.adapters.RecitersAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlin.getValue
import android.view.LayoutInflater
import android.view.ViewGroup



@AndroidEntryPoint
class RecitersFragment : Fragment() {
    private var _binding: FragmentRecitersBinding? = null
    private val binding get() = _binding!!

    private val viewModel: RecitersViewModel by viewModels()
    private lateinit var recitersAdapter: RecitersAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupFilters()
        observeViewModel()
        viewModel.loadReciters()
    }

    private fun observeViewModel() {
        viewModel.reciters.observe(viewLifecycleOwner) { reciters ->
            recitersAdapter.submitList(reciters)
            binding.emptyStateTextView.visibility = if (reciters.isEmpty()) View.VISIBLE else View.GONE
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) { message ->
            message?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
                viewModel.clearErrorMessage()
            }
        }
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRecitersBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    private fun setupRecyclerView() {
        recitersAdapter = RecitersAdapter { reciter ->
            navigateToReciterSurahs(reciter)
        }

        binding.recitersRecyclerView.apply {
            adapter = recitersAdapter
            layoutManager = GridLayoutManager(context, 3)
        }
    }

    private fun setupFilters() {
        binding.chipGroup.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.chipAll -> viewModel.loadReciters()
                R.id.chipHafs -> viewModel.filterByRewaya("hafs")
                R.id.chipWarsh -> viewModel.filterByRewaya("warsh")
            }
        }
    }

    private fun navigateToReciterSurahs(reciter: DomainReciter) {
        val bundle = Bundle().apply {
            putInt("reciter_id", reciter.id)
            putString("reciter_name", reciter.nameAr)
            putString("server", reciter.server)
        }
        findNavController().navigate(R.id.reciterSurahsFragment, bundle)
    }
}