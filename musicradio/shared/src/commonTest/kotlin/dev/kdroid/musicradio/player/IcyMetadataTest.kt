package dev.kdroid.musicradio.player

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/**
 * ICY is the only part of the app that parses something a third party controls, and both of these
 * functions were written against stations that break the format in different ways. The cases below
 * are the ones actually observed in the catalog.
 */
class IcyMetadataTest {

    @Test
    fun `reads the title out of a metadata block`() {
        assertEquals(
            "Avraham Fried - Aderaba",
            parseStreamTitle("StreamTitle='Avraham Fried - Aderaba';StreamUrl='';"),
        )
    }

    @Test
    fun `keeps a title that spans lines`() {
        // `[\s\S]` is deliberate: at least one mount wraps long titles.
        assertEquals("first\nsecond", parseStreamTitle("StreamTitle='first\nsecond';"))
    }

    @Test
    fun `trims the padding stations leave around the title`() {
        assertEquals("Shwekey", parseStreamTitle("StreamTitle='   Shwekey   ';"))
    }

    @Test
    fun `has nothing to report when the field is absent`() {
        assertNull(parseStreamTitle("StreamUrl='https://example.test';"))
    }

    @Test
    fun `has nothing to report when the field is empty`() {
        assertNull(parseStreamTitle("StreamTitle='';"))
    }

    @Test
    fun `drops the placeholders stations park in the field forever`() {
        // Showing "line" under the station name is worse than showing nothing.
        for (placeholder in listOf("line", "LINE", "unknown", "-", "n/a", "N/A")) {
            assertNull(parseStreamTitle("StreamTitle='$placeholder';"), "kept $placeholder")
        }
    }

    @Test
    fun `drops the CDN advert some mounts send instead of a song`() {
        assertNull(parseStreamTitle("StreamTitle='Powered By ShoutCast';"))
        assertNull(parseStreamTitle("StreamTitle='powered by liquidsoap';"))
    }

    @Test
    fun `decodes a UTF-8 station unchanged`() {
        val hebrew = "מוטי שטיינמץ"
        assertEquals(hebrew, decodeIcyText(hebrew.encodeToByteArray()))
    }

    @Test
    fun `decodes a windows-1255 station that would otherwise be mojibake`() {
        // 0xF9 0xEC 0xE5 0xED is shin-lamed-vav-final-mem in windows-1255, and not valid UTF-8 -
        // which is exactly what makes the fallback detectable.
        val bytes = byteArrayOf(0xF9.toByte(), 0xEC.toByte(), 0xE5.toByte(), 0xED.toByte())
        assertEquals("שלום", decodeIcyText(bytes))
    }

    @Test
    fun `decodes a mixed windows-1255 line without dropping the ASCII`() {
        // "FM 24 אידיש": ASCII passes through the same table untouched.
        val bytes = byteArrayOf(
            'F'.code.toByte(), 'M'.code.toByte(), ' '.code.toByte(),
            '2'.code.toByte(), '4'.code.toByte(), ' '.code.toByte(),
            0xE0.toByte(), 0xE9.toByte(), 0xE3.toByte(), 0xE9.toByte(), 0xF9.toByte(),
        )
        assertEquals("FM 24 אידיש", decodeIcyText(bytes))
    }

    @Test
    fun `leaves plain ASCII alone`() {
        assertEquals("Kol Play", decodeIcyText("Kol Play".encodeToByteArray()))
    }
}
