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
-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

# Preserve generic type information
-keepattributes Signature

# Preserve Gson TypeToken and its subclasses
-keep class com.google.gson.reflect.TypeToken { *; }
-keep class * extends com.google.gson.reflect.TypeToken

# Збереження інформації для налагодження та рефлексії
-keepattributes SourceFile,LineNumberTable
-keepattributes Signature, *Annotation*, InnerClasses, EnclosingMethod

# Gson TypeToken
-keep class com.google.gson.reflect.TypeToken { *; }
-keep class * extends com.google.gson.reflect.TypeToken { *; }

# Збереження полів моделей даних, серіалізованих через Gson
-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
}

# (Опціонально) Якщо клас моделей не використовує @SerializedName, збережіть пакет моделей повністю:
# -keep class media.suspilne.kazky.models.** { *; }
