package dev.kdroid.musicradio.app

import dev.kdroid.musicradio.domain.AccentColor
import dev.kdroid.musicradio.domain.StartupPlay
import dev.kdroid.musicradio.domain.StationCategory
import dev.kdroid.musicradio.domain.ThemeMode
import dev.kdroid.musicradio.domain.UiLanguage

sealed interface AppIntent {
    data class Navigate(val destination: AppKey) : AppIntent
    data object OpenNowPlaying : AppIntent
    data object Back : AppIntent
    data object Quit : AppIntent

    /** Plays the station's flagship channel, or resumes it when it is already the current one. */
    data class SelectStation(val stationId: String) : AppIntent
    data class SelectChannel(val channelId: String) : AppIntent
    data object TogglePlay : AppIntent
    data object Stop : AppIntent
    data object NextStation : AppIntent
    data object PreviousStation : AppIntent
    data class SetVolume(val percent: Int) : AppIntent
    data object ToggleMute : AppIntent

    /** [id] is a station id or a channel id; see `AppData.toggleFavorite`. */
    data class ToggleFavorite(val id: String) : AppIntent
    data class SetSearchQuery(val query: String) : AppIntent

    /** `null` clears the filter and shows every category. */
    data class SetCategory(val category: StationCategory?) : AppIntent

    data class SetTheme(val mode: ThemeMode) : AppIntent
    data class SetAccent(val accent: AccentColor) : AppIntent

    /** `null` follows the OS language. */
    data class SetUiLanguage(val language: UiLanguage?) : AppIntent
    data class SetShowNews(val on: Boolean) : AppIntent
    data class SetStreamsView(val on: Boolean) : AppIntent
    data class SetStartupPlay(val mode: StartupPlay) : AppIntent

    /** Picks the channel played on launch, and switches [StartupPlay.Chosen] on with it. */
    data class SetStartupChannel(val channelId: String) : AppIntent
    data class SetPlayOnWake(val on: Boolean) : AppIntent
    data class SetWakeChannel(val channelId: String) : AppIntent

    data class OpenUrl(val url: String) : AppIntent

    data object ResetApp : AppIntent
    data object ConfirmDialog : AppIntent
    data object DismissDialog : AppIntent
    data object DismissMessage : AppIntent
}
