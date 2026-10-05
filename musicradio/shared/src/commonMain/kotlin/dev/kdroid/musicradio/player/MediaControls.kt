package dev.kdroid.musicradio.player

import androidx.compose.runtime.Immutable

/** What the OS media center can ask the app to do. */
enum class MediaCommand { Play, Pause, Toggle, Next, Previous, Stop }

/**
 * What the OS media center shows about the current stream.
 *
 * [artworkUri] points at a file the media center can open on its own - see [mediaArtworkUri].
 */
@Immutable
data class NowPlaying(val station: String = "", val title: String = "", val artist: String = "", val artworkUri: String? = null)

/**
 * The system media center — SMTC on Windows, MPRIS on Linux, Now Playing on macOS. Lets the
 * hardware media keys and the OS flyout drive the app, and puts the current stream where the OS
 * expects to find it.
 */
interface MediaControls {
    val available: Boolean

    /** Replaces any previous listener. */
    fun attach(onCommand: (MediaCommand) -> Unit)
    fun update(nowPlaying: NowPlaying, status: PlaybackStatus)
    fun setVolume(percent: Int)
    fun release()
}

/** Stands in where the platform has no media center to talk to. */
object NoMediaControls : MediaControls {
    override val available: Boolean = false
    override fun attach(onCommand: (MediaCommand) -> Unit) = Unit
    override fun update(nowPlaying: NowPlaying, status: PlaybackStatus) = Unit
    override fun setVolume(percent: Int) = Unit
    override fun release() = Unit
}

expect fun createMediaControls(): MediaControls
