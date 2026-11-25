# Consumer ProGuard rules for :data module
# Automatically applied by consumer modules during release shrinking and obfuscation

# Room Database
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class * { *; }
-keep @androidx.room.Dao interface * { *; }
-keepclassmembers class * {
    @androidx.room.TypeConverter *;
}

# Room runtime and annotations
-dontwarn androidx.room.paging.**

# Keep Data module entities and DAOs
-keep class com.voltai.data.local.** { *; }
-keep class com.example.data.local.** { *; }

# DataStore and Preferences
-keep class androidx.datastore.** { *; }
-keep class com.example.data.preferences.** { *; }

# Repositories
-keep class com.voltai.data.repository.** { *; }
-keep class com.example.data.repository.** { *; }

# Dagger / Hilt bindings
-keep class com.voltai.data.di.** { *; }
-keep class com.example.data.di.** { *; }
