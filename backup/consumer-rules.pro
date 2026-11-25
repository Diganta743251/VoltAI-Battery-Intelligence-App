# Consumer ProGuard rules for :backup module
# Preserves Google Drive API, encrypted storage, and cloud backup workers

# Google Drive API and Google API Client
-keep class com.google.api.services.drive.** { *; }
-keep class com.google.api.client.** { *; }
-keep class com.google.api.client.googleapis.** { *; }
-keep class com.google.api.client.json.** { *; }
-keep class com.google.api.client.http.** { *; }
-keep class com.google.android.gms.auth.** { *; }

-dontwarn com.google.api.client.**
-dontwarn com.google.api.services.drive.**
-dontwarn org.apache.http.**
-dontwarn com.google.common.**
-dontwarn javax.annotation.**

# Jetpack Security / EncryptedSharedPreferences
-keep class androidx.security.crypto.** { *; }

# Backup Managers and Workers
-keep class com.voltai.backup.** { *; }
-keep class * extends androidx.work.ListenableWorker {
    public <init>(android.content.Context, androidx.work.WorkerParameters);
}
-keep class com.voltai.backup.ExportLogsWorker { *; }
