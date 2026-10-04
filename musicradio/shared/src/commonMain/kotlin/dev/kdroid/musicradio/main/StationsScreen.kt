package dev.kdroid.musicradio.main

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.QueueMusic
import androidx.compose.material.icons.outlined.Radio
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.kdroid.musicradio.app.AppIntent
import dev.kdroid.musicradio.app.AppState
import dev.kdroid.musicradio.app.ChannelEntry
import dev.kdroid.musicradio.app.filterChannels
import dev.kdroid.musicradio.app.filterStations
import dev.kdroid.musicradio.domain.Station
import dev.kdroid.musicradio.domain.StationCategory
import dev.kdroid.musicradio.domain.isFavorite
import dev.kdroid.musicradio.ui.ChannelCard
import dev.kdroid.musicradio.ui.StationCard
import dev.kdroid.musicradio.ui.label
import musicradio.shared.generated.resources.Res
import musicradio.shared.generated.resources.category_all
import musicradio.shared.generated.resources.favorites_empty
import musicradio.shared.generated.resources.stations_empty
import musicradio.shared.generated.resources.stations_search
import musicradio.shared.generated.resources.view_stations
import musicradio.shared.generated.resources.view_streams
import org.jetbrains.compose.resources.stringResource

/** Shared by the search field and the view toggle so they line up exactly. */
private val CONTROL_HEIGHT = 52.dp

/** Both grids space their cells the same way, and the column maths has to agree with it. */
private val GRID_SPACING = 12.dp

/** How wide a tile wants to be before the grid starts adding columns of its own accord. */
private val TILE_MIN_WIDTH = 170.dp

/**
 * [GridCells.Adaptive] with a floor under the column count.
 *
 * Two cells of [TILE_MIN_WIDTH] plus the spacing between them and the 20dp of screen padding on
 * each side want a 392dp window, and plenty of Android phones are narrower than that: a 360dp
 * screen leaves 320dp, one of the small 480x640 hdpi handsets leaves 280dp. Adaptive answers that
 * with a single column, so one station filled the width and browsing the catalogue became a page
 * of scrolling per tile. With a floor the cells simply get narrower instead - 134dp on the
 * smallest of them, which the square artwork and its two lines of text take without reflowing.
 */
private data class AdaptiveAtLeast(private val minSize: Dp, private val minColumns: Int) : GridCells {
    override fun Density.calculateCrossAxisCellSizes(availableSize: Int, spacing: Int): List<Int> {
        val count = ((availableSize + spacing) / (minSize.roundToPx() + spacing)).coerceAtLeast(minColumns)
        // Same distribution as GridCells.Fixed: the leftover pixels go one each to the first
        // columns rather than being dropped, so the row ends exactly on the edge.
        val forCells = availableSize - spacing * (count - 1)
        val cell = forCells / count
        val remainder = forCells % count
        return List(count) { cell + if (it < remainder) 1 else 0 }
    }
}

@Composable
fun StationsScreen(state: AppState, onIntent: (AppIntent) -> Unit, modifier: Modifier = Modifier) {
    val stations = state.browsable
    // Names live in the resource bundle, so the search box can only be applied once they are resolved.
    val names = stations.associate { it.id to stringResource(it.name) }
    val streamsView = state.data.settings.streamsView

    Column(modifier.fillMaxSize().padding(horizontal = 20.dp)) {
        Row(
            Modifier.fillMaxWidth().padding(top = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // Pill shaped and the same height as the toggle beside it, so the row reads as one
            // control strip rather than a text box parked next to some buttons.
            OutlinedTextField(
                value = state.query,
                onValueChange = { onIntent(AppIntent.SetSearchQuery(it)) },
                modifier = Modifier.weight(1f).height(CONTROL_HEIGHT),
                singleLine = true,
                shape = CircleShape,
                leadingIcon = { Icon(Icons.Outlined.Search, null, Modifier.size(20.dp)) },
                placeholder = { Text(stringResource(Res.string.stations_search), style = MaterialTheme.typography.bodyMedium) },
                textStyle = MaterialTheme.typography.bodyMedium,
            )
            ViewToggle(
                streamsView = streamsView,
                onChange = { onIntent(AppIntent.SetStreamsView(it)) },
                modifier = Modifier.height(CONTROL_HEIGHT),
            )
        }
        CategoryFilter(state, onIntent, Modifier.padding(vertical = 12.dp))
        if (streamsView) {
            ChannelGrid(
                entries = filterChannels(stations, state.query, names),
                state = state,
                onIntent = onIntent,
                emptyText = stringResource(Res.string.stations_empty),
                modifier = Modifier.weight(1f),
            )
        } else {
            StationGrid(
                stations = filterStations(stations, state.query, names),
                state = state,
                onIntent = onIntent,
                emptyText = stringResource(Res.string.stations_empty),
                modifier = Modifier.weight(1f),
            )
        }
    }
}

/**
 * Stations and channels in one grid rather than behind the stations/streams toggle the browse
 * screen uses: you starred these individually, and splitting them across two views hides half of
 * what you saved behind a control you have to discover.
 */
@Composable
fun FavoritesScreen(state: AppState, onIntent: (AppIntent) -> Unit, modifier: Modifier = Modifier) {
    val stations = state.favorites
    val channels = state.favoriteChannels
    val grid = modifier.fillMaxSize().padding(horizontal = 20.dp)
    if (stations.isEmpty() && channels.isEmpty()) {
        EmptyGrid(stringResource(Res.string.favorites_empty), grid)
        return
    }
    LazyVerticalGrid(
        columns = AdaptiveAtLeast(minSize = TILE_MIN_WIDTH, minColumns = 2),
        modifier = grid,
        contentPadding = PaddingValues(top = 4.dp, bottom = 24.dp),
        horizontalArrangement = Arrangement.spacedBy(GRID_SPACING),
        verticalArrangement = Arrangement.spacedBy(GRID_SPACING),
    ) {
        items(stations, key = { it.id }) { station ->
            StationCard(
                station = station,
                playing = state.playback.stationId == station.id && state.playback.status.active,
                favorite = true,
                onClick = { onIntent(AppIntent.SelectStation(station.id)) },
                onToggleFavorite = { onIntent(AppIntent.ToggleFavorite(station.id)) },
            )
        }
        items(channels, key = { it.channel.id }) { entry ->
            ChannelCard(
                station = entry.station,
                channel = entry.channel,
                playing = state.playback.channelId == entry.channel.id && state.playback.status.active,
                favorite = true,
                onClick = { onIntent(AppIntent.SelectChannel(entry.channel.id)) },
                onToggleFavorite = { onIntent(AppIntent.ToggleFavorite(entry.channel.id)) },
            )
        }
    }
}

/** Icon-only so it stays out of the search field's way; the labels live in the descriptions. */
@Composable
private fun ViewToggle(streamsView: Boolean, onChange: (Boolean) -> Unit, modifier: Modifier = Modifier) {
    SingleChoiceSegmentedButtonRow(modifier) {
        SegmentedButton(
            selected = !streamsView,
            onClick = { onChange(false) },
            shape = SegmentedButtonDefaults.itemShape(0, 2),
            icon = {},
        ) {
            Icon(Icons.Outlined.Radio, stringResource(Res.string.view_stations), Modifier.size(20.dp))
        }
        SegmentedButton(
            selected = streamsView,
            onClick = { onChange(true) },
            shape = SegmentedButtonDefaults.itemShape(1, 2),
            icon = {},
        ) {
            Icon(Icons.AutoMirrored.Outlined.QueueMusic, stringResource(Res.string.view_streams), Modifier.size(20.dp))
        }
    }
}

@Composable
private fun CategoryFilter(state: AppState, onIntent: (AppIntent) -> Unit, modifier: Modifier = Modifier) {
    val categories = StationCategory.entries.filter { state.data.settings.showNews || it != StationCategory.News }
    Row(modifier, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        FilterChip(
            selected = state.category == null,
            onClick = { onIntent(AppIntent.SetCategory(null)) },
            label = { Text(stringResource(Res.string.category_all)) },
        )
        categories.forEach { category ->
            FilterChip(
                selected = state.category == category,
                onClick = { onIntent(AppIntent.SetCategory(category.takeIf { it != state.category })) },
                label = { Text(category.label()) },
            )
        }
    }
}

@Composable
private fun StationGrid(
    stations: List<Station>,
    state: AppState,
    onIntent: (AppIntent) -> Unit,
    emptyText: String,
    modifier: Modifier = Modifier,
) {
    if (stations.isEmpty()) {
        EmptyGrid(emptyText, modifier)
        return
    }
    LazyVerticalGrid(
        columns = AdaptiveAtLeast(minSize = TILE_MIN_WIDTH, minColumns = 2),
        modifier = modifier,
        contentPadding = PaddingValues(top = 4.dp, bottom = 24.dp),
        horizontalArrangement = Arrangement.spacedBy(GRID_SPACING),
        verticalArrangement = Arrangement.spacedBy(GRID_SPACING),
    ) {
        items(stations, key = { it.id }) { station ->
            StationCard(
                station = station,
                playing = state.playback.stationId == station.id && state.playback.status.active,
                favorite = state.data.isFavorite(station.id),
                onClick = { onIntent(AppIntent.SelectStation(station.id)) },
                onToggleFavorite = { onIntent(AppIntent.ToggleFavorite(station.id)) },
            )
        }
    }
}

@Composable
private fun ChannelGrid(
    entries: List<ChannelEntry>,
    state: AppState,
    onIntent: (AppIntent) -> Unit,
    emptyText: String,
    modifier: Modifier = Modifier,
) {
    if (entries.isEmpty()) {
        EmptyGrid(emptyText, modifier)
        return
    }
    LazyVerticalGrid(
        columns = AdaptiveAtLeast(minSize = TILE_MIN_WIDTH, minColumns = 2),
        modifier = modifier,
        contentPadding = PaddingValues(top = 4.dp, bottom = 24.dp),
        horizontalArrangement = Arrangement.spacedBy(GRID_SPACING),
        verticalArrangement = Arrangement.spacedBy(GRID_SPACING),
    ) {
        items(entries, key = { it.channel.id }) { entry ->
            ChannelCard(
                station = entry.station,
                channel = entry.channel,
                playing = state.playback.channelId == entry.channel.id && state.playback.status.active,
                favorite = state.data.isFavorite(entry.channel.id),
                onClick = { onIntent(AppIntent.SelectChannel(entry.channel.id)) },
                onToggleFavorite = { onIntent(AppIntent.ToggleFavorite(entry.channel.id)) },
            )
        }
    }
}

@Composable
private fun EmptyGrid(text: String, modifier: Modifier = Modifier) {
    Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(
            text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}
