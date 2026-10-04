package dev.kdroid.musicradio.player

import dev.nucleusframework.media.control.MediaControlEvent
import dev.nucleusframework.media.control.MediaControlService
import dev.nucleusframework.media.control.MediaMetadata
import dev.nucleusframework.media.control.MediaPlaybackState
import dev.nucleusframework.media.control.MediaPlaybackStatus

actual fun createMediaControls(): MediaControls = if (MediaControlService.isAvailable()) NucleusMediaControls() else NoMediaControls

private class NucleusMediaControls : MediaControls {
    override val available: Boolean = true

    init {
        MediaControlService.configure(dbusName = MPRIS_BUS_NAME, displayName = APP_DISPLAY_NAME)
    }

    override fun attach(onCommand: (MediaCommand) -> Unit) {
        MediaControlService.attach { event ->
            val command = when (event) {
                MediaControlEvent.Play -> MediaCommand.Play

                MediaControlEvent.Pause -> MediaCommand.Pause

                MediaControlEvent.Toggle -> MediaCommand.Toggle

                MediaControlEvent.Next -> MediaCommand.Next

                MediaControlEvent.Previous -> MediaCommand.Previous

                MediaControlEvent.Stop -> MediaCommand.Stop

                // Seeking and positions mean nothing on a live stream; volume and window
                // commands are the OS's business, not ours.
                else -> null
            }
            if (command != null) onCommand(command)
        }
    }

    override fun update(nowPlaying: NowPlaying, status: PlaybackStatus) {
        MediaControlService.setMetadata(
            MediaMetadata(
                title = nowPlaying.title.ifBlank { nowPlaying.station }.takeIf { it.isNotBlank() },
                artist = nowPlaying.artist.takeIf { it.isNotBlank() },
                album = nowPlaying.station.takeIf { it.isNotBlank() },
                coverUrl = nowPlaying.artworkUri,
                // A live stream has no length; -1 tells the OS to hide the scrubber.
                duration = null,
            ),
        )
        MediaControlService.setPlaybackState(
            MediaPlaybackState(
                status = when (status) {
                    PlaybackStatus.Playing, PlaybackStatus.Buffering -> MediaPlaybackStatus.PLAYING
                    PlaybackStatus.Paused -> MediaPlaybackStatus.PAUSED
                    PlaybackStatus.Idle, PlaybackStatus.Error -> MediaPlaybackStatus.STOPPED
                },
            ),
        )
    }

    override fun setVolume(percent: Int) {
        MediaControlService.setVolume(percent.coerceIn(0, 100) / 100.0)
    }

    override fun release() {
        MediaControlService.detach()
    }

    private companion object {
        const val APP_DISPLAY_NAME = "Music Radio"

        // Left to itself, Nucleus builds the MPRIS name from app.id, which the plugin injects
        // verbatim from packageName - "Music Radio", space included. A space is illegal in a
        // D-Bus well-known name: g_bus_own_name() asserts and returns without ever calling
        // on_name_acquired or on_name_lost, so the native attach() waits on a condition variable
        // nothing will signal. It runs on the Tao main thread during first composition, which
        // means the window never appears and the process keeps holding the single-instance lock.
        // Passing a legal name of our own sidesteps all of it.
        // https://github.com/NucleusFramework/Nucleus/issues/548
        const val MPRIS_BUS_NAME = "org.mpris.MediaPlayer2.MusicRadio"
    }
}
