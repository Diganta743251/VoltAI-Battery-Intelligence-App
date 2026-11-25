# Consumer ProGuard rules for :domain module
# Preserves domain models, repository contracts, and use cases during release builds

# Keep all domain data models and serializables
-keep class com.example.domain.model.** { *; }
-keep class com.voltai.domain.model.** { *; }

# Keep domain constants
-keep class com.example.domain.DomainConstants { *; }

# Keep repository interfaces implemented across data/service/app layers
-keep interface com.example.domain.repository.** { *; }
-keep interface com.voltai.domain.repository.** { *; }

# Keep use cases and their public invocation methods
-keep class com.example.domain.usecase.** { *; }
-keepclassmembers class com.example.domain.usecase.** {
    public *;
}

# Keep Kotlin Coroutines / Flow classes used in domain boundaries
-keep class kotlinx.coroutines.flow.** { *; }
