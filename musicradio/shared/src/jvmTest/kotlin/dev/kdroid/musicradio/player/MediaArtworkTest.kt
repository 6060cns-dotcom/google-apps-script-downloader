package dev.kdroid.musicradio.player

import kotlinx.coroutines.test.runTest
import musicradio.shared.generated.resources.Res
import musicradio.shared.generated.resources.station_kol_hay
import java.net.URI
import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class MediaArtworkTest {

    @Test
    fun `writes bundled artwork to a file the media center can open`() = runTest {
        val uri = assertNotNull(mediaArtworkUri("kol_hay", Res.drawable.station_kol_hay))
        assertTrue(uri.startsWith("file:/"), "MPRIS needs a file:// URI, got $uri")
        val file = Path.of(URI.create(uri))
        assertTrue(Files.isRegularFile(file), "no file at $file")
        assertTrue(Files.size(file) > 0, "empty artwork file")
    }
}
