package dev.kdroid.musicradio.platform

internal actual fun syncWakeListener(enabled: Boolean) = Unit

internal actual val wakeListenerSupported: Boolean = false

internal actual fun wakeEventLog(): String = ""
