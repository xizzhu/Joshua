# AndroidX
-keep class androidx.appcompat.widget.AppCompatCheckBox { *; }
-keep class androidx.appcompat.widget.AppCompatImageView { *; }
-keep class androidx.appcompat.widget.AppCompatSpinner { *; }
-keep class androidx.appcompat.widget.SearchView { *; }
-keep class androidx.appcompat.widget.SwitchCompat { *; }
-keep class androidx.appcompat.widget.Toolbar { *; }

# Crashlytics
# https://firebase.google.com/docs/crashlytics/get-deobfuscated-reports?platform=android
-keepattributes SourceFile,LineNumberTable
-keep public class * extends java.lang.Exception

-keep class com.google.firebase.crashlytics.** { *; }
-dontwarn com.google.firebase.crashlytics.**

# Kotlin Intrinsics
-assumenosideeffects class kotlin.jvm.internal.Intrinsics { *; }
