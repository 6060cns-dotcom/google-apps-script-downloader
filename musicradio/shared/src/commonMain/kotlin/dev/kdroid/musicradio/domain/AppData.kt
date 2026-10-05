package dev.kdroid.musicradio.domain

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

enum class ThemeMode { System, Light, Dark }

/** What the app does the moment it opens. */
enum class StartupPlay {
    /** Nothing starts by itself. */
    Off,

    /** Carries on with whatever was playing when the app last closed. */
    LastPlayed,

    /** Always opens on [UserSettings.startupChannelId]. */
    Chosen,
}

enum class UiLanguage(val code: String, val label: String, val rtl: Boolean) {
    English("en", "English", false),
    Hebrew("he", "עברית", true),
    French("fr", "Français", false),
    ;

    companion object {
        /**
         * ISO 639 renamed three languages in 1989 and Java kept the old codes for compatibility.
         * Android never moved: `Locale.getLanguage()` there answers "iw" for Hebrew no matter how
         * the locale was built, so the OS language has to come through this map or every Hebrew
         * device reads as English. Desktop is on JDK 17 or later, which returns the modern codes.
         */
        private val LEGACY_CODES = mapOf("iw" to "he", "ji" to "yi", "in" to "id")

        fun fromCode(raw: String): UiLanguage {
            val code = raw.substringBefore('-').substringBefore('_').lowercase()
            val current = LEGACY_CODES[code] ?: code
            return entries.firstOrNull { it.code == current } ?: English
        }
    }
}

/** Seed colours for MaterialKolor. The whole scheme is derived from the one the user picks. */
enum class AccentColor(val seed: Color) {
    Indigo(Color(0xFF4C5BD4)),
    Teal(Color(0xFF00786B)),
    Amber(Color(0xFFB4690E)),
    Rose(Color(0xFFB3245C)),
    Violet(Color(0xFF7A4FCF)),
    Slate(Color(0xFF4F5B62)),
}

@Immutable
data class UserSettings(
    val theme: ThemeMode = ThemeMode.System,
    val accent: AccentColor = AccentColor.Indigo,
    val uiLanguage: UiLanguage = UiLanguage.English,
    /** `true` while the interface follows the OS language rather than an explicit pick. */
    val uiLanguageAuto: Boolean = true,
    val showNews: Boolean = true,
    /** `true` lays out every individual stream as its own card instead of grouping by station. */
    val streamsView: Boolean = false,
    val startupPlay: StartupPlay = StartupPlay.Off,
    /** Channel id played on launch when [startupPlay] is [StartupPlay.Chosen]. */
    val startupChannelId: String = "",
    /** Android only: start [wakeChannelId] whenever the device wakes from sleep. */
    val playOnWake: Boolean = false,
    val wakeChannelId: String = "",
    val volume: Int = 70,
    val muted: Boolean = false,
)

@Immutable
data class AppData(
    val settings: UserSettings = UserSettings(),
    val favorites: Set<String> = emptySet(),
    /** Channel id of whatever was playing when the app last closed. */
    val lastChannel: String = "",
)

/**
 * [id] is a station id or a channel id - the set holds both, and they cannot collide because a
 * channel id is always `station/channel` while a station id never carries a slash. Storing them
 * together is what lets an existing snapshot keep working: station favorites saved before channels
 * were starrable read back unchanged.
 */
fun AppData.isFavorite(id: String): Boolean = id in favorites

fun AppData.toggleFavorite(id: String): AppData = copy(favorites = if (id in favorites) favorites - id else favorites + id)

/**
 * The channel the app should start on launch, or `null` when nothing should start. A stored id the
 * catalog no longer carries reads as "no choice" rather than as an error.
 */
fun AppData.launchChannelId(): String? = when (settings.startupPlay) {
    StartupPlay.Off -> null
    StartupPlay.LastPlayed -> lastChannel
    StartupPlay.Chosen -> settings.startupChannelId
}?.takeIf { Stations.channel(it) != null }

/** The channel to start when the device wakes, or `null` when the feature is off or unset. */
fun AppData.wakeChannelIdOrNull(): String? =
    settings.wakeChannelId.takeIf { settings.playOnWake && Stations.channel(it) != null }
