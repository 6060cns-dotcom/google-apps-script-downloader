package dev.kdroid.musicradio.platform

/**
 * Starts or stops whatever the platform uses to notice the device waking from sleep. Where the
 * platform has no such thing this does nothing, and the setting is simply never offered.
 */
internal expect fun syncWakeListener(enabled: Boolean)

/** `true` where [syncWakeListener] actually does something, so the setting is only shown there. */
internal expect val wakeListenerSupported: Boolean

/** What the wake listener recently saw and did, one event per line; empty where there is none. */
internal expect fun wakeEventLog(): String
