package com.zakrni.app.widget

import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver

/**
 * Broadcast receiver that hosts [ZakrniGlanceWidget] on the home screen.
 * Registered in AndroidManifest with the APPWIDGET_UPDATE intent-filter + provider meta-data.
 */
class ZakrniGlanceWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = ZakrniGlanceWidget()
}
