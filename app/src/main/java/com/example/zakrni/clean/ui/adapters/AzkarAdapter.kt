package com.example.zakrni.clean.ui.adapters

import android.animation.ValueAnimator
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.TextView
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.example.zakrni.databinding.ItemAzkarBinding
import com.example.zakrni.clean.ui.models.*
import com.example.zakrni.clean.ui.utils.AzkarType

class AzkarAdapter : RecyclerView.Adapter<AzkarAdapter.Holder>() {

    private var azkarSections: List<AzkarSection> = emptyList()
    private var onHeaderClick: ((AzkarType) -> Unit)? = null
    private var expandedSections: Set<AzkarType> = emptySet()

    fun submitList(azkarResponse: PresentationAzkarResponse, expandedSections: Set<AzkarType>) {
        this.expandedSections = expandedSections
        azkarSections = buildList {
            // Morning Azkar
            add(AzkarSectionHeader("أذكار الصباح", AzkarType.MORNING, azkarResponse.morning_azkar))

            // Evening Azkar
            add(AzkarSectionHeader("أذكار المساء", AzkarType.EVENING, azkarResponse.evening_azkar))

            // Sleep Azkar
            add(AzkarSectionHeader("أذكار قبل النوم", AzkarType.SLEEP, azkarResponse.sleep_azkar))

            // Wake Up Azkar
            add(AzkarSectionHeader("أذكار الاستيقاظ", AzkarType.WAKE_UP, azkarResponse.wake_up_azkar))

            // Prayer Azkar
            add(AzkarSectionHeader("أذكار الصلاة", AzkarType.PRAYER, azkarResponse.prayer_azkar))

            // Add other sections only if they have data
            if (azkarResponse.food_azkar.isNotEmpty()) {
                add(AzkarSectionHeader("أذكار الطعام", AzkarType.FOOD, azkarResponse.food_azkar))
            }

            if (azkarResponse.wudu_azkar.isNotEmpty()) {
                add(AzkarSectionHeader("أذكار الوضوء", AzkarType.WUDU, azkarResponse.wudu_azkar))
            }

            if (azkarResponse.adhan_azkar.isNotEmpty()) {
                add(AzkarSectionHeader("أذكار الأذان", AzkarType.ADHAN, azkarResponse.adhan_azkar))
            }

            if (azkarResponse.mosque_azkar.isNotEmpty()) {
                add(AzkarSectionHeader("أذكار المسجد", AzkarType.MOSQUE, azkarResponse.mosque_azkar))
            }

            if (azkarResponse.home_azkar.isNotEmpty()) {
                add(AzkarSectionHeader("أذكار البيت", AzkarType.HOME, azkarResponse.home_azkar))
            }

            if (azkarResponse.miscellaneous_azkar.isNotEmpty()) {
                add(AzkarSectionHeader("أذكار متنوعة", AzkarType.MISCELLANEOUS, azkarResponse.miscellaneous_azkar))
            }

            if (azkarResponse.hajj_and_umrah_azkar.isNotEmpty()) {
                add(AzkarSectionHeader("أذكار الحج والعمرة", AzkarType.HAJJ_UMRAH, azkarResponse.hajj_and_umrah_azkar))
            }

            if (azkarResponse.khala_azkar.isNotEmpty()) {
                add(AzkarSectionHeader("أذكار الخلاء", AzkarType.KHALA, azkarResponse.khala_azkar))
            }

            if (azkarResponse.prayer_later_azkar.isNotEmpty()) {
                add(AzkarSectionHeader("أذكار ما بعد الصلاة", AzkarType.PRAYER_LATER, azkarResponse.prayer_later_azkar))
            }
        }
        notifyDataSetChanged()
    }

    fun setOnHeaderClickListener(listener: (AzkarType) -> Unit) {
        onHeaderClick = listener
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

                val isExpanded = expandedSections.contains(header.azkarType)

                // Set initial state
                contentLayout.isVisible = isExpanded
                updateChevronRotation(isExpanded)

                // Remove background from main content layout
                contentLayout.setBackgroundColor(android.graphics.Color.TRANSPARENT)

                if (isExpanded) {
                    populateAzkarItems(header.azkarList)
                }

                headerLayout.setOnClickListener {
                    onHeaderClick?.invoke(header.azkarType)
                    toggleContent(header)
                }
            }
        }

        private fun toggleContent(header: AzkarSectionHeader) {
            val isExpanded = expandedSections.contains(header.azkarType)

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
                layoutDirection = View.LAYOUT_DIRECTION_RTL

                // Set individual background with rounded corners
                background = createRoundedBackground()
            }

            // Extract text and count from different azkar types
            val (text, count) = when (azkar) {
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
                else -> "Unknown" to 1
            }

            // Create count TextView
            val countTextView = TextView(context).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
                this.text = if (count > 1) "$count  مرات " else "1 مرة  "
                gravity = android.view.Gravity.END
                textSize = 12f
                setTextColor(android.graphics.Color.parseColor("#666666"))
                textDirection = View.TEXT_DIRECTION_RTL
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
                textSize = 16f
                setTextColor(android.graphics.Color.parseColor("#333333"))
                textDirection = View.TEXT_DIRECTION_RTL
            }

            // Add views to item layout
            itemLayout.addView(countTextView)
            itemLayout.addView(azkarTextView)

            return itemLayout
        }

        private fun createRoundedBackground(): GradientDrawable {
            return GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                setColor(android.graphics.Color.parseColor("#F0E6D2"))
                cornerRadius = dpToPx(12).toFloat()

                // Optional: Add subtle border
                setStroke(dpToPx(1), android.graphics.Color.parseColor("#E0D5C7"))
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