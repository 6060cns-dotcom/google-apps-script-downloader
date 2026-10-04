package dev.kdroid.musicradio.player

/**
 * Android's equivalent is a `MediaSession`, which has to be owned by the ExoPlayer instance —
 * and that instance lives inside Compose Media Player, which does not expose it. Until it does,
 * the lock-screen and notification controls stay unimplemented here.
 */
actual fun createMediaControls(): MediaControls = NoMediaControls
