package com.zakrni.app.clean.ui.adapters

import android.animation.Animator
import android.animation.ValueAnimator
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.zakrni.app.R
import com.zakrni.app.databinding.ItemDuaBinding
import com.zakrni.app.clean.domain.models.DomainHisnDua
import com.zakrni.app.clean.domain.models.DomainHisnSection
import com.zakrni.app.clean.ui.utils.HisnLocalizationUtils
import com.zakrni.app.clean.ui.utils.LocaleHelper

/**
 * Represents a section header with its duas
 */
data class HisnDuaSectionItem(
    val section: DomainHisnSection,
    val duas: List<DomainHisnDua>
)

class HisnDuasAdapter : RecyclerView.Adapter<HisnDuasAdapter.Holder>() {

    private var sections: List<HisnDuaSectionItem> = emptyList()
    private var onHeaderClick: ((String) -> Unit)? = null
    private var expandedSections: Set<String> = emptySet()

    fun submitList(
        sectionsList: List<DomainHisnSection>,
        duasMap: Map<String, List<DomainHisnDua>>,
        expandedSections: Set<String>
    ) {
        this.expandedSections = expandedSections
        sections = sectionsList.map { section ->
            HisnDuaSectionItem(
                section = section,
                duas = duasMap[section.name] ?: emptyList()
            )
        }
        notifyDataSetChanged()
    }

    fun setOnHeaderClickListener(listener: (String) -> Unit) {
        onHeaderClick = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val binding = ItemDuaBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return Holder(binding)
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.bind(sections[position], position)
    }

    override fun getItemCount(): Int = sections.size

    inner class Holder(private val binding: ItemDuaBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: HisnDuaSectionItem, position: Int) {
            val isArabic = LocaleHelper.isArabic(binding.root.context)
            binding.apply {
                tvHeaderTitle.text = HisnLocalizationUtils.localizeSectionTitle(
                    sectionName = item.section.name,
                    sectionEnglish = item.section.englishName ?: item.duas.firstOrNull()?.sectionEnglish,
                    isArabic = isArabic,
                    index = position
                )

                val isExpanded = expandedSections.contains(item.section.name)

                // Set initial state
                contentLayout.isVisible = isExpanded
                updateChevronRotation(isExpanded)

                // Remove background from main content layout
                contentLayout.setBackgroundColor(Color.TRANSPARENT)

                if (isExpanded) {
                    populateDuaItems(item.duas)
                }

                headerLayout.setOnClickListener {
                    onHeaderClick?.invoke(item.section.name)
                    toggleContent(item)
                }
            }
        }

        private fun toggleContent(item: HisnDuaSectionItem) {
            val isExpanded = expandedSections.contains(item.section.name)

            if (isExpanded) {
                // Collapse
                collapseContent()
            } else {
                // Expand
                populateDuaItems(item.duas)
                expandContent()
            }

            animateChevron(!isExpanded)
        }

        private fun populateDuaItems(duaList: List<DomainHisnDua>) {
            binding.duaItemsContainer.removeAllViews()

            duaList.forEachIndexed { index, dua ->
                val contentView = createDuaContentView(dua, index)
                binding.duaItemsContainer.addView(contentView)
            }
        }

        private fun createDuaContentView(dua: DomainHisnDua, index: Int): View {
            val context = binding.root.context
            val isArabic = LocaleHelper.isArabic(context)

            val itemLayout = LinearLayout(context).apply {
                layoutParams = ViewGroup.MarginLayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                ).apply {
                    topMargin = dpToPx(12)
                    leftMargin = dpToPx(12)
                    rightMargin = dpToPx(12)
                    bottomMargin = if (index == 0) dpToPx(4) else dpToPx(4)
                }
                orientation = LinearLayout.HORIZONTAL
                setPadding(dpToPx(16), dpToPx(20), dpToPx(16), dpToPx(20))
                layoutDirection = View.LAYOUT_DIRECTION_LTR
                background = createRoundedBackground()
                gravity = Gravity.CENTER_VERTICAL
            }

            // Font scale from settings
            val fontScale = com.zakrni.app.clean.ui.utils.ThemeManager.getFontScale(context)

            // Create number badge
            val numberBadge = TextView(context).apply {
                layoutParams = LinearLayout.LayoutParams(
                    dpToPx(32),
                    dpToPx(32)
                ).apply {
                    marginStart = dpToPx(12)
                }
                text = "${index + 1}"
                gravity = Gravity.CENTER
                textSize = 14f * fontScale
                setTextColor(ContextCompat.getColor(context, R.color.prayer_text_white))
                background = createCircleBackground()
            }

            // Create dua text TextView
            val duaTextView = TextView(context).apply {
                layoutParams = LinearLayout.LayoutParams(
                    0,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    1f
                )
                this.text = HisnLocalizationUtils.localizeDuaText(dua, isArabic)
                gravity = Gravity.END
                textSize = 20f * fontScale
                setTextColor(ContextCompat.getColor(context, R.color.text_dark))
                textDirection = if (isArabic) View.TEXT_DIRECTION_RTL else View.TEXT_DIRECTION_LTR
                setLineSpacing(12f, 1.4f)
                typeface = android.graphics.Typeface.create("sans-serif", android.graphics.Typeface.NORMAL)
            }

            itemLayout.addView(duaTextView)
            itemLayout.addView(numberBadge)

            return itemLayout
        }

        private fun createCircleBackground(): GradientDrawable {
            return GradientDrawable().apply {
                shape = GradientDrawable.OVAL
                setColor(ContextCompat.getColor(binding.root.context, R.color.quran_header))
            }
        }

        private fun createRoundedBackground(): GradientDrawable {
            return GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                setColor(ContextCompat.getColor(binding.root.context, R.color.card_background))
                cornerRadius = dpToPx(16).toFloat()
                setStroke(dpToPx(1), ContextCompat.getColor(binding.root.context, R.color.stroke_warm))
            }
        }

        private fun dpToPx(dp: Int): Int {
            val context = binding.root.context
            return (dp * context.resources.displayMetrics.density).toInt()
        }

        private fun expandContent() {
            binding.contentLayout.isVisible = true

            val content = binding.contentLayout
            content.measure(
                View.MeasureSpec.makeMeasureSpec(binding.root.width, View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
            )
            val targetHeight = content.measuredHeight

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
            animator.addListener(object : Animator.AnimatorListener {
                override fun onAnimationStart(animation: Animator) {}
                override fun onAnimationEnd(animation: Animator) {
                    val lp = content.layoutParams
                    lp.height = ViewGroup.LayoutParams.WRAP_CONTENT
                    content.layoutParams = lp
                }
                override fun onAnimationCancel(animation: Animator) {}
                override fun onAnimationRepeat(animation: Animator) {}
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
