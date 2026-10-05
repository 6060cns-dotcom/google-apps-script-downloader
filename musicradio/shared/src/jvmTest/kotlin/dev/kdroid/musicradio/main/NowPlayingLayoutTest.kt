package dev.kdroid.musicradio.main

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.runComposeUiTest
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.kdroid.musicradio.app.AppState
import dev.kdroid.musicradio.app.PlaybackState
import dev.kdroid.musicradio.domain.Stations
import dev.kdroid.musicradio.player.PlaybackStatus
import kotlinx.coroutines.runBlocking
import musicradio.shared.generated.resources.Res
import musicradio.shared.generated.resources.player_back
import musicradio.shared.generated.resources.player_pause
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.getString
import kotlin.test.Test
import kotlin.test.assertTrue

/**
 * The compact player has no scroll: whatever does not fit is simply not reachable. The artwork used
 * to be 80% of the width with a square ratio, so on a short window it grew past the room left for
 * the transport row and the play button went off the bottom edge with nothing to say so.
 *
 * These render the screen at real phone sizes and check the play button lands inside the window,
 * which is the part that silently broke.
 */
@OptIn(ExperimentalTestApi::class)
class NowPlayingLayoutTest {

    private val station = Stations.all.first { it.multiChannel }

    // Resolved the same way the screen resolves it: these run under whatever locale the JVM is in,
    // and hardcoding the English label fails on a Hebrew machine for reasons that have nothing to
    // do with layout.
    private fun label(resource: StringResource): String = runBlocking { getString(resource) }

    private fun state() = AppState(
        playback = PlaybackState(
            stationId = station.id,
            channelId = station.channels.first().id,
            status = PlaybackStatus.Playing,
            nowPlaying = "Some rather long track title that wants two lines",
        ),
    )

    private fun assertPlayButtonFits(width: Dp, height: Dp) = runComposeUiTest {
        setContent {
            Box(Modifier.requiredSize(width, height)) {
                NowPlayingScreen(state(), {})
            }
        }
        // The play button carries whichever action it would perform, so it reads "pause" here.
        val bounds = onNodeWithContentDescription(label(Res.string.player_pause)).getUnclippedBoundsInRoot()
        val window = "${width}x$height"
        val buttonHeight = bounds.bottom - bounds.top

        // Height, not just position. The old layout did not push this button off the bottom - it
        // squeezed it to nothing, top and bottom landing on the same pixel, which reads as "the
        // play button is missing" and passes any check that only asks whether it is above the fold.
        assertTrue(
            buttonHeight >= MinimumTouchTarget,
            "the play button collapsed to $buttonHeight in a $window window",
        )
        assertTrue(bounds.top >= 0.dp, "the play button ran off the top of a $window window")
        assertTrue(
            bounds.bottom <= height,
            "the play button ran past the bottom of a $window window: ${bounds.bottom}",
        )
    }

    @Test
    fun `play button fits a tall phone`() = assertPlayButtonFits(390.dp, 844.dp)

    @Test
    fun `play button fits a short phone`() = assertPlayButtonFits(360.dp, 640.dp)

    @Test
    fun `play button fits a very short window`() = assertPlayButtonFits(360.dp, 480.dp)

    @Test
    fun `play button fits landscape`() = assertPlayButtonFits(740.dp, 360.dp)

    @Test
    fun `the way back is on the screen`() = runComposeUiTest {
        setContent {
            Box(Modifier.requiredSize(360.dp, 640.dp)) {
                NowPlayingScreen(state(), {})
            }
        }
        // Nothing else on this screen goes back, and it is not inside MainShell's bottom bar.
        onNodeWithContentDescription(label(Res.string.player_back)).assertExists()
    }

    private companion object {
        /** Material's minimum, and well under the 52.dp the compact transport row asks for. */
        val MinimumTouchTarget = 40.dp
    }
}
