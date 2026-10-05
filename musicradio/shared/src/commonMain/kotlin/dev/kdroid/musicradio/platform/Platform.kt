package dev.kdroid.musicradio.platform

import dev.kdroid.musicradio.domain.UiLanguage

internal expect object Platform {
    val osLabel: String
    val appVersion: String

    /** Writable directory the app owns: the settings snapshot lives here. */
    fun appDir(): String
    fun readText(path: String): String?
    fun writeText(path: String, content: String)
    fun delete(path: String): Boolean
    fun mkdir(path: String)
    fun now(): Long
    fun applyLocale(tag: String)

    /**
     * The OS language, captured at startup - [applyLocale] overwrites the default locale, so this
     * has to be read before the app ever applies its own.
     */
    fun systemLanguage(): String
    fun openUrl(url: String)
}

internal fun systemUiLanguage(): UiLanguage = UiLanguage.fromCode(Platform.systemLanguage())

internal fun pathSeparator(path: String): Char = if (path.contains('\\') && !path.contains('/')) '\\' else '/'

internal fun joinPath(dir: String, name: String): String {
    val sep = pathSeparator(dir)
    return if (dir.endsWith('/') || dir.endsWith('\\')) dir + name else dir + sep + name
}
