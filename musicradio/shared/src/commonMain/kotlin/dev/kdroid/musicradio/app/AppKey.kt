package dev.kdroid.musicradio.app

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Radio
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Star
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation3.runtime.NavKey
import musicradio.shared.generated.resources.Res
import musicradio.shared.generated.resources.nav_about
import musicradio.shared.generated.resources.nav_favorites
import musicradio.shared.generated.resources.nav_settings
import musicradio.shared.generated.resources.nav_stations
import org.jetbrains.compose.resources.stringResource

@Immutable
sealed interface AppKey : NavKey {
    data object Stations : AppKey
    data object Favorites : AppKey
    data object Settings : AppKey
    data object About : AppKey

    /** Only reachable on a compact window, where the player cannot share the screen with the list. */
    data object NowPlaying : AppKey
}

val MainDestinations: List<AppKey> = listOf(
    AppKey.Stations,
    AppKey.Favorites,
    AppKey.Settings,
    AppKey.About,
)

fun AppKey.isMain(): Boolean = this in MainDestinations

@Composable
fun AppKey.label(): String = when (this) {
    AppKey.Stations -> stringResource(Res.string.nav_stations)
    AppKey.Favorites -> stringResource(Res.string.nav_favorites)
    AppKey.Settings -> stringResource(Res.string.nav_settings)
    AppKey.About -> stringResource(Res.string.nav_about)
    AppKey.NowPlaying -> ""
}

fun AppKey.icon(): ImageVector = when (this) {
    AppKey.Stations -> Icons.Outlined.Radio
    AppKey.Favorites -> Icons.Outlined.Star
    AppKey.Settings -> Icons.Outlined.Settings
    AppKey.About -> Icons.Outlined.Info
    AppKey.NowPlaying -> Icons.Outlined.Radio
}
