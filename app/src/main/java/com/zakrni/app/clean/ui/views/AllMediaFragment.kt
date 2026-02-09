package com.zakrni.app.clean.ui.views

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.zakrni.app.R
import com.zakrni.app.databinding.FragmentAllMediaBinding

class AllMediaFragment : Fragment() {

    private var _binding: FragmentAllMediaBinding? = null
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
        return inflater.inflate(R.layout.fragment_all_media, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentAllMediaBinding.bind(view)

        onClick()
    }

    private fun onClick() {
        binding.apply {
            // الخطب والمحاضرات - دروس إسلامية
            articlesFragment.setOnClickListener {
                findNavController().navigate(
                    AllMediaFragmentDirections.actionAllMediaFragmentToArticleFragment(contentType = "lectures"),
                    navOptions
                )
            }

            // الصوتيات - قرآن وتلاوات
            audiosFragment.setOnClickListener {
                findNavController().navigate(
                    AllMediaFragmentDirections.actionAllMediaFragmentToArticleFragment(contentType = "quran"),
                    navOptions
                )
            }
            
            // الفيديوهات - محتوى إسلامي عام
            videosFragment.setOnClickListener {
                findNavController().navigate(
                    AllMediaFragmentDirections.actionAllMediaFragmentToArticleFragment(contentType = "videos"),
                    navOptions
                )
            }
            
            // الخطب - خطب الجمعة
            sermonsFragment.setOnClickListener {
                findNavController().navigate(
                    AllMediaFragmentDirections.actionAllMediaFragmentToArticleFragment(contentType = "sermons"),
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