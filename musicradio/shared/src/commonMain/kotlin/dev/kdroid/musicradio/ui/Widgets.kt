package dev.kdroid.musicradio.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.GraphicEq
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import dev.kdroid.musicradio.domain.Channel
import dev.kdroid.musicradio.domain.Station
import dev.kdroid.musicradio.domain.StationCategory
import musicradio.shared.generated.resources.Res
import musicradio.shared.generated.resources.category_music
import musicradio.shared.generated.resources.category_news
import musicradio.shared.generated.resources.category_torah
import musicradio.shared.generated.resources.favorite_add
import musicradio.shared.generated.resources.favorite_remove
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun StationCategory.label(): String = when (this) {
    StationCategory.Music -> stringResource(Res.string.category_music)
    StationCategory.Torah -> stringResource(Res.string.category_torah)
    StationCategory.News -> stringResource(Res.string.category_news)
}

/** [channel] wins when the broadcaster published a cover for it; otherwise the station's logo. */
@Composable
fun StationArtwork(
    station: Station,
    modifier: Modifier = Modifier,
    shape: androidx.compose.ui.graphics.Shape = RoundedCornerShape(16.dp),
    channel: Channel? = null,
) {
    Image(
        painter = painterResource(channel?.artwork ?: station.artwork),
        contentDescription = null,
        contentScale = ContentScale.Crop,
        modifier = modifier.clip(shape).background(MaterialTheme.colorScheme.surfaceContainerHighest),
    )
}

/**
 * One tile in the station grid. The whole tile is the play target; the star is a separate press so
 * bookmarking never costs you the stream you were listening to.
 */
@Composable
fun StationCard(
    station: Station,
    playing: Boolean,
    favorite: Boolean,
    onClick: () -> Unit,
    onToggleFavorite: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    Surface(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        color = if (playing) colors.primaryContainer else colors.surfaceContainerLow,
        tonalElevation = if (playing) 2.dp else 0.dp,
    ) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Box(Modifier.fillMaxWidth().aspectRatio(1f)) {
                StationArtwork(station, Modifier.fillMaxWidth().aspectRatio(1f))
                // The star sits on the artwork rather than beside the title: in the row it landed
                // on the opposite edge from the name and read as unrelated decoration.
                Surface(
                    modifier = Modifier.align(Alignment.TopStart).padding(8.dp),
                    shape = CircleShape,
                    color = colors.scrim.copy(alpha = 0.45f),
                ) {
                    IconButton(onClick = onToggleFavorite, modifier = Modifier.size(36.dp)) {
                        Icon(
                            if (favorite) Icons.Filled.Star else Icons.Outlined.StarBorder,
                            stringResource(if (favorite) Res.string.favorite_remove else Res.string.favorite_add),
                            modifier = Modifier.size(20.dp),
                            // White either way: the scrim guarantees contrast on any cover.
                            tint = if (favorite) colors.primary else Color.White,
                        )
                    }
                }
                if (playing) {
                    Surface(
                        modifier = Modifier.align(Alignment.BottomEnd).padding(8.dp),
                        shape = RoundedCornerShape(50),
                        color = colors.primary,
                        contentColor = colors.onPrimary,
                    ) {
                        Icon(Icons.Outlined.GraphicEq, null, Modifier.padding(6.dp).size(16.dp))
                    }
                }
            }
            Column {
                Text(
                    stringResource(station.name),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = if (playing) colors.onPrimaryContainer else colors.onSurface,
                )
                Text(
                    station.category.label(),
                    style = MaterialTheme.typography.labelSmall,
                    color = if (playing) colors.onPrimaryContainer else colors.onSurfaceVariant,
                )
            }
        }
    }
}

/**
 * One tile per individual stream, starrable in its own right: a station with 83 channels is not
 * something you want to bookmark whole just to keep one of them close.
 */
@Composable
fun ChannelCard(
    station: Station,
    channel: Channel,
    playing: Boolean,
    favorite: Boolean,
    onClick: () -> Unit,
    onToggleFavorite: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    val stationName = stringResource(station.name)
    Surface(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        color = if (playing) colors.primaryContainer else colors.surfaceContainerLow,
        tonalElevation = if (playing) 2.dp else 0.dp,
    ) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Box(Modifier.fillMaxWidth().aspectRatio(1f)) {
                StationArtwork(station, Modifier.fillMaxWidth().aspectRatio(1f), channel = channel)
                // Placed exactly as the station tile's star, so the two grids stay readable as one.
                Surface(
                    modifier = Modifier.align(Alignment.TopStart).padding(8.dp),
                    shape = CircleShape,
                    color = colors.scrim.copy(alpha = 0.45f),
                ) {
                    IconButton(onClick = onToggleFavorite, modifier = Modifier.size(36.dp)) {
                        Icon(
                            if (favorite) Icons.Filled.Star else Icons.Outlined.StarBorder,
                            stringResource(if (favorite) Res.string.favorite_remove else Res.string.favorite_add),
                            modifier = Modifier.size(20.dp),
                            tint = if (favorite) colors.primary else Color.White,
                        )
                    }
                }
                if (playing) {
                    Surface(
                        modifier = Modifier.align(Alignment.BottomEnd).padding(8.dp),
                        shape = RoundedCornerShape(50),
                        color = colors.primary,
                        contentColor = colors.onPrimary,
                    ) {
                        Icon(Icons.Outlined.GraphicEq, null, Modifier.padding(6.dp).size(16.dp))
                    }
                }
            }
            Column {
                Text(
                    channel.title ?: stationName,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = if (playing) colors.onPrimaryContainer else colors.onSurface,
                )
                Text(
                    stationName,
                    style = MaterialTheme.typography.labelSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = if (playing) colors.onPrimaryContainer else colors.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
fun SectionHeader(title: String, modifier: Modifier = Modifier) {
    Text(
        title,
        modifier = modifier.padding(top = 8.dp, bottom = 4.dp),
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.primary,
    )
}

/** A settings line: label and supporting text on one side, whatever control on the other. */
@Composable
fun SettingRow(title: String, modifier: Modifier = Modifier, subtitle: String? = null, control: @Composable () -> Unit) {
    Row(
        modifier.fillMaxWidth().padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge)
            if (subtitle != null) {
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        control()
    }
}

/**
 * A settings line whose control is too wide to share a row with its label — a segmented control
 * splits its width evenly between the segments, so the longest option decides how much room the
 * whole thing needs. Giving it its own full-width row keeps every label readable in every language.
 */
@Composable
fun SettingBlock(title: String, modifier: Modifier = Modifier, subtitle: String? = null, control: @Composable () -> Unit) {
    Column(
        modifier.fillMaxWidth().padding(vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Column {
            Text(title, style = MaterialTheme.typography.bodyLarge)
            if (subtitle != null) {
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        control()
    }
}
