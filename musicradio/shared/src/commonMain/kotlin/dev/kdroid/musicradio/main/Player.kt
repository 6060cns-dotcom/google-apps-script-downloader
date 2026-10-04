package dev.kdroid.musicradio.main

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.VolumeOff
import androidx.compose.material.icons.automirrored.outlined.VolumeUp
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material.icons.outlined.Pause
import androidx.compose.material.icons.outlined.SkipNext
import androidx.compose.material.icons.outlined.SkipPrevious
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material.icons.outlined.Stop
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import dev.kdroid.musicradio.app.AppIntent
import dev.kdroid.musicradio.app.AppState
import dev.kdroid.musicradio.domain.Station
import dev.kdroid.musicradio.domain.isFavorite
import dev.kdroid.musicradio.player.PlaybackStatus
import dev.kdroid.musicradio.ui.StationArtwork
import musicradio.shared.generated.resources.Res
import musicradio.shared.generated.resources.favorite_add
import musicradio.shared.generated.resources.favorite_remove
import musicradio.shared.generated.resources.player_back
import musicradio.shared.generated.resources.player_buffering
import musicradio.shared.generated.resources.player_live
import musicradio.shared.generated.resources.player_mute
import musicradio.shared.generated.resources.player_next
import musicradio.shared.generated.resources.player_nothing
import musicradio.shared.generated.resources.player_pause
import musicradio.shared.generated.resources.player_paused
import musicradio.shared.generated.resources.player_play
import musicradio.shared.generated.resources.player_previous
import musicradio.shared.generated.resources.player_stop
import musicradio.shared.generated.resources.player_unmute
import org.jetbrains.compose.resources.stringResource

/** The channel's own name, falling back to the station's for a station's flagship stream. */
@Composable
fun channelTitle(state: AppState): String {
    val station = state.currentStation ?: return ""
    val channel = state.currentChannel ?: return stringResource(station.name)
    return channel.title ?: stringResource(station.name)
}

@Composable
private fun statusLabel(status: PlaybackStatus): String = when (status) {
    PlaybackStatus.Buffering -> stringResource(Res.string.player_buffering)
    PlaybackStatus.Playing -> stringResource(Res.string.player_live)
    PlaybackStatus.Paused -> stringResource(Res.string.player_paused)
    PlaybackStatus.Idle, PlaybackStatus.Error -> ""
}

/**
 * The desktop player: a fixed strip across the bottom of the window, always showing what is on
 * and what else the current station carries.
 */
@Composable
fun PlayerBar(state: AppState, onIntent: (AppIntent) -> Unit, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    val station = state.currentStation
    Surface(modifier.fillMaxWidth(), color = colors.surfaceContainer) {
        Column {
            HorizontalDivider(color = colors.outlineVariant)
            Row(
                Modifier.fillMaxWidth().height(88.dp).padding(horizontal = 20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(20.dp),
            ) {
                NowPlayingLabel(state, station, Modifier.weight(1f))
                // The desktop bar is the only player surface on a wide window, so without this
                // there is no way to bookmark what is playing: the grid tile that carries the
                // other star is not on screen once you are inside a station's channels.
                FavoriteButton(state, onIntent)
                ChannelPicker(state, onIntent)
                TransportControls(state, onIntent, big = false)
                VolumeControl(state, onIntent, Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun NowPlayingLabel(state: AppState, station: Station?, modifier: Modifier = Modifier) {
    Row(modifier, verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
        if (station == null) {
            Text(
                stringResource(Res.string.player_nothing),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            return@Row
        }
        StationArtwork(station, Modifier.size(56.dp), RoundedCornerShape(12.dp), state.currentChannel)
        Column(Modifier.widthIn(max = 320.dp)) {
            Text(
                stringResource(station.name),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            // The track pushes the status line out: while something is playing, its name is the
            // more useful of the two, and "Live" is already implied by the running button.
            val song = state.playback.nowPlaying
            Text(
                song.ifBlank { statusLabel(state.playback.status) },
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

/** Compact host: a tappable strip that opens the full player. */
@Composable
fun MiniPlayerBar(state: AppState, onIntent: (AppIntent) -> Unit, modifier: Modifier = Modifier) {
    val station = state.currentStation ?: return
    val colors = MaterialTheme.colorScheme
    Surface(
        modifier.fillMaxWidth().clickable { onIntent(AppIntent.OpenNowPlaying) },
        color = colors.surfaceContainerHigh,
    ) {
        Column {
            HorizontalDivider(color = colors.outlineVariant)
            Row(
                Modifier.fillMaxWidth().height(64.dp).padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                StationArtwork(station, Modifier.size(44.dp), RoundedCornerShape(10.dp), state.currentChannel)
                Column(Modifier.weight(1f)) {
                    Text(
                        stringResource(station.name),
                        style = MaterialTheme.typography.titleSmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        state.playback.nowPlaying.ifBlank { statusLabel(state.playback.status) },
                        style = MaterialTheme.typography.labelSmall,
                        color = colors.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                FavoriteButton(state, onIntent)
                PlayButton(state.playback.status, size = 44.dp) { onIntent(AppIntent.TogglePlay) }
            }
        }
    }
}

/** Toggles the playing station, so it is reachable without going back to the grid. */
@Composable
private fun FavoriteButton(state: AppState, onIntent: (AppIntent) -> Unit, size: Dp = 24.dp) {
    val station = state.currentStation ?: return
    // On a multi-channel station the star follows the channel you are actually on; bookmarking the
    // whole of Kol Chai Music because you liked one of its 83 streams is not what the press means.
    // A single-channel station has nothing finer to point at, so it stays the station itself, and
    // favorites saved before channels were starrable keep matching.
    val target = state.currentChannel?.id?.takeIf { station.multiChannel } ?: station.id
    val favorite = state.data.isFavorite(target)
    IconButton(onClick = { onIntent(AppIntent.ToggleFavorite(target)) }) {
        Icon(
            if (favorite) Icons.Filled.Star else Icons.Outlined.StarBorder,
            stringResource(if (favorite) Res.string.favorite_remove else Res.string.favorite_add),
            modifier = Modifier.size(size),
            tint = if (favorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

/**
 * The compact player, and the only screen that is not inside [MainShell] - there is no bottom bar
 * behind it, so it carries its own way back.
 *
 * Sized off the height rather than the width. The artwork used to be 80% of the width with a square
 * ratio, which on a short screen is taller than everything else put together: the transport row and
 * the volume slider were pushed past the bottom edge with nothing to scroll, so on a phone like the
 * F21 the play button simply was not there. The artwork is now capped by whichever of the two
 * dimensions runs out first, and the controls step down a size once the window is short.
 */
@Composable
fun NowPlayingScreen(state: AppState, onIntent: (AppIntent) -> Unit, modifier: Modifier = Modifier) {
    BoxWithConstraints(modifier.fillMaxSize()) {
        val short = maxHeight < ShortWindowHeight
        val gap = if (short) 8.dp else 20.dp
        val artworkMax = maxWidth * 0.8f
        Column(Modifier.fillMaxSize().padding(horizontal = 24.dp, vertical = if (short) 8.dp else 16.dp)) {
            IconButton(onClick = { onIntent(AppIntent.Back) }, modifier = Modifier.align(Alignment.Start)) {
                Icon(Icons.AutoMirrored.Outlined.ArrowBack, stringResource(Res.string.player_back))
            }
            val station = state.currentStation
            Column(
                Modifier.fillMaxWidth().weight(1f),
                verticalArrangement = Arrangement.spacedBy(gap, Alignment.CenterVertically),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                if (station == null) {
                    Text(stringResource(Res.string.player_nothing), style = MaterialTheme.typography.bodyLarge)
                    return@Column
                }
                // The artwork is the only thing here that can give: weight(fill = false) hands it
                // whatever is left once the rows below have taken their natural height, so the
                // transport row keeps its real size instead of being squeezed - it used to be
                // compressed to nothing on a short window, which is how the play button vanished.
                StationArtwork(
                    station,
                    Modifier.weight(1f, fill = false).widthIn(max = artworkMax).aspectRatio(1f),
                    RoundedCornerShape(28.dp),
                    state.currentChannel,
                )
                Text(
                    stringResource(station.name),
                    style = if (short) MaterialTheme.typography.titleMedium else MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                val song = state.playback.nowPlaying
                if (song.isNotBlank()) {
                    Text(
                        song,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center,
                        maxLines = if (short) 1 else 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                // The status line is the first thing to go: it repeats what the play button already
                // shows, and on a short window that row is worth more than the word "Live".
                if (!short) {
                    Text(
                        statusLabel(state.playback.status),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    FavoriteButton(state, onIntent, size = 28.dp)
                    ChannelPicker(state, onIntent)
                }
                TransportControls(state, onIntent, big = !short)
                VolumeControl(state, onIntent, Modifier.fillMaxWidth(), Alignment.CenterHorizontally)
            }
        }
    }
}

/**
 * Below this the compact player switches to its tighter sizes. A 16:9 phone in portrait clears it
 * comfortably; the ones that do not are the short-and-wide ones, and landscape.
 */
private val ShortWindowHeight = 600.dp

@Composable
fun ChannelPicker(state: AppState, onIntent: (AppIntent) -> Unit, modifier: Modifier = Modifier) {
    val station = state.currentStation ?: return
    if (!station.multiChannel) return
    var expanded by remember { mutableStateOf(false) }
    Box(modifier) {
        OutlinedButton(onClick = { expanded = true }) {
            Text(channelTitle(state), maxLines = 1, overflow = TextOverflow.Ellipsis)
            Icon(Icons.Outlined.ExpandMore, null, Modifier.padding(start = 6.dp).size(18.dp))
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            station.channels.forEach { channel ->
                val title = channel.title ?: stringResource(station.name)
                DropdownMenuItem(
                    text = {
                        Text(
                            title,
                            fontWeight = if (channel.id == state.playback.channelId) FontWeight.Bold else FontWeight.Normal,
                        )
                    },
                    onClick = {
                        expanded = false
                        onIntent(AppIntent.SelectChannel(channel.id))
                    },
                )
            }
        }
    }
}

@Composable
fun TransportControls(state: AppState, onIntent: (AppIntent) -> Unit, big: Boolean, modifier: Modifier = Modifier) {
    // The Row already puts "previous" on the right in Hebrew, but the glyphs do not follow the
    // layout direction on their own. SkipNext and SkipPrevious are exact mirror images, so each
    // button simply takes the other's glyph rather than being flipped through a graphics layer.
    val rtl = LocalLayoutDirection.current == LayoutDirection.Rtl
    val previousIcon = if (rtl) Icons.Outlined.SkipNext else Icons.Outlined.SkipPrevious
    val nextIcon = if (rtl) Icons.Outlined.SkipPrevious else Icons.Outlined.SkipNext
    Row(
        modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(if (big) 20.dp else 8.dp),
    ) {
        IconButton(onClick = { onIntent(AppIntent.PreviousStation) }) {
            Icon(previousIcon, stringResource(Res.string.player_previous))
        }
        PlayButton(state.playback.status, size = if (big) 72.dp else 52.dp) { onIntent(AppIntent.TogglePlay) }
        IconButton(onClick = { onIntent(AppIntent.NextStation) }) {
            Icon(nextIcon, stringResource(Res.string.player_next))
        }
        if (state.playback.status != PlaybackStatus.Idle) {
            IconButton(onClick = { onIntent(AppIntent.Stop) }) {
                Icon(Icons.Outlined.Stop, stringResource(Res.string.player_stop))
            }
        }
    }
}

@Composable
private fun PlayButton(status: PlaybackStatus, size: androidx.compose.ui.unit.Dp, onClick: () -> Unit) {
    FilledIconButton(onClick = onClick, modifier = Modifier.size(size), shape = CircleShape) {
        when (status) {
            // The spinner replaces the glyph rather than sitting next to it: the button keeps its
            // hit target, and a stream that takes four seconds to open still looks like it is working.
            PlaybackStatus.Buffering -> CircularProgressIndicator(
                Modifier.size(size / 2.4f),
                strokeWidth = 2.dp,
                color = MaterialTheme.colorScheme.onPrimary,
            )

            PlaybackStatus.Playing -> Icon(Icons.Outlined.Pause, stringResource(Res.string.player_pause), Modifier.size(size / 2.2f))

            else -> Icon(Icons.Filled.PlayArrow, stringResource(Res.string.player_play), Modifier.size(size / 2.2f))
        }
    }
}

/**
 * [alignment] is trailing-edge for the desktop bar, where the row is the last thing in a wide strip
 * and belongs against the window edge. The compact player passes centre: there the row is given the
 * full width under a centred column, so hugging the edge left the slider visibly off-axis from
 * everything above it.
 */
@Composable
fun VolumeControl(
    state: AppState,
    onIntent: (AppIntent) -> Unit,
    modifier: Modifier = Modifier,
    alignment: Alignment.Horizontal = Alignment.End,
) {
    val settings = state.data.settings
    Row(
        modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp, alignment),
    ) {
        IconButton(onClick = { onIntent(AppIntent.ToggleMute) }) {
            Icon(
                if (settings.muted) Icons.AutoMirrored.Outlined.VolumeOff else Icons.AutoMirrored.Outlined.VolumeUp,
                stringResource(if (settings.muted) Res.string.player_unmute else Res.string.player_mute),
            )
        }
        Slider(
            value = settings.volume.toFloat(),
            onValueChange = { onIntent(AppIntent.SetVolume(it.toInt())) },
            valueRange = 0f..100f,
            modifier = Modifier.widthIn(max = 220.dp).width(180.dp),
        )
    }
}
