package com.zakrni.app.clean.ui.compose.settings

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.zakrni.app.clean.ads.SubscriptionManager
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent

/**
 * Hilt bridge so Compose screens can reach the singleton [SubscriptionManager]
 * without a dedicated ViewModel. The manager is already created and `initialize()`d
 * by [com.zakrni.app.clean.App]; here we only read its flows and launch billing flows.
 */
@EntryPoint
@InstallIn(SingletonComponent::class)
interface SubscriptionEntryPoint {
    fun subscriptionManager(): SubscriptionManager
}

/** Remember the app's [SubscriptionManager] singleton from within Composition. */
@Composable
fun rememberSubscriptionManager(): SubscriptionManager {
    val context = LocalContext.current
    return remember(context) {
        EntryPointAccessors
            .fromApplication(context.applicationContext, SubscriptionEntryPoint::class.java)
            .subscriptionManager()
    }
}

/** Walk the [ContextWrapper] chain to find the host [Activity] (needed to launch billing). */
fun Context.findActivity(): Activity? {
    var ctx = this
    while (ctx is ContextWrapper) {
        if (ctx is Activity) return ctx
        ctx = ctx.baseContext
    }
    return null
}
