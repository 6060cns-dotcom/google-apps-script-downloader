package dev.kdroid.musicradio.platform

import dev.nucleusframework.core.runtime.NucleusApp
import java.awt.Desktop
import java.io.File
import java.net.URI
import java.util.Locale

private const val APP_DIR_NAME = "MusicRadio"

internal actual object Platform {
    actual val osLabel: String
        get() {
            val os = System.getProperty("os.name").orEmpty().lowercase()
            return when {
                os.contains("mac") -> "macOS"
                os.contains("win") -> "Windows"
                else -> "Linux"
            }
        }

    actual val appVersion: String
        get() = NucleusApp.version.orEmpty()

    /** Per-OS user data location: %APPDATA% on Windows, ~/Library on macOS, XDG on Linux. */
    actual fun appDir(): String {
        val home = System.getProperty("user.home").orEmpty()
        val os = System.getProperty("os.name").orEmpty().lowercase()
        val base = when {
            os.contains("win") -> System.getenv("APPDATA")?.takeIf { it.isNotBlank() } ?: joinPath(home, "AppData\\Roaming")
            os.contains("mac") -> joinPath(joinPath(home, "Library"), "Application Support")
            else -> System.getenv("XDG_DATA_HOME")?.takeIf { it.isNotBlank() } ?: joinPath(joinPath(home, ".local"), "share")
        }
        val dir = joinPath(base, APP_DIR_NAME)
        mkdir(dir)
        return dir
    }

    actual fun readText(path: String): String? = runCatching {
        File(path).takeIf { it.isFile }?.readText()
    }.getOrNull()

    actual fun writeText(path: String, content: String) {
        runCatching {
            val file = File(path)
            file.parentFile?.mkdirs()
            file.writeText(content)
        }
    }

    actual fun delete(path: String): Boolean = runCatching { File(path).delete() }.getOrDefault(false)

    actual fun mkdir(path: String) {
        runCatching { File(path).mkdirs() }
    }

    actual fun now(): Long = System.currentTimeMillis()

    actual fun applyLocale(tag: String) {
        Locale.setDefault(Locale.forLanguageTag(tag))
    }

    // Read once at object init, which happens before the first applyLocale() call.
    private val bootLanguage: String = Locale.getDefault().language

    actual fun systemLanguage(): String = bootLanguage

    actual fun openUrl(url: String) {
        runCatching {
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                Desktop.getDesktop().browse(URI(url))
                return
            }
            val command = when {
                osLabel == "Windows" -> listOf("rundll32", "url.dll,FileProtocolHandler", url)
                osLabel == "macOS" -> listOf("open", url)
                else -> listOf("xdg-open", url)
            }
            ProcessBuilder(command).start()
        }
    }
}
