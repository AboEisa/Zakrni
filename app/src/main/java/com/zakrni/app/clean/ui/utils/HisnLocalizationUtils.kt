package com.zakrni.app.clean.ui.utils

import com.zakrni.app.clean.domain.models.DomainHisnDua

object HisnLocalizationUtils {

    private val sectionTranslations = mapOf(
        "المقدمة" to "Introduction",
        "فضل الذكر" to "Virtues of remembrance",
        "أذكار الاستيقاظ من النوم" to "Adhkar upon waking",
        "دعاء لبس الثوب" to "Supplication for wearing clothes",
        "دعاء لبس الثوب الجديد" to "Supplication for new clothes",
        "الدعاء لمن لبس ثوبا جديدا" to "Supplication for someone wearing new clothes",
        "الدعاء لمن لبس ثوباً جديداً" to "Supplication for someone wearing new clothes",
        "ما يقول إذا وضع الثوب" to "Supplication when removing clothes",
        "دعاء دخول الخلاء" to "Supplication before entering the restroom",
        "دعاء الخروج من الخلاء" to "Supplication after leaving the restroom",
        "الذكر قبل الوضوء" to "Remembrance before ablution",
        "الذكر بعد الفراغ من الوضوء" to "Remembrance after ablution",
        "الذكر عند الخروج من المنزل" to "Remembrance when leaving home",
        "الذكر عند دخول المنزل" to "Remembrance when entering home",
        "دعاء الذهاب إلى المسجد" to "Supplication on the way to the mosque",
        "دعاء دخول المسجد" to "Supplication when entering the mosque",
        "دعاء الخروج من المسجد" to "Supplication when leaving the mosque",
        "أذكار الأذان" to "Adhkar during the adhan",
        "أذكار بعد السلام من الصلاة المفروضة" to "Adhkar after obligatory prayer",
        "أذكار النوم" to "Adhkar before sleep",
        "دعاء القنوت" to "Qunoot supplication",
        "الرقية الشرعية" to "Ruqyah supplications"
    )

    fun localizeSectionTitle(
        sectionName: String,
        sectionEnglish: String?,
        isArabic: Boolean,
        index: Int
    ): String {
        if (isArabic) return sectionName

        sectionEnglish?.trim()?.takeIf { it.isNotBlank() && !containsArabic(it) }?.let { return it }
        sectionTranslations[sectionName]?.let { return it }
        return if (containsArabic(sectionName)) "Supplication section ${index + 1}" else sectionName
    }

    fun localizeDuaText(dua: DomainHisnDua, isArabic: Boolean): String {
        if (isArabic) return dua.arabic

        val englishTranslation = dua.translation?.trim().orEmpty()
        if (englishTranslation.isNotBlank() && !containsArabic(englishTranslation)) {
            return englishTranslation
        }

        val transliteration = dua.transliteration?.trim().orEmpty()
        if (transliteration.isNotBlank() && !containsArabic(transliteration)) {
            return transliteration
        }

        return dua.arabic
    }

    fun localizeGenericAzkarText(text: String, isArabic: Boolean): String {
        if (isArabic) return text
        return text
    }

    private fun containsArabic(text: String): Boolean {
        return ARABIC_REGEX.containsMatchIn(text)
    }

    private val ARABIC_REGEX = Regex("[\\u0600-\\u06FF]")
}
