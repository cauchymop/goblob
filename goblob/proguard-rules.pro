# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in /Users/abela/Library/Android/sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

-dontobfuscate
-keep class com.cauchymop.** { *; }

# Butterknife
# Retain generated class which implement Unbinder.
-keep public class * implements butterknife.Unbinder { public <init>(...); }

# Prevent obfuscation of types which use ButterKnife annotations since the simple name
# is used to reflectively look up the generated ViewBinding.
-keep class butterknife.*
-keepclasseswithmembernames class * { @butterknife.* <methods>; }
-keepclasseswithmembernames class * { @butterknife.* <fields>; }

# Guava
-dontwarn sun.misc.Unsafe
-dontwarn com.google.common.collect.MinMaxPriorityQueue
-dontwarn com.google.common.util.concurrent.FuturesGetChecked**
-dontwarn javax.lang.model.element.Modifier
-dontwarn afu.org.checkerframework.**
-dontwarn org.checkerframework.**