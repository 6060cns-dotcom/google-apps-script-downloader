package dev.kdroid.musicradio.domain

import dev.kdroid.musicradio.data.decodeSnapshot
import dev.kdroid.musicradio.data.encodeSnapshot
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class StartupSettingsTest {

    private val channel = Stations.all.first { it.multiChannel }.channels.last()

    @Test
    fun `startup and wake choices survive a restart`() {
        val data = AppData(
            settings = UserSettings(
                startupPlay = StartupPlay.Chosen,
                startupChannelId = channel.id,
                playOnWake = true,
                wakeChannelId = channel.id,
            ),
        )
        val restored = decodeSnapshot(encodeSnapshot(data)).settings
        assertEquals(StartupPlay.Chosen, restored.startupPlay)
        assertEquals(channel.id, restored.startupChannelId)
        assertEquals(true, restored.playOnWake)
        assertEquals(channel.id, restored.wakeChannelId)
    }

    @Test
    fun `the old resume switch becomes last played`() {
        assertEquals(StartupPlay.LastPlayed, decodeSnapshot("resumeOnLaunch=true").settings.startupPlay)
        assertEquals(StartupPlay.Off, decodeSnapshot("resumeOnLaunch=false").settings.startupPlay)
    }

    @Test
    fun `launch channel follows the mode`() {
        val other = Stations.all.first { !it.multiChannel }.channels.first()
        val base = AppData(lastChannel = other.id, settings = UserSettings(startupChannelId = channel.id))

        assertNull(base.launchChannelId())
        assertEquals(
            other.id,
            base.copy(settings = base.settings.copy(startupPlay = StartupPlay.LastPlayed)).launchChannelId(),
        )
        assertEquals(
            channel.id,
            base.copy(settings = base.settings.copy(startupPlay = StartupPlay.Chosen)).launchChannelId(),
        )
    }

    @Test
    fun `a channel the catalog dropped starts nothing`() {
        val gone = AppData(settings = UserSettings(startupPlay = StartupPlay.Chosen, startupChannelId = "gone/1"))
        assertNull(gone.launchChannelId())
        val wake = AppData(settings = UserSettings(playOnWake = true, wakeChannelId = "gone/1"))
        assertNull(wake.wakeChannelIdOrNull())
    }

    @Test
    fun `wake channel needs both the switch and a channel`() {
        val off = AppData(settings = UserSettings(playOnWake = false, wakeChannelId = channel.id))
        assertNull(off.wakeChannelIdOrNull())
        val on = off.copy(settings = off.settings.copy(playOnWake = true))
        assertEquals(channel.id, on.wakeChannelIdOrNull())
    }
}
