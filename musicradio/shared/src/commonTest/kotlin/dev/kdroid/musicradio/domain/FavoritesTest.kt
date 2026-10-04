package dev.kdroid.musicradio.domain

import dev.kdroid.musicradio.data.decodeSnapshot
import dev.kdroid.musicradio.data.encodeSnapshot
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Favorites hold station ids and channel ids in one set. The two can only share a set because a
 * channel id always carries a slash and a station id never does, and they only survive a restart
 * because the snapshot joins them on a comma - so both of those are worth pinning down rather than
 * rediscovering the day an id grows a comma in it.
 */
class FavoritesTest {

    @Test
    fun `a station and a channel of that station are starred independently`() {
        val station = Stations.all.first { it.multiChannel }
        val channel = station.channels.first()

        val withChannel = AppData().toggleFavorite(channel.id)
        assertTrue(withChannel.isFavorite(channel.id), "the channel did not stick")
        assertFalse(withChannel.isFavorite(station.id), "starring a channel dragged its station along")

        val withBoth = withChannel.toggleFavorite(station.id)
        assertTrue(withBoth.isFavorite(channel.id) && withBoth.isFavorite(station.id))

        val channelRemoved = withBoth.toggleFavorite(channel.id)
        assertFalse(channelRemoved.isFavorite(channel.id), "un-starring the channel did not take")
        assertTrue(channelRemoved.isFavorite(station.id), "un-starring the channel took its station too")
    }

    @Test
    fun `no catalog id contains the separator the snapshot joins favorites on`() {
        val ids = Stations.all.map { it.id } + Stations.all.flatMap { it.channels }.map { it.id }
        val offenders = ids.filter { ',' in it || '\n' in it || '=' in it }
        assertEquals(emptyList(), offenders, "these ids cannot round-trip through the snapshot")
    }

    @Test
    fun `stations and channels both come back after a restart`() {
        val station = Stations.all.first { it.multiChannel }
        val data = AppData()
            .toggleFavorite(station.id)
            .toggleFavorite(station.channels.first().id)
            .toggleFavorite(station.channels.last().id)

        assertEquals(data.favorites, decodeSnapshot(encodeSnapshot(data)).favorites)
    }

    @Test
    fun `a snapshot written before channels were starrable still reads`() {
        // Only station ids, no slashes: what an older install has on disk.
        val ids = Stations.all.take(3).map { it.id }
        val restored = decodeSnapshot("favorites=${ids.joinToString(",")}")
        assertEquals(ids.toSet(), restored.favorites)
    }
}
