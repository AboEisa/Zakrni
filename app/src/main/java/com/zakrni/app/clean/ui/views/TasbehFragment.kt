package com.zakrni.app.clean.ui.views

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.BounceInterpolator
import android.view.animation.OvershootInterpolator
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.zakrni.app.R
import com.zakrni.app.clean.ui.utils.DialogStyler
import com.zakrni.app.clean.ui.utils.LocaleHelper
import com.zakrni.app.clean.ui.utils.TasbehPreferences
import com.zakrni.app.databinding.FragmentTasbehBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputLayout
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class TasbehFragment : Fragment() {

    private var _binding: FragmentTasbehBinding? = null
    private val binding get() = _binding!!

    private lateinit var preferences: TasbehPreferences

    // Dhikr data class
    data class DhikrItem(
        val id: Int,
        val arabicText: String,
        val targetCount: Int,
        val color: String,
        var currentCount: Int = 0,
        var isCompleted: Boolean = false,
        var isCustom: Boolean = false
    )


    private val dhikrList = mutableListOf<DhikrItem>()

    private var currentDhikrIndex = 0
    private var currentCount = 0
    private val countHistory = mutableListOf<Int>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTasbehBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        preferences = TasbehPreferences(requireContext())
        loadState()
        setupListeners()
        populateDhikrList()
        updateUI()
    }

    private fun loadState() {
        val savedList = preferences.getDhikrList()
        if (savedList.isNotEmpty()) {
            dhikrList.clear()
            dhikrList.addAll(localizeSavedDhikrsIfNeeded(savedList))
        } else {
            dhikrList.addAll(getDefaultDhikrs())
        }

        currentDhikrIndex = preferences.getCurrentDhikrIndex()
        if (currentDhikrIndex >= dhikrList.size) {
            currentDhikrIndex = 0
        }
        currentCount = dhikrList.getOrNull(currentDhikrIndex)?.currentCount ?: 0
    }

    private fun saveState() {
        preferences.saveDhikrList(dhikrList)
        preferences.saveCurrentDhikrIndex(currentDhikrIndex)
    }

    private fun setupListeners() {
        binding.backButton.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        binding.menuButton.setOnClickListener {
            showMenuOptions()
        }

        binding.tapButton.setOnClickListener {
            incrementCounter()
        }

        binding.resetCounterButton.setOnClickListener {
            showResetConfirmation()
        }

        binding.undoButton.setOnClickListener {
            undoLastCount()
        }

        binding.addDhikrButton.setOnClickListener {
            showAddDhikrDialog()
        }
    }

    private fun populateDhikrList() {
        binding.dhikrListContainer.removeAllViews()

        dhikrList.forEachIndexed { index, dhikr ->
            val dhikrItemView = createDhikrStatusItem(dhikr, index)
            binding.dhikrListContainer.addView(dhikrItemView)

            if (index < dhikrList.size - 1) {
                val spacer = View(requireContext())
                val params = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    8
                )
                spacer.layoutParams = params
                binding.dhikrListContainer.addView(spacer)
            }
        }
    }

    private fun createDhikrStatusItem(dhikr: DhikrItem, index: Int): View {
        val itemView = LayoutInflater.from(requireContext())
            .inflate(R.layout.item_dikhr, binding.dhikrListContainer, false)

        val statusIcon = itemView.findViewById<ImageView>(R.id.statusIcon)
        val dhikrText = itemView.findViewById<TextView>(R.id.dhikrText)
        val countText = itemView.findViewById<TextView>(R.id.countText)
        val container = itemView.findViewById<LinearLayout>(R.id.itemContainer)

        dhikrText.text = dhikr.arabicText
        countText.text = formatCountText(dhikr.currentCount, dhikr.targetCount)

        if (dhikr.isCompleted) {
            statusIcon.setImageResource(R.drawable.ic_check_circle)
            statusIcon.setColorFilter(ContextCompat.getColor(requireContext(), R.color.status_success))
            container.setBackgroundResource(R.drawable.bg_status_completed)
        } else {
            statusIcon.setImageResource(R.drawable.ic_close_circle)
            statusIcon.setColorFilter(ContextCompat.getColor(requireContext(), R.color.status_error))
            container.setBackgroundResource(R.drawable.bg_status_incompleted)
        }

        if (index == currentDhikrIndex) {
            container.alpha = 1.0f
            itemView.elevation = 8f

            val borderDrawable = when {
                dhikr.isCompleted -> R.drawable.bg_status_completed
                else -> R.drawable.bg_status_incompleted
            }
            container.setBackgroundResource(borderDrawable)

            container.scaleX = 1.02f
            container.scaleY = 1.02f
        } else {
            container.alpha = 0.7f
            itemView.elevation = 2f
            container.scaleX = 1.0f
            container.scaleY = 1.0f
        }

        container.setOnClickListener {
            switchDhikr(index)
        }

        if (dhikr.isCustom) {
            container.setOnLongClickListener {
                showDeleteDhikrDialog(index)
                true
            }
        }

        return itemView
    }

    private fun incrementCounter() {
        val currentDhikr = dhikrList[currentDhikrIndex]

        if (currentCount < currentDhikr.targetCount) {
            countHistory.add(currentCount)
            currentCount++
            currentDhikr.currentCount = currentCount

            animateTapButton()
            animateCounterIncrement()
            animateRipple()

            updateUI()
            saveState()

            if (currentCount == currentDhikr.targetCount) {
                onDhikrCompleted()
            }
        } else {
            animateCompletedFeedback()
        }
    }

    private fun undoLastCount() {
        if (countHistory.isNotEmpty()) {
            currentCount = countHistory.removeAt(countHistory.size - 1)
            dhikrList[currentDhikrIndex].currentCount = currentCount
            dhikrList[currentDhikrIndex].isCompleted = false

            updateUI()
            populateDhikrList()
            saveState()
            animateUndo()
        }
    }

    private fun switchDhikr(index: Int) {
        if (currentDhikrIndex != index) {
            currentDhikrIndex = index
            currentCount = dhikrList[index].currentCount
            countHistory.clear()

            animateDhikrSwitch()
            updateUI()
            populateDhikrList()
            saveState()
        }
    }

    private fun updateUI() {
        val currentDhikr = dhikrList.getOrNull(currentDhikrIndex) ?: return

        binding.dhikrTextArabic.text = currentDhikr.arabicText
        binding.counterText.text = String.format("%02d", currentCount)
        binding.tapButton.setCardBackgroundColor(Color.parseColor(currentDhikr.color))
        populateDhikrList()
    }

    private fun animateTapButton() {
        val scaleDown = AnimatorSet().apply {
            playTogether(
                ObjectAnimator.ofFloat(binding.tapButton, "scaleX", 1f, 0.85f),
                ObjectAnimator.ofFloat(binding.tapButton, "scaleY", 1f, 0.85f)
            )
            duration = 100
        }

        val scaleUp = AnimatorSet().apply {
            playTogether(
                ObjectAnimator.ofFloat(binding.tapButton, "scaleX", 0.85f, 1f),
                ObjectAnimator.ofFloat(binding.tapButton, "scaleY", 0.85f, 1f)
            )
            duration = 100
            interpolator = OvershootInterpolator()
        }

        AnimatorSet().apply {
            playSequentially(scaleDown, scaleUp)
            start()
        }
    }

    private fun animateCounterIncrement() {
        val scaleX = ObjectAnimator.ofFloat(binding.counterText, "scaleX", 1f, 1.2f, 1f)
        val scaleY = ObjectAnimator.ofFloat(binding.counterText, "scaleY", 1f, 1.2f, 1f)

        AnimatorSet().apply {
            playTogether(scaleX, scaleY)
            duration = 200
            interpolator = BounceInterpolator()
            start()
        }
    }

    private fun animateRipple() {
        val fadeIn = ObjectAnimator.ofFloat(binding.rippleEffect, "alpha", 0f, 0.3f)
        val fadeOut = ObjectAnimator.ofFloat(binding.rippleEffect, "alpha", 0.3f, 0f)

        AnimatorSet().apply {
            playSequentially(fadeIn, fadeOut)
            duration = 150
            start()
        }
    }

    private fun animateDhikrSwitch() {
        val fadeOut = ObjectAnimator.ofFloat(binding.dhikrTextArabic, "alpha", 1f, 0f)
        val fadeIn = ObjectAnimator.ofFloat(binding.dhikrTextArabic, "alpha", 0f, 1f)

        fadeOut.duration = 150
        fadeIn.duration = 150

        AnimatorSet().apply {
            playSequentially(fadeOut, fadeIn)
            start()
        }
    }

    private fun animateCompletedFeedback() {
        val shake = ObjectAnimator.ofFloat(binding.tapButton, "translationX", 0f, 25f, -25f, 25f, -25f, 15f, -15f, 6f, -6f, 0f)
        shake.duration = 500
        shake.start()
    }

    private fun animateUndo() {
        val rotate = ObjectAnimator.ofFloat(binding.counterText, "rotation", 0f, -15f, 0f)
        rotate.duration = 300
        rotate.start()
    }

    private fun onDhikrCompleted() {
        dhikrList[currentDhikrIndex].isCompleted = true

        val scaleX = ObjectAnimator.ofFloat(binding.counterText, "scaleX", 1f, 1.3f, 1f)
        val scaleY = ObjectAnimator.ofFloat(binding.counterText, "scaleY", 1f, 1.3f, 1f)
        val rotation = ObjectAnimator.ofFloat(binding.mainCounterCard, "rotation", 0f, 5f, -5f, 0f)

        AnimatorSet().apply {
            playTogether(scaleX, scaleY, rotation)
            duration = 500
            interpolator = BounceInterpolator()
            start()
        }

        Snackbar.make(
            binding.root,
            localizedText(
                "ما شاء الله! تم إكمال ${dhikrList[currentDhikrIndex].arabicText} ✓",
                "Well done! Completed ${dhikrList[currentDhikrIndex].arabicText} ✓"
            ),
            Snackbar.LENGTH_LONG
        ).setAction(localizedText("التالي", "Next")) {
            val nextIncompleteIndex = dhikrList.indexOfFirst { !it.isCompleted && it.id != currentDhikrIndex }
            if (nextIncompleteIndex != -1) {
                switchDhikr(nextIncompleteIndex)
            }
        }.show()

        saveState()
        populateDhikrList()
    }

    private fun showResetConfirmation() {
        Snackbar.make(
            binding.root,
            localizedText("هل تريد إعادة تعيين العداد الحالي؟", "Reset current counter?"),
            Snackbar.LENGTH_LONG
        ).setAction(localizedText("نعم", "Yes")) {
            resetCurrentCounter()
        }.show()
    }

    private fun resetCurrentCounter() {
        currentCount = 0
        dhikrList[currentDhikrIndex].currentCount = 0
        dhikrList[currentDhikrIndex].isCompleted = false
        countHistory.clear()

        updateUI()
        saveState()
        val flash = ObjectAnimator.ofFloat(binding.counterText, "alpha", 1f, 0.3f, 1f)
        flash.duration = 300
        flash.start()
    }

    private fun showMenuOptions() {
        val options = arrayOf(
            localizedText("إعادة تعيين الكل", "Reset all counters"),
            localizedText("حذف التسبيحات المخصصة", "Delete custom tasbeeh"),
            localizedText("استعادة الإعدادات الافتراضية", "Restore defaults")
        )

        val dialog = createTasbehDialogBuilder()
            .setIcon(R.drawable.ic_settings)
            .setTitle(localizedText("إعدادات التسبيح", "Tasbeeh settings"))
            .setItems(options) { _, which ->
                when (which) {
                    0 -> resetAllCounters()
                    1 -> deleteCustomDhikrs()
                    2 -> restoreDefaults()
                }
            }
            .setNegativeButton(localizedText("إغلاق", "Close"), null)
            .create()

        showStyledTasbehDialog(dialog)
    }

    private fun showAddDhikrDialog() {
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_add_dhikr, null)

        val dhikrInput = dialogView.findViewById<EditText>(R.id.dhikrInput)
        val countInput = dialogView.findViewById<EditText>(R.id.countInput)
        val dhikrInputLayout = dialogView.findViewById<TextInputLayout>(R.id.dhikrInputLayout)
        val countInputLayout = dialogView.findViewById<TextInputLayout>(R.id.countInputLayout)
        val subtitleText = dialogView.findViewById<TextView>(R.id.dialogSubtitle)
        subtitleText.text = localizedText(
            "أضف ذِكرًا جديدًا وحدد العدد المستهدف",
            "Add a new dhikr and set the target count"
        )
        countInputLayout.helperText = localizedText("الحد المسموح من ١ إلى ١٠٠٠٠", "Allowed range: 1 to 10000")

        val dialog = createTasbehDialogBuilder()
            .setIcon(R.drawable.ic_add)
            .setTitle(localizedText("إضافة تسبيحة جديدة", "Add new tasbeeh"))
            .setView(dialogView)
            .setPositiveButton(localizedText("إضافة", "Add"), null)
            .setNegativeButton(localizedText("إلغاء", "Cancel"), null)
            .create()

        showStyledTasbehDialog(dialog)
        dialog.getButton(AlertDialog.BUTTON_POSITIVE)?.setOnClickListener {
            dhikrInputLayout.error = null
            countInputLayout.error = null

            val dhikrText = dhikrInput.text.toString().trim()
            val countRaw = countInput.text.toString().trim()
            val count = countRaw.toIntOrNull()

            var hasError = false

            if (dhikrText.isEmpty()) {
                dhikrInputLayout.error = localizedText("اكتب التسبيحة أولًا", "Please enter a tasbeeh text")
                hasError = true
            } else if (dhikrText.length > 120) {
                dhikrInputLayout.error = localizedText("النص طويل، الحد الأقصى 120 حرفًا", "Text is too long, max 120 chars")
                hasError = true
            }

            if (countRaw.isEmpty()) {
                countInputLayout.error = localizedText("حدد العدد المستهدف", "Please enter a target count")
                hasError = true
            } else if (count == null || count !in 1..10_000) {
                countInputLayout.error = localizedText("العدد لازم يكون بين 1 و 10000", "Count must be between 1 and 10000")
                hasError = true
            }

            if (!hasError && count != null) {
                addCustomDhikr(dhikrText, count)
                dialog.dismiss()
            }
        }
    }

    private fun showDeleteDhikrDialog(index: Int) {
        val dialog = createTasbehDialogBuilder()
            .setIcon(R.drawable.ic_close_circle)
            .setTitle(localizedText("حذف التسبيحة", "Delete tasbeeh"))
            .setMessage(localizedText("هل تريد حذف هذه التسبيحة؟", "Do you want to delete this tasbeeh?"))
            .setPositiveButton(localizedText("حذف", "Delete")) { _, _ ->
                deleteCustomDhikr(index)
            }
            .setNegativeButton(localizedText("إلغاء", "Cancel"), null)
            .create()

        showStyledTasbehDialog(dialog)
    }

    private fun createTasbehDialogBuilder(): MaterialAlertDialogBuilder = DialogStyler.builder(requireContext())

    private fun showStyledTasbehDialog(dialog: AlertDialog) {
        dialog.show()
        DialogStyler.apply(dialog, requireContext())
    }

    private fun addCustomDhikr(text: String, count: Int) {
        val colors = listOf("#27AE60", "#E67E22", "#3498DB", "#9B59B6", "#E74C3C", "#16A085")
        val randomColor = colors.random()

        val newDhikr = DhikrItem(
            id = dhikrList.size,
            arabicText = text,
            targetCount = count,
            color = randomColor,
            isCustom = true
        )

        dhikrList.add(newDhikr)
        saveState()
        populateDhikrList()

        Snackbar.make(
            binding.root,
            localizedText("تمت الإضافة ✓", "Added successfully ✓"),
            Snackbar.LENGTH_SHORT
        ).show()
    }

    private fun deleteCustomDhikr(index: Int) {
        dhikrList.removeAt(index)

        if (currentDhikrIndex >= dhikrList.size) {
            currentDhikrIndex = dhikrList.size - 1
        }
        if (currentDhikrIndex == index) {
            currentDhikrIndex = 0
            currentCount = dhikrList[0].currentCount
        }

        saveState()
        populateDhikrList()
        updateUI()
    }

    private fun deleteCustomDhikrs() {
        dhikrList.removeAll { it.isCustom }
        currentDhikrIndex = 0
        currentCount = dhikrList[0].currentCount
        saveState()
        populateDhikrList()
        updateUI()

        Snackbar.make(
            binding.root,
            localizedText("تم حذف التسبيحات المخصصة", "Custom tasbeeh deleted"),
            Snackbar.LENGTH_SHORT
        ).show()
    }

    private fun resetAllCounters() {
        dhikrList.forEach {
            it.currentCount = 0
            it.isCompleted = false
        }
        currentCount = 0
        currentDhikrIndex = 0
        countHistory.clear()

        saveState()
        updateUI()

        Snackbar.make(
            binding.root,
            localizedText("تم إعادة تعيين جميع العدادات", "All counters reset"),
            Snackbar.LENGTH_SHORT
        ).show()
    }

    private fun restoreDefaults() {
        dhikrList.clear()
        dhikrList.addAll(getDefaultDhikrs())
        currentDhikrIndex = 0
        currentCount = 0
        countHistory.clear()

        saveState()
        populateDhikrList()
        updateUI()

        Snackbar.make(
            binding.root,
            localizedText("تم استعادة الإعدادات الافتراضية", "Default settings restored"),
            Snackbar.LENGTH_SHORT
        ).show()
    }

    private fun convertToArabicNumerals(input: String): String {
        val arabicNumerals = arrayOf("٠", "١", "٢", "٣", "٤", "٥", "٦", "٧", "٨", "٩")
        var result = input
        for (i in 0..9) {
            result = result.replace(i.toString(), arabicNumerals[i])
        }
        return result
    }

    override fun onPause() {
        super.onPause()
        saveState()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun getDefaultDhikrs(): List<DhikrItem> {
        return if (LocaleHelper.isArabic(requireContext())) {
            listOf(
                DhikrItem(0, "سبحان الله", 33, "#27AE60"),
                DhikrItem(1, "الحمد لله", 33, "#E67E22"),
                DhikrItem(2, "لا إله إلا الله", 33, "#3498DB"),
                DhikrItem(
                    3,
                    "لا إله إلا الله وحده لا شريك له له الملك وله الحمد وهو على كل شيء قدير",
                    10,
                    "#9B59B6"
                )
            )
        } else {
            listOf(
                DhikrItem(0, "Subhan Allah", 33, "#27AE60"),
                DhikrItem(1, "Alhamdulillah", 33, "#E67E22"),
                DhikrItem(2, "La ilaha illa Allah", 33, "#3498DB"),
                DhikrItem(
                    3,
                    "La ilaha illa Allah wahdahu la sharika lahu, lahul-mulku wa lahul-hamdu wa huwa ala kulli shay'in qadeer",
                    10,
                    "#9B59B6"
                )
            )
        }
    }

    private fun localizeSavedDhikrsIfNeeded(saved: List<DhikrItem>): List<DhikrItem> {
        val isArabic = LocaleHelper.isArabic(requireContext())
        val arToEn = mapOf(
            "سبحان الله" to "Subhan Allah",
            "الحمد لله" to "Alhamdulillah",
            "لا إله إلا الله" to "La ilaha illa Allah",
            "لا إله إلا الله وحده لا شريك له له الملك وله الحمد وهو على كل شيء قدير" to
                    "La ilaha illa Allah wahdahu la sharika lahu, lahul-mulku wa lahul-hamdu wa huwa ala kulli shay'in qadeer"
        )
        val enToAr = arToEn.entries.associate { (ar, en) -> en to ar }

        return saved.map { item ->
            if (item.isCustom) return@map item
            if (isArabic) {
                enToAr[item.arabicText]?.let { item.copy(arabicText = it) } ?: item
            } else {
                arToEn[item.arabicText]?.let { item.copy(arabicText = it) } ?: item
            }
        }
    }

    private fun formatCountText(current: Int, target: Int): String {
        val rawText = localizedText("$current من أصل $target", "$current of $target")
        return if (LocaleHelper.isArabic(requireContext())) {
            convertToArabicNumerals(rawText)
        } else {
            rawText
        }
    }

    private fun localizedText(arabic: String, english: String): String {
        return if (LocaleHelper.isArabic(requireContext())) arabic else english
    }
}
