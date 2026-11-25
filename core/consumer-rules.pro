# Consumer ProGuard rules for :core module
# Preserves TensorFlow Lite models, AI battery analyzers, and core utilities

# TensorFlow Lite
-keep class org.tensorflow.lite.** { *; }
-dontwarn org.tensorflow.lite.**

# Core AI and analytics engines
-keep class com.voltai.core.** { *; }
-keep class com.example.core.** { *; }

# Battery monitors and system service adapters
-keep class com.example.core.utils.** { *; }
-keep class com.voltai.core.utils.** { *; }

# Core DI modules
-keep class com.voltai.core.di.** { *; }
