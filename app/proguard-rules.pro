# Release builds are obfuscated (R8).
-keep class com.gmmdistributors.app.MainActivity
-assumenosideeffects class android.util.Log { public static int d(...); public static int v(...); }
