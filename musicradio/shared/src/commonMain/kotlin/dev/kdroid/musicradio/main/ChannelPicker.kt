package dev.kdroid.musicradio.main

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import dev.kdroid.musicradio.domain.Station
import dev.kdroid.musicradio.domain.Stations
import musicradio.shared.generated.resources.Res
import musicradio.shared.generated.resources.dialog_cancel
import musicradio.shared.generated.resources.picker_back
import musicradio.shared.generated.resources.picker_choose
import musicradio.shared.generated.resources.picker_choose_channel
import musicradio.shared.generated.resources.picker_none
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

/**
 * Picks one stream in two steps: the station first, then - only for a station that carries several
 * channels - the channel. [onPick] receives a channel id, which is what every setting stores.
 */
@Composable
fun ChannelPickerDialog(onPick: (String) -> Unit, onDismiss: () -> Unit) {
    var station by remember { mutableStateOf<Station?>(null) }
    val chosen = station
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(stringResource(if (chosen == null) Res.string.picker_choose else Res.string.picker_choose_channel))
        },
        text = {
            LazyColumn(Modifier.heightIn(max = 420.dp)) {
                if (chosen == null) {
                    items(Stations.all, key = { it.id }) { entry ->
                        PickerRow(
                            name = stringResource(entry.name),
                            station = entry,
                            onClick = {
                                if (entry.multiChannel) station = entry else onPick(entry.channels.first().id)
                            },
                        )
                    }
                } else {
                    items(chosen.channels, key = { it.id }) { channel ->
                        PickerRow(
                            name = channel.title ?: stringResource(chosen.name),
                            station = chosen,
                            onClick = { onPick(channel.id) },
                        )
                    }
                }
            }
        },
        confirmButton = {
            if (chosen != null) {
                TextButton(onClick = { station = null }) { Text(stringResource(Res.string.picker_back)) }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(Res.string.dialog_cancel)) }
        },
    )
}

@Composable
private fun PickerRow(name: String, station: Station, onClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().clickable(onClick = onClick).padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Image(
            painterResource(station.artwork),
            contentDescription = null,
            modifier = Modifier.size(40.dp).clip(RoundedCornerShape(8.dp)),
            contentScale = ContentScale.Crop,
        )
        Text(name, style = MaterialTheme.typography.bodyLarge, maxLines = 2)
    }
    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
}

/** "Station · Channel" for a stored channel id, or a placeholder when nothing valid is stored. */
@Composable
fun channelLabel(channelId: String): String {
    val station = Stations.stationOfChannel(channelId)
    val channel = Stations.channel(channelId)
    if (station == null || channel == null) return stringResource(Res.string.picker_none)
    val stationName = stringResource(station.name)
    return channel.title?.let { "$stationName · $it" } ?: stationName
}
