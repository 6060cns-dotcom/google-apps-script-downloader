package dev.kdroid.musicradio.app

import dev.kdroid.musicradio.data.MemoryStore
import dev.kdroid.musicradio.domain.AppData
import dev.kdroid.musicradio.domain.Stations
import dev.kdroid.musicradio.player.IcyMetadata
import dev.kdroid.musicradio.player.PlaybackStatus
import dev.kdroid.musicradio.player.RadioPlayer
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respondError
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * What the play button does to a live stream that is sitting paused.
 *
 * Worth a test of its own because the wrong answer is invisible from the inside: the app reported
 * "playing" either way, and the only symptom was that the audio carried on from where the pause
 * had been - minutes of stale ad break - instead of from the live broadcast. The user's report was
 * that pressing the station twice fixed it, which is exactly the difference between reopening the
 * stream and un-pausing the backend.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class TogglePlayTest {

    /** Records what the ViewModel asked for, and reports whatever status the test dictates. */
    private class RecordingPlayer : RadioPlayer {
        val calls = mutableListOf<String>()

        private val _status = MutableStateFlow(PlaybackStatus.Idle)
        override val status: StateFlow<PlaybackStatus> = _status.asStateFlow()

        fun report(status: PlaybackStatus) {
            _status.value = status
        }

        override fun play(url: String) {
            calls += "play($url)"
        }

        override fun resume() {
            calls += "resume"
        }

        override fun pause() {
            calls += "pause"
        }

        override fun stop() {
            calls += "stop"
        }

        override fun setVolume(percent: Int) = Unit
        override fun release() = Unit
    }

    private val channel = Stations.all.first().channels.first()

    /** Never reached: the metadata poll only runs while a stream is actually playing. */
    private fun offlineMetadata() = IcyMetadata(
        HttpClient(MockEngine { respondError(HttpStatusCode.ServiceUnavailable) }),
    )

    private fun viewModel(player: RadioPlayer, dispatcher: TestDispatcher) = AppViewModel(
        store = MemoryStore(AppData(lastChannel = channel.id)),
        player = player,
        icyMetadata = offlineMetadata(),
        dispatcher = dispatcher,
    )

    @Test
    fun `playing a paused live stream reopens it rather than resuming the buffer`() = runTest {
        val player = RecordingPlayer()
        val vm = viewModel(player, StandardTestDispatcher(testScheduler))
        advanceUntilIdle()

        // The state the report describes: a station chosen, and playback paused on it.
        player.report(PlaybackStatus.Paused)
        advanceUntilIdle()
        assertEquals(PlaybackStatus.Paused, vm.state.value.playback.status)
        player.calls.clear()

        vm.onIntent(AppIntent.TogglePlay)

        assertEquals(listOf("play(${channel.streamUrl})"), player.calls)
        assertTrue("resume" !in player.calls, "resume() hands back the stale buffer from the pause")
    }

    @Test
    fun `playing a stopped live stream still opens the stream`() = runTest {
        val player = RecordingPlayer()
        val vm = viewModel(player, StandardTestDispatcher(testScheduler))
        advanceUntilIdle()
        player.calls.clear()

        vm.onIntent(AppIntent.TogglePlay)

        assertEquals(listOf("play(${channel.streamUrl})"), player.calls)
        assertEquals(PlaybackStatus.Buffering, vm.state.value.playback.status)
    }

    @Test
    fun `pressing play while it is running pauses instead`() = runTest {
        val player = RecordingPlayer()
        val vm = viewModel(player, StandardTestDispatcher(testScheduler))
        advanceUntilIdle()
        player.report(PlaybackStatus.Playing)
        advanceUntilIdle()
        player.calls.clear()

        vm.onIntent(AppIntent.TogglePlay)

        assertEquals(listOf("pause"), player.calls)
    }
}
