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
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.zakrni.app.R
import com.zakrni.app.databinding.ItemDuaBinding
import com.zakrni.app.clean.ui.models.*
import com.zakrni.app.clean.ui.utils.DuaType

class DuasAdapter : RecyclerView.Adapter<DuasAdapter.Holder>() {

    private var duaSections: List<DuaSectionHeader> = emptyList()
    private var onHeaderClick: ((DuaType) -> Unit)? = null
    private var expandedSections: Set<DuaType> = emptySet()

    fun submitList(duaResponse: PresentationDuaResponse, expandedSections: Set<DuaType>) {
        val isArabic = isArabicUi()
        this.expandedSections = expandedSections
        duaSections = buildList {
            // Prophetic Duas
            add(
                DuaSectionHeader(
                    if (isArabic) "الأدعية النبوية" else "Prophetic supplications",
                    DuaType.PROPHETIC,
                    duaResponse.prophetic_duas
                )
            )

            // Prophets Duas
            add(
                DuaSectionHeader(
                    if (isArabic) "أدعية الأنبياء" else "Prophets' supplications",
                    DuaType.PROPHETS,
                    duaResponse.prophets_duas
                )
            )

            // Quran Completion Duas
            add(
                DuaSectionHeader(
                    if (isArabic) "أدعية ختم القرآن" else "Quran completion supplications",
                    DuaType.QURAN_COMPLETION,
                    duaResponse.quran_completion_duas
                )
            )

            // Quran Duas
            add(
                DuaSectionHeader(
                    if (isArabic) "أدعية من القرآن" else "Supplications from the Quran",
                    DuaType.QURAN,
                    duaResponse.quran_duas
                )
            )
        }
        notifyDataSetChanged()
    }

    fun setOnHeaderClickListener(listener: (DuaType) -> Unit) {
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
        holder.bind(duaSections[position])
    }

    override fun getItemCount(): Int = duaSections.size

    inner class Holder(private val binding: ItemDuaBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(header: DuaSectionHeader) {
            binding.apply {
                tvHeaderTitle.text = header.title

                val isExpanded = expandedSections.contains(header.duaType)

                // Set initial state
                contentLayout.isVisible = isExpanded
                updateChevronRotation(isExpanded)

                // Remove background from main content layout
                contentLayout.setBackgroundColor(Color.TRANSPARENT)

                if (isExpanded) {
                    populateDuaItems(header.duaList)
                }

                headerLayout.setOnClickListener {
                    onHeaderClick?.invoke(header.duaType)
                    toggleContent(header)
                }
            }
        }

        private fun toggleContent(header: DuaSectionHeader) {
            val isExpanded = expandedSections.contains(header.duaType)

            if (isExpanded) {
                // Collapse
                collapseContent()
            } else {
                // Expand
                populateDuaItems(header.duaList)
                expandContent()
            }

            animateChevron(!isExpanded)
        }

        private fun populateDuaItems(duaList: List<Any>) {
            binding.duaItemsContainer.removeAllViews()

            duaList.forEachIndexed { index, dua ->
                val contentView = createDuaContentView(dua, index)
                binding.duaItemsContainer.addView(contentView)
            }
        }

        private fun createDuaContentView(dua: Any, index: Int): View {
            val context = binding.root.context

            val itemLayout = LinearLayout(context).apply {
                layoutParams = ViewGroup.MarginLayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                ).apply {
                    if (index > 0) {
                        topMargin = dpToPx(8)
                    }
                    leftMargin = dpToPx(8)
                    rightMargin = dpToPx(8)
                }
                orientation = LinearLayout.VERTICAL
                setPadding(dpToPx(16), dpToPx(16), dpToPx(16), dpToPx(16))
                layoutDirection = View.LAYOUT_DIRECTION_LTR
                background = createRoundedBackground()
            }

            val text = when (dua) {
                is PresentationPropheticDua -> dua.text
                is PresentationProphetsDua -> dua.text
                is PresentationQuranCompletionDua -> dua.text
                is PresentationQuranDua -> dua.text
                else -> "Unknown"
            }

            // Font scale from settings
            val fontScale = com.zakrni.app.clean.ui.utils.ThemeManager.getFontScale(context)

            // Create dua text TextView only (removed count TextView)
            val duaTextView = TextView(context).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
                this.text = text
                gravity = Gravity.END
                textSize = 16f * fontScale
                setTextColor(ContextCompat.getColor(context, R.color.text_dark_secondary))
                textDirection = View.TEXT_DIRECTION_RTL
            }

            // Add only the dua text view to item layout
            itemLayout.addView(duaTextView)

            return itemLayout
        }

        private fun createRoundedBackground(): GradientDrawable {
            return GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                setColor(ContextCompat.getColor(binding.root.context, R.color.background_warm_light))
                cornerRadius = dpToPx(12).toFloat()
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

    private fun isArabicUi(): Boolean {
        val appLocale = AppCompatDelegate.getApplicationLocales()[0]
        val language = appLocale?.language ?: "en"
        return language.equals("ar", ignoreCase = true)
    }
}
