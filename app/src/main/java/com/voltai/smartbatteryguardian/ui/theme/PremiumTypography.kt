package com.voltai.smartbatteryguardian.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Premium Typography System
 * Future-proofed with modern font families and accessibility
 */

// Premium font family using system fonts for reliability
val PremiumFontFamily = FontFamily.Default

// Premium Typography with enhanced readability
val PremiumTypography =
    Typography(
        // Display styles for hero sections
        displayLarge =
        TextStyle(
            fontFamily = PremiumFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 57.sp,
            lineHeight = 64.sp,
            letterSpacing = (-0.25).sp,
        ),
        displayMedium =
        TextStyle(
            fontFamily = PremiumFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 45.sp,
            lineHeight = 52.sp,
            letterSpacing = 0.sp,
        ),
        displaySmall =
        TextStyle(
            fontFamily = PremiumFontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 36.sp,
            lineHeight = 44.sp,
            letterSpacing = 0.sp,
        ),
        // Headlines for sections
        headlineLarge =
        TextStyle(
            fontFamily = PremiumFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 32.sp,
            lineHeight = 40.sp,
            letterSpacing = 0.sp,
        ),
        headlineMedium =
        TextStyle(
            fontFamily = PremiumFontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 28.sp,
            lineHeight = 36.sp,
            letterSpacing = 0.sp,
        ),
        headlineSmall =
        TextStyle(
            fontFamily = PremiumFontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 24.sp,
            lineHeight = 32.sp,
            letterSpacing = 0.sp,
        ),
        // Titles for cards and components
        titleLarge =
        TextStyle(
            fontFamily = PremiumFontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 22.sp,
            lineHeight = 28.sp,
            letterSpacing = 0.sp,
        ),
        titleMedium =
        TextStyle(
            fontFamily = PremiumFontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 18.sp,
            lineHeight = 24.sp,
            letterSpacing = 0.15.sp,
        ),
        titleSmall =
        TextStyle(
            fontFamily = PremiumFontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 16.sp,
            lineHeight = 20.sp,
            letterSpacing = 0.1.sp,
        ),
        // Body text
        bodyLarge =
        TextStyle(
            fontFamily = PremiumFontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 16.sp,
            lineHeight = 24.sp,
            letterSpacing = 0.5.sp,
        ),
        bodyMedium =
        TextStyle(
            fontFamily = PremiumFontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 14.sp,
            lineHeight = 20.sp,
            letterSpacing = 0.25.sp,
        ),
        bodySmall =
        TextStyle(
            fontFamily = PremiumFontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 12.sp,
            lineHeight = 16.sp,
            letterSpacing = 0.4.sp,
        ),
        // Labels for UI elements
        labelLarge =
        TextStyle(
            fontFamily = PremiumFontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp,
            lineHeight = 20.sp,
            letterSpacing = 0.1.sp,
        ),
        labelMedium =
        TextStyle(
            fontFamily = PremiumFontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 12.sp,
            lineHeight = 16.sp,
            letterSpacing = 0.5.sp,
        ),
        labelSmall =
        TextStyle(
            fontFamily = PremiumFontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 10.sp,
            lineHeight = 14.sp,
            letterSpacing = 0.5.sp,
        ),
    )

/**
 * Premium Shapes System
 * Modern rounded corners with accessibility considerations
 */
val PremiumShapes =
    Shapes(
        extraSmall = RoundedCornerShape(4.dp),
        small = RoundedCornerShape(8.dp),
        medium = RoundedCornerShape(12.dp),
        large = RoundedCornerShape(16.dp),
        extraLarge = RoundedCornerShape(24.dp),
    )

// Extended shapes for premium components
object PremiumExtendedShapes {
    val cardShape = RoundedCornerShape(20.dp)
    val dialogShape = RoundedCornerShape(24.dp)
    val bottomSheetShape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
    val chipShape = RoundedCornerShape(50)
    val progressBarShape = RoundedCornerShape(8.dp)
    val buttonShape = RoundedCornerShape(16.dp)
    val fabShape = RoundedCornerShape(16.dp)
    val navigationBarShape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
}

// Premium text styles for specific use cases
object PremiumTextStyles {
    val premiumBadge =
        TextStyle(
            fontFamily = PremiumFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 10.sp,
            lineHeight = 12.sp,
            letterSpacing = 0.8.sp,
        )

    val metricValue =
        TextStyle(
            fontFamily = PremiumFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 24.sp,
            lineHeight = 28.sp,
            letterSpacing = 0.sp,
        )

    val metricLabel =
        TextStyle(
            fontFamily = PremiumFontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 12.sp,
            lineHeight = 16.sp,
            letterSpacing = 0.4.sp,
        )

    val cardTitle =
        TextStyle(
            fontFamily = PremiumFontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 16.sp,
            lineHeight = 20.sp,
            letterSpacing = 0.1.sp,
        )

    val cardSubtitle =
        TextStyle(
            fontFamily = PremiumFontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 14.sp,
            lineHeight = 18.sp,
            letterSpacing = 0.25.sp,
        )

    val buttonText =
        TextStyle(
            fontFamily = PremiumFontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 16.sp,
            lineHeight = 20.sp,
            letterSpacing = 0.1.sp,
        )

    val tabText =
        TextStyle(
            fontFamily = PremiumFontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp,
            lineHeight = 20.sp,
            letterSpacing = 0.1.sp,
        )
}
