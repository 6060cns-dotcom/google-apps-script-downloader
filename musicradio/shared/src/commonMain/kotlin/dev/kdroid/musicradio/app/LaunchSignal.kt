package dev.kdroid.musicradio.app

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * "The app was just opened again". A launcher, a head unit's wake-up task or the wake listener can
 * start the activity while the process is still alive, in which case nothing is created afresh and
 * the startup setting would never be looked at. The host posts here, and the view model treats it
 * like a launch.
 */
object LaunchSignal {
    private val _events = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val events: SharedFlow<Unit> = _events.asSharedFlow()

    fun emit() {
        _events.tryEmit(Unit)
    }
}
