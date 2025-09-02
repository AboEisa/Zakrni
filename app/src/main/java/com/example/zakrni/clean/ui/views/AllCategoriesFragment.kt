package com.example.zakrni.clean.ui.views

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.example.zakrni.R
import com.example.zakrni.databinding.FragmentAllCategoriesBinding

class AllCategoriesFragment : Fragment() {

    private var _binding: FragmentAllCategoriesBinding? = null
    private val binding get() = _binding!!

    private val navOptions by lazy {
        NavOptions.Builder()
            .setEnterAnim(R.anim.animation)
            .setExitAnim(R.anim.animation2)
            .setPopEnterAnim(R.anim.animation3)
            .setPopExitAnim(R.anim.animation4)
            .build()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_all_categories, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentAllCategoriesBinding.bind(view)

        onClick()
    }

    private fun onClick() {
        binding.apply {
            hadithFragment.setOnClickListener {
                findNavController().navigate(
                    AllCategoriesFragmentDirections.actionAllCategoriesFragmentToHadithFragment(),
                    navOptions
                )
            }

            duaFragment.setOnClickListener {
                findNavController().navigate(
                    AllCategoriesFragmentDirections.actionAllCategoriesFragmentToDuaFragment(),
                    navOptions
                )
            }

            azkarFragment.setOnClickListener {
                findNavController().navigate(
                    AllCategoriesFragmentDirections.actionAllCategoriesFragmentToAzkarFragment(),
                    navOptions
                )
            }

            quranFragment.setOnClickListener {
                findNavController().navigate(
                    AllCategoriesFragmentDirections.actionAllCategoriesFragmentToQuran2Fragment(),
                    navOptions
                )
            }

            allahNamesFragment.setOnClickListener {
                findNavController().navigate(
                    AllCategoriesFragmentDirections.actionAllCategoriesFragmentToAllahNamesFragment(),
                    navOptions
                )
            }

            tasbehFragment.setOnClickListener {
                findNavController().navigate(
                    AllCategoriesFragmentDirections.actionAllCategoriesFragmentToTasbehFragment(),
                    navOptions
                )
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}