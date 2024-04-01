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

# Preserve all classes in your package containing ResetPasswordActivity
-keep class in.dbit.csiapp.mActivityManager.** { *; }

# Preserve all public methods in ResetPasswordActivity
-keepclassmembers class in.dbit.csiapp.mActivityManager.ResetPasswordActivity {
    public *;
}

# Keep any custom views, including those referenced in XML layouts
-keep public class * extends android.view.View {
    public <init>(android.content.Context);
    public <init>(android.content.Context, android.util.AttributeSet);
    public <init>(android.content.Context, android.util.AttributeSet, int);
    public void set*(...);
}

# Preserve annotations, if you use any
-keepattributes *Annotation*

# If you are using Gson library, preserve its classes
-keep class com.google.gson.** { *; }

# If you are using OkHttp library, preserve its classes
-keep class okhttp3.** { *; }

# If you are using Retrofit library, preserve its classes
-keep class retrofit2.** { *; }
