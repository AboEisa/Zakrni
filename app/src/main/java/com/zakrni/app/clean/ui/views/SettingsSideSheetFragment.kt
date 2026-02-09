package com.zakrni.app.clean.ui.views

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.animation.DecelerateInterpolator
import android.view.animation.AccelerateInterpolator
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.zakrni.app.R
import com.zakrni.app.clean.ui.utils.ThemeManager
import com.zakrni.app.databinding.SideSheetSettingsBinding

class SettingsSideSheetFragment : DialogFragment() {

    private var _binding: SideSheetSettingsBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NO_FRAME, R.style.SideSheetDialogTheme)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = SideSheetSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Initial state for animation
        binding.sideSheetCard.translationX = 400f
        binding.dimBackground.alpha = 0f
        
        // Animate in
        animateIn()
        
        loadSettings()
        setupListeners()
    }

    override fun onStart() {
        super.onStart()
        
        dialog?.window?.apply {
            setLayout(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT
            )
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }
    }

    private fun animateIn() {
        val slideIn = ObjectAnimator.ofFloat(binding.sideSheetCard, "translationX", 400f, 0f).apply {
            duration = 350
            interpolator = DecelerateInterpolator(1.5f)
        }
        
        val fadeIn = ObjectAnimator.ofFloat(binding.dimBackground, "alpha", 0f, 1f).apply {
            duration = 300
        }
        
        AnimatorSet().apply {
            playTogether(slideIn, fadeIn)
            start()
        }
    }

    private fun animateOut(onEnd: () -> Unit) {
        val slideOut = ObjectAnimator.ofFloat(binding.sideSheetCard, "translationX", 0f, 400f).apply {
            duration = 280
            interpolator = AccelerateInterpolator(1.2f)
        }
        
        val fadeOut = ObjectAnimator.ofFloat(binding.dimBackground, "alpha", 1f, 0f).apply {
            duration = 250
        }
        
        AnimatorSet().apply {
            playTogether(slideOut, fadeOut)
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    onEnd()
                }
            })
            start()
        }
    }

    private fun loadSettings() {
        val context = requireContext()
        
        binding.switchDarkMode.isChecked = ThemeManager.isDarkMode(context)
        
        // Update dark mode icon
        updateDarkModeIcon(ThemeManager.isDarkMode(context))
    }
    
    private fun updateDarkModeIcon(isDark: Boolean) {
        val iconRes = if (isDark) R.drawable.ic_light_mode else R.drawable.ic_dark_mode
        binding.darkModeIcon.setImageResource(iconRes)
    }

    private fun setupListeners() {
        // Close button
        binding.btnClose.setOnClickListener {
            dismissWithAnimation()
        }
        
        // Dim background click to close
        binding.dimBackground.setOnClickListener {
            dismissWithAnimation()
        }
        
        // Dark Mode - Recreate activity to apply theme properly
        binding.switchDarkMode.setOnCheckedChangeListener { buttonView, isChecked ->
            if (!buttonView.isPressed) return@setOnCheckedChangeListener
            updateDarkModeIcon(isChecked)
            
            // Save the setting first using commit() for synchronous save
            ThemeManager.setDarkMode(requireContext(), isChecked)

            dismiss()
        }
        
        // Menu items
        binding.menuHome.setOnClickListener {
            dismissWithAnimation()
            // Navigate to home if needed
        }
        
        binding.menuMoreApps.setOnClickListener {
            openMoreApps()
        }
        
        binding.btnShareApp.setOnClickListener {
            shareApp()
        }
    }
    
    private fun openMoreApps() {
        try {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=${requireContext().packageName}")))
        } catch (e: Exception) {
            showToast(getString(R.string.cannot_open_store))
        }
    }
    
    private fun shareApp() {
        try {
            val shareText = getString(R.string.share_app_text) +
                "\nhttps://play.google.com/store/apps/details?id=${requireContext().packageName}"
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_title))
                putExtra(Intent.EXTRA_TEXT, shareText)
            }
            startActivity(Intent.createChooser(shareIntent, getString(R.string.share_app_via)))
        } catch (e: Exception) {
            showToast(getString(R.string.share_error))
        }
    }
    
    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }
    
    private fun dismissWithAnimation() {
        animateOut {
            dismiss()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "SettingsSideSheet"
        
        fun newInstance(): SettingsSideSheetFragment {
            return SettingsSideSheetFragment()
        }
    }
}
