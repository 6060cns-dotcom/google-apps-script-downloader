package dev.kdroid.musicradio.app

import dev.kdroid.musicradio.data.MemoryStore
import dev.kdroid.musicradio.domain.AppData
import dev.kdroid.musicradio.player.IcyMetadata
import dev.kdroid.musicradio.player.SilentRadioPlayer
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respondError
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * What a rail or bottom-bar tap does to the back stack.
 *
 * RootScreen reads `backStack.last()` on every composition and NavDisplay animates every change it
 * sees, so a tap that rebuilds a destination it was already showing costs a real screen - its
 * scroll position, and a transition that had no reason to run.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class NavigationTest {

    private fun viewModel(dispatcher: TestDispatcher) = AppViewModel(
        store = MemoryStore(AppData()),
        player = SilentRadioPlayer(),
        icyMetadata = IcyMetadata(HttpClient(MockEngine { respondError(HttpStatusCode.ServiceUnavailable) })),
        dispatcher = dispatcher,
    )

    @Test
    fun `tapping the tab you are already on leaves the stack alone`() = runTest {
        val vm = viewModel(StandardTestDispatcher(testScheduler))
        advanceUntilIdle()
        vm.onIntent(AppIntent.Navigate(AppKey.Settings))
        val before = vm.backStack.single()

        vm.onIntent(AppIntent.Navigate(AppKey.Settings))

        // Identity, not equality: a replaced entry is a new screen, holding none of the scroll
        // position the old one had.
        assertTrue(before === vm.backStack.single(), "the destination was torn down and rebuilt")
    }

    @Test
    fun `switching tabs leaves exactly one destination behind`() = runTest {
        val vm = viewModel(StandardTestDispatcher(testScheduler))
        advanceUntilIdle()

        MainDestinations.forEach { dest ->
            vm.onIntent(AppIntent.Navigate(dest))
            assertEquals(listOf(dest), vm.backStack.toList())
        }
    }

    @Test
    fun `a tab switch drops whatever was stacked on top of it`() = runTest {
        val vm = viewModel(StandardTestDispatcher(testScheduler))
        advanceUntilIdle()
        vm.onIntent(AppIntent.OpenNowPlaying)
        assertEquals(listOf(AppKey.Stations, AppKey.NowPlaying), vm.backStack.toList())

        vm.onIntent(AppIntent.Navigate(AppKey.About))

        assertEquals(listOf(AppKey.About), vm.backStack.toList())
    }
}
