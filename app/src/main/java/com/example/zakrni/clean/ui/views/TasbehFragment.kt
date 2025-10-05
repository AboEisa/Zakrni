package com.example.zakrni.clean.ui.views

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.app.AlertDialog
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
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.zakrni.R
import com.example.zakrni.clean.ui.utils.TasbehPreferences
import com.example.zakrni.databinding.FragmentTasbehBinding
import com.google.android.material.snackbar.Snackbar
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
            dhikrList.addAll(savedList)
        } else {
            dhikrList.addAll(
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
            )
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
        countText.text = convertToArabicNumerals("${dhikr.currentCount} من أصل ${dhikr.targetCount}")

        if (dhikr.isCompleted) {
            statusIcon.setImageResource(R.drawable.ic_check_circle)
            statusIcon.setColorFilter(Color.parseColor("#27AE60"))
            container.setBackgroundResource(R.drawable.bg_status_completed)
        } else {
            statusIcon.setImageResource(R.drawable.ic_close_circle)
            statusIcon.setColorFilter(Color.parseColor("#E74C3C"))
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
            "ما شاء الله! تم إكمال ${dhikrList[currentDhikrIndex].arabicText} ✓",
            Snackbar.LENGTH_LONG
        ).setAction("التالي") {
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
            "هل تريد إعادة تعيين العداد الحالي؟",
            Snackbar.LENGTH_LONG
        ).setAction("نعم") {
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
            "إعادة تعيين الكل",
            "حذف التسبيحات المخصصة",
            "استعادة الإعدادات الافتراضية"
        )

        AlertDialog.Builder(requireContext())
            .setTitle("إعدادات التسبيح")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> resetAllCounters()
                    1 -> deleteCustomDhikrs()
                    2 -> restoreDefaults()
                }
            }
            .show()
    }

    private fun showAddDhikrDialog() {
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_add_dhikr, null)

        val dhikrInput = dialogView.findViewById<EditText>(R.id.dhikrInput)
        val countInput = dialogView.findViewById<EditText>(R.id.countInput)

        AlertDialog.Builder(requireContext())
            .setTitle("إضافة تسبيحة جديدة")
            .setView(dialogView)
            .setPositiveButton("إضافة") { _, _ ->
                val dhikrText = dhikrInput.text.toString().trim()
                val count = countInput.text.toString().toIntOrNull() ?: 33

                if (dhikrText.isNotEmpty()) {
                    addCustomDhikr(dhikrText, count)
                }
            }
            .setNegativeButton("إلغاء", null)
            .show()
    }

    private fun showDeleteDhikrDialog(index: Int) {
        AlertDialog.Builder(requireContext())
            .setTitle("حذف التسبيحة")
            .setMessage("هل تريد حذف هذه التسبيحة؟")
            .setPositiveButton("حذف") { _, _ ->
                deleteCustomDhikr(index)
            }
            .setNegativeButton("إلغاء", null)
            .show()
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

        Snackbar.make(binding.root, "تمت الإضافة ✓", Snackbar.LENGTH_SHORT).show()
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

        Snackbar.make(binding.root, "تم حذف التسبيحات المخصصة", Snackbar.LENGTH_SHORT).show()
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

        Snackbar.make(binding.root, "تم إعادة تعيين جميع العدادات", Snackbar.LENGTH_SHORT).show()
    }

    private fun restoreDefaults() {
        dhikrList.clear()
        dhikrList.addAll(
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
        )
        currentDhikrIndex = 0
        currentCount = 0
        countHistory.clear()

        saveState()
        populateDhikrList()
        updateUI()

        Snackbar.make(binding.root, "تم استعادة الإعدادات الافتراضية", Snackbar.LENGTH_SHORT).show()
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
}

