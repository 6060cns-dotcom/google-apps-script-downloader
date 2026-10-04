package dev.kdroid.musicradio.platform

import web.dom.document
import web.navigator.navigator
import web.storage.localStorage
import web.window.WindowName
import web.window.window
import kotlin.time.Clock

private const val APP_DIR = "musicradio"

private fun setCustomLocale(tag: String): Unit = js("window.__customLocale = tag")

internal actual object Platform {
    actual val osLabel: String = "Web"

    actual val appVersion: String = ""

    actual fun appDir(): String = APP_DIR

    actual fun readText(path: String): String? = localStorage.getItem(path)

    actual fun writeText(path: String, content: String) {
        localStorage.setItem(path, content)
    }

    actual fun delete(path: String): Boolean {
        if (localStorage.getItem(path) == null) return false
        localStorage.removeItem(path)
        return true
    }

    actual fun mkdir(path: String) = Unit

    actual fun now(): Long = Clock.System.now().toEpochMilliseconds()

    actual fun applyLocale(tag: String) {
        val normalized = tag.replace('_', '-')
        document.documentElement.lang = normalized
        setCustomLocale(normalized)
    }

    private val bootLanguage: String = navigator.language

    actual fun systemLanguage(): String = bootLanguage

    actual fun openUrl(url: String) {
        window.open(url, WindowName("_blank"))
    }
}
