package com.voltai.smartbatteryguardian.billing

import android.app.Activity
import android.content.Context
import android.util.Log
import com.android.billingclient.api.*
import com.android.billingclient.api.BillingClient.BillingResponseCode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

/**
 * Comprehensive Google Play Billing implementation for VoltAI premium features
 * Handles subscriptions, one-time purchases, and billing state management
 */
@Singleton
class GooglePlayBillingManager
@Inject
constructor(
    private val context: Context,
) : PurchasesUpdatedListener, BillingClientStateListener {
    companion object {
        private const val TAG = "GooglePlayBillingManager"

        // Product IDs (must match Google Play Console)
        const val PREMIUM_MONTHLY_SKU = "premium_monthly"
        const val PREMIUM_YEARLY_SKU = "premium_yearly"
        const val PREMIUM_LIFETIME_SKU = "premium_lifetime"

        // Additional feature SKUs
        const val ADVANCED_ANALYTICS_SKU = "advanced_analytics"
        const val CLOUD_BACKUP_SKU = "cloud_backup"
        const val PREMIUM_THEMES_SKU = "premium_themes"
    }

    private val billingClient: BillingClient =
        BillingClient.newBuilder(context)
            .setListener(this)
            .enablePendingPurchases()
            .build()

    // Billing State Management
    private val _billingState = MutableStateFlow(BillingConnectionState.DISCONNECTED)
    val billingState: StateFlow<BillingConnectionState> = _billingState.asStateFlow()

    private val _purchasedProducts = MutableStateFlow<Set<String>>(emptySet())
    val purchasedProducts: StateFlow<Set<String>> = _purchasedProducts.asStateFlow()

    private val _subscriptionProducts = MutableStateFlow<List<ProductDetails>>(emptyList())
    val subscriptionProducts: StateFlow<List<ProductDetails>> = _subscriptionProducts.asStateFlow()

    private val _inAppProducts = MutableStateFlow<List<ProductDetails>>(emptyList())
    val inAppProducts: StateFlow<List<ProductDetails>> = _inAppProducts.asStateFlow()

    // Purchase callback
    private var purchaseCallback: ((PurchaseResult) -> Unit)? = null

    enum class BillingConnectionState {
        DISCONNECTED,
        CONNECTING,
        CONNECTED,
        ERROR,
    }

    enum class PurchaseResult {
        SUCCESS,
        USER_CANCELLED,
        ERROR,
        ALREADY_PURCHASED,
        PENDING,
    }

    init {
        startConnection()
    }

    /**
     * Initialize billing client connection
     */
    private fun startConnection() {
        if (billingClient.isReady) return

        _billingState.value = BillingConnectionState.CONNECTING
        billingClient.startConnection(this)
    }

    // BillingClientStateListener Implementation
    override fun onBillingSetupFinished(billingResult: BillingResult) {
        when (billingResult.responseCode) {
            BillingResponseCode.OK -> {
                Log.d(TAG, "Billing setup finished successfully")
                _billingState.value = BillingConnectionState.CONNECTED
                queryProducts()
                queryPurchases()
            }
            BillingResponseCode.BILLING_UNAVAILABLE -> {
                Log.w(TAG, "Billing unavailable on this device")
                _billingState.value = BillingConnectionState.ERROR
            }
            else -> {
                Log.w(TAG, "Billing setup failed: ${billingResult.debugMessage}")
                _billingState.value = BillingConnectionState.ERROR
            }
        }
    }

    override fun onBillingServiceDisconnected() {
        Log.w(TAG, "Billing service disconnected")
        _billingState.value = BillingConnectionState.DISCONNECTED
    }

    // PurchasesUpdatedListener Implementation
    override fun onPurchasesUpdated(
        billingResult: BillingResult,
        purchases: MutableList<Purchase>?,
    ) {
        when (billingResult.responseCode) {
            BillingResponseCode.OK -> {
                purchases?.let { processPurchases(it) }
                purchaseCallback?.invoke(PurchaseResult.SUCCESS)
            }
            BillingResponseCode.USER_CANCELED -> {
                Log.d(TAG, "User cancelled purchase")
                purchaseCallback?.invoke(PurchaseResult.USER_CANCELLED)
            }
            BillingResponseCode.ITEM_ALREADY_OWNED -> {
                Log.d(TAG, "Item already owned")
                purchaseCallback?.invoke(PurchaseResult.ALREADY_PURCHASED)
            }
            else -> {
                Log.e(TAG, "Purchase failed: ${billingResult.debugMessage}")
                purchaseCallback?.invoke(PurchaseResult.ERROR)
            }
        }
        purchaseCallback = null
    }

    /**
     * Query available products (subscriptions and in-app purchases)
     */
    private fun queryProducts() {
        // Query subscriptions
        val subscriptionParams =
            QueryProductDetailsParams.newBuilder()
                .setProductList(
                    listOf(
                        QueryProductDetailsParams.Product.newBuilder()
                            .setProductId(PREMIUM_MONTHLY_SKU)
                            .setProductType(BillingClient.ProductType.SUBS)
                            .build(),
                        QueryProductDetailsParams.Product.newBuilder()
                            .setProductId(PREMIUM_YEARLY_SKU)
                            .setProductType(BillingClient.ProductType.SUBS)
                            .build(),
                    ),
                ).build()

        billingClient.queryProductDetailsAsync(subscriptionParams) { billingResult, productDetailsList ->
            if (billingResult.responseCode == BillingResponseCode.OK) {
                _subscriptionProducts.value = productDetailsList
                Log.d(TAG, "Found ${productDetailsList.size} subscription products")
            }
        }

        // Query in-app purchases
        val inAppParams =
            QueryProductDetailsParams.newBuilder()
                .setProductList(
                    listOf(
                        QueryProductDetailsParams.Product.newBuilder()
                            .setProductId(PREMIUM_LIFETIME_SKU)
                            .setProductType(BillingClient.ProductType.INAPP)
                            .build(),
                        QueryProductDetailsParams.Product.newBuilder()
                            .setProductId(ADVANCED_ANALYTICS_SKU)
                            .setProductType(BillingClient.ProductType.INAPP)
                            .build(),
                        QueryProductDetailsParams.Product.newBuilder()
                            .setProductId(CLOUD_BACKUP_SKU)
                            .setProductType(BillingClient.ProductType.INAPP)
                            .build(),
                        QueryProductDetailsParams.Product.newBuilder()
                            .setProductId(PREMIUM_THEMES_SKU)
                            .setProductType(BillingClient.ProductType.INAPP)
                            .build(),
                    ),
                ).build()

        billingClient.queryProductDetailsAsync(inAppParams) { billingResult, productDetailsList ->
            if (billingResult.responseCode == BillingResponseCode.OK) {
                _inAppProducts.value = productDetailsList
                Log.d(TAG, "Found ${productDetailsList.size} in-app products")
            }
        }
    }

    /**
     * Query existing purchases
     */
    private fun queryPurchases() {
        // Query subscription purchases
        billingClient.queryPurchasesAsync(
            QueryPurchasesParams.newBuilder()
                .setProductType(BillingClient.ProductType.SUBS)
                .build(),
        ) { billingResult, purchasesList ->
            if (billingResult.responseCode == BillingResponseCode.OK) {
                processPurchases(purchasesList)
            }
        }

        // Query in-app purchases
        billingClient.queryPurchasesAsync(
            QueryPurchasesParams.newBuilder()
                .setProductType(BillingClient.ProductType.INAPP)
                .build(),
        ) { billingResult, purchasesList ->
            if (billingResult.responseCode == BillingResponseCode.OK) {
                processPurchases(purchasesList)
            }
        }
    }

    /**
     * Process purchase results and update state
     */
    private fun processPurchases(purchases: List<Purchase>) {
        val ownedProducts = mutableSetOf<String>()

        for (purchase in purchases) {
            if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
                // Acknowledge purchase if not acknowledged
                if (!purchase.isAcknowledged) {
                    acknowledgePurchase(purchase)
                }

                // Add to owned products
                ownedProducts.addAll(purchase.products)

                Log.d(TAG, "Processed purchase: ${purchase.products}")
            }
        }

        _purchasedProducts.value = ownedProducts
    }

    /**
     * Acknowledge a purchase
     */
    private fun acknowledgePurchase(purchase: Purchase) {
        val params =
            AcknowledgePurchaseParams.newBuilder()
                .setPurchaseToken(purchase.purchaseToken)
                .build()

        billingClient.acknowledgePurchase(params) { billingResult ->
            if (billingResult.responseCode == BillingResponseCode.OK) {
                Log.d(TAG, "Purchase acknowledged: ${purchase.products}")
            } else {
                Log.e(TAG, "Failed to acknowledge purchase: ${billingResult.debugMessage}")
            }
        }
    }

    /**
     * Launch purchase flow for subscription
     */
    fun purchaseSubscription(
        activity: Activity,
        productDetails: ProductDetails,
        callback: (PurchaseResult) -> Unit,
    ) {
        purchaseCallback = callback

        val offerDetails = productDetails.subscriptionOfferDetails?.firstOrNull()
        if (offerDetails == null) {
            callback(PurchaseResult.ERROR)
            return
        }

        val billingFlowParams =
            BillingFlowParams.newBuilder()
                .setProductDetailsParamsList(
                    listOf(
                        BillingFlowParams.ProductDetailsParams.newBuilder()
                            .setProductDetails(productDetails)
                            .setOfferToken(offerDetails.offerToken)
                            .build(),
                    ),
                ).build()

        billingClient.launchBillingFlow(activity, billingFlowParams)
    }

    /**
     * Launch purchase flow for in-app purchase
     */
    fun purchaseInApp(
        activity: Activity,
        productDetails: ProductDetails,
        callback: (PurchaseResult) -> Unit,
    ) {
        purchaseCallback = callback

        val billingFlowParams =
            BillingFlowParams.newBuilder()
                .setProductDetailsParamsList(
                    listOf(
                        BillingFlowParams.ProductDetailsParams.newBuilder()
                            .setProductDetails(productDetails)
                            .build(),
                    ),
                ).build()

        billingClient.launchBillingFlow(activity, billingFlowParams)
    }

    /**
     * Get product details by SKU
     */
    fun getSubscriptionProductDetails(sku: String): ProductDetails? {
        return _subscriptionProducts.value.find { it.productId == sku }
    }

    fun getInAppProductDetails(sku: String): ProductDetails? {
        return _inAppProducts.value.find { it.productId == sku }
    }

    /**
     * Check if a product is purchased
     */
    fun isPurchased(sku: String): Boolean {
        return _purchasedProducts.value.contains(sku)
    }

    /**
     * Check if user has any premium subscription
     */
    fun hasPremiumSubscription(): Boolean {
        return isPurchased(PREMIUM_MONTHLY_SKU) ||
            isPurchased(PREMIUM_YEARLY_SKU) ||
            isPurchased(PREMIUM_LIFETIME_SKU)
    }

    /**
     * Get premium status type
     */
    fun getPremiumStatus(): PremiumStatusType {
        return when {
            isPurchased(PREMIUM_LIFETIME_SKU) -> PremiumStatusType.LIFETIME
            isPurchased(PREMIUM_YEARLY_SKU) -> PremiumStatusType.YEARLY
            isPurchased(PREMIUM_MONTHLY_SKU) -> PremiumStatusType.MONTHLY
            else -> PremiumStatusType.FREE
        }
    }

    /**
     * Get formatted price for a product
     */
    fun getFormattedPrice(sku: String): String? {
        getSubscriptionProductDetails(sku)?.let { productDetails ->
            return productDetails.subscriptionOfferDetails?.firstOrNull()?.pricingPhases?.pricingPhaseList?.firstOrNull()?.formattedPrice
        }

        getInAppProductDetails(sku)?.let { productDetails ->
            return productDetails.oneTimePurchaseOfferDetails?.formattedPrice
        }

        return null
    }

    /**
     * Restore purchases (re-query from Google Play)
     */
    suspend fun restorePurchases(): RestoreResult =
        suspendCancellableCoroutine { continuation ->
            if (!billingClient.isReady) {
                continuation.resume(RestoreResult.ERROR)
                return@suspendCancellableCoroutine
            }

            var subscriptionsProcessed = false
            var inAppProcessed = false
            val restoredProducts = mutableSetOf<String>()

            fun checkCompletion() {
                if (subscriptionsProcessed && inAppProcessed) {
                    val result =
                        if (restoredProducts.isNotEmpty()) {
                            _purchasedProducts.value = restoredProducts
                            RestoreResult.SUCCESS
                        } else {
                            RestoreResult.NO_PURCHASES
                        }
                    continuation.resume(result)
                }
            }

            // Restore subscriptions
            billingClient.queryPurchasesAsync(
                QueryPurchasesParams.newBuilder()
                    .setProductType(BillingClient.ProductType.SUBS)
                    .build(),
            ) { billingResult, purchasesList ->
                if (billingResult.responseCode == BillingResponseCode.OK) {
                    purchasesList.forEach { purchase ->
                        if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
                            restoredProducts.addAll(purchase.products)
                        }
                    }
                }
                subscriptionsProcessed = true
                checkCompletion()
            }

            // Restore in-app purchases
            billingClient.queryPurchasesAsync(
                QueryPurchasesParams.newBuilder()
                    .setProductType(BillingClient.ProductType.INAPP)
                    .build(),
            ) { billingResult, purchasesList ->
                if (billingResult.responseCode == BillingResponseCode.OK) {
                    purchasesList.forEach { purchase ->
                        if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
                            restoredProducts.addAll(purchase.products)
                        }
                    }
                }
                inAppProcessed = true
                checkCompletion()
            }
        }

    /**
     * Cleanup billing client
     */
    fun cleanup() {
        billingClient.endConnection()
        _billingState.value = BillingConnectionState.DISCONNECTED
    }

    enum class PremiumStatusType {
        FREE,
        MONTHLY,
        YEARLY,
        LIFETIME,
    }

    enum class RestoreResult {
        SUCCESS,
        NO_PURCHASES,
        ERROR,
    }
}
