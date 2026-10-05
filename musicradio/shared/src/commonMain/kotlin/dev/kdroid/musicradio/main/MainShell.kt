package dev.kdroid.musicradio.main

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.movableContentOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.kdroid.musicradio.app.AppIntent
import dev.kdroid.musicradio.app.AppKey
import dev.kdroid.musicradio.app.AppState
import dev.kdroid.musicradio.app.MainDestinations
import dev.kdroid.musicradio.app.icon
import dev.kdroid.musicradio.app.label
import dev.kdroid.musicradio.platform.Platform
import musicradio.shared.generated.resources.Res
import musicradio.shared.generated.resources.app_name
import org.jetbrains.compose.resources.stringResource

/**
 * The running app: navigation on the side (desktop) or at the bottom (phone), the player pinned
 * below the content either way, and the current screen in between.
 */
@Composable
fun MainShell(
    state: AppState,
    destination: AppKey,
    onIntent: (AppIntent) -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val compact = LocalCompactLayout.current
    // The screen is called from two different places in the tree - inside a Row when the rail is
    // showing, directly in the Column when the bottom bar is. Composed twice it would be two
    // separate instances, so crossing the compact threshold (a window resize, a phone rotating)
    // would throw away the scroll position and everything else the screen was remembering.
    // movableContent moves the same instance, with its state, between the two slots.
    val screen = remember(content) { movableContentOf { content() } }
    Column(modifier.fillMaxSize()) {
        // On desktop the brand lives in the window title bar, so it is not repeated here.
        if (!LocalHostHasTitleBar.current) BrandBar()
        if (compact) {
            Box(Modifier.weight(1f).fillMaxWidth()) { screen() }
            MiniPlayerBar(state, onIntent)
            BottomBar(destination, onIntent)
        } else {
            Row(Modifier.weight(1f).fillMaxWidth()) {
                NavRail(destination, onIntent)
                Box(Modifier.weight(1f).fillMaxHeight()) { screen() }
            }
            PlayerBar(state, onIntent)
        }
    }
}

@Composable
private fun BrandBar(modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    Row(
        modifier.fillMaxWidth().height(52.dp).background(colors.surfaceContainer).padding(horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            stringResource(Res.string.app_name),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = colors.onSurface,
        )
    }
}

@Composable
private fun NavRail(selected: AppKey, onIntent: (AppIntent) -> Unit, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    Column(
        modifier.width(216.dp).fillMaxHeight().background(colors.surfaceContainer)
            // The rail's own surface moves the window, macOS-style. The items are clickable, so
            // they claim their presses and stay out of the drag.
            .then(LocalWindowDrag.current)
            .padding(horizontal = 12.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        MainDestinations.filter { it != AppKey.Settings && it != AppKey.About }.forEach { dest ->
            NavRailItem(dest, dest == selected) { onIntent(AppIntent.Navigate(dest)) }
        }
        Spacer(Modifier.weight(1f))
        NavRailItem(AppKey.Settings, AppKey.Settings == selected) { onIntent(AppIntent.Navigate(AppKey.Settings)) }
        NavRailItem(AppKey.About, AppKey.About == selected) { onIntent(AppIntent.Navigate(AppKey.About)) }
        HorizontalDivider(color = colors.outlineVariant)
        VersionLabel(Modifier.padding(start = 4.dp, top = 12.dp))
    }
}

@Composable
private fun VersionLabel(modifier: Modifier = Modifier) {
    val version = Platform.appVersion
    if (version.isEmpty()) return
    Text(version, modifier, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
}

@Composable
private fun NavRailItem(dest: AppKey, selected: Boolean, onClick: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    val easing = CubicBezierEasing(0.2833f, 0.99f, 0.31833f, 0.99f)
    val background by animateColorAsState(
        if (selected) colors.primaryContainer else colors.surfaceContainer,
        tween(280, easing = easing),
        label = "nav-bg",
    )
    val foreground by animateColorAsState(
        if (selected) colors.onPrimaryContainer else colors.onSurfaceVariant,
        tween(280, easing = easing),
        label = "nav-fg",
    )
    Row(
        Modifier.fillMaxWidth().height(52.dp).clip(RoundedCornerShape(26.dp)).background(background)
            .clickable(onClick = onClick).padding(horizontal = 18.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Icon(dest.icon(), null, tint = foreground)
        Text(dest.label(), style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Medium, color = foreground)
    }
}

@Composable
private fun BottomBar(selected: AppKey, onIntent: (AppIntent) -> Unit, modifier: Modifier = Modifier) {
    NavigationBar(modifier) {
        MainDestinations.forEach { dest ->
            NavigationBarItem(
                selected = dest == selected,
                onClick = { onIntent(AppIntent.Navigate(dest)) },
                icon = { Icon(dest.icon(), null) },
                label = { Text(dest.label()) },
            )
        }
    }
}
