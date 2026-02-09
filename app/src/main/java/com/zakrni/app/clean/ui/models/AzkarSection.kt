package com.zakrni.app.clean.ui.models

import com.zakrni.app.clean.ui.utils.AzkarType

// Base class for AzkarSection
abstract class AzkarSection {
    abstract val type: String
}

// Header section class
data class AzkarSectionHeader(
    val title: String,
    val azkarType: AzkarType,
    val azkarList: List<Any> = emptyList(),
    val sectionKey: String? = null,
    val sectionEnglish: String? = null
) : AzkarSection() {
    override val type: String = "header"
}

// Content section class (if still needed)
data class AzkarSectionContent(
    val text: String,
    val count: Int
) : AzkarSection() {
    override val type: String = "content"
}
