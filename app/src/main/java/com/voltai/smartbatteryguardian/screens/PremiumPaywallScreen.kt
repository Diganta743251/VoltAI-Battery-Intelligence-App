package com.voltai.smartbatteryguardian.screens

import androidx.activity.ComponentActivity
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.voltai.smartbatteryguardian.premium.PremiumManager
import com.voltai.smartbatteryguardian.viewmodels.PremiumViewModel

/**
 * Premium paywall screen with attractive subscription offers
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PremiumPaywallScreen(
    onBackClick: () -> Unit = {},
    onDismiss: () -> Unit = {},
    premiumViewModel: PremiumViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val premiumState by premiumViewModel.premiumState.collectAsState()
    val billingState by premiumViewModel.billingState.collectAsState()
    
    var selectedPlan by remember { mutableStateOf("premium_yearly") }
    var isLoading by remember { mutableStateOf(false) }
    
    // Premium benefits animation
    val animatedFloat by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(1000, easing = EaseOutBounce)
    )
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF1A1A2E),
                        Color(0xFF16213E),
                        Color(0xFF0F3460)
                    )
                )
            )
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Header Section
            item {
                PremiumHeader(onBackClick = onBackClick)
            }
            
            // Hero Section
            item {
                PremiumHeroSection(animatedFloat)
            }
            
            // Features Grid
            item {
                PremiumFeaturesGrid(animatedFloat)
            }
            
            // Pricing Plans
            item {
                PremiumPricingSection(
                    selectedPlan = selectedPlan,
                    onPlanSelected = { selectedPlan = it },
                    isLoading = isLoading
                )
            }
            
            // Purchase Button
            item {
                PremiumPurchaseButton(
                    selectedPlan = selectedPlan,
                    isLoading = isLoading,
                    billingState = billingState,
                    onPurchaseClick = {
                        isLoading = true
                        premiumViewModel.purchasePremium(
                            activity = context as ComponentActivity,
                            sku = selectedPlan
                        ) { result ->
                            isLoading = false
                            when (result) {
                                PremiumManager.PurchaseResult.SUCCESS -> onDismiss()
                                else -> { /* Handle error */ }
                            }
                        }
                    }
                )
            }
            
            // Trust Indicators
            item {
                TrustIndicators()
            }
            
            // Footer
            item {
                PremiumFooter(
                    onRestoreClick = {
                        premiumViewModel.restorePurchases { result ->
                            if (result == PremiumManager.RestoreResult.SUCCESS) {
                                onDismiss()
                            }
                        }
                    }
                )
            }
        }
        
        // Close Button (floating)
        FloatingActionButton(
            onClick = onDismiss,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp)
                .size(48.dp),
            containerColor = Color.White.copy(alpha = 0.1f),
            contentColor = Color.White
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Close"
            )
        }
    }
}

@Composable
private fun PremiumHeader(onBackClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBackClick) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Back",
                tint = Color.White
            )
        }
        
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = Color(0xFFFFD700).copy(alpha = 0.2f)
        ) {
            Text(
                text = "💎 Premium",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFFFD700),
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }
    }
}

@Composable
private fun PremiumHeroSection(animatedFloat: Float) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Animated Premium Badge
        Box(
            modifier = Modifier
                .size((120 * animatedFloat).dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color(0xFFFFD700),
                            Color(0xFFFFA000)
                        )
                    ),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "⚡",
                fontSize = (32 * animatedFloat).sp,
                color = Color.White
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = "Unlock VoltAI Premium",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "Advanced AI-powered battery management",
            fontSize = 16.sp,
            color = Color.White.copy(alpha = 0.8f),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun PremiumFeaturesGrid(animatedFloat: Float) {
    val features = listOf(
        PremiumFeatureItem(
            icon = Icons.Default.Star,
            title = "Advanced Analytics",
            description = "Detailed insights and 6-month history",
            color = Color(0xFF4CAF50)
        ),
        PremiumFeatureItem(
            icon = Icons.Default.Star,
            title = "Cloud Backup",
            description = "Sync across all your devices",
            color = Color(0xFF2196F3)
        ),
        PremiumFeatureItem(
            icon = Icons.Default.Star,
            title = "Premium Themes",
            description = "12+ beautiful themes",
            color = Color(0xFF9C27B0)
        ),
        PremiumFeatureItem(
            icon = Icons.Default.Notifications,
            title = "Smart Alerts",
            description = "AI-powered notifications",
            color = Color(0xFFFF9800)
        ),
        PremiumFeatureItem(
            icon = Icons.Default.Star,
            title = "Premium Widgets",
            description = "Advanced home screen widgets",
            color = Color(0xFFE91E63)
        ),
        PremiumFeatureItem(
            icon = Icons.Default.Star,
            title = "Export Data",
            description = "CSV/JSON export capabilities",
            color = Color(0xFF607D8B)
        )
    )
    
    LazyColumn(
        modifier = Modifier.height(300.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(features.chunked(2)) { rowFeatures ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                rowFeatures.forEach { feature ->
                    PremiumFeatureCard(
                        feature = feature,
                        animatedFloat = animatedFloat,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun PremiumFeatureCard(
    feature: PremiumFeatureItem,
    animatedFloat: Float,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(120.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = feature.icon,
                contentDescription = feature.title,
                tint = feature.color,
                modifier = Modifier.size((24 * animatedFloat).dp)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = feature.title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center
            )
            
            Text(
                text = feature.description,
                fontSize = 11.sp,
                color = Color.White.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun PremiumPricingSection(
    selectedPlan: String,
    onPlanSelected: (String) -> Unit,
    isLoading: Boolean
) {
    val pricingPlans = listOf(
        PricingPlanItem(
            sku = "premium_monthly",
            title = "Monthly",
            price = "$2.99",
            period = "/month",
            savings = null,
            isPopular = false
        ),
        PricingPlanItem(
            sku = "premium_yearly",
            title = "Yearly",
            price = "$17.99",
            period = "/year",
            savings = "Save 50%",
            isPopular = true
        ),
        PricingPlanItem(
            sku = "premium_lifetime",
            title = "Lifetime",
            price = "$49.99",
            period = "one-time",
            savings = "Best Value",
            isPopular = false
        )
    )
    
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "Choose Your Plan",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        pricingPlans.forEach { plan ->
            PricingPlanCard(
                plan = plan,
                isSelected = selectedPlan == plan.sku,
                onSelected = { onPlanSelected(plan.sku) },
                enabled = !isLoading
            )
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
private fun PricingPlanCard(
    plan: PricingPlanItem,
    isSelected: Boolean,
    onSelected: () -> Unit,
    enabled: Boolean
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = enabled) { onSelected() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) 
                Color(0xFFFFD700).copy(alpha = 0.2f) 
            else 
                Color.White.copy(alpha = 0.1f)
        ),
        border = if (isSelected) 
            BorderStroke(2.dp, Color(0xFFFFD700)) 
        else null
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = plan.title,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    
                    if (plan.isPopular) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFFFD700)
                        ) {
                            Text(
                                text = "POPULAR",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
                
                Row(
                    verticalAlignment = Alignment.Bottom
                ) {
                    Text(
                        text = plan.price,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = plan.period,
                        fontSize = 14.sp,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                }
                
                plan.savings?.let { savings ->
                    Text(
                        text = savings,
                        fontSize = 12.sp,
                        color = Color(0xFF4CAF50),
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            
            RadioButton(
                selected = isSelected,
                onClick = onSelected,
                enabled = enabled,
                colors = RadioButtonDefaults.colors(
                    selectedColor = Color(0xFFFFD700),
                    unselectedColor = Color.White.copy(alpha = 0.6f)
                )
            )
        }
    }
}

@Composable
private fun PremiumPurchaseButton(
    selectedPlan: String,
    isLoading: Boolean,
    billingState: PremiumManager.BillingState,
    onPurchaseClick: () -> Unit
) {
    val buttonText = when {
        isLoading -> "Processing..."
        billingState != PremiumManager.BillingState.CONNECTED -> "Connecting..."
        else -> "Start Premium"
    }
    
    Button(
        onClick = onPurchaseClick,
        enabled = !isLoading && billingState == PremiumManager.BillingState.CONNECTED,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFFFFD700),
            contentColor = Color.Black,
            disabledContainerColor = Color(0xFFFFD700).copy(alpha = 0.5f)
        )
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                color = Color.Black,
                strokeWidth = 2.dp
            )
            Spacer(modifier = Modifier.width(8.dp))
        }
        
        Text(
            text = buttonText,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun TrustIndicators() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        TrustIndicatorItem(
            icon = Icons.Default.Check,
            text = "Secure Payment"
        )
        TrustIndicatorItem(
            icon = Icons.Default.Check,
            text = "Cancel Anytime"
        )
        TrustIndicatorItem(
            icon = Icons.Default.Check,
            text = "24/7 Support"
        )
    }
}

@Composable
private fun TrustIndicatorItem(
    icon: ImageVector,
    text: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = text,
            tint = Color(0xFF4CAF50),
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = text,
            fontSize = 12.sp,
            color = Color.White.copy(alpha = 0.7f),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun PremiumFooter(onRestoreClick: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        TextButton(onClick = onRestoreClick) {
            Text(
                text = "Restore Purchases",
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 14.sp
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Terms of Service",
                color = Color.White.copy(alpha = 0.6f),
                fontSize = 12.sp
            )
            Text(
                text = "Privacy Policy",
                color = Color.White.copy(alpha = 0.6f),
                fontSize = 12.sp
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
    }
}

// Data classes
private data class PremiumFeatureItem(
    val icon: ImageVector,
    val title: String,
    val description: String,
    val color: Color
)

private data class PricingPlanItem(
    val sku: String,
    val title: String,
    val price: String,
    val period: String,
    val savings: String?,
    val isPopular: Boolean
)
