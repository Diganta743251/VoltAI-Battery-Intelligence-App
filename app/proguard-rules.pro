# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.

# Keep Compose classes
-keep class androidx.compose.** { *; }
-keep class kotlin.** { *; }
-keep class kotlinx.** { *; }

# Keep Google Play Services
-keep class com.google.android.gms.** { *; }
-dontwarn com.google.android.gms.**

# Keep Google API Client
-keep class com.google.api.** { *; }
-dontwarn com.google.api.**

# Ignore missing classes from Apache HTTP and other optional dependencies
-dontwarn javax.naming.**
-dontwarn org.apache.log.**
-dontwarn org.apache.log4j.**
-dontwarn org.ietf.jgss.**
-dontwarn org.apache.http.**
-dontwarn org.apache.commons.logging.**

# Keep HTTP client classes that are used
-keep class org.apache.http.** { *; }
-keep class org.apache.commons.logging.** { *; }

# Keep battery related classes
-keep class android.os.BatteryManager { *; }
-keep class android.content.Intent { *; }

# Keep data classes and models
-keep class * extends java.io.Serializable { *; }
-keepclassmembers class * extends java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    !static !transient <fields>;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

# Keep Parcelable classes
-keep class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator *;
}

# Keep enum classes
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# Remove logging in release
-assumenosideeffects class android.util.Log {
    public static boolean isLoggable(java.lang.String, int);
    public static int v(...);
    public static int i(...);
    public static int w(...);
    public static int d(...);
    public static int e(...);
}

# Keep line numbers for crash reports
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile