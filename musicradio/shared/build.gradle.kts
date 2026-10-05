import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.android.kmp.library)
    alias(libs.plugins.metro)
    alias(libs.plugins.structured.coroutines)
    alias(libs.plugins.stability.analyzer)
    alias(libs.plugins.aboutLibraries)
}

kotlin {
    android {
        namespace = "dev.kdroid.musicradio"
        // 37 is a floor, not a preference: materialKolor, aboutlibraries and the AndroidX
        // Compose artifacts all publish AAR metadata demanding it.
        compileSdk = 37
        minSdk = 26
        androidResources.enable = true
        compilerOptions { jvmTarget = JvmTarget.JVM_17 }
    }

    jvm {
        compilerOptions { jvmTarget.set(JvmTarget.JVM_17) }
    }

    js {
        browser()
    }

    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        browser()
    }

    sourceSets {
        commonMain.dependencies {
            api(libs.compose.runtime)
            api(libs.compose.ui)
            api(libs.compose.foundation)
            api(libs.compose.resources)
            api(libs.compose.ui.tooling.preview)
            api(libs.compose.material3)
            implementation(compose.materialIconsExtended)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.structured.coroutines.annotations)
            implementation(libs.androidx.lifecycle.viewmodel)
            implementation(libs.androidx.lifecycle.runtime)
            implementation(libs.androidx.lifecycle.viewmodel.navigation3)
            implementation(libs.compose.nav3)
            implementation(libs.kotlinx.datetime)
            implementation(libs.materialKolor)
            implementation(libs.aboutlibraries.compose.m3)
            implementation(libs.ktor.client.core)
            api(libs.composemediaplayer.audio)
        }

        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation(libs.compose.ui.test)
            implementation(libs.kotlinx.coroutines.test)
            // Lets a test build an IcyMetadata whose client cannot reach the network, so a test
            // that constructs the whole AppViewModel is guaranteed not to poll a real station.
            implementation(libs.ktor.client.mock)
        }

        androidMain.dependencies {
            implementation(libs.kotlinx.coroutines.android)
            implementation(libs.androidx.activityCompose)
            implementation(libs.ktor.client.okhttp)
            implementation(libs.androidx.media3.exoplayer)
            implementation(libs.androidx.media3.session)
        }

        jvmMain.dependencies {
            implementation(compose.desktop.currentOs)
            implementation(libs.kotlinx.coroutines.swing)
            implementation(libs.ktor.client.okhttp)
            implementation(libs.nucleus.core.runtime)
            implementation(libs.nucleus.application)
            implementation(libs.nucleus.decorated.window.tao)
            implementation(libs.nucleus.system.color)
            implementation(libs.nucleus.media.control)
            implementation(libs.nucleus.native.http)
            implementation(libs.nucleus.native.http.ktor)
            implementation(libs.nucleus.updater.runtime)
        }

        // webMain is the Kotlin 2.4 default-hierarchy parent of jsMain and wasmJsMain.
        webMain.dependencies {
            implementation(libs.ktor.client.js)
            implementation(libs.wrappers.browser)
        }
    }
}

dependencies {
    androidRuntimeClasspath(libs.compose.ui.tooling)
}

structuredCoroutines {
    useKmpCommonProfile()
}

compose.resources {
    // Independent of rootProject.name - renaming the app must not move Res.
    packageOfResClass = "musicradio.shared.generated.resources"
    // The platform modules host the windows, so they need the shared drawables too.
    publicResClass = true
}

val stabilityConfig = rootProject.layout.projectDirectory.file("config/stability-config.conf")

composeCompiler {
    stabilityConfigurationFiles.add(stabilityConfig)
}

aboutLibraries {
    export {
        outputFile = file("src/commonMain/composeResources/files/aboutlibraries.json")
    }
    library {
        duplicationMode = com.mikepenz.aboutlibraries.plugin.DuplicateMode.MERGE
    }
}

/**
 * Publishes the Hebrew bundle a second time under the language code Android insists on.
 *
 * Compose Resources chooses a `values-*` directory by comparing its qualifier with
 * `Locale.getLanguage()`, and that call on Android still answers with the ISO 639 codes retired in
 * 1989: a Hebrew locale reports "iw" and never "he", whichever way the locale was built. aapt2
 * papers over this for native Android resources by rewriting values-he as it packages; Compose
 * Resources has its own loader and does not, so values-he was unreachable on every Android device
 * and the interface stayed English both when the system language was Hebrew and after an explicit
 * pick in Settings. Desktop never showed it: JDK 17 and later report the modern codes, so the same
 * build reads values-he there.
 *
 * Generated rather than committed so the translation keeps a single source, the same way the
 * about-libraries data is produced into composeResources. Drop it once Compose Resources maps the
 * legacy codes itself.
 */
val mirrorHebrewStringsForAndroid by tasks.registering(Copy::class) {
    val resources = layout.projectDirectory.dir("src/commonMain/composeResources")
    from(resources.dir("values-he"))
    into(resources.dir("values-iw"))
}

// Every stage of the resource pipeline reads the directory the mirror writes into, so all of them
// have to wait for it - naming them one by one only invited Gradle to find the one that was missed.
tasks.matching { it.name.endsWith("ForCommonMain") }.configureEach {
    dependsOn(mirrorHebrewStringsForAndroid)
}

tasks.matching {
    it.name.startsWith("generateResourceAccessorsForCommonMain") ||
        it.name.startsWith("copyNonXmlValueResourcesForCommonMain") ||
        it.name.startsWith("prepareComposeResourcesTaskForCommonMain")
}.configureEach {
    dependsOn("exportLibraryDefinitions")
}

composeStabilityAnalyzer {
    stabilityConfigurationFiles.add(stabilityConfig)
    traceAll {
        // `variants` only filters Android compilations - the plugin instruments every non-Android
        // main compilation whenever this is on, so leaving it hardcoded shipped recomposition
        // logging in every desktop and web release. Opt in locally with -PcomposeTraceAll.
        enabled.set(providers.gradleProperty("composeTraceAll").map(String::toBoolean).orElse(false))
        threshold.set(2)
        variants.set(listOf("debug"))
    }
    stabilityValidation {
        enabled.set(true)
        outputDir.set(layout.projectDirectory.dir("stability"))
        includeTests.set(false)
        // No baseline is checked in yet: the first run writes one instead of failing.
        failOnStabilityChange.set(false)
    }
}
