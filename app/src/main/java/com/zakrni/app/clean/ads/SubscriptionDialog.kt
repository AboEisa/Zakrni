package com.zakrni.app.clean.ads

import android.app.Activity
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.LinearLayout
import android.widget.TextView
import com.android.billingclient.api.ProductDetails
import com.google.android.material.button.MaterialButton
import com.zakrni.app.R
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collectLatest
import kotlin.math.roundToInt

/**
 * Dialog that shows subscription options to remove ads.
 * Handles loading, error, and product display states.
 */
class SubscriptionDialog(
    private val activity: Activity,
    private val subscriptionManager: SubscriptionManager
) {
    private var dialog: Dialog? = null
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    companion object {
        private const val TAG = "SubscriptionDialog"
    }

    fun show() {
        dialog = Dialog(activity).apply {
            requestWindowFeature(Window.FEATURE_NO_TITLE)
            setContentView(R.layout.dialog_subscription)
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            window?.setLayout(
                (activity.resources.displayMetrics.widthPixels * 0.9).toInt(),
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            setCancelable(true)
            setOnDismissListener { cleanup() }

            val loadingContainer = findViewById<LinearLayout>(R.id.loadingContainer)
            val errorContainer = findViewById<LinearLayout>(R.id.errorContainer)
            val errorText = findViewById<TextView>(R.id.errorText)
            val btnRetry = findViewById<MaterialButton>(R.id.btnRetry)
            val buttonsContainer = findViewById<LinearLayout>(R.id.buttonsContainer)
            val btnMonthly = findViewById<MaterialButton>(R.id.btnMonthlySubscription)
            val btnYearly = findViewById<MaterialButton>(R.id.btnYearlySubscription)
            val btnLater = findViewById<TextView>(R.id.btnLater)

            btnLater.setOnClickListener { dismiss() }
            btnRetry.setOnClickListener {
                loadProducts(loadingContainer, errorContainer, buttonsContainer, errorText,
                    btnMonthly, btnYearly, forceRefresh = true)
            }

            // Start loading products
            loadProducts(loadingContainer, errorContainer, buttonsContainer, errorText,
                btnMonthly, btnYearly, forceRefresh = false)

            show()
        }
    }

    private fun loadProducts(
        loadingContainer: LinearLayout,
        errorContainer: LinearLayout,
        buttonsContainer: LinearLayout,
        errorText: TextView,
        btnMonthly: MaterialButton,
        btnYearly: MaterialButton,
        forceRefresh: Boolean
    ) {
        // Show loading
        loadingContainer.visibility = View.VISIBLE
        errorContainer.visibility = View.GONE
        buttonsContainer.visibility = View.GONE

        if (forceRefresh) {
            subscriptionManager.refreshProducts()
        }

        // Check current products first
        val currentProducts = subscriptionManager.subscriptionProducts.value
        if (currentProducts.isNotEmpty()) {
            showProducts(currentProducts, loadingContainer, errorContainer, buttonsContainer,
                btnMonthly, btnYearly)
            return
        }

        // Observe products with timeout
        scope.launch {
            var loaded = false
            val timeoutJob = launch {
                delay(8000) // 8 second timeout
                if (!loaded) {
                    showError(loadingContainer, errorContainer, buttonsContainer, errorText,
                        activity.getString(R.string.subscription_error_no_plans_details)
                    )
                }
            }

            subscriptionManager.subscriptionProducts.collectLatest { products ->
                if (products.isNotEmpty()) {
                    loaded = true
                    timeoutJob.cancel()
                    showProducts(products, loadingContainer, errorContainer, buttonsContainer,
                        btnMonthly, btnYearly)
                }
            }
        }
    }

    private fun showProducts(
        products: List<ProductDetails>,
        loadingContainer: LinearLayout,
        errorContainer: LinearLayout,
        buttonsContainer: LinearLayout,
        btnMonthly: MaterialButton,
        btnYearly: MaterialButton
    ) {
        loadingContainer.visibility = View.GONE
        errorContainer.visibility = View.GONE
        buttonsContainer.visibility = View.VISIBLE

        val monthlyProduct = products.find { it.productId == SubscriptionManager.MONTHLY_SUB_ID }
        val yearlyProduct = products.find { it.productId == SubscriptionManager.YEARLY_SUB_ID }

        Log.d(TAG, "Products available: monthly=${monthlyProduct != null}, yearly=${yearlyProduct != null}")

        // Monthly
        if (monthlyProduct != null) {
            val price = getDisplayPrice(monthlyProduct, "P1M")
            btnMonthly.text = if (price.isNotEmpty()) {
                activity.getString(R.string.subscription_monthly_with_price_format, price)
            } else {
                activity.getString(R.string.subscription_monthly_title)
            }
            btnMonthly.isEnabled = true
            btnMonthly.setOnClickListener {
                launchPurchase(monthlyProduct)
            }
        } else {
            btnMonthly.text = activity.getString(R.string.subscription_monthly_unavailable)
            btnMonthly.isEnabled = false
        }

        // Yearly
        if (yearlyProduct != null) {
            val price = getDisplayPrice(yearlyProduct, "P1Y")
            val savingsPercent = monthlyProduct?.let { calculateYearlySavingsPercent(it, yearlyProduct) }
            btnYearly.text = if (price.isNotEmpty() && savingsPercent != null) {
                activity.getString(
                    R.string.subscription_yearly_with_price_and_save_format,
                    price,
                    savingsPercent
                )
            } else if (price.isNotEmpty()) {
                activity.getString(R.string.subscription_yearly_with_price_format, price)
            } else if (savingsPercent != null) {
                activity.getString(R.string.subscription_yearly_with_save_title_format, savingsPercent)
            } else {
                activity.getString(R.string.subscription_yearly_title)
            }
            btnYearly.isEnabled = true
            btnYearly.setOnClickListener {
                launchPurchase(yearlyProduct)
            }
        } else {
            btnYearly.text = activity.getString(R.string.subscription_yearly_unavailable)
            btnYearly.isEnabled = false
        }
    }

    private fun showError(
        loadingContainer: LinearLayout,
        errorContainer: LinearLayout,
        buttonsContainer: LinearLayout,
        errorText: TextView,
        message: String
    ) {
        loadingContainer.visibility = View.GONE
        errorContainer.visibility = View.VISIBLE
        buttonsContainer.visibility = View.GONE
        errorText.text = message
    }

    private fun launchPurchase(product: ProductDetails) {
        Log.d(TAG, "Launching purchase for ${product.productId}")
        subscriptionManager.launchSubscriptionFlow(activity, product)
        // Don't dismiss yet - onPurchasesUpdated will handle the result
    }

    private fun cleanup() {
        scope.cancel()
    }

    fun dismiss() {
        dialog?.dismiss()
        dialog = null
    }

    private fun getDisplayPrice(product: ProductDetails, preferredPeriod: String): String {
        val phases = product.subscriptionOfferDetails
            ?.flatMap { it.pricingPhases.pricingPhaseList }
            .orEmpty()

        val preferred = phases.firstOrNull {
            it.billingPeriod == preferredPeriod && it.priceAmountMicros > 0
        }
        val fallback = phases.firstOrNull { it.priceAmountMicros > 0 } ?: phases.firstOrNull()

        return (preferred ?: fallback)?.formattedPrice.orEmpty()
    }

    private fun calculateYearlySavingsPercent(
        monthlyProduct: ProductDetails,
        yearlyProduct: ProductDetails
    ): Int? {
        val monthlyMicros = getPriceMicros(monthlyProduct, "P1M") ?: return null
        val yearlyMicros = getPriceMicros(yearlyProduct, "P1Y") ?: return null

        if (monthlyMicros <= 0L || yearlyMicros <= 0L) return null

        val monthlyTotalForYear = monthlyMicros.toDouble() * 12.0
        val savingsPercent = ((1.0 - (yearlyMicros.toDouble() / monthlyTotalForYear)) * 100.0)
            .roundToInt()

        return savingsPercent.takeIf { it > 0 }
    }

    private fun getPriceMicros(product: ProductDetails, preferredPeriod: String): Long? {
        val phases = product.subscriptionOfferDetails
            ?.flatMap { it.pricingPhases.pricingPhaseList }
            .orEmpty()

        val preferred = phases.firstOrNull {
            it.billingPeriod == preferredPeriod && it.priceAmountMicros > 0
        }
        val fallback = phases.firstOrNull { it.priceAmountMicros > 0 } ?: phases.firstOrNull()

        return (preferred ?: fallback)?.priceAmountMicros
    }
}
