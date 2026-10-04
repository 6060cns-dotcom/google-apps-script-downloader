package dev.kdroid.musicradio.app

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.snapshots.Snapshot
import androidx.lifecycle.ViewModel
import androidx.navigation3.runtime.NavBackStack
import dev.kdroid.musicradio.data.AppStore
import dev.kdroid.musicradio.data.seedData
import dev.kdroid.musicradio.domain.Channel
import dev.kdroid.musicradio.domain.Station
import dev.kdroid.musicradio.domain.Stations
import dev.kdroid.musicradio.domain.StartupPlay
import dev.kdroid.musicradio.domain.UserSettings
import dev.kdroid.musicradio.domain.launchChannelId
import dev.kdroid.musicradio.domain.toggleFavorite
import dev.kdroid.musicradio.platform.Platform
import dev.kdroid.musicradio.platform.localizedString
import dev.kdroid.musicradio.platform.syncWakeListener
import dev.kdroid.musicradio.platform.systemUiLanguage
import dev.kdroid.musicradio.player.IcyMetadata
import dev.kdroid.musicradio.player.MediaCommand
import dev.kdroid.musicradio.player.MediaControls
import dev.kdroid.musicradio.player.NoMediaControls
import dev.kdroid.musicradio.player.NowPlaying
import dev.kdroid.musicradio.player.PlaybackStatus
import dev.kdroid.musicradio.player.RadioPlayer
import dev.kdroid.musicradio.player.mediaArtworkUri
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import io.github.santimattius.structured.annotations.StructuredScope
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/** Tracks run minutes, and each poll costs a real (if small) read off the stream. */
private const val METADATA_POLL_MS = 20_000L

@AssistedInject
class AppViewModel(
    private val store: AppStore,
    private val player: RadioPlayer,
    private val mediaControls: MediaControls = NoMediaControls,
    /**
     * Deliberately non-null and without a default. Metro keys bindings on the full Kotlin type,
     * nullability included, so `IcyMetadata?` matches no binding - and rather than failing the
     * build it quietly took the default, leaving [watchNowPlaying] with nothing to poll and the
     * track title permanently blank on every platform. Written this way, the same mistake is a
     * compile error instead of a feature that is simply never there.
     */
    private val icyMetadata: IcyMetadata,
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default,
    @Assisted private val onQuit: () -> Unit = {},
) : ViewModel() {

    @AssistedFactory
    fun interface Factory {
        fun create(onQuit: () -> Unit): AppViewModel
    }

    private val job = SupervisorJob()

    @StructuredScope
    private val scope = CoroutineScope(job + dispatcher)

    private val _state = MutableStateFlow(restore())
    val state: StateFlow<AppState> = _state.asStateFlow()

    val backStack: NavBackStack<AppKey> = NavBackStack(AppKey.Stations)

    private var saveJob: Job? = null

    init {
        applyVolume(_state.value.data.settings)
        scope.launch {
            player.status.collect { status -> onPlaybackStatus(status) }
        }
        bindMediaControls()
        watchNowPlaying()
        syncWakeListener(_state.value.data.settings.playOnWake)
        startOnLaunch()
    }

    /** Plays what the startup setting asks for, going through [start] so the UI shows it too. */
    private fun startOnLaunch() {
        val channelId = _state.value.data.launchChannelId() ?: return
        val station = Stations.stationOfChannel(channelId) ?: return
        start(station, channelId)
    }

    override fun onCleared() {
        mediaControls.release()
        player.release()
        job.cancel()
        super.onCleared()
    }

    /**
     * Hardware media keys and the OS media flyout drive the app through the same intents the UI
     * uses, so there is one code path for "pause", not two.
     */
    private fun bindMediaControls() {
        if (mediaControls.available) {
            mediaControls.attach { command ->
                when (command) {
                    MediaCommand.Toggle -> onIntent(AppIntent.TogglePlay)
                    MediaCommand.Play -> if (!_state.value.playback.status.active) onIntent(AppIntent.TogglePlay)
                    MediaCommand.Pause -> if (_state.value.playback.status.active) onIntent(AppIntent.TogglePlay)
                    MediaCommand.Next -> onIntent(AppIntent.NextStation)
                    MediaCommand.Previous -> onIntent(AppIntent.PreviousStation)
                    MediaCommand.Stop -> onIntent(AppIntent.Stop)
                }
            }
        }
        // Runs whether or not MediaControls exists. Android has no implementation of it, yet its
        // player still needs these names for the media session behind the notification and the
        // lock screen - gating this on `available` left that session permanently blank.
        scope.launch {
            _state
                .map { it.playback }
                .distinctUntilChanged()
                .collect { playback -> publishNowPlaying(playback) }
        }
    }

    private suspend fun publishNowPlaying(playback: PlaybackState) {
        val station = Stations.of(playback.stationId)
        val language = _state.value.data.settings.uiLanguage.code
        val stationName = station?.let { runCatching { localizedString(language, it.name) }.getOrNull() }.orEmpty()
        val channel = Stations.channel(playback.channelId)
        val channelLabel = channel?.title ?: stationName
        // Most stations send "Artist - Track"; when they don't, the whole string is the title.
        val song = playback.nowPlaying
        val separator = song.indexOf(" - ")
        val songArtist = if (separator > 0) song.take(separator).trim() else ""
        val songTitle = if (separator > 0) song.drop(separator + 3).trim() else song.trim()
        // A channel only carries its own cover when the broadcaster publishes one;
        // otherwise the station logo stands in, exactly as in the station list.
        val artworkUri = mediaArtworkUri(
            id = playback.channelId.ifEmpty { playback.stationId },
            artwork = channel?.artwork ?: station?.artwork,
        )
        mediaControls.update(
            NowPlaying(
                station = stationName,
                title = songTitle.ifBlank { channelLabel },
                artist = songArtist.ifBlank { if (song.isBlank()) channelLabel else stationName },
                artworkUri = artworkUri,
            ),
            playback.status,
        )
        // Android reads this off the player's own session rather than through MediaControls.
        player.setNowPlaying(station = channelLabel, song = song, artworkUri = artworkUri)
    }

    /**
     * Reads the track title off the stream while it plays. Keyed on the channel and whether audio
     * is running — not on the state as a whole, or writing the title back would restart the poll
     * that produced it.
     */
    private fun watchNowPlaying() {
        scope.launch {
            _state
                .map { it.playback.channelId to it.playback.status.active }
                .distinctUntilChanged()
                .collectLatest { (channelId, active) ->
                    if (!active || channelId.isEmpty()) {
                        setNowPlaying("")
                        return@collectLatest
                    }
                    val url = Stations.channel(channelId)?.streamUrl ?: return@collectLatest
                    while (isActive) {
                        setNowPlaying(icyMetadata.fetchTitle(url).orEmpty())
                        delay(METADATA_POLL_MS)
                    }
                }
        }
    }

    private fun setNowPlaying(title: String) {
        mutate { s ->
            if (s.playback.nowPlaying == title) s else s.copy(playback = s.playback.copy(nowPlaying = title))
        }
    }

    fun onIntent(intent: AppIntent) {
        when (intent) {
            AppIntent.Quit -> {
                player.stop()
                onQuit()
            }

            is AppIntent.SelectStation -> selectStation(intent.stationId)

            is AppIntent.SelectChannel -> selectChannel(intent.channelId)

            AppIntent.TogglePlay -> togglePlay()

            AppIntent.Stop -> {
                player.stop()
                mutate { it.copy(playback = it.playback.copy(status = PlaybackStatus.Idle)) }
            }

            AppIntent.NextStation -> step(1)

            AppIntent.PreviousStation -> step(-1)

            is AppIntent.SetVolume -> setVolume(intent.percent)

            AppIntent.ToggleMute -> toggleMute()

            is AppIntent.OpenUrl -> Platform.openUrl(intent.url)

            AppIntent.ConfirmDialog -> confirmDialog()

            else -> {
                applyNavigation(intent)
                mutate { reduce(it, intent) }
                afterReduce(intent)
            }
        }
    }

    private fun restore(): AppState {
        var data = store.load()
        // Re-resolve on every launch: the OS language can change between runs.
        if (data.settings.uiLanguageAuto) {
            data = data.copy(settings = data.settings.copy(uiLanguage = systemUiLanguage()))
        }
        val channel = Stations.channel(data.lastChannel)
        val station = channel?.let { Stations.stationOfChannel(it.id) }
        if (channel == null && data.lastChannel.isNotEmpty()) {
            // The catalog moved on: forget a channel that no longer exists.
            data = data.copy(lastChannel = "")
        }
        return AppState(
            data = data,
            playback = PlaybackState(
                status = PlaybackStatus.Idle,
                stationId = station?.id.orEmpty(),
                channelId = channel?.id.orEmpty(),
            ),
        )
    }

    private fun reduce(s: AppState, intent: AppIntent): AppState = when (intent) {
        is AppIntent.Navigate -> s.copy(message = null)

        AppIntent.OpenNowPlaying, AppIntent.Back -> s

        is AppIntent.ToggleFavorite -> s.copy(data = s.data.toggleFavorite(intent.id))

        is AppIntent.SetSearchQuery -> s.copy(query = intent.query)

        is AppIntent.SetCategory -> s.copy(category = intent.category)

        is AppIntent.SetTheme -> s.updateSettings { it.copy(theme = intent.mode) }

        is AppIntent.SetAccent -> s.updateSettings { it.copy(accent = intent.accent) }

        is AppIntent.SetUiLanguage -> s.updateSettings {
            it.copy(uiLanguage = intent.language ?: systemUiLanguage(), uiLanguageAuto = intent.language == null)
        }

        is AppIntent.SetShowNews -> s.updateSettings { it.copy(showNews = intent.on) }

        is AppIntent.SetStreamsView -> s.updateSettings { it.copy(streamsView = intent.on) }

        is AppIntent.SetStartupPlay -> s.updateSettings { it.copy(startupPlay = intent.mode) }

        // Choosing a channel is itself the decision to open on it, so the mode follows the pick.
        is AppIntent.SetStartupChannel -> s.updateSettings {
            it.copy(startupChannelId = intent.channelId, startupPlay = StartupPlay.Chosen)
        }

        is AppIntent.SetPlayOnWake -> s.updateSettings { it.copy(playOnWake = intent.on) }

        is AppIntent.SetWakeChannel -> s.updateSettings { it.copy(wakeChannelId = intent.channelId, playOnWake = true) }

        AppIntent.ResetApp -> s.copy(dialog = AppDialog.ConfirmReset)

        AppIntent.DismissDialog -> s.copy(dialog = AppDialog.Hidden)

        AppIntent.DismissMessage -> s.copy(message = null)

        AppIntent.Quit,
        is AppIntent.SelectStation,
        is AppIntent.SelectChannel,
        AppIntent.TogglePlay,
        AppIntent.Stop,
        AppIntent.NextStation,
        AppIntent.PreviousStation,
        is AppIntent.SetVolume,
        AppIntent.ToggleMute,
        is AppIntent.OpenUrl,
        AppIntent.ConfirmDialog,
        -> s
    }

    private fun afterReduce(intent: AppIntent) {
        when (intent) {
            is AppIntent.SetSearchQuery, is AppIntent.SetCategory,
            AppIntent.DismissMessage, AppIntent.DismissDialog,
            is AppIntent.Navigate, AppIntent.OpenNowPlaying, AppIntent.Back,
            -> Unit

            // Hiding the news category can pull the playing station out from under the user.
            is AppIntent.SetShowNews -> {
                if (!intent.on) stopIfHidden()
                persist()
            }

            // The wake listener is a platform service that reads the saved snapshot on its own, so
            // the file has to be written before the service is told anything changed.
            is AppIntent.SetPlayOnWake, is AppIntent.SetWakeChannel -> {
                persist(now = true)
                syncWakeListener(_state.value.data.settings.playOnWake)
            }

            else -> persist()
        }
    }

    private fun applyNavigation(intent: AppIntent) {
        when (intent) {
            is AppIntent.Navigate -> setMain(intent.destination)

            AppIntent.OpenNowPlaying -> if (backStack.lastOrNull() != AppKey.NowPlaying) backStack.add(AppKey.NowPlaying)

            // removeAt, not removeLast(): Kotlin resolves removeLast() to the java.util.List
            // method added in Java 21, which only exists from API 35. On Android 14 and below the
            // call throws NoSuchMethodError, so every press of Back crashed the app there.
            AppIntent.Back -> if (backStack.size > 1) backStack.removeAt(backStack.lastIndex)

            else -> Unit
        }
    }

    /**
     * Replaces the whole back stack with one destination - a rail or bottom-bar tap.
     *
     * Two things it deliberately does not do. It does not replace a destination with itself: a
     * second tap on the tab you are already on used to tear the screen down and build a new one,
     * losing its scroll position for nothing. And it does not clear and then add, which left the
     * stack momentarily empty between the two writes - RootScreen reads `backStack.last()`, so
     * that window has no valid answer. `withMutableSnapshot` makes the pair land as one change,
     * which also means NavDisplay sees one target change per tap rather than two, and starts one
     * transition instead of two overlapping ones.
     */
    private fun setMain(key: AppKey) {
        if (backStack.size == 1 && backStack[0] == key) return
        Snapshot.withMutableSnapshot {
            backStack.clear()
            backStack.add(key)
        }
    }

    private fun selectStation(stationId: String) {
        val station = Stations.of(stationId) ?: return
        val current = _state.value.playback
        // Tapping the station you are already on is a play/pause toggle, not a restart.
        if (current.stationId == stationId && current.status.active) {
            player.pause()
            return
        }
        val channel = station.channels.firstOrNull { it.id == current.channelId }
            ?: station.channels.first()
        start(station, channel.id)
    }

    private fun selectChannel(channelId: String) {
        val station = Stations.stationOfChannel(channelId) ?: return
        start(station, channelId)
    }

    private fun start(station: Station, channelId: String) {
        val channel = station.channels.firstOrNull { it.id == channelId } ?: return
        mutate {
            it.copy(
                playback = PlaybackState(PlaybackStatus.Buffering, station.id, channel.id),
                data = it.data.copy(lastChannel = channel.id),
                message = null,
            )
        }
        player.play(channel.streamUrl)
        persist()
    }

    private fun togglePlay() {
        val playback = _state.value.playback
        val channel = Stations.channel(playback.channelId)
        when {
            channel == null -> {
                // Nothing has been picked yet: start at the top of the list the user is looking at.
                val first = _state.value.browsable.firstOrNull() ?: return
                start(first, first.channels.first().id)
            }

            playback.status.active -> player.pause()

            else -> {
                // The URL again, never resume(): a live stream has no position to come back to.
                // resume() hands back whatever the backend still holds, which is as old as the
                // pause - so someone who paused through an ad break and came back a few minutes
                // later carried on in the middle of the ads, and reached the live broadcast only
                // by stopping the station and starting it again. Reopening is what play means here.
                player.play(channel.streamUrl)
                mutate { it.copy(playback = it.playback.copy(status = PlaybackStatus.Buffering), message = null) }
            }
        }
    }

    private fun step(delta: Int) {
        val list = _state.value.browsable
        if (list.isEmpty()) return
        val index = list.indexOfFirst { it.id == _state.value.playback.stationId }
        val next = if (index < 0) 0 else ((index + delta) % list.size + list.size) % list.size
        val station = list[next]
        start(station, station.channels.first().id)
    }

    private fun setVolume(percent: Int) {
        val clamped = percent.coerceIn(0, 100)
        mutate { it.updateSettings { s -> s.copy(volume = clamped, muted = clamped == 0 && s.muted) } }
        applyVolume(_state.value.data.settings)
        persist()
    }

    private fun toggleMute() {
        mutate { it.updateSettings { s -> s.copy(muted = !s.muted) } }
        applyVolume(_state.value.data.settings)
        persist()
    }

    private fun applyVolume(settings: UserSettings) {
        val level = if (settings.muted) 0 else settings.volume
        player.setVolume(level)
        mediaControls.setVolume(level)
    }

    /** A station that just left the visible list must not keep playing behind a hidden row. */
    private fun stopIfHidden() {
        val id = _state.value.playback.stationId
        if (id.isEmpty()) return
        val stillVisible = _state.value.browsable.any { it.id == id }
        if (stillVisible) return
        player.stop()
        mutate { it.copy(playback = PlaybackState()) }
    }

    private fun onPlaybackStatus(status: PlaybackStatus) {
        mutate { current ->
            if (!current.playback.hasStation && status != PlaybackStatus.Error) {
                current
            } else {
                current.copy(
                    playback = current.playback.copy(status = status),
                    message = if (status == PlaybackStatus.Error) AppMessage.StreamFailed else current.message,
                )
            }
        }
    }

    private fun confirmDialog() {
        if (_state.value.dialog != AppDialog.ConfirmReset) {
            mutate { it.copy(dialog = AppDialog.Hidden) }
            return
        }
        player.stop()
        store.clear()
        val fresh = seedData()
        mutate {
            AppState(data = fresh, message = AppMessage.ResetDone)
        }
        applyVolume(fresh.settings)
        persist(now = true)
        setMain(AppKey.Stations)
    }

    private fun persist(now: Boolean = false) {
        saveJob?.cancel()
        val snapshot = _state.value.data
        if (now) {
            store.save(snapshot)
            return
        }
        saveJob = scope.launch {
            delay(200)
            store.save(snapshot)
        }
    }

    private fun mutate(block: (AppState) -> AppState) = _state.update(block)

    private fun AppState.updateSettings(block: (UserSettings) -> UserSettings): AppState =
        copy(data = data.copy(settings = block(data.settings)))
}

/** The station list after the search box, resolved against names the caller has already localised. */
fun filterStations(stations: List<Station>, query: String, names: Map<String, String>): List<Station> {
    val needle = query.trim()
    if (needle.isEmpty()) return stations
    return stations.filter { station ->
        names[station.id].orEmpty().contains(needle, ignoreCase = true) ||
            station.channels.any { it.title?.contains(needle, ignoreCase = true) == true }
    }
}

/** One row of the flat view: a single stream, carrying the station it belongs to. */
@Immutable
data class ChannelEntry(val station: Station, val channel: Channel)

/** Every stream on its own, rather than one entry per station. */
fun filterChannels(stations: List<Station>, query: String, names: Map<String, String>): List<ChannelEntry> {
    val entries = stations.flatMap { station -> station.channels.map { ChannelEntry(station, it) } }
    val needle = query.trim()
    if (needle.isEmpty()) return entries
    return entries.filter { (station, channel) ->
        val stationName = names[station.id].orEmpty()
        stationName.contains(needle, ignoreCase = true) ||
            channel.title?.contains(needle, ignoreCase = true) == true
    }
}
