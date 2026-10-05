package dev.kdroid.musicradio.player

import org.jetbrains.compose.resources.DrawableResource

/**
 * Makes bundled artwork reachable by the OS media center, which loads the cover itself and cannot
 * see inside the app: MPRIS takes a `file://` URI, SMTC and Now Playing take a stream opened from
 * one, and Android's MediaSession takes the same URI so Media3 can decode it into the notification
 * and lock-screen Bitmap. Returns null when there is nothing to show, or when the artwork could
 * not be materialised - a missing cover is not worth failing a track change over.
 *
 * [id] identifies the artwork across calls, so the same stream is only ever written out once.
 */
expect suspend fun mediaArtworkUri(id: String, artwork: DrawableResource?): String?

/**
 * The media center sniffs the file rather than trusting us, but Windows in particular refuses a
 * cover whose extension contradicts its content, so name it after what the bytes actually are.
 */
internal fun ByteArray.imageExtension(): String = when {
    size >= 3 && this[0] == 0xFF.toByte() && this[1] == 0xD8.toByte() && this[2] == 0xFF.toByte() -> ".jpg"
    size >= 4 && this[0] == 0x89.toByte() && this[1] == 'P'.code.toByte() -> ".png"
    else -> ".img"
}

internal val UNSAFE_IN_FILE_NAME = Regex("[^A-Za-z0-9._-]")
