package dev.kdroid.musicradio.data

import dev.kdroid.musicradio.domain.AppData
import dev.kdroid.musicradio.domain.UiLanguage
import dev.kdroid.musicradio.domain.UserSettings
import dev.kdroid.musicradio.platform.Platform
import dev.kdroid.musicradio.platform.joinPath
import dev.kdroid.musicradio.platform.systemUiLanguage
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject

interface AppStore {
    fun load(): AppData
    fun save(data: AppData)
    fun clear()
}

class MemoryStore(initial: AppData = AppData()) : AppStore {
    private var data: AppData = initial
    override fun load(): AppData = data
    override fun save(data: AppData) {
        this.data = data
    }

    override fun clear() {
        data = AppData()
    }
}

@ContributesBinding(AppScope::class)
@Inject
class FileStore(private val dir: () -> String = { Platform.appDir() }) : AppStore {
    private val file get() = joinPath(dir(), "state.txt")

    override fun load(): AppData {
        val raw = Platform.readText(file)
        if (raw.isNullOrBlank()) return seedData()
        return runCatching { decodeSnapshot(raw) }.getOrElse { seedData() }
    }

    override fun save(data: AppData) {
        Platform.writeText(file, encodeSnapshot(data))
    }

    override fun clear() {
        Platform.delete(file)
    }
}

/** First launch: the interface starts in the OS language, with news channels visible. */
fun seedData(): AppData = AppData(
    settings = UserSettings(uiLanguage = systemUiLanguage(), uiLanguageAuto = true),
)

fun UiLanguage.tag(): String = code
