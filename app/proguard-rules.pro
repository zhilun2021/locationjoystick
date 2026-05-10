# Keep Hilt generated classes
-keep class dagger.hilt.** { *; }
-keep class * extends dagger.hilt.** { *; }

# Keep Android classes
-keep public class * extends android.app.Activity
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider
-keep public class * extends android.view.View

# Keep kotlinx serialization
-keepclassmembers class * {
    *** Companion;
}

# Keep model classes
-keep class com.locationjoystick.core.model.** { *; }
