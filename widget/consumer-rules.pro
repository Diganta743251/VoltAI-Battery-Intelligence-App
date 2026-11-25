# Consumer ProGuard rules for :widget module
# Preserves AppWidgets, Glance compositions, and Quick Settings tiles

# AppWidgetProviders
-keep class * extends android.appwidget.AppWidgetProvider { *; }
-keep class com.voltai.widget.** { *; }
-keep class com.example.widget.** { *; }

# Glance Widgets and Glance AppWidget Receivers
-keep class * extends androidx.glance.appwidget.GlanceAppWidget { *; }
-keep class * extends androidx.glance.appwidget.GlanceAppWidgetReceiver { *; }
-keep class androidx.glance.** { *; }
-dontwarn androidx.glance.**

# Quick Settings TileService
-keep class * extends android.service.quicksettings.TileService { *; }
-keepclassmembers class * extends android.service.quicksettings.TileService {
    public void onTileAdded();
    public void onStartListening();
    public void onStopListening();
    public void onClick();
    public void onTileRemoved();
}
