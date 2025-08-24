package com.example.zakrni.clean.ui.models

import com.example.zakrni.clean.ui.utils.AzkarType

// Base class for AzkarSection
abstract class AzkarSection {
    abstract val type: String
}

// Header section class
data class AzkarSectionHeader(
    val title: String,
    val azkarType: AzkarType,
    val azkarList: List<Any> = emptyList()
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