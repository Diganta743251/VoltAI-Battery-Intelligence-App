# Consumer ProGuard rules for :ui module
# Preserves Compose UI components, custom Canvas views, design system, and themes

# Compose Runtime and Foundation
-keep class androidx.compose.** { *; }
-dontwarn androidx.compose.**

# Composable functions
-keepclassmembers class * {
    @androidx.compose.runtime.Composable *;
}

# VoltAI UI design system and components
-keep class com.voltai.ui.** { *; }
-keep class com.voltai.ui.components.** { *; }
-keep class com.voltai.ui.theme.** { *; }
-keep class com.voltai.ui.designsystem.** { *; }

# Compose Navigation
-keep class androidx.navigation.** { *; }
