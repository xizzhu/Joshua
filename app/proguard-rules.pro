# AndroidX
-keep class androidx.appcompat.widget.AppCompatCheckBox { *; }
-keep class androidx.appcompat.widget.AppCompatImageView { *; }
-keep class androidx.appcompat.widget.AppCompatSpinner { *; }
-keep class androidx.appcompat.widget.SearchView { *; }
-keep class androidx.appcompat.widget.SwitchCompat { *; }
-keep class androidx.appcompat.widget.Toolbar { *; }

# Crashlytics
-keepattributes *Annotation*
-keepattributes SourceFile,LineNumberTable
-keep public class * extends java.lang.Exception

-keep class com.crashlytics.** { *; }
-dontwarn com.crashlytics.**
