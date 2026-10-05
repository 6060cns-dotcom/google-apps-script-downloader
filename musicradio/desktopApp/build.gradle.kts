import dev.nucleusframework.desktop.application.dsl.CompressionLevel
import dev.nucleusframework.desktop.application.dsl.NativeImageMarch
import dev.nucleusframework.desktop.application.dsl.NativeImageOptimization
import dev.nucleusframework.desktop.application.dsl.ReleaseChannel
import dev.nucleusframework.desktop.application.dsl.ReleaseType
import dev.nucleusframework.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.nucleus)
}

kotlin {
    compilerOptions { jvmTarget.set(JvmTarget.JVM_25) }
}

java {
    toolchain { languageVersion.set(JavaLanguageVersion.of(25)) }
}

dependencies {
    implementation(project(":shared"))
    implementation(compose.desktop.currentOs)
    implementation(libs.nucleus.application)
    implementation(libs.nucleus.decorated.window.tao)
    implementation(libs.nucleus.decorated.window.material3)
}

val releaseVersion =
    System.getenv("RELEASE_VERSION")
        ?.removePrefix("v")
        ?.takeIf { it.isNotBlank() && it.first().isDigit() }
        ?: "1.0.0"

val nativePackageVersion = releaseVersion.substringBefore("-")

nucleus.application {
    mainClass = "MainKt"
    // Nucleus `run` forks `javaHome`, not the Java plugin toolchain. Point it at JDK 25
    // so a JBR 21 Gradle daemon (typical from IDEA) does not launch class-file 69 bytecode.
    javaHome =
        javaToolchains
            .launcherFor(java.toolchain)
            .get()
            .metadata.installationPath.asFile.absolutePath

    graalvm {
        isEnabled = true
        imageName = "MusicRadio"
        optimization = NativeImageOptimization.SIZE
        // Without this, native-image bakes in whatever instruction set the build machine happens
        // to have, and the binary dies with SIGILL on any older CPU that downloads it. The release
        // runners are newer than plenty of machines the app is meant to run on, so the ceiling has
        // to be set here rather than left to the builder.
        march = NativeImageMarch.COMPATIBILITY
    }

    nativeDistributions {
        targetFormats(TargetFormat.Dmg, TargetFormat.Zip, TargetFormat.Nsis, TargetFormat.Deb)
        packageName = "Music Radio"
        packageVersion = releaseVersion
        vendor = "abaye"
        cleanupNativeLibs = true
        compressionLevel = CompressionLevel.Ultra
        // electron-builder refuses to build a .deb without it: "Please specify project homepage".
        homepage = "https://github.com/abaye123/MusicRadio"

        // Where the app looks for its own updates. GitHubProvider in DesktopUpdate.kt reads the
        // same coordinates; both have to name the repository the release workflow publishes to.
        publish {
            github {
                enabled = true
                owner = "abaye123"
                repo = "MusicRadio"
                channel = ReleaseChannel.Latest
                releaseType = ReleaseType.Release
            }
        }

        linux {
            iconFile.set(project.file("appIcons/LinuxIcon.png"))
            debPackageVersion = releaseVersion
            // Likewise mandatory for .deb; the address only has to be well-formed.
            debMaintainer = "abaye <abaye123@users.noreply.github.com>"

            // A .deb can only install itself without a password prompt if it is signed, so this is
            // what makes the Linux self-update silent rather than a sudo dialog. Inert until the
            // LINUX_GPG_* secrets exist: the CI step below writes the key settings into
            // gradle.properties only when the secret is present, and this block reads them from
            // there. Windows and macOS need none of it.
            signing {
                enabled.set(true)
                silentUpdate.set(true)
            }
        }
        windows {
            iconFile.set(project.file("appIcons/WindowsIcon.ico"))
            packageVersion = nativePackageVersion
            upgradeUuid = "3f1c9a52-84d7-4e63-9b0a-6c2f7d18e405"
        }
        macOS {
            iconFile.set(project.file("appIcons/MacOsIcon.icns"))
            packageVersion = nativePackageVersion
            bundleID = "co.abaye.musicradio"
        }
    }
}
