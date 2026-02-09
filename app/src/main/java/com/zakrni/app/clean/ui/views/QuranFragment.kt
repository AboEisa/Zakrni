package com.zakrni.app.clean.ui.views

import android.graphics.Color
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.text.style.RelativeSizeSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.animation.AnimationUtils
import android.widget.SeekBar
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.zakrni.app.R
import com.zakrni.app.clean.domain.models.DomainAyah
import com.zakrni.app.clean.domain.models.DomainSurah
import com.zakrni.app.clean.ui.adapters.QuranAdapter
import com.zakrni.app.clean.ui.utils.AudioPreferences
import com.zakrni.app.clean.ui.utils.LocaleHelper
import com.zakrni.app.clean.ui.viewmodels.QuranViewModel
import com.zakrni.app.databinding.FragmentQuranBinding
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class QuranFragment : Fragment() {
    private var _binding: FragmentQuranBinding? = null
    private val binding get() = _binding!!

    private val viewModel: QuranViewModel by activityViewModels()
    private lateinit var adapter: QuranAdapter
    private var currentSurahNumber: Int = -1
    private var isUserSeeking = false

    @Inject
    lateinit var audioPreferences: AudioPreferences

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentQuranBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Get surah number fresh every time
        currentSurahNumber = getSurahNumber()
        android.util.Log.d("QuranFragment", "🚀 onViewCreated - Surah number: $currentSurahNumber")
        android.util.Log.d("QuranFragment", "📦 Arguments: ${arguments?.keySet()?.joinToString { "$it=${arguments?.get(it)}" }}")

        setupRecyclerView()
        setupClickListeners()
        setupAudioControls()
        setupNotificationCallbacks()

        // Load the surah — ViewModel will skip if already loaded/loading
        if (currentSurahNumber in 1..114) {
            android.util.Log.d("QuranFragment", "📖 Loading surah $currentSurahNumber")

            // Check if ViewModel already has data for this surah (pre-loaded by Quran2Fragment or cached)
            if (viewModel.isSurahLoaded(currentSurahNumber)) {
                // Data is already loaded — display instantly, no loading screen
                android.util.Log.d("QuranFragment", "⚡ Data already available, displaying instantly")
                val existingVerses = viewModel.verses.value
                displayVerses(existingVerses)
                viewModel.currentSurah.value?.let { updateSurahHeader(it) }
            } else {
                // Data not ready — show loading and request it
                showLoading()
                viewModel.loadQuranVerses(currentSurahNumber)
            }
        }

        // Register observers for ongoing updates
        observeViewModel()
    }
    
    private fun setupNotificationCallbacks() {
        // Set callbacks for notification next/previous buttons
        viewModel.audioPlayerManager.onNextSurah = {
            if (currentSurahNumber < 114) {
                lifecycleScope.launch {
                    navigateToSurah(currentSurahNumber + 1)
                    kotlinx.coroutines.delay(500)
                    viewModel.playSurahAudio(currentSurahNumber)
                }
            }
        }
        
        viewModel.audioPlayerManager.onPreviousSurah = {
            if (currentSurahNumber > 1) {
                lifecycleScope.launch {
                    navigateToSurah(currentSurahNumber - 1)
                    kotlinx.coroutines.delay(500)
                    viewModel.playSurahAudio(currentSurahNumber)
                }
            }
        }
    }
    private fun getSurahNumber(): Int {
        android.util.Log.d("QuranFragment", "🔍 getSurahNumber() called")
        android.util.Log.d("QuranFragment", "📦 Raw arguments: $arguments")
        
        // Try multiple ways to get the surah number
        return try {
            // First try Safe Args
            val args = QuranFragmentArgs.fromBundle(requireArguments())
            val safeArgsSurah = args.surahNumber
            android.util.Log.d("QuranFragment", "🎯 Safe Args surahNumber: $safeArgsSurah")
            if (safeArgsSurah in 1..114) {
                android.util.Log.d("QuranFragment", "✅ Got surah from Safe Args: $safeArgsSurah")
                safeArgsSurah
            } else {
                throw Exception("Invalid Safe Args number: $safeArgsSurah")
            }
        } catch (e: Exception) {
            android.util.Log.w("QuranFragment", "⚠️ Safe Args failed: ${e.message}")
            // Try regular arguments
            val bundleSurah = arguments?.getInt("surahNumber", 0) ?: 0
            android.util.Log.d("QuranFragment", "🎯 Bundle surahNumber: $bundleSurah")
            if (bundleSurah in 1..114) {
                android.util.Log.d("QuranFragment", "✅ Got surah from bundle: $bundleSurah")
                bundleSurah
            } else {
                // Try "surah_number" key as well
                val altSurah = arguments?.getInt("surah_number", 0) ?: 0
                android.util.Log.d("QuranFragment", "🎯 Alt surah_number: $altSurah")
                if (altSurah in 1..114) {
                    android.util.Log.d("QuranFragment", "✅ Got surah from alt key: $altSurah")
                    altSurah
                } else {
                    // Default to Al-Fatiha (1) if nothing works
                    android.util.Log.w("QuranFragment", "⚠️ Could not get surah number, defaulting to 1")
                    1
                }
            }
        }
    }

    private fun setupRecyclerView() {
        // RecyclerView is now hidden - Mushaf-style flowing text is used instead
        adapter = QuranAdapter()
    }

    private fun setupAudioControls() {
        // Auto-play control is intentionally hidden from this screen.
        audioPreferences.isAutoPlayEnabled = false
        binding.autoplayToggle?.visibility = View.GONE
        binding.settingsButton?.visibility = View.GONE

        // Keep repeat active by default for the compact player UI.
        if (audioPreferences.repeatMode == AudioPreferences.REPEAT_OFF) {
            audioPreferences.repeatMode = AudioPreferences.REPEAT_ALL
        }

        audioPreferences.selectedReciterName = audioPreferences.getReciterDisplayName(
            audioPreferences.selectedReciter,
            isArabicUi()
        )
        updateReciterNameUI()
        updateRepeatButtonUI()
        
        // Sync reciter with ViewModel to ensure the displayed reciter is used
        val savedReciter = audioPreferences.selectedReciter
        if (viewModel.getCurrentReciter() != savedReciter) {
            viewModel.setReciter(savedReciter)
        }
    }

    private fun setupClickListeners() {
        binding.backButton.setOnClickListener {
            findNavController().navigateUp()
        }

        // Show audio controls
        binding.audioControls.visibility = View.VISIBLE

        // Play/Pause button - Professional implementation
        binding.playButton.setOnClickListener {
            android.util.Log.d("QuranFragment", "🔘 Play button clicked, currentSurahNumber=$currentSurahNumber")
            
            // Prevent multiple clicks while loading
            if (viewModel.isAudioLoading.value) {
                Snackbar.make(
                    binding.root,
                    localizedText("جاري التحميل...", "Loading..."),
                    Snackbar.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }
            
            // Check if valid surah number (this should never happen now with the fix)
            if (currentSurahNumber !in 1..114) {
                android.util.Log.e("QuranFragment", "❌ Invalid surah number: $currentSurahNumber")
                // Try to fix it
                currentSurahNumber = getSurahNumber()
                android.util.Log.d("QuranFragment", "🔄 Retried getSurahNumber, got: $currentSurahNumber")
                
                if (currentSurahNumber !in 1..114) {
                    Snackbar.make(
                        binding.root,
                        localizedText("خطأ في تحميل السورة", "Failed to load surah"),
                        Snackbar.LENGTH_SHORT
                    ).show()
                    return@setOnClickListener
                }
            }
            
            val isPlaying = viewModel.isPlayingSurah(currentSurahNumber)
            val isCurrentSurah = viewModel.audioPlayerManager.isCurrentSurah(currentSurahNumber)
            
            when {
                isPlaying -> {
                    // Currently playing this surah, pause it
                    viewModel.pauseAudio()
                }
                isCurrentSurah && !isPlaying -> {
                    // Paused on this surah, resume it
                    viewModel.resumeAudio()
                }
                else -> {
                    // Not playing anything or playing different surah, start this surah
                    viewModel.playSurahAudio(currentSurahNumber)
                }
            }
        }

        // Previous Surah button - Professional navigation
        binding.previousButton.setOnClickListener {
            // Prevent navigation while audio is loading
            if (viewModel.isAudioLoading.value) {
                Snackbar.make(
                    binding.root,
                    localizedText("الرجاء الانتظار...", "Please wait..."),
                    Snackbar.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }
            
            if (currentSurahNumber > 1) {
                navigateToSurah(currentSurahNumber - 1)
            } else {
                Snackbar.make(
                    binding.root,
                    localizedText("هذه أول سورة", "This is the first surah"),
                    Snackbar.LENGTH_SHORT
                ).show()
            }
        }

        // Next Surah button - Professional navigation
        binding.nextButton.setOnClickListener {
            // Prevent navigation while audio is loading
            if (viewModel.isAudioLoading.value) {
                Snackbar.make(
                    binding.root,
                    localizedText("الرجاء الانتظار...", "Please wait..."),
                    Snackbar.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }
            
            if (currentSurahNumber < 114) {
                navigateToSurah(currentSurahNumber + 1)
            } else {
                Snackbar.make(
                    binding.root,
                    localizedText("هذه آخر سورة", "This is the last surah"),
                    Snackbar.LENGTH_SHORT
                ).show()
            }
        }

        // Repeat button
        binding.repeatButton.setOnClickListener {
            val newMode = audioPreferences.cycleRepeatMode()
            updateRepeatButtonUI()
            val modeText = when (newMode) {
                AudioPreferences.REPEAT_OFF -> localizedText("إيقاف التكرار", "Repeat off")
                AudioPreferences.REPEAT_ONE -> localizedText("تكرار السورة", "Repeat current surah")
                AudioPreferences.REPEAT_ALL -> localizedText("تكرار الكل", "Repeat all")
                else -> ""
            }
            Snackbar.make(binding.root, modeText, Snackbar.LENGTH_SHORT).show()
        }

        // Reciter selector
        binding.reciterSelector.setOnClickListener {
            showReciterBottomSheet()
        }

        // Setup SeekBar listener
        binding.audioProgress.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    isUserSeeking = true
                    binding.currentTime.text = formatTime(progress)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                isUserSeeking = true
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                isUserSeeking = false
                seekBar?.let { bar ->
                    // SeekBar progress is already in milliseconds
                    viewModel.seekToPosition(bar.progress)
                }
            }
        })
    }

    private fun showReciterBottomSheet() {
        val bottomSheet = ReciterBottomSheetFragment.newInstance(audioPreferences.selectedReciter)
        bottomSheet.setOnReciterSelectedListener { identifier, _ ->
            // Check if reciter actually changed
            if (identifier == audioPreferences.selectedReciter) {
                return@setOnReciterSelectedListener
            }

            val wasPlaying = viewModel.isPlayingSurah(currentSurahNumber)
            val displayName = audioPreferences.getReciterDisplayName(identifier, isArabicUi())

            // Update preferences first
            audioPreferences.selectedReciter = identifier
            audioPreferences.selectedReciterName = displayName
            
            // Change reciter (this will stop audio and clear cache)
            viewModel.setReciter(identifier)
            updateReciterNameUI()
            
            // Reset UI
            updatePlayPauseButton(false)
            binding.audioProgress.progress = 0
            binding.currentTime.text = "0:00"

            // Show confirmation message
            Snackbar.make(
                binding.root,
                if (isArabicUi()) "تم اختيار القارئ: $displayName" else "Reciter selected: $displayName",
                Snackbar.LENGTH_SHORT
            ).show()

            // If was playing, start with new reciter after a brief delay
            if (wasPlaying && currentSurahNumber in 1..114) {
                lifecycleScope.launch {
                    kotlinx.coroutines.delay(500) // Brief delay for smooth transition
                    viewModel.playSurahAudio(currentSurahNumber)
                }
            }
        }
        bottomSheet.show(childFragmentManager, "ReciterBottomSheet")
    }

    private fun navigateToSurah(surahNumber: Int) {
        // Stop any currently playing audio
        viewModel.stopAudio()
        
        // Update current surah number
        currentSurahNumber = surahNumber
        
        // Reset UI state for the new surah
        updatePlayPauseButton(false)
        binding.audioProgress.progress = 0
        binding.currentTime.text = "0:00"
        binding.totalTime.text = "0:00"
        
        // Clear old data so ViewModel fetches fresh for new surah
        viewModel.clearVerses()

        // Show loading before fetching
        showLoading()
        
        // Load new surah verses
        viewModel.loadQuranVerses(surahNumber)
        
        // Scroll to top
        binding.mushafScroll.scrollTo(0, 0)
        
        // Show surah change message
        lifecycleScope.launch {
            kotlinx.coroutines.delay(200)
            viewModel.currentSurah.value?.let { surah ->
                val surahTitle = if (isArabicUi()) {
                    surah.name
                } else {
                    surah.englishName.ifBlank { "Surah ${surah.number}" }
                }
                Snackbar.make(
                    binding.root,
                    surahTitle,
                    Snackbar.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun updateReciterNameUI() {
        val displayName = audioPreferences.getReciterDisplayName(
            audioPreferences.selectedReciter,
            isArabicUi()
        )
        binding.reciterName.text = displayName
        audioPreferences.selectedReciterName = displayName
    }

    private fun updateRepeatButtonUI() {
        val mode = audioPreferences.repeatMode
        val tint = when (mode) {
            AudioPreferences.REPEAT_OFF -> ContextCompat.getColor(requireContext(), R.color.white)
            AudioPreferences.REPEAT_ONE -> ContextCompat.getColor(requireContext(), R.color.player_accent)
            AudioPreferences.REPEAT_ALL -> ContextCompat.getColor(requireContext(), R.color.player_accent)
            else -> ContextCompat.getColor(requireContext(), R.color.white)
        }
        binding.repeatButton.setColorFilter(tint)
        binding.repeatStatusText?.text = when (mode) {
            AudioPreferences.REPEAT_OFF -> localizedText("التكرار: إيقاف", "Repeat: Off")
            AudioPreferences.REPEAT_ONE -> localizedText("التكرار: السورة", "Repeat: Current")
            AudioPreferences.REPEAT_ALL -> localizedText("التكرار: الكل", "Repeat: All")
            else -> localizedText("التكرار", "Repeat")
        }
    }

    private fun formatTime(milliseconds: Int): String {
        val totalSeconds = milliseconds / 1000
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return String.format("%d:%02d", minutes, seconds)
    }

    private fun displayVerses(verses: List<DomainAyah>) {
        buildMushafText(verses)
        showMushafContent()

        // Show juz/page info from first ayah
        val firstAyah = verses.first()
        if (isArabicUi()) {
            binding.surahInfoJuz.text = "الجزء ${toArabicNumerals(firstAyah.juz)}"
            binding.surahInfoPage.text = "صفحة ${toArabicNumerals(firstAyah.page)}"
        } else {
            binding.surahInfoJuz.text = "Juz ${firstAyah.juz}"
            binding.surahInfoPage.text = "Page ${firstAyah.page}"
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {

                // Observe verses + isLoading together to avoid race conditions
                launch {
                    combine(
                        viewModel.verses,
                        viewModel.isLoading
                    ) { verses, isLoading -> Pair(verses, isLoading) }
                        .collectLatest { (verses, isLoading) ->
                            if (isLoading) {
                                showLoading()
                            } else if (verses.isNotEmpty()) {
                                displayVerses(verses)
                            }
                        }
                }

                // Observe current surah for header updates
                launch {
                    viewModel.currentSurah.collectLatest { surah ->
                        surah?.let {
                            updateSurahHeader(it)
                        }
                    }
                }

                // Observe errors
                launch {
                    viewModel.error.collectLatest { error ->
                        error?.let {
                            hideLoading()
                            Snackbar.make(binding.root, it, Snackbar.LENGTH_LONG).show()
                            viewModel.clearError()
                        }
                    }
                }

                // Observe non-blocking audio notices (fallback, source switch, etc.)
                launch {
                    viewModel.audioNotice.collectLatest { notice ->
                        if (notice.isNotBlank()) {
                            Snackbar.make(binding.root, notice, Snackbar.LENGTH_SHORT).show()
                        }
                    }
                }

                // Observe audio playing state
                launch {
                    viewModel.isPlaying.collectLatest { isPlaying ->
                        val isCurrentSurah = viewModel.currentPlayingSurah.value == currentSurahNumber
                        updatePlayPauseButton(isPlaying && isCurrentSurah)

                        // Keep screen on while audio is playing
                        if (isPlaying) {
                            activity?.window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                        } else {
                            activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                        }
                    }
                }

                launch {
                    viewModel.currentPlayingSurah.collectLatest { playingSurahNumber ->
                        val isCurrentSurah = playingSurahNumber == currentSurahNumber
                        updatePlayPauseButton(viewModel.isPlaying.value && isCurrentSurah)

                        // Reset progress UI when a different surah is playing
                        if (!isCurrentSurah) {
                            binding.audioProgress.progress = 0
                            binding.audioProgress.max = 100
                            binding.currentTime.text = "0:00"
                            binding.totalTime.text = "0:00"
                        }
                    }
                }

                // Observe audio progress — only show for the currently displayed surah
                launch {
                    viewModel.audioProgress.collectLatest { positionMs ->
                        if (!isUserSeeking && viewModel.currentPlayingSurah.value == currentSurahNumber) {
                            binding.audioProgress.progress = positionMs
                            binding.currentTime.text = formatTime(positionMs)
                        }
                    }
                }

                launch {
                    viewModel.isAudioLoading.collectLatest { isLoading ->
                        updateAudioLoadingState(isLoading)
                    }
                }

                // Observe audio duration — only show for the currently displayed surah
                launch {
                    viewModel.audioDuration.collectLatest { durationMs ->
                        if (viewModel.currentPlayingSurah.value == currentSurahNumber && durationMs > 0) {
                            binding.audioProgress.max = durationMs
                            binding.totalTime.text = formatTime(durationMs)
                        } else if (viewModel.currentPlayingSurah.value != currentSurahNumber) {
                            binding.audioProgress.max = 100
                            binding.totalTime.text = "0:00"
                        }
                    }
                }
            }
        }
    }

    private fun updatePlayPauseButton(isPlaying: Boolean) {
        if (isPlaying) {
            binding.playButton.setImageResource(R.drawable.ic_pause)
        } else {
            binding.playButton.setImageResource(R.drawable.ic_play_arrow)
        }
    }

    /**
     * Professional loading state handling for audio player
     */
    private fun updateAudioLoadingState(isLoading: Boolean) {
        binding.apply {
            playButton.isEnabled = !isLoading
            
            if (isLoading) {
                // Show loading state with visual feedback
                playButton.alpha = 0.6f
                
                // Disable navigation buttons during loading for smooth UX
                previousButton.isEnabled = false
                nextButton.isEnabled = false
                previousButton.alpha = 0.5f
                nextButton.alpha = 0.5f
                
                // Show loading text
                currentTime.text = localizedText("جاري التحميل...", "Loading...")
            } else {
                // Restore normal state
                playButton.alpha = 1.0f
                
                // Re-enable navigation buttons
                previousButton.isEnabled = true
                nextButton.isEnabled = true
                previousButton.alpha = 1.0f
                nextButton.alpha = 1.0f
            }
        }
    }

    private fun updateSurahHeader(surah: DomainSurah) {
        binding.apply {
            val isArabic = isArabicUi()
            val title = if (isArabic) surah.name else surah.englishName.ifBlank { "Surah ${surah.number}" }
            surahHeader.text = title
            surahBadgeName.text = title
            surahType.text = when (surah.revelationType.lowercase()) {
                "meccan" -> if (isArabic) "مكية" else "Meccan"
                "medinan" -> if (isArabic) "مدنية" else "Medinan"
                else -> surah.revelationType
            }
            versesCount.text = if (isArabic) "${surah.ayahs.size} آية" else "${surah.ayahs.size} verses"
            
            // Hide bismillah for Al-Fatiha (part of first ayah) and At-Tawbah (no bismillah)
            val hideBismillah = surah.number == 1 || surah.number == 9
            bismillah.visibility = if (hideBismillah) View.GONE else View.VISIBLE
            dividerBelowBismillah.visibility = if (hideBismillah) View.GONE else View.VISIBLE
        }
    }

    /**
     * Show beautiful loading state with shimmer skeleton
     */
    private fun showLoading() {
        binding.apply {
            mushafScroll.visibility = View.GONE
            loadingContainer.visibility = View.VISIBLE

            // Animate shimmer lines with pulse effect
            val pulseAnim = AnimationUtils.loadAnimation(requireContext(), R.anim.pulse_fade)
            shimmerLinesContainer.startAnimation(pulseAnim)
        }
    }

    /**
     * Hide loading state
     */
    private fun hideLoading() {
        binding.apply {
            shimmerLinesContainer.clearAnimation()
            loadingContainer.visibility = View.GONE
        }
    }

    /**
     * Transition from loading to Mushaf content with fade effect
     */
    private fun showMushafContent() {
        binding.apply {
            shimmerLinesContainer.clearAnimation()
            loadingContainer.visibility = View.GONE

            mushafScroll.alpha = 0f
            mushafScroll.visibility = View.VISIBLE
            mushafScroll.animate()
                .alpha(1f)
                .setDuration(350)
                .start()
        }
    }

    /**
     * Build Mushaf-style flowing text from all ayahs of the surah.
     * Renders verses as continuous Arabic text with inline verse number markers ﴿١﴾
     */
    private fun buildMushafText(verses: List<DomainAyah>) {
        val builder = SpannableStringBuilder()
        val verseNumberColor = ContextCompat.getColor(requireContext(), R.color.traditional_gold)

        for ((index, ayah) in verses.withIndex()) {
            // Append verse text
            builder.append(ayah.text)

            // Add verse number marker ﴿١﴾
            val marker = " \uFD3F${toArabicNumerals(ayah.numberInSurah)}\uFD3E "
            val markerStart = builder.length
            builder.append(marker)
            val markerEnd = builder.length

            // Style the verse number marker
            builder.setSpan(
                ForegroundColorSpan(verseNumberColor),
                markerStart, markerEnd,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            builder.setSpan(
                RelativeSizeSpan(0.75f),
                markerStart, markerEnd,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )

            // Add a small separator between verses (not after the last one)
            if (index < verses.size - 1) {
                builder.append(" ")
            }
        }

        binding.mushafText.text = builder
    }

    /**
     * Convert a number to Arabic-Indic numerals (٠١٢٣٤٥٦٧٨٩)
     */
    private fun toArabicNumerals(number: Int): String {
        val arabicDigits = charArrayOf('٠', '١', '٢', '٣', '٤', '٥', '٦', '٧', '٨', '٩')
        return number.toString().map { arabicDigits[it - '0'] }.joinToString("")
    }

    private fun isArabicUi(): Boolean = LocaleHelper.isArabic(requireContext())

    private fun localizedText(arabic: String, english: String): String {
        return if (isArabicUi()) arabic else english
    }

    override fun onPause() {
        super.onPause()
        // Save current playback position (in milliseconds)
        val currentPosition = viewModel.audioProgress.value
        audioPreferences.savePlaybackState(currentSurahNumber, currentPosition)

        // Audio continues playing in background via AudioPlaybackService
        // No need to pause here - user requested background playback
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Clear the keep screen on flag when leaving fragment
        activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        // Audio continues playing in background via AudioPlaybackService
        _binding = null
    }
}
