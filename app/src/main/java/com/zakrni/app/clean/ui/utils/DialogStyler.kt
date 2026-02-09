package com.zakrni.app.clean.ui.utils

import android.content.Context
import android.graphics.Typeface
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.zakrni.app.R

object DialogStyler {

    fun builder(context: Context): MaterialAlertDialogBuilder {
        return MaterialAlertDialogBuilder(context, R.style.CustomAlertDialogStyle)
    }

    fun apply(dialog: AlertDialog, context: Context) {
        dialog.window?.setBackgroundDrawableResource(R.drawable.bg_tasbeh_dialog)
        dialog.getButton(AlertDialog.BUTTON_POSITIVE)?.apply {
            setTextColor(ContextCompat.getColor(context, R.color.primary))
            setTypeface(typeface, Typeface.BOLD)
            isAllCaps = false
        }
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE)?.apply {
            setTextColor(ContextCompat.getColor(context, R.color.text_secondary))
            isAllCaps = false
        }
        dialog.getButton(AlertDialog.BUTTON_NEUTRAL)?.apply {
            setTextColor(ContextCompat.getColor(context, R.color.text_secondary))
            isAllCaps = false
        }
    }
}
