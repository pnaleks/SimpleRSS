# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in C:\Users\User\AppData\Local\Android\sdk/tools/proguard/proguard-android.txt
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

#-keepclassmembers class nl.matshofman.saxrssreader.* { public *; }

-libraryjars <java.home>/lib/rt.jar(java/**,javax/**)

-keep class ru.pnapp.simple_rss.** { *; }
-keep class org.simpleframework.xml.** { *; }
-keep class com.squareup.picasso.** { *; }
-keep class android.support.v7.widget.ShareActionProvider { *; }

-dontwarn com.squareup.okhttp.**
