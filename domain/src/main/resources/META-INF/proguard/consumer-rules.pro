# Consumer ProGuard rules for :domain module (packaged into jar META-INF/proguard)
-keep class com.example.domain.model.** { *; }
-keep class com.voltai.domain.model.** { *; }
-keep class com.example.domain.DomainConstants { *; }
-keep interface com.example.domain.repository.** { *; }
-keep interface com.voltai.domain.repository.** { *; }
-keep class com.example.domain.usecase.** { *; }
-keepclassmembers class com.example.domain.usecase.** {
    public *;
}
-keep class kotlinx.coroutines.flow.** { *; }
