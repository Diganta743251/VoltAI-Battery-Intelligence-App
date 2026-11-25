# Consumer ProGuard rules for :service module
# Preserves background services, broadcast receivers, and WorkManager workers

# Android Services and BroadcastReceivers
-keep class * extends android.app.Service
-keep class * extends android.content.BroadcastReceiver
-keep class com.voltai.service.** { *; }

# Preserve lifecycle callback methods in Services
-keepclassmembers class * extends android.app.Service {
    public void onCreate();
    public int onStartCommand(android.content.Intent, int, int);
    public void onDestroy();
    public android.os.IBinder onBind(android.content.Intent);
}

# WorkManager Workers
-keep class * extends androidx.work.ListenableWorker {
    public <init>(android.content.Context, androidx.work.WorkerParameters);
}
-keep class * extends androidx.work.Worker {
    public <init>(android.content.Context, androidx.work.WorkerParameters);
}
-keep class com.voltai.service.ChargingAlertWorker { *; }
-keep class com.voltai.service.BatteryDataCollector { *; }

# Dagger / Hilt components for service
-keep class com.voltai.service.di.** { *; }
