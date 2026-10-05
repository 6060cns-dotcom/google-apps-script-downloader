package dev.kdroid.musicradio.player

import io.github.kdroidfilter.composemediaplayer.audio.AudioPlayer
import io.github.kdroidfilter.composemediaplayer.audio.AudioPlayerState
import io.github.kdroidfilter.composemediaplayer.audio.ErrorListener
import io.github.santimattius.structured.annotations.StructuredScope
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

private const val POLL_INTERVAL_MS = 250L

/**
 * [RadioPlayer] on top of Compose Media Player's audio module - Media3 on Android, Rodio on the
 * desktop JVM.
 *
 * The backend only offers a pollable state, so this class polls it. What it does *not* do is trust
 * that state blindly: connecting to an Icecast stream can take seconds, during which the backend
 * still reports `IDLE`. Reporting that back as "stopped" would flip the play button under the
 * user's finger, so a fresh [play] holds [PlaybackStatus.Buffering] until either the stream comes
 * up or the backend reports an error.
 */
class StreamRadioPlayer(dispatcher: CoroutineDispatcher = Dispatchers.Default, private val backend: AudioPlayer = AudioPlayer()) :
    RadioPlayer {

    private enum class Wanted { Stopped, Playing, Paused }

    // Owned by this player and cancelled in release(), so the poll loop cannot outlive the backend.
    @StructuredScope
    private val scope = CoroutineScope(SupervisorJob() + dispatcher)
    private val _status = MutableStateFlow(PlaybackStatus.Idle)
    override val status: StateFlow<PlaybackStatus> = _status.asStateFlow()

    private var wanted: Wanted = Wanted.Stopped

    init {
        backend.setOnErrorListener(
            object : ErrorListener {
                override fun onError(message: String?) = fail()
            },
        )
        scope.launch {
            while (isActive) {
                delay(POLL_INTERVAL_MS)
                sync()
            }
        }
    }

    override fun play(url: String) {
        wanted = Wanted.Playing
        _status.value = PlaybackStatus.Buffering
        runCatching { backend.play(url) }.onFailure { fail() }
    }

    override fun resume() {
        if (wanted == Wanted.Playing) return
        wanted = Wanted.Playing
        _status.value = PlaybackStatus.Buffering
        runCatching { backend.play() }.onFailure { fail() }
    }

    override fun pause() {
        wanted = Wanted.Paused
        _status.value = PlaybackStatus.Paused
        runCatching { backend.pause() }.onFailure { fail() }
    }

    override fun stop() {
        wanted = Wanted.Stopped
        _status.value = PlaybackStatus.Idle
        runCatching { backend.stop() }.onFailure { fail() }
    }

    override fun setVolume(percent: Int) {
        runCatching { backend.setVolume(percent.coerceIn(0, 100) / 100f) }
    }

    override fun release() {
        wanted = Wanted.Stopped
        runCatching { backend.stop() }
        runCatching { backend.release() }
        _status.value = PlaybackStatus.Idle
        scope.cancel()
    }

    private fun fail() {
        wanted = Wanted.Stopped
        _status.value = PlaybackStatus.Error
    }

    private fun sync() {
        // An error stays on screen until the user acts on it; polling must not wipe it.
        if (_status.value == PlaybackStatus.Error) return
        val reported = runCatching { backend.currentPlayerState() }.getOrNull()
        _status.value = when (wanted) {
            Wanted.Stopped -> PlaybackStatus.Idle

            Wanted.Paused -> PlaybackStatus.Paused

            // IDLE while we asked for playback means the connection is still being opened.
            Wanted.Playing -> when (reported) {
                AudioPlayerState.PLAYING -> PlaybackStatus.Playing
                AudioPlayerState.PAUSED -> PlaybackStatus.Paused
                else -> PlaybackStatus.Buffering
            }
        }
    }
}
