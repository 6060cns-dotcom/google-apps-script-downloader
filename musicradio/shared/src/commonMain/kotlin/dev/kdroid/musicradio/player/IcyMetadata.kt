package dev.kdroid.musicradio.player

import io.ktor.client.HttpClient
import io.ktor.client.request.header
import io.ktor.client.request.prepareGet
import io.ktor.client.statement.bodyAsChannel
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.readAvailable
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive

/**
 * Shoutcast/Icecast interleave the current track into the audio stream itself: ask for it with
 * `Icy-MetaData: 1`, and every `icy-metaint` bytes the server injects a short text block.
 *
 * The audio backend swallows this (Rodio reports it to a callback Compose Media Player leaves
 * empty), so the title is read over a second, short-lived connection: open, skip one interval,
 * take the block, hang up. That costs one interval of audio per poll — around 16 KB — instead of
 * holding a duplicate stream open for as long as the user listens.
 */
class IcyMetadata(private val http: HttpClient) {

    /** The current track, or `null` when the stream carries no usable metadata. */
    suspend fun fetchTitle(streamUrl: String): String? = runCatching {
        http.prepareGet(streamUrl) {
            header("Icy-MetaData", "1")
            header("User-Agent", USER_AGENT)
        }.execute { response ->
            val interval = response.headers["icy-metaint"]?.toIntOrNull()
                ?: return@execute null
            if (interval <= 0 || interval > MAX_INTERVAL) return@execute null
            readTitle(response.bodyAsChannel(), interval)
        }
    }.getOrNull()

    private suspend fun readTitle(channel: ByteReadChannel, interval: Int): String? {
        val scratch = ByteArray(SCRATCH_SIZE)
        // Servers often send an empty block first; give them a few intervals before giving up.
        repeat(MAX_BLOCKS) {
            if (!channel.skipExactly(interval, scratch)) return null
            val lengthByte = ByteArray(1)
            if (!channel.readExactly(lengthByte, 1)) return null
            val length = (lengthByte[0].toInt() and 0xFF) * BLOCK_UNIT
            if (length == 0) return@repeat
            val block = ByteArray(length)
            if (!channel.readExactly(block, length)) return null
            val title = parseStreamTitle(decodeIcyText(block))
            if (title != null) return title
        }
        return null
    }

    private companion object {
        const val USER_AGENT = "MusicRadio"
        const val BLOCK_UNIT = 16
        const val MAX_BLOCKS = 4
        const val SCRATCH_SIZE = 8192

        /** Guards against a bogus header turning into a multi-megabyte read. */
        const val MAX_INTERVAL = 1 shl 20
    }
}

// Both loops check for cancellation on every pass: the poll is torn down the moment the user
// switches channel, and a stalled server must not keep it alive waiting on a read that never fills.
private suspend fun ByteReadChannel.readExactly(dst: ByteArray, length: Int): Boolean {
    var read = 0
    while (read < length) {
        currentCoroutineContext().ensureActive()
        val n = readAvailable(dst, read, length - read)
        if (n <= 0) return false
        read += n
    }
    return true
}

private suspend fun ByteReadChannel.skipExactly(count: Int, scratch: ByteArray): Boolean {
    var left = count
    while (left > 0) {
        currentCoroutineContext().ensureActive()
        val n = readAvailable(scratch, 0, minOf(left, scratch.size))
        if (n <= 0) return false
        left -= n
    }
    return true
}

// `[\s\S]` rather than `.` + DOT_MATCHES_ALL: the latter is JVM-only, and ICY titles can
// legally span a line break even if they almost never do.
private val StreamTitlePattern = Regex("StreamTitle='([\\s\\S]*?)';")

/**
 * Placeholders some stations park in the field forever. Showing "line" or a CDN advert under the
 * station name is worse than showing nothing.
 */
private val Placeholders = setOf("line", "unknown", "-", "n/a")

internal fun parseStreamTitle(text: String): String? {
    val raw = StreamTitlePattern.find(text)?.groupValues?.get(1)?.trim().orEmpty()
    if (raw.isEmpty()) return null
    if (raw.lowercase() in Placeholders) return null
    if (raw.contains("Powered By", ignoreCase = true)) return null
    return raw
}

/**
 * ICY has no charset field. Most stations send UTF-8, but some Hebrew ones still send
 * windows-1255, so a decode that produced replacement characters is retried against that table.
 */
internal fun decodeIcyText(bytes: ByteArray): String {
    val utf8 = bytes.decodeToString()
    if (!utf8.contains(REPLACEMENT_CHAR)) return utf8
    return decodeWindows1255(bytes)
}

private const val REPLACEMENT_CHAR = '�'

/**
 * The subset of windows-1255 that matters here: ASCII, the Hebrew block, and the handful of
 * punctuation marks stations actually use. Anything else becomes a space rather than mojibake.
 */
private fun decodeWindows1255(bytes: ByteArray): String = buildString(bytes.size) {
    for (byte in bytes) {
        val code = byte.toInt() and 0xFF
        val char = when {
            code < 0x80 -> code.toChar()

            // 0xE0..0xFA is aleph through tav, contiguous in both this table and Unicode.
            code in 0xE0..0xFA -> (code - 0xE0 + 0x05D0).toChar()

            // Niqqud and cantillation, also contiguous.
            code in 0xC0..0xD2 -> (code - 0xC0 + 0x05B0).toChar()

            code == 0x93 || code == 0x94 -> '"'

            code == 0x91 || code == 0x92 -> '\''

            code == 0x96 || code == 0x97 -> '-'

            code == 0xA0 -> ' '

            else -> ' '
        }
        append(char)
    }
}
