package dev.kdroid.musicradio.platform

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.os.SystemClock
import android.provider.Settings
import android.util.Log
import dev.kdroid.musicradio.data.FileStore
import dev.kdroid.musicradio.domain.Stations
import dev.kdroid.musicradio.domain.UiLanguage
import dev.kdroid.musicradio.domain.wakeChannelIdOrNull
import dev.kdroid.musicradio.player.MediaSessionRadioPlayer
import io.github.santimattius.structured.annotations.StructuredScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Starts the chosen station whenever the device wakes from sleep.
 *
 * Android delivers `ACTION_SCREEN_ON` only to a receiver registered at runtime, never to one in the
 * manifest, so something has to stay alive to hold that registration. That is this service: a
 * foreground service with a small, quiet notification, running only while the setting is on.
 *
 * Playback itself is not done here. The service asks the same [PlaybackService] session the app
 * uses to play the stream, so the lock screen and the notification behave exactly as they do when
 * the user presses play in the app.
 */
internal const val EXTRA_FROM_BOOT = "fromBoot"

internal class WakeListenerService : Service() {

    @StructuredScope
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var player: MediaSessionRadioPlayer? = null
    private var lastStartedAt = -DEBOUNCE_MS

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == Intent.ACTION_SCREEN_ON) {
                WakeLog.add(context, "screen on")
                onWake()
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        bindAndroidContext(this)
        WakeLog.add(this, "service started")
        showNotification(FileStore().load().settings.uiLanguage)
        // Connected now rather than at the first wake, so the player already knows whether the
        // app is playing when the screen comes on - a listener must not swap the station under
        // someone who is already listening.
        player = MediaSessionRadioPlayer(applicationContext)
        val filter = IntentFilter(Intent.ACTION_SCREEN_ON)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(receiver, filter, Context.RECEIVER_EXPORTED)
        } else {
            registerReceiver(receiver, filter)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Started again after a reboot or an update with the setting since turned off: nothing to do.
        if (FileStore().load().wakeChannelIdOrNull() == null) {
            stopSelf()
            return START_NOT_STICKY
        }
        if (intent?.getBooleanExtra(EXTRA_FROM_BOOT, false) == true) {
            WakeLog.add(this, "started after boot")
            // The screen comes first: opening the app needs no network, so it does not wait for it.
            openApp()
            scope.launch { startAfterBoot() }
        }
        return START_STICKY
    }

    /**
     * A device that has just booted has no network for a few seconds, and a stream opened before
     * that fails. So wait a moment, then start, and look again a few times: a station that did not
     * take is started once more rather than left spinning.
     */
    private suspend fun startAfterBoot() {
        delay(BOOT_DELAY_MS)
        repeat(BOOT_ATTEMPTS) {
            onWake()
            delay(BOOT_RETRY_MS)
            if (player?.status?.value?.active == true) return
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        runCatching { unregisterReceiver(receiver) }
        player?.release()
        player = null
        scope.cancel()
        super.onDestroy()
    }

    private fun onWake() {
        runCatching { handleWake() }.onFailure { Log.e(TAG, "wake start failed", it) }
    }

    private fun handleWake() {
        val now = SystemClock.elapsedRealtime()
        // The screen can flicker on twice in a row; one start is enough.
        if (now - lastStartedAt < DEBOUNCE_MS) return WakeLog.add(this, "skipped: just started")
        val player = player ?: return WakeLog.add(this, "skipped: no player")
        if (player.status.value.active) return WakeLog.add(this, "skipped: already playing")
        val data = FileStore().load()
        val channelId = data.wakeChannelIdOrNull() ?: return WakeLog.add(this, "skipped: no station chosen")
        val station = Stations.stationOfChannel(channelId) ?: return WakeLog.add(this, "skipped: unknown station")
        val channel = Stations.channel(channelId) ?: return WakeLog.add(this, "skipped: unknown channel")
        WakeLog.add(this, "playing $channelId")
        lastStartedAt = now
        val settings = data.settings
        player.setVolume(if (settings.muted) 0 else settings.volume)
        scope.launch {
            val name = runCatching { localizedString(settings.uiLanguage.code, station.name) }.getOrDefault("")
            player.setNowPlaying(station = channel.title ?: name, song = "", artworkUri = null)
            player.play(channel.streamUrl)
        }
        // So that "continue with the last station" on the next launch means this one.
        FileStore().save(data.copy(lastChannel = channelId))
        openApp()
    }

    /**
     * Brings the app to the front as well. Android blocks activities started from the background
     * unless the user has granted "display over other apps", so without that permission the radio
     * still plays and the screen is simply left as it is.
     */
    private fun openApp() {
        if (!Settings.canDrawOverlays(this)) return WakeLog.add(this, "app not opened: no overlay permission")
        val launch = packageManager.getLaunchIntentForPackage(packageName) ?: return
        runCatching { startActivity(launch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) }
            .onSuccess { WakeLog.add(this, "app opened") }
            .onFailure { WakeLog.add(this, "app not opened: ${it.javaClass.simpleName}") }
    }

    private fun showNotification(language: UiLanguage) {
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(
            NotificationChannel(CHANNEL_ID, text(language, channel = true), NotificationManager.IMPORTANCE_MIN),
        )
        val open = packageManager.getLaunchIntentForPackage(packageName)?.let {
            PendingIntent.getActivity(this, 0, it, PendingIntent.FLAG_IMMUTABLE)
        }
        val notification = Notification.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setContentTitle(text(language, channel = false))
            .setContentIntent(open)
            .setOngoing(true)
            .build()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE)
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
    }

    // Android resources do not reach this module, so the two lines it shows live here.
    private fun text(language: UiLanguage, channel: Boolean): String = when (language) {
        UiLanguage.Hebrew -> if (channel) "הפעלה בהתעוררות" else "הרדיו יתנגן כשהמכשיר מתעורר"
        UiLanguage.French -> if (channel) "Lecture au réveil" else "La radio démarre au réveil de l'appareil"
        UiLanguage.English -> if (channel) "Play on wake" else "Radio starts when the device wakes"
    }

    private companion object {
        const val TAG = "MusicRadioWake"
        const val CHANNEL_ID = "wake_listener"
        const val NOTIFICATION_ID = 2
        const val DEBOUNCE_MS = 10_000L
        const val BOOT_DELAY_MS = 3_000L
        const val BOOT_RETRY_MS = 12_000L
        const val BOOT_ATTEMPTS = 4
    }
}

/**
 * After a reboot, starts the chosen station and opens the app, as if the device had just woken; after
 * an app update it only puts the wake listener back. Does nothing unless the setting is on.
 */
internal class WakeBootReceiver : BroadcastReceiver() {
    private companion object {
        // The two QUICKBOOT actions are what several phone makers send instead of the standard one.
        val BOOT_ACTIONS = setOf(
            Intent.ACTION_BOOT_COMPLETED,
            "android.intent.action.QUICKBOOT_POWERON",
            "com.htc.intent.action.QUICKBOOT_POWERON",
            // Android head units announce ignition and wake-up with their own broadcasts.
            "autochips.intent.action.QB_POWERON",
            "com.microntek.bootcheck",
            "android.intent.action.ACC_ON",
            "com.cayboy.action.ACC_ON",
        )
    }

    override fun onReceive(context: Context, intent: Intent) {
        val booted = intent.action in BOOT_ACTIONS
        if (!booted && intent.action != Intent.ACTION_MY_PACKAGE_REPLACED) return
        bindAndroidContext(context)
        WakeLog.add(context, "received ${intent.action}")
        if (FileStore().load().wakeChannelIdOrNull() == null) return WakeLog.add(context, "ignored: play on wake is off")
        // Only a real boot plays; an app update just puts the listener back.
        val start = Intent(context, WakeListenerService::class.java).putExtra(EXTRA_FROM_BOOT, booted)
        runCatching { context.startForegroundService(start) }
    }
}
