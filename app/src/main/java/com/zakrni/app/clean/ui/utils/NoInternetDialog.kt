package com.zakrni.app.clean.ui.utils

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.provider.Settings
import android.view.Window
import android.widget.Button
import android.widget.ProgressBar
import com.zakrni.app.R

class NoInternetDialog(private val context: Context) {

    private var dialog: Dialog? = null
    private var onRetry: (() -> Unit)? = null
    private var onCancel: (() -> Unit)? = null
    private var retryRunnable: Runnable? = null
    private var retryButton: Button? = null

    fun show(onRetry: (() -> Unit)? = null, onCancel: (() -> Unit)? = null) {
        if (dialog?.isShowing == true) return

        this.onRetry = onRetry
        this.onCancel = onCancel

        dialog = Dialog(context).apply {
            requestWindowFeature(Window.FEATURE_NO_TITLE)
            setCancelable(false)
            setContentView(R.layout.dialog_no_internet)
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }

        setupDialogViews()
        dialog?.show()
    }

    private fun setupDialogViews() {
        dialog?.let { dialog ->
            retryButton = dialog.findViewById<Button>(R.id.btnRetry)
            val btnSettings = dialog.findViewById<Button>(R.id.btnSettings)
            val btnCancel = dialog.findViewById<Button>(R.id.btnCancel)
            val progressLoading = dialog.findViewById<ProgressBar>(R.id.progress_loading)

            retryButton?.setOnClickListener {
                showLoading(true)
                // Execute the retry callback after a short delay to show the loading animation
                retryRunnable = Runnable {
                    onRetry?.invoke()
                }
                retryButton?.postDelayed(retryRunnable!!, 500)
            }

            btnSettings?.setOnClickListener {
                openWifiSettings()
            }

            btnCancel?.setOnClickListener {
                onCancel?.invoke()
                dismiss()
            }
        }
    }

    fun showLoading(show: Boolean) {
        dialog?.let { dialog ->
            val btnRetry = dialog.findViewById<Button>(R.id.btnRetry)
            val progressLoading = dialog.findViewById<ProgressBar>(R.id.progress_loading)

            if (show) {
                btnRetry?.visibility = android.view.View.GONE
                progressLoading?.visibility = android.view.View.VISIBLE
            } else {
                btnRetry?.visibility = android.view.View.VISIBLE
                progressLoading?.visibility = android.view.View.GONE
            }
        }
    }

    private fun openWifiSettings() {
        val intent = Intent(Settings.ACTION_WIFI_SETTINGS)
        context.startActivity(intent)
    }

    fun dismiss() {
        // Remove any pending callbacks to avoid memory leaks
        retryRunnable?.let { runnable ->
            retryButton?.removeCallbacks(runnable)
        }
        retryRunnable = null
        retryButton = null
        dialog?.dismiss()
        dialog = null
    }

    fun isShowing(): Boolean = dialog?.isShowing == true
}