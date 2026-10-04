package dev.kdroid.musicradio.player

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.getDrawableResourceBytes
import org.jetbrains.compose.resources.getSystemResourceEnvironment
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import java.util.concurrent.ConcurrentHashMap
import kotlin.coroutines.cancellation.CancellationException

@OptIn(ExperimentalResourceApi::class)
actual suspend fun mediaArtworkUri(id: String, artwork: DrawableResource?): String? {
    if (artwork == null || id.isBlank()) return null
    cached[id]?.let { return it }
    return withContext(Dispatchers.IO) {
        try {
            Files.createDirectories(cacheDir)
            val bytes = getDrawableResourceBytes(getSystemResourceEnvironment(), artwork)
            val file = cacheDir.resolve(id.replace(UNSAFE_IN_FILE_NAME, "_") + bytes.imageExtension())
            if (!Files.isRegularFile(file) || Files.size(file) != bytes.size.toLong()) {
                // Written aside and moved into place: the media center may still be reading the
                // previous copy, and a half-written file shows up as a broken cover.
                val partial = Files.createTempFile(cacheDir, "artwork", ".part")
                Files.write(partial, bytes)
                Files.move(partial, file, StandardCopyOption.REPLACE_EXISTING)
            }
            file.toUri().toString().also { cached[id] = it }
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
            null
        }
    }
}

private val cached = ConcurrentHashMap<String, String>()

// Temp rather than a user cache directory: these are a few kilobytes each, they are rewritten from
// the binary whenever they go missing, and nothing outside a running instance wants to read them.
private val cacheDir: Path = Path.of(System.getProperty("java.io.tmpdir"), "music-radio-artwork")
