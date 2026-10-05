# Retrace crash reports after R8 renames classes and methods.
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Ktor's no-arg HttpClient() loads the engine through ServiceLoader
# (META-INF/services/io.ktor.client.HttpClientEngineContainer → OkHttp).
-keep class io.ktor.client.HttpClientEngineContainer
-keep class * implements io.ktor.client.HttpClientEngineContainer

# Generated Compose Resources accessors and collectors. Stations.kt holds
# static references to every drawable, but the resource loader also walks
# the generated maps, and R8 full mode has dropped those helpers before.
-keep class musicradio.shared.generated.resources.** { *; }
