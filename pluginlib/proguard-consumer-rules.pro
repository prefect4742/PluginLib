# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

-keep public class com.prefect47.pluginlib.annotations.** { public *; }
-keep public class com.prefect47.pluginlib.datastore.** { public *; }
-keep public class com.prefect47.pluginlib.discoverables.** { public *; }
-keep public class com.prefect47.pluginlib.ui.** { public *; }
-keep public class com.prefect47.pluginlib.*  { public *; }
-keep class androidx.preference.PreferenceDataStore { public *; }
-keep class androidx.preference.PreferenceFragmentCompat { public *; }

#-dontwarn androidx.**
#-keep class androidx.** { *; }
#-keep interface androidx.** { *; }

# Kotlin
-keep class kotlin.Metadata { *; }
-dontnote kotlin.internal.PlatformImplementationsKt
-dontnote kotlin.reflect.jvm.internal.**
-dontnote kotlin.coroutines.jvm.internal.DebugMetadataKt
