package com.zakrni.app.clean.ui.utils

import android.app.Activity
import android.content.Context
import android.content.res.Configuration
import android.view.View
import java.util.Locale

object LocaleHelper {
    /**
     * Forces the app's chosen language (default Arabic) onto [context] by building a configuration
     * context. Applied in each Activity/Application `attachBaseContext`, so resources + layout
     * direction reliably match the saved language regardless of the device locale.
     */
    fun wrap(context: Context): Context {
        val lang = context.getSharedPreferences("zakrni_settings", Context.MODE_PRIVATE)
            .getString("app_language", null)?.takeIf { it.isNotBlank() } ?: "ar"
        val locale = Locale(lang)
        Locale.setDefault(locale)
        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)
        return context.createConfigurationContext(config)
    }

    fun isArabic(context: Context): Boolean {
        val locale = context.resources.configuration.locales[0] ?: Locale.getDefault()
        return locale.language.equals("ar", ignoreCase = true)
    }

    fun enforceLtr(activity: Activity) {
        activity.window?.decorView?.layoutDirection = View.LAYOUT_DIRECTION_LTR
    }
}
