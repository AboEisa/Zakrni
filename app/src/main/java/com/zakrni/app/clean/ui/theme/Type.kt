package com.zakrni.app.clean.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.unit.sp
import com.zakrni.app.R

/**
 * Typography for the Compose redesign.
 *
 * - [QuranFamily]  Amiri Quran — traditional Mushaf face, used for ayah / Arabic display text.
 * - [UiFamily]     The general UI face. Currently the platform default; swapped for a
 *                  Downloadable Google Font (Cairo/Tajawal) in a later step without touching callers.
 */
val QuranFamily = FontFamily(Font(R.font.amiri_quran, FontWeight.Normal))

/** Tajawal — modern, highly legible Arabic + Latin face used across the whole UI. */
val UiFamily: FontFamily = FontFamily(
    Font(R.font.tajawal_light, FontWeight.Light),
    Font(R.font.tajawal_regular, FontWeight.Normal),
    Font(R.font.tajawal_medium, FontWeight.Medium),
    Font(R.font.tajawal_medium, FontWeight.SemiBold),
    Font(R.font.tajawal_bold, FontWeight.Bold),
    Font(R.font.tajawal_extrabold, FontWeight.ExtraBold),
)

private val tightLineHeight = LineHeightStyle(
    alignment = LineHeightStyle.Alignment.Center,
    trim = LineHeightStyle.Trim.None,
)

val ZakrniTypography = Typography(
    displayLarge = TextStyle(fontFamily = UiFamily, fontWeight = FontWeight.Bold, fontSize = 40.sp, lineHeight = 48.sp),
    displayMedium = TextStyle(fontFamily = UiFamily, fontWeight = FontWeight.Bold, fontSize = 32.sp, lineHeight = 40.sp),
    displaySmall = TextStyle(fontFamily = UiFamily, fontWeight = FontWeight.Bold, fontSize = 26.sp, lineHeight = 34.sp),

    headlineLarge = TextStyle(fontFamily = UiFamily, fontWeight = FontWeight.Bold, fontSize = 24.sp, lineHeight = 32.sp),
    headlineMedium = TextStyle(fontFamily = UiFamily, fontWeight = FontWeight.Bold, fontSize = 22.sp, lineHeight = 28.sp),
    headlineSmall = TextStyle(fontFamily = UiFamily, fontWeight = FontWeight.SemiBold, fontSize = 19.sp, lineHeight = 26.sp),

    titleLarge = TextStyle(fontFamily = UiFamily, fontWeight = FontWeight.Bold, fontSize = 20.sp, lineHeight = 26.sp, lineHeightStyle = tightLineHeight),
    titleMedium = TextStyle(fontFamily = UiFamily, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, lineHeight = 22.sp, lineHeightStyle = tightLineHeight),
    titleSmall = TextStyle(fontFamily = UiFamily, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, lineHeight = 20.sp, lineHeightStyle = tightLineHeight),

    bodyLarge = TextStyle(fontFamily = UiFamily, fontWeight = FontWeight.Normal, fontSize = 16.sp, lineHeight = 24.sp),
    bodyMedium = TextStyle(fontFamily = UiFamily, fontWeight = FontWeight.Normal, fontSize = 14.sp, lineHeight = 20.sp),
    bodySmall = TextStyle(fontFamily = UiFamily, fontWeight = FontWeight.Normal, fontSize = 12.sp, lineHeight = 16.sp),

    labelLarge = TextStyle(fontFamily = UiFamily, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, lineHeight = 18.sp),
    labelMedium = TextStyle(fontFamily = UiFamily, fontWeight = FontWeight.SemiBold, fontSize = 12.sp, lineHeight = 16.sp),
    labelSmall = TextStyle(fontFamily = UiFamily, fontWeight = FontWeight.Medium, fontSize = 11.sp, lineHeight = 14.sp),
)

/** Large Mushaf ayah style — for the Quran reader. */
val AyahTextStyle = TextStyle(
    fontFamily = QuranFamily,
    fontWeight = FontWeight.Normal,
    fontSize = 26.sp,
    lineHeight = 46.sp,
)
