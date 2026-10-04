package dev.kdroid.musicradio.platform

import android.content.Intent

internal actual fun syncWakeListener(enabled: Boolean) {
    val context = androidContext()
    val intent = Intent(context, WakeListenerService::class.java)
    // Called from the foreground app, so starting a foreground service is allowed. A refusal must
    // never take the settings screen down with it.
    runCatching { if (enabled) context.startForegroundService(intent) else context.stopService(intent) }
}

internal actual val wakeListenerSupported: Boolean = true
