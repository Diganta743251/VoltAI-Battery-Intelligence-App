package com.voltai.smartbatteryguardian.premium

import android.app.Activity
import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.voltai.smartbatteryguardian.billing.GooglePlayBillingManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages premium features and billing for VoltAI Smart Battery Guardian
 */
@Singleton
class PremiumManager @Inject constructor(
    private val context: Context,
    private val billingManager: GooglePlayBillingManager
) {

    private val _premiumStatus = MutableStateFlow(PremiumStatus.FREE)
    val premiumStatus: StateFlow<PremiumStatus> = _premiumStatus.asStateFlow()

    private val _billingState = MutableStateFlow(BillingState.DISCONNECTED)
    val billingState: StateFlow<BillingState> = _billingState.asStateFlow()

    private val scope = CoroutineScope(Dispatchers.Main)

    enum class PremiumStatus {
        FREE,
        PREMIUM_MONTHLY,
        PREMIUM_YEARLY,
        PREMIUM_LIFETIME
    }

    enum class BillingState {
        DISCONNECTED,
        CONNECTING,
        CONNECTED,
        ERROR
    }

    data class PremiumFeature(
        val id: String,
        val title: String,
        val description: String,
        val isPremium: Boolean,
        val category: FeatureCategory
    )

    enum class FeatureCategory {
        ANALYTICS,
        OPTIMIZATION,
        CUSTOMIZATION,
        BACKUP,
        ALERTS,
        WIDGETS
    }

    data class PricingPlan(
        val sku: String,
        val title: String,
        val description: String,
        val price: String,
        val originalPrice: String? = null,
        val discount: Int? = null,
        val features: List<String>,
        val isPopular: Boolean = false
    )

    // Premium Features Definition
    val premiumFeatures = listOf(
        PremiumFeature(
            id = "advanced_analytics",
            title = "Advanced Analytics",
            description = "Detailed battery analytics with 6-month history and predictive modeling",
            isPremium = true,
            category = FeatureCategory.ANALYTICS
        ),
        PremiumFeature(
            id = "thermal_management",
            title = "Thermal Management",
            description = "Real-time temperature monitoring and automatic cooling recommendations",
            isPremium = true,
            category = FeatureCategory.OPTIMIZATION
        ),
        PremiumFeature(
            id = "custom_themes",
            title = "Custom Themes",
            description = "12+ premium themes and customizable color schemes",
            isPremium = true,
            category = FeatureCategory.CUSTOMIZATION
        ),
        PremiumFeature(
            id = "cloud_backup",
            title = "Cloud Backup",
            description = "Automatic encrypted backup to Google Drive with sync across devices",
            isPremium = true,
            category = FeatureCategory.BACKUP
        ),
        PremiumFeature(
            id = "smart_alerts",
            title = "Smart Alerts",
            description = "AI-powered battery alerts and health notifications",
            isPremium = true,
            category = FeatureCategory.ALERTS
        ),
        PremiumFeature(
            id = "premium_widgets",
            title = "Premium Widgets",
            description = "Advanced home screen widgets with customizable layouts",
            isPremium = true,
            category = FeatureCategory.WIDGETS
        ),
        PremiumFeature(
            id = "unlimited_history",
            title = "Unlimited History",
            description = "Store unlimited battery history (Free: 7 days)",
            isPremium = true,
            category = FeatureCategory.ANALYTICS
        ),
        PremiumFeature(
            id = "export_data",
            title = "Export Data",
            description = "Export battery data to CSV/JSON formats",
            isPremium = true,
            category = FeatureCategory.ANALYTICS
        ),
        // Free Features
        PremiumFeature(
            id = "basic_monitoring",
            title = "Basic Battery Monitoring",
            description = "Real-time battery level, temperature, and charging status",
            isPremium = false,
            category = FeatureCategory.ANALYTICS
        ),
        PremiumFeature(
            id = "basic_notifications",
            title = "Basic Notifications",
            description = "Low battery and charging complete notifications",
            isPremium = false,
            category = FeatureCategory.ALERTS
        ),
        PremiumFeature(
            id = "home_cards",
            title = "Customizable Cards",
            description = "Drag-and-drop home screen cards with basic customization",
            isPremium = false,
            category = FeatureCategory.CUSTOMIZATION
        )
    )

    // Pricing Plans
    val pricingPlans = listOf(
        PricingPlan(
            sku = "premium_monthly",
            title = "Premium Monthly",
            description = "Full access to all premium features",
            price = "$2.99",
            features = listOf(
                "Advanced Analytics",
                "Thermal Management", 
                "Cloud Backup",
                "Smart Alerts",
                "Premium Themes",
                "Premium Widgets",
                "Unlimited History",
                "Data Export",
                "Priority Support"
            )
        ),
        PricingPlan(
            sku = "premium_yearly",
            title = "Premium Yearly",
            description = "Best value - Save 50%",
            price = "$17.99",
            originalPrice = "$35.88",
            discount = 50,
            features = listOf(
                "All Premium Monthly features",
                "50% savings",
                "Priority support",
                "Early access to new features"
            ),
            isPopular = true
        ),
        PricingPlan(
            sku = "premium_lifetime",
            title = "Lifetime Premium",
            description = "One-time purchase, lifetime access",
            price = "$49.99",
            originalPrice = "$99.99",
            discount = 50,
            features = listOf(
                "All premium features forever",
                "Future updates included",
                "VIP support",
                "Early beta access",
                "One-time payment"
            )
        )
    )

    init {
        // Initialize billing connection
        initializeBilling()
    }

    private fun initializeBilling() {
        scope.launch {
            // Monitor billing state
            billingManager.billingState.collect { state ->
                _billingState.value = when (state) {
                    GooglePlayBillingManager.BillingConnectionState.DISCONNECTED -> BillingState.DISCONNECTED
                    GooglePlayBillingManager.BillingConnectionState.CONNECTING -> BillingState.CONNECTING
                    GooglePlayBillingManager.BillingConnectionState.CONNECTED -> {
                        checkPremiumStatus()
                        BillingState.CONNECTED
                    }
                    GooglePlayBillingManager.BillingConnectionState.ERROR -> BillingState.ERROR
                }
            }
        }
    }

    private suspend fun checkPremiumStatus() {
        // Check actual purchases from Google Play Billing
        val premiumStatus = when (billingManager.getPremiumStatus()) {
            GooglePlayBillingManager.PremiumStatusType.MONTHLY -> PremiumStatus.PREMIUM_MONTHLY
            GooglePlayBillingManager.PremiumStatusType.YEARLY -> PremiumStatus.PREMIUM_YEARLY
            GooglePlayBillingManager.PremiumStatusType.LIFETIME -> PremiumStatus.PREMIUM_LIFETIME
            GooglePlayBillingManager.PremiumStatusType.FREE -> PremiumStatus.FREE
        }
        _premiumStatus.value = premiumStatus
    }

    /**
     * Check if a specific feature is available
     */
    fun isFeatureAvailable(featureId: String): Boolean {
        val feature = premiumFeatures.find { it.id == featureId }
        return if (feature?.isPremium == true) {
            isPremium()
        } else {
            true // Free features are always available
        }
    }

    /**
     * Check if user has premium access
     */
    fun isPremium(): Boolean {
        return _premiumStatus.value != PremiumStatus.FREE
    }

    /**
     * Get premium features by category
     */
    fun getFeaturesByCategory(category: FeatureCategory): List<PremiumFeature> {
        return premiumFeatures.filter { it.category == category }
    }

    /**
     * Get premium-only features
     */
    fun getPremiumOnlyFeatures(): List<PremiumFeature> {
        return premiumFeatures.filter { it.isPremium }
    }

    /**
     * Launch purchase flow
     */
    fun purchasePremium(activity: Activity, sku: String, callback: (PurchaseResult) -> Unit) {
        scope.launch {
            try {
                // Check if it's a subscription or in-app purchase
                val subscriptionProduct = billingManager.getSubscriptionProductDetails(sku)
                val inAppProduct = billingManager.getInAppProductDetails(sku)
                
                when {
                    subscriptionProduct != null -> {
                        billingManager.purchaseSubscription(activity, subscriptionProduct) { result ->
                            val mappedResult = mapPurchaseResult(result)
                            callback(mappedResult)
                            if (mappedResult == PurchaseResult.SUCCESS) {
                                scope.launch { checkPremiumStatus() }
                            }
                        }
                    }
                    inAppProduct != null -> {
                        billingManager.purchaseInApp(activity, inAppProduct) { result ->
                            val mappedResult = mapPurchaseResult(result)
                            callback(mappedResult)
                            if (mappedResult == PurchaseResult.SUCCESS) {
                                scope.launch { checkPremiumStatus() }
                            }
                        }
                    }
                    else -> {
                        callback(PurchaseResult.ERROR)
                    }
                }
            } catch (e: Exception) {
                callback(PurchaseResult.ERROR)
            }
        }
    }

    /**
     * Restore purchases
     */
    fun restorePurchases(callback: (RestoreResult) -> Unit) {
        scope.launch {
            try {
                val result = billingManager.restorePurchases()
                val mappedResult = when (result) {
                    GooglePlayBillingManager.RestoreResult.SUCCESS -> {
                        checkPremiumStatus()
                        RestoreResult.SUCCESS
                    }
                    GooglePlayBillingManager.RestoreResult.NO_PURCHASES -> RestoreResult.NO_PURCHASES
                    GooglePlayBillingManager.RestoreResult.ERROR -> RestoreResult.ERROR
                }
                callback(mappedResult)
            } catch (e: Exception) {
                callback(RestoreResult.ERROR)
            }
        }
    }
    
    private fun mapPurchaseResult(billingResult: GooglePlayBillingManager.PurchaseResult): PurchaseResult {
        return when (billingResult) {
            GooglePlayBillingManager.PurchaseResult.SUCCESS -> PurchaseResult.SUCCESS
            GooglePlayBillingManager.PurchaseResult.USER_CANCELLED -> PurchaseResult.CANCELLED
            GooglePlayBillingManager.PurchaseResult.ALREADY_PURCHASED -> PurchaseResult.ALREADY_OWNED
            else -> PurchaseResult.ERROR
        }
    }

    /**
     * Show premium paywall for feature
     */
    fun showPremiumPaywall(featureId: String): PremiumPaywallData {
        val feature = premiumFeatures.find { it.id == featureId }
        return PremiumPaywallData(
            title = "Unlock ${feature?.title ?: "Premium Feature"}",
            description = feature?.description ?: "This feature requires premium access",
            benefits = getPremiumBenefits(),
            recommendedPlan = pricingPlans.find { it.isPopular } ?: pricingPlans[1]
        )
    }

    private fun getPremiumBenefits(): List<String> {
        return listOf(
            "Advanced AI-powered battery analytics",
            "Unlimited battery history tracking",
            "Thermal management and cooling alerts",
            "Cloud backup with device sync",
            "Premium themes and customization",
            "Smart charging optimization",
            "Export data to CSV/JSON",
            "Priority customer support",
            "Early access to new features"
        )
    }

    enum class PurchaseResult {
        SUCCESS,
        CANCELLED,
        ERROR,
        ALREADY_OWNED
    }

    enum class RestoreResult {
        SUCCESS,
        NO_PURCHASES,
        ERROR
    }

    data class PremiumPaywallData(
        val title: String,
        val description: String,
        val benefits: List<String>,
        val recommendedPlan: PricingPlan
    )

    /**
     * Get feature usage analytics (Premium feature)
     */
    fun getFeatureUsageAnalytics(): FeatureUsageStats? {
        return if (isPremium()) {
            FeatureUsageStats(
                totalFeatures = premiumFeatures.size,
                premiumFeaturesUsed = 6,
                mostUsedFeature = "advanced_analytics",
                usageScore = 85
            )
        } else null
    }

    data class FeatureUsageStats(
        val totalFeatures: Int,
        val premiumFeaturesUsed: Int,
        val mostUsedFeature: String,
        val usageScore: Int
    )

    /**
     * Check trial status (if applicable)
     */
    fun getTrialStatus(): TrialStatus {
        // TODO: Implement trial logic
        return TrialStatus(
            isTrialActive = false,
            daysRemaining = 0,
            trialUsed = false
        )
    }

    data class TrialStatus(
        val isTrialActive: Boolean,
        val daysRemaining: Int,
        val trialUsed: Boolean
    )

    /**
     * Get premium upgrade suggestions based on usage
     */
    fun getUpgradeSuggestions(): List<UpgradeSuggestion> {
        if (isPremium()) return emptyList()
        
        return listOf(
            UpgradeSuggestion(
                featureId = "advanced_analytics",
                title = "Unlock Advanced Analytics",
                description = "Get detailed battery insights and predictions",
                urgency = SuggestionUrgency.HIGH
            ),
            UpgradeSuggestion(
                featureId = "cloud_backup",
                title = "Enable Cloud Backup", 
                description = "Keep your battery data safe and synced across devices",
                urgency = SuggestionUrgency.MEDIUM
            )
        )
    }

    data class UpgradeSuggestion(
        val featureId: String,
        val title: String,
        val description: String,
        val urgency: SuggestionUrgency
    )

    enum class SuggestionUrgency {
        LOW, MEDIUM, HIGH
    }

    fun cleanup() {
        billingManager.cleanup()
    }
}
