package dev.kdroid.musicradio.platform

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.PowerManager
import android.provider.Settings

private const val PREFS = "wake_listener"
private const val ASKED_OVERLAY = "askedOverlay"
private const val ASKED_BATTERY = "askedBattery"

internal actual fun syncWakeListener(enabled: Boolean) {
    val context = androidContext()
    val intent = Intent(context, WakeListenerService::class.java)
    // Called from the foreground app, so starting a foreground service is allowed. A refusal must
    // never take the settings screen down with it.
    runCatching { if (enabled) context.startForegroundService(intent) else context.stopService(intent) }
    if (enabled) {
        askOverlayPermissionOnce(context)
        askIgnoreBatteryOptimisationOnce(context)
    }
}

/**
 * Opening the app from the background needs "display over other apps". Asked for once, when the
 * feature is first switched on; declining leaves playback working without the screen opening.
 */
private fun askOverlayPermissionOnce(context: Context) {
    val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
    if (Settings.canDrawOverlays(context) || prefs.getBoolean(ASKED_OVERLAY, false)) return
    prefs.edit().putBoolean(ASKED_OVERLAY, true).apply()
    val request = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:${context.packageName}"))
        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    runCatching { context.startActivity(request) }
}

/**
 * A listener the system is free to freeze never hears the screen come on, so the app asks once to
 * be left out of battery optimisation. Declining is fine; some devices simply need it.
 */
private fun askIgnoreBatteryOptimisationOnce(context: Context) {
    val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
    val power = context.getSystemService(PowerManager::class.java)
    if (power.isIgnoringBatteryOptimizations(context.packageName) || prefs.getBoolean(ASKED_BATTERY, false)) return
    prefs.edit().putBoolean(ASKED_BATTERY, true).apply()
    val request = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS, Uri.parse("package:${context.packageName}"))
        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    runCatching { context.startActivity(request) }
}

internal actual val wakeListenerSupported: Boolean = true

internal actual fun wakeEventLog(): String = WakeLog.read(androidContext())
