package dev.kdroid.musicradio.player

import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class MediaArtworkCommonTest {

    @Test
    fun `returns null without artwork`() = runTest {
        assertNull(mediaArtworkUri("kol_hay", null))
    }

    @Test
    fun `returns null without an id`() = runTest {
        assertNull(mediaArtworkUri("", null))
    }

    @Test
    fun `names a jpeg after its content`() {
        assertEquals(
            ".jpg",
            byteArrayOf(0xFF.toByte(), 0xD8.toByte(), 0xFF.toByte()).imageExtension(),
        )
    }

    @Test
    fun `names a png after its content`() {
        assertEquals(
            ".png",
            byteArrayOf(0x89.toByte(), 'P'.code.toByte(), 'N'.code.toByte(), 'G'.code.toByte()).imageExtension(),
        )
    }

    @Test
    fun `falls back when the bytes are not a known image`() {
        assertEquals(".img", byteArrayOf(1, 2, 3).imageExtension())
    }
}
