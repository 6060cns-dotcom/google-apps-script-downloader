package dev.kdroid.musicradio.app

import androidx.compose.runtime.Immutable
import dev.kdroid.musicradio.domain.AppData
import dev.kdroid.musicradio.domain.Station
import dev.kdroid.musicradio.domain.StationCategory
import dev.kdroid.musicradio.domain.Stations
import dev.kdroid.musicradio.domain.visibleStations
import dev.kdroid.musicradio.player.PlaybackStatus

@Immutable
sealed interface AppDialog {
    data object Hidden : AppDialog
    data object ConfirmReset : AppDialog
}

@Immutable
data class PlaybackState(
    val status: PlaybackStatus = PlaybackStatus.Idle,
    val stationId: String = "",
    val channelId: String = "",
    /** The track the stream reports, empty when it reports none. */
    val nowPlaying: String = "",
) {
    val hasStation: Boolean get() = stationId.isNotEmpty()
}

@Immutable
data class AppState(
    val data: AppData = AppData(),
    val playback: PlaybackState = PlaybackState(),
    val query: String = "",
    val category: StationCategory? = null,
    val dialog: AppDialog = AppDialog.Hidden,
    val message: AppMessage? = null,
) {
    /** Everything the station list may show, before the search box narrows it further. */
    val browsable: List<Station>
        get() = visibleStations(data.settings.showNews).filter { category == null || it.category == category }

    val favorites: List<Station>
        get() = visibleStations(data.settings.showNews).filter { it.id in data.favorites }

    /** Starred channels, in catalog order, so the favorites grid can list them under the stations. */
    val favoriteChannels: List<ChannelEntry>
        get() = visibleStations(data.settings.showNews)
            .flatMap { station -> station.channels.map { ChannelEntry(station, it) } }
            .filter { it.channel.id in data.favorites }

    val currentStation: Station? get() = Stations.of(playback.stationId)

    val currentChannel get() = Stations.channel(playback.channelId)
}
