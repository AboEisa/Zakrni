package com.zakrni.app.clean.ads

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.android.billingclient.api.*
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SubscriptionManager @Inject constructor(
    @ApplicationContext private val context: Context
) : PurchasesUpdatedListener {

    companion object {
        private const val TAG = "SubscriptionManager"
        private const val PREFS_NAME = "subscription_prefs"
        private const val KEY_IS_SUBSCRIBED = "is_subscribed"

        // ⚠️ Replace with your real product ID from Google Play Console
        const val MONTHLY_SUB_ID = "zakrni_premium_monthly"
        const val YEARLY_SUB_ID = "zakrni_premium_yearly"

        // One-time (INAPP) lifetime purchase — must be created as a managed
        // (non-consumable) in-app product in Play Console.
        const val LIFETIME_INAPP_ID = "zakrni_premium_lifetime"

        private val PREMIUM_PRODUCT_IDS = setOf(MONTHLY_SUB_ID, YEARLY_SUB_ID, LIFETIME_INAPP_ID)
    }

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _isSubscribedState = MutableStateFlow(prefs.getBoolean(KEY_IS_SUBSCRIBED, false))
    val isSubscribedState: StateFlow<Boolean> get() = _isSubscribedState

    private val _subscriptionProducts = MutableStateFlow<List<ProductDetails>>(emptyList())
    val subscriptionProducts: StateFlow<List<ProductDetails>> get() = _subscriptionProducts

    /** One-time INAPP products (currently the lifetime purchase). */
    private val _inAppProducts = MutableStateFlow<List<ProductDetails>>(emptyList())
    val inAppProducts: StateFlow<List<ProductDetails>> get() = _inAppProducts

    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    private var billingClient: BillingClient? = null

    // Independent entitlement sources, combined in recomputeEntitlement().
    private var hasActiveSubscription: Boolean = false
    private var hasLifetimePurchase: Boolean = false

    fun isSubscribed(): Boolean = _isSubscribedState.value

    /**
     * Initialize the billing client and check existing subscriptions
     */
    fun initialize() {
        billingClient = BillingClient.newBuilder(context)
            .setListener(this)
            .enablePendingPurchases()
            .build()

        connectBillingClient()
    }

    private fun connectBillingClient() {
        billingClient?.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(result: BillingResult) {
                if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                    Log.d(TAG, "✅ Billing client connected")
                    querySubscriptionProducts()
                    queryInAppProducts()
                    checkExistingSubscriptions()
                    checkExistingInAppPurchases()
                } else {
                    Log.w(TAG, "❌ Billing setup failed: ${result.debugMessage}")
                }
            }

            override fun onBillingServiceDisconnected() {
                Log.w(TAG, "⚠️ Billing service disconnected, retrying...")
                scope.launch {
                    delay(3000)
                    connectBillingClient()
                }
            }
        })
    }

    /**
     * Query available subscription products from Google Play
     */
    private fun querySubscriptionProducts() {
        val productList = listOf(
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(MONTHLY_SUB_ID)
                .setProductType(BillingClient.ProductType.SUBS)
                .build(),
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(YEARLY_SUB_ID)
                .setProductType(BillingClient.ProductType.SUBS)
                .build()
        )

        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(productList)
            .build()

        billingClient?.queryProductDetailsAsync(params) { result, productDetailsList ->
            if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                _subscriptionProducts.value = productDetailsList
                Log.d(TAG, "✅ Found ${productDetailsList.size} subscription products")
                productDetailsList.forEach { product ->
                    Log.d(TAG, "  - ${product.productId}: ${product.name}")
                }
            } else {
                Log.w(TAG, "❌ Failed to query products: ${result.debugMessage}")
            }
        }
    }

    /**
     * Query the one-time (INAPP) products — currently the lifetime purchase.
     */
    private fun queryInAppProducts() {
        val productList = listOf(
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(LIFETIME_INAPP_ID)
                .setProductType(BillingClient.ProductType.INAPP)
                .build()
        )

        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(productList)
            .build()

        billingClient?.queryProductDetailsAsync(params) { result, productDetailsList ->
            if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                _inAppProducts.value = productDetailsList
                Log.d(TAG, "✅ Found ${productDetailsList.size} in-app products")
                productDetailsList.forEach { product ->
                    Log.d(TAG, "  - ${product.productId}: ${product.name}")
                }
            } else {
                Log.w(TAG, "❌ Failed to query in-app products: ${result.debugMessage}")
            }
        }
    }

    /**
     * Force refresh subscription + in-app products (called from dialog/paywall retry)
     */
    fun refreshProducts() {
        Log.d(TAG, "🔄 Refreshing products...")
        if (billingClient?.isReady == true) {
            querySubscriptionProducts()
            queryInAppProducts()
        } else {
            Log.d(TAG, "⚠️ Billing client not ready, reconnecting...")
            connectBillingClient()
        }
    }

    /**
     * Re-query Google Play for purchases the user already owns (subscriptions and
     * the one-time lifetime purchase) and update [isSubscribedState] accordingly.
     * Acknowledges any pending-but-unacknowledged premium purchases.
     */
    fun restorePurchases() {
        Log.d(TAG, "🔄 Restoring purchases...")
        if (billingClient?.isReady == true) {
            checkExistingSubscriptions()
            checkExistingInAppPurchases()
        } else {
            Log.d(TAG, "⚠️ Billing client not ready, reconnecting...")
            connectBillingClient()
        }
    }

    /**
     * Check if user has an active subscription
     */
    private fun checkExistingSubscriptions() {
        billingClient?.queryPurchasesAsync(
            QueryPurchasesParams.newBuilder()
                .setProductType(BillingClient.ProductType.SUBS)
                .build()
        ) { result, purchases ->
            if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                val hasActive = purchases.any { purchase ->
                    purchase.purchaseState == Purchase.PurchaseState.PURCHASED &&
                        purchase.isAcknowledged &&
                        isPremiumPurchase(purchase)
                }
                hasActiveSubscription = hasActive
                recomputeEntitlement()
                Log.d(TAG, "✅ Subscription status: ${if (hasActive) "ACTIVE" else "NOT ACTIVE"}")

                // Acknowledge any unacknowledged purchases
                purchases.filter {
                    it.purchaseState == Purchase.PurchaseState.PURCHASED &&
                        !it.isAcknowledged &&
                        isPremiumPurchase(it)
                }.forEach { purchase ->
                    acknowledgePurchase(purchase)
                }
            }
        }
    }

    /**
     * Check if user owns the one-time lifetime purchase.
     */
    private fun checkExistingInAppPurchases() {
        billingClient?.queryPurchasesAsync(
            QueryPurchasesParams.newBuilder()
                .setProductType(BillingClient.ProductType.INAPP)
                .build()
        ) { result, purchases ->
            if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                val hasLifetime = purchases.any { purchase ->
                    purchase.purchaseState == Purchase.PurchaseState.PURCHASED &&
                        purchase.isAcknowledged &&
                        isPremiumPurchase(purchase)
                }
                hasLifetimePurchase = hasLifetime
                recomputeEntitlement()
                Log.d(TAG, "✅ Lifetime status: ${if (hasLifetime) "OWNED" else "NOT OWNED"}")

                // Acknowledge any unacknowledged lifetime purchases
                purchases.filter {
                    it.purchaseState == Purchase.PurchaseState.PURCHASED &&
                        !it.isAcknowledged &&
                        isPremiumPurchase(it)
                }.forEach { purchase ->
                    acknowledgePurchase(purchase)
                }
            }
        }
    }

    /**
     * Launch subscription purchase flow
     */
    fun launchSubscriptionFlow(activity: Activity, productDetails: ProductDetails) {
        val offerToken = productDetails.subscriptionOfferDetails?.firstOrNull()?.offerToken
        if (offerToken == null) {
            Log.w(TAG, "❌ No offer token found for ${productDetails.productId}")
            return
        }

        val productDetailsParamsList = listOf(
            BillingFlowParams.ProductDetailsParams.newBuilder()
                .setProductDetails(productDetails)
                .setOfferToken(offerToken)
                .build()
        )

        val billingFlowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(productDetailsParamsList)
            .build()

        billingClient?.launchBillingFlow(activity, billingFlowParams)
    }

    /**
     * Launch the one-time (INAPP) purchase flow for the lifetime product.
     * INAPP products have no offer token.
     */
    fun launchInAppPurchaseFlow(activity: Activity, productDetails: ProductDetails) {
        val productDetailsParamsList = listOf(
            BillingFlowParams.ProductDetailsParams.newBuilder()
                .setProductDetails(productDetails)
                .build()
        )

        val billingFlowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(productDetailsParamsList)
            .build()

        billingClient?.launchBillingFlow(activity, billingFlowParams)
    }

    /**
     * Called when a purchase is updated (new purchase or status change)
     */
    override fun onPurchasesUpdated(result: BillingResult, purchases: MutableList<Purchase>?) {
        when (result.responseCode) {
            BillingClient.BillingResponseCode.OK -> {
                purchases?.forEach { purchase ->
                    if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED &&
                        isPremiumPurchase(purchase)
                    ) {
                        acknowledgePurchase(purchase)
                        markPurchaseEntitlement(purchase)
                        Log.d(TAG, "✅ Purchase successful!")
                    }
                }
            }
            BillingClient.BillingResponseCode.USER_CANCELED -> {
                Log.d(TAG, "User cancelled purchase")
            }
            else -> {
                Log.w(TAG, "❌ Purchase error: ${result.debugMessage}")
            }
        }
    }

    /**
     * Acknowledge a purchase (required within 3 days or it gets refunded)
     */
    private fun acknowledgePurchase(purchase: Purchase) {
        if (purchase.isAcknowledged) return

        val params = AcknowledgePurchaseParams.newBuilder()
            .setPurchaseToken(purchase.purchaseToken)
            .build()

        billingClient?.acknowledgePurchase(params) { result ->
            if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                Log.d(TAG, "✅ Purchase acknowledged")
                markPurchaseEntitlement(purchase)
            } else {
                Log.w(TAG, "❌ Failed to acknowledge: ${result.debugMessage}")
            }
        }
    }

    /** Flip the right entitlement flag based on which premium product was purchased. */
    private fun markPurchaseEntitlement(purchase: Purchase) {
        if (purchase.products.contains(LIFETIME_INAPP_ID)) {
            hasLifetimePurchase = true
        }
        if (purchase.products.any { it == MONTHLY_SUB_ID || it == YEARLY_SUB_ID }) {
            hasActiveSubscription = true
        }
        recomputeEntitlement()
    }

    /** Combine subscription + lifetime ownership into the single premium flag. */
    private fun recomputeEntitlement() {
        val isSubscribed = hasActiveSubscription || hasLifetimePurchase
        _isSubscribedState.value = isSubscribed
        prefs.edit().putBoolean(KEY_IS_SUBSCRIBED, isSubscribed).apply()
    }

    private fun isPremiumPurchase(purchase: Purchase): Boolean {
        return purchase.products.any { productId -> productId in PREMIUM_PRODUCT_IDS }
    }

    fun destroy() {
        try {
            billingClient?.let { client ->
                if (client.isReady) {
                    client.endConnection()
                }
            }
        } catch (_: Exception) {}
        billingClient = null
        scope.cancel()
    }
}
