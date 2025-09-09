package com.voltai.smartbatteryguardian.viewmodels

import android.app.Activity
import androidx.lifecycle.ViewModel
import com.voltai.smartbatteryguardian.premium.PremiumManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

/**
 * ViewModel for managing premium features and billing operations
 */
@HiltViewModel
class PremiumViewModel @Inject constructor(
    private val premiumManager: PremiumManager
) : ViewModel() {

    // Expose premium status from PremiumManager
    val premiumState: StateFlow<PremiumManager.PremiumStatus> = premiumManager.premiumStatus
    val billingState: StateFlow<PremiumManager.BillingState> = premiumManager.billingState

    /**
     * Check if a specific feature is available
     */
    fun isFeatureAvailable(featureId: String): Boolean {
        return premiumManager.isFeatureAvailable(featureId)
    }

    /**
     * Check if user has premium access
     */
    fun isPremium(): Boolean {
        return premiumManager.isPremium()
    }

    /**
     * Get premium features by category
     */
    fun getFeaturesByCategory(category: PremiumManager.FeatureCategory): List<PremiumManager.PremiumFeature> {
        return premiumManager.getFeaturesByCategory(category)
    }

    /**
     * Get premium-only features
     */
    fun getPremiumOnlyFeatures(): List<PremiumManager.PremiumFeature> {
        return premiumManager.getPremiumOnlyFeatures()
    }

    /**
     * Launch purchase flow
     */
    fun purchasePremium(
        activity: Activity,
        sku: String,
        callback: (PremiumManager.PurchaseResult) -> Unit
    ) {
        premiumManager.purchasePremium(activity, sku, callback)
    }

    /**
     * Restore purchases
     */
    fun restorePurchases(callback: (PremiumManager.RestoreResult) -> Unit) {
        premiumManager.restorePurchases(callback)
    }

    /**
     * Show premium paywall for a specific feature
     */
    fun showPremiumPaywall(featureId: String): PremiumManager.PremiumPaywallData {
        return premiumManager.showPremiumPaywall(featureId)
    }

    /**
     * Get feature usage analytics (Premium feature)
     */
    fun getFeatureUsageAnalytics(): PremiumManager.FeatureUsageStats? {
        return premiumManager.getFeatureUsageAnalytics()
    }

    /**
     * Get premium upgrade suggestions based on usage
     */
    fun getUpgradeSuggestions(): List<PremiumManager.UpgradeSuggestion> {
        return premiumManager.getUpgradeSuggestions()
    }

    /**
     * Get trial status (if applicable)
     */
    fun getTrialStatus(): PremiumManager.TrialStatus {
        return premiumManager.getTrialStatus()
    }

    /**
     * Get pricing plans
     */
    fun getPricingPlans(): List<PremiumManager.PricingPlan> {
        return premiumManager.pricingPlans
    }

    /**
     * Get all premium features
     */
    fun getPremiumFeatures(): List<PremiumManager.PremiumFeature> {
        return premiumManager.premiumFeatures
    }

    override fun onCleared() {
        super.onCleared()
        premiumManager.cleanup()
    }
}
