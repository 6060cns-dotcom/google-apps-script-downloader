package dev.kdroid.musicradio.platform

import android.content.Context
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * A short, plain-text trail of what the wake listener saw and did, kept in a file so it survives
 * the process being killed. Head-unit and phone makers each block background starts in their own
 * way, and without this the only answer to "why did it not start" is a guess.
 */
internal object WakeLog {
    private const val FILE = "wake.log"
    private const val MAX_LINES = 40

    fun add(context: Context, message: String) {
        runCatching {
            val file = File(context.filesDir, FILE)
            val stamp = SimpleDateFormat("dd/MM HH:mm:ss", Locale.US).format(Date())
            val lines = (file.takeIf { it.isFile }?.readLines().orEmpty() + "$stamp  $message").takeLast(MAX_LINES)
            file.writeText(lines.joinToString("\n"))
        }
    }

    fun read(context: Context): String =
        runCatching { File(context.filesDir, FILE).takeIf { it.isFile }?.readText() }.getOrNull().orEmpty()
}
