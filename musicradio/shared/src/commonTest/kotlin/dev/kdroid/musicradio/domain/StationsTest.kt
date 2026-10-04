package dev.kdroid.musicradio.domain

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * The catalog is 150-odd hand-maintained entries, and the lookups over it are built with
 * `associateBy`, which drops duplicates without a word. A repeated id therefore does not fail the
 * build - it quietly makes one channel unreachable and sends the player to the wrong stream. These
 * check the shape of the data, not which stations happen to be in it, so adding a station is free.
 */
class StationsTest {

    @Test
    fun `station ids are unique`() {
        assertNoDuplicates(Stations.all.map { it.id }, "station id")
    }

    @Test
    fun `channel ids are unique across the whole catalog`() {
        // Not just within a station: Stations.channel() looks up a single flat map.
        assertNoDuplicates(Stations.all.flatMap { it.channels }.map { it.id }, "channel id")
    }

    @Test
    fun `every station resolves by its own id`() {
        for (station in Stations.all) {
            assertEquals(station, Stations.of(station.id), "Stations.of lost ${station.id}")
        }
    }

    @Test
    fun `every channel resolves by its own id`() {
        for (station in Stations.all) {
            for (channel in station.channels) {
                assertEquals(channel, Stations.channel(channel.id), "Stations.channel lost ${channel.id}")
                assertEquals(
                    station,
                    Stations.stationOfChannel(channel.id),
                    "${channel.id} resolved to the wrong station",
                )
            }
        }
    }

    @Test
    fun `every station has at least one channel`() {
        for (station in Stations.all) {
            assertTrue(station.channels.isNotEmpty(), "${station.id} has no channels")
        }
    }

    @Test
    fun `every stream url is an absolute http url`() {
        for (station in Stations.all) {
            for (channel in station.channels) {
                val url = channel.streamUrl
                assertTrue(url.isNotBlank(), "${channel.id} has no stream url")
                assertTrue(
                    url.startsWith("http://") || url.startsWith("https://"),
                    "${channel.id} is not an absolute http url: $url",
                )
                assertTrue(url.trim() == url, "${channel.id} has whitespace around its url")
            }
        }
    }

    @Test
    fun `a station never lists the same stream twice`() {
        // Two entries pointing at one mount is a copy-paste that survives every other check:
        // both play, so nothing looks broken, and the picker just shows a phantom channel.
        for (station in Stations.all) {
            assertNoDuplicates(station.channels.map { it.streamUrl }, "stream url in ${station.id}")
        }
    }

    @Test
    fun `a channel title is either absent or meaningful`() {
        for (station in Stations.all) {
            for (channel in station.channels) {
                val title = channel.title ?: continue
                assertTrue(title.isNotBlank(), "${channel.id} has a blank title; use null instead")
                assertEquals(title.trim(), title, "${channel.id} has whitespace around its title")
            }
        }
    }

    @Test
    fun `multiChannel agrees with the channel list`() {
        for (station in Stations.all) {
            assertEquals(station.channels.size > 1, station.multiChannel, "${station.id}")
        }
    }

    @Test
    fun `hiding the news leaves every other station in place`() {
        val hidden = visibleStations(showNews = false)
        assertEquals(Stations.all.filter { it.category != StationCategory.News }, hidden)
        assertTrue(hidden.none { it.category == StationCategory.News }, "a news station survived")
        assertEquals(Stations.all, visibleStations(showNews = true))
    }

    @Test
    fun `the catalog is not empty`() {
        // Guards against a refactor that leaves `all` initialised but unpopulated: every other
        // test here passes vacuously over an empty list.
        assertTrue(Stations.all.size > 10, "only ${Stations.all.size} stations")
        assertNotNull(Stations.all.firstOrNull { it.multiChannel }, "no multi channel station left")
    }
}

private fun assertNoDuplicates(values: List<String>, what: String) {
    val duplicates = values.groupingBy { it }.eachCount().filterValues { it > 1 }.keys
    assertTrue(duplicates.isEmpty(), "duplicate $what: ${duplicates.sorted()}")
}
