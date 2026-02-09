package com.zakrni.app.clean.ui.utils

import android.app.Activity
import android.content.Context
import android.view.View
import java.util.Locale

object LocaleHelper {
    fun wrap(context: Context): Context {
        // Do not force a locale so Android can load resources based on
        // device language or any app-specific language the user selects.
        return context
    }

    fun isArabic(context: Context): Boolean {
        val locale = context.resources.configuration.locales[0] ?: Locale.getDefault()
        return locale.language.equals("ar", ignoreCase = true)
    }

    fun enforceLtr(activity: Activity) {
        activity.window?.decorView?.layoutDirection = View.LAYOUT_DIRECTION_LTR
    }
}
