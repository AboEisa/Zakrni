package com.zakrni.app.clean.ui.adapters

import android.animation.ValueAnimator
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.zakrni.app.R
import com.zakrni.app.databinding.ItemAzkarBinding
import com.zakrni.app.clean.domain.models.DomainHisnDua
import com.zakrni.app.clean.ui.models.*
import com.zakrni.app.clean.ui.utils.AzkarType
import com.zakrni.app.clean.ui.utils.HisnLocalizationUtils
import com.zakrni.app.clean.ui.utils.LocaleHelper

class AzkarAdapter : RecyclerView.Adapter<AzkarAdapter.Holder>() {

    private var azkarSections: List<AzkarSection> = emptyList()
    private var onHeaderClick: ((AzkarType) -> Unit)? = null
    private var onHisnHeaderClick: ((String) -> Unit)? = null
    private var expandedSections: Set<AzkarType> = emptySet()
    private var expandedHisnSections: Set<String> = emptySet()

    fun submitList(
        azkarResponse: PresentationAzkarResponse,
        expandedSections: Set<AzkarType>,
        isArabicUi: Boolean,
        hisnAzkarSections: Map<String, List<Any>> = emptyMap(),
        expandedHisnSections: Set<String> = emptySet()
    ) {
        val isArabic = isArabicUi
        this.expandedSections = expandedSections
        this.expandedHisnSections = expandedHisnSections
        azkarSections = buildList {
            // Morning Azkar
            add(
                AzkarSectionHeader(
                    if (isArabic) "أذكار الصباح" else "Morning adhkar",
                    AzkarType.MORNING,
                    azkarResponse.morning_azkar
                )
            )

            // Evening Azkar
            add(
                AzkarSectionHeader(
                    if (isArabic) "أذكار المساء" else "Evening adhkar",
                    AzkarType.EVENING,
                    azkarResponse.evening_azkar
                )
            )

            // Hisn أذكار sections (from Duas API)
            hisnAzkarSections.entries.forEachIndexed { index, (sectionName, duas) ->
                if (duas.isNotEmpty()) {
                    val sectionEnglish = duas
                        .firstOrNull { it is DomainHisnDua }
                        ?.let { (it as DomainHisnDua).sectionEnglish }

                    add(
                        AzkarSectionHeader(
                            title = HisnLocalizationUtils.localizeSectionTitle(
                                sectionName = sectionName,
                                sectionEnglish = sectionEnglish,
                                isArabic = isArabic,
                                index = index
                            ),
                            azkarType = AzkarType.MISCELLANEOUS,
                            azkarList = duas,
                            sectionKey = sectionName,
                            sectionEnglish = sectionEnglish
                        )
                    )
                }
            }
        }
        notifyDataSetChanged()
    }

    fun setOnHeaderClickListener(listener: (AzkarType) -> Unit) {
        onHeaderClick = listener
    }

    fun setOnHisnHeaderClickListener(listener: (String) -> Unit) {
        onHisnHeaderClick = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val binding = ItemAzkarBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return Holder(binding)
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.bind(azkarSections[position] as AzkarSectionHeader)
    }

    override fun getItemCount(): Int = azkarSections.size

    inner class Holder(private val binding: ItemAzkarBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(header: AzkarSectionHeader) {
            binding.apply {
                tvHeaderTitle.text = header.title

                val sectionKey = header.sectionKey
                val isHisnSection = sectionKey != null
                val isExpanded = if (isHisnSection) {
                    expandedHisnSections.contains(sectionKey)
                } else {
                    expandedSections.contains(header.azkarType)
                }

                // Set initial state
                contentLayout.isVisible = isExpanded
                updateChevronRotation(isExpanded)

                // Remove background from main content layout
                contentLayout.setBackgroundColor(android.graphics.Color.TRANSPARENT)

                if (isExpanded) {
                    populateAzkarItems(header.azkarList)
                }

                headerLayout.setOnClickListener {
                    if (isHisnSection) {
                        onHisnHeaderClick?.invoke(sectionKey!!)
                    } else {
                        onHeaderClick?.invoke(header.azkarType)
                    }
                    toggleContent(header)
                }
            }
        }

        private fun toggleContent(header: AzkarSectionHeader) {
            val sectionKey = header.sectionKey
            val isHisnSection = sectionKey != null
            val isExpanded = if (isHisnSection) {
                expandedHisnSections.contains(sectionKey)
            } else {
                expandedSections.contains(header.azkarType)
            }

            if (isExpanded) {
                // Collapse
                collapseContent()
            } else {
                // Expand
                populateAzkarItems(header.azkarList)
                expandContent()
            }

            animateChevron(!isExpanded)
        }

        private fun populateAzkarItems(azkarList: List<Any>) {
            binding.azkarItemsContainer.removeAllViews()

            azkarList.forEachIndexed { index, azkar ->
                // Create content view programmatically with individual background
                val contentView = createAzkarContentView(azkar, index)
                binding.azkarItemsContainer.addView(contentView)
            }
        }

        private fun createAzkarContentView(azkar: Any, index: Int): View {
            val context = binding.root.context
            val isArabic = LocaleHelper.isArabic(context)

            // Create a LinearLayout for each azkar item with individual background
            val itemLayout = android.widget.LinearLayout(context).apply {
                layoutParams = ViewGroup.MarginLayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                ).apply {
                    // Add margin between items
                    if (index > 0) {
                        topMargin = dpToPx(8)
                    }
                    leftMargin = dpToPx(8)
                    rightMargin = dpToPx(8)
                }
                orientation = android.widget.LinearLayout.VERTICAL
                setPadding(dpToPx(16), dpToPx(16), dpToPx(16), dpToPx(16))
                layoutDirection = View.LAYOUT_DIRECTION_LTR

                // Set individual background with rounded corners
                background = createRoundedBackground()
            }

            // Extract text and count from different azkar types
            val (rawText, count) = when (azkar) {
                is PresentationMorningAzkar -> azkar.text to azkar.count
                is PresentationEveningAzkar -> azkar.text to azkar.count
                is PresentationSleepAzkar -> azkar.text to azkar.count
                is PresentationWakeUpAzkar -> azkar.text to azkar.count
                is PresentationPrayerAzkar -> azkar.text to azkar.count
                is PresentationFoodAzkar -> azkar.text to azkar.count
                is PresentationWuduAzkar -> azkar.text to azkar.count
                is PresentationAdhanAzkar -> azkar.text to azkar.count
                is PresentationMosqueAzkar -> azkar.text to azkar.count
                is PresentationHomeAzkar -> azkar.text to azkar.count
                is PresentationMiscellaneousAzkar -> azkar.text to azkar.count
                is PresentationHajjAndUmrahAzkar -> azkar.text to azkar.count
                is PresentationKhalaAzkar -> azkar.text to azkar.count
                is PresentationPrayerLaterAzkar -> azkar.text to azkar.count
                is DomainHisnDua -> HisnLocalizationUtils.localizeDuaText(azkar, isArabic) to azkar.count
                else -> "Unknown" to 1
            }
            val text = HisnLocalizationUtils.localizeGenericAzkarText(rawText, isArabic)

            // Font scale from settings
            val fontScale = com.zakrni.app.clean.ui.utils.ThemeManager.getFontScale(context)

            // Create count TextView
            val countTextView = TextView(context).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
                this.text = if (isArabic) {
                    if (count > 1) "$count  مرات " else "1 مرة  "
                } else {
                    if (count > 1) "$count times" else "1 time"
                }
                gravity = android.view.Gravity.END
                textSize = 12f * fontScale
                setTextColor(ContextCompat.getColor(context, R.color.text_secondary))
                textDirection = if (isArabic) View.TEXT_DIRECTION_RTL else View.TEXT_DIRECTION_LTR
            }

            // Create azkar text TextView
            val azkarTextView = TextView(context).apply {
                layoutParams = ViewGroup.MarginLayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                ).apply {
                    topMargin = dpToPx(8)
                }
                this.text = text
                gravity = android.view.Gravity.END
                textSize = 16f * fontScale
                setTextColor(ContextCompat.getColor(context, R.color.text_dark_secondary))
                textDirection = if (isArabic) View.TEXT_DIRECTION_RTL else View.TEXT_DIRECTION_LTR
            }

            // Add views to item layout
            itemLayout.addView(countTextView)
            itemLayout.addView(azkarTextView)

            return itemLayout
        }

        private fun createRoundedBackground(): GradientDrawable {
            return GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                setColor(ContextCompat.getColor(binding.root.context, R.color.background_warm_light))
                cornerRadius = dpToPx(12).toFloat()

                // Optional: Add subtle border
                setStroke(dpToPx(1), ContextCompat.getColor(binding.root.context, R.color.stroke_warm))
            }
        }

        private fun dpToPx(dp: Int): Int {
            val context = binding.root.context
            return (dp * context.resources.displayMetrics.density).toInt()
        }

        private fun expandContent() {
            binding.contentLayout.isVisible = true

            // Animate height from 0 to wrap_content
            val content = binding.contentLayout
            content.measure(
                View.MeasureSpec.makeMeasureSpec(binding.root.width, View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
            )
            val targetHeight = content.measuredHeight

            // Reset height to 0 for animation
            val layoutParams = content.layoutParams
            layoutParams.height = 0
            content.layoutParams = layoutParams

            val animator = ValueAnimator.ofInt(0, targetHeight)
            animator.addUpdateListener { animation ->
                val value = animation.animatedValue as Int
                val lp = content.layoutParams
                lp.height = value
                content.layoutParams = lp
            }
            animator.addListener(object : android.animation.Animator.AnimatorListener {
                override fun onAnimationStart(animation: android.animation.Animator) {}
                override fun onAnimationEnd(animation: android.animation.Animator) {
                    // Set to wrap_content after animation
                    val lp = content.layoutParams
                    lp.height = ViewGroup.LayoutParams.WRAP_CONTENT
                    content.layoutParams = lp
                }
                override fun onAnimationCancel(animation: android.animation.Animator) {}
                override fun onAnimationRepeat(animation: android.animation.Animator) {}
            })
            animator.interpolator = AccelerateDecelerateInterpolator()
            animator.duration = 300
            animator.start()
        }

        private fun collapseContent() {
            val content = binding.contentLayout
            val initialHeight = content.height

            val animator = ValueAnimator.ofInt(initialHeight, 0)
            animator.addUpdateListener { animation ->
                val value = animation.animatedValue as Int
                val layoutParams = content.layoutParams
                layoutParams.height = value
                content.layoutParams = layoutParams
                if (value == 0) {
                    content.isGone = true
                }
            }
            animator.interpolator = AccelerateDecelerateInterpolator()
            animator.duration = 300
            animator.start()
        }

        private fun animateChevron(expand: Boolean) {
            val rotation = if (expand) 180f else 0f
            binding.ivChevron.animate()
                .rotation(rotation)
                .setDuration(300)
                .setInterpolator(AccelerateDecelerateInterpolator())
                .start()
        }

        private fun updateChevronRotation(isExpanded: Boolean) {
            binding.ivChevron.rotation = if (isExpanded) 180f else 0f
        }
    }

}
