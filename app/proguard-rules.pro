# Release builds are obfuscated (R8).
-keep class com.gmmdistributors.app.MainActivity
-assumenosideeffects class android.util.Log { public static int d(...); public static int v(...); }

# WorkManager creates SyncWorker by name.
-keep class com.gmmdistributors.app.SyncWorker { public <init>(android.content.Context, androidx.work.WorkerParameters); }
# Stack traces in Play Console stay readable (Play deobfuscates with the uploaded mapping file).
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile
-keep class com.gmmdistributors.app.StatusWorker { public <init>(android.content.Context, androidx.work.WorkerParameters); }
