package dev.kdroid.musicradio.app

import androidx.compose.animation.ContentTransform
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import dev.kdroid.musicradio.main.AboutScreen
import dev.kdroid.musicradio.main.FavoritesScreen
import dev.kdroid.musicradio.main.LocalCompactLayout
import dev.kdroid.musicradio.main.MainShell
import dev.kdroid.musicradio.main.NowPlayingScreen
import dev.kdroid.musicradio.main.SettingsScreen
import dev.kdroid.musicradio.main.StationsScreen
import dev.kdroid.musicradio.ui.AppDialogHost
import dev.kdroid.musicradio.ui.MessageBar

/** Below this the navigation moves to the bottom and the player gets a screen of its own. */
private val CompactWidth = 720.dp

@Composable
fun RootScreen(state: AppState, backStack: NavBackStack<AppKey>, onIntent: (AppIntent) -> Unit, modifier: Modifier = Modifier) {
    BoxWithConstraints(modifier.fillMaxSize()) {
        val compact = maxWidth < CompactWidth
        CompositionLocalProvider(LocalCompactLayout provides compact) {
            Box(Modifier.fillMaxSize()) {
                val current = backStack.last()
                if (current.isMain()) {
                    MainShell(state = state, destination = current, onIntent = onIntent) {
                        AppNavDisplay(backStack, state, onIntent)
                    }
                } else {
                    AppNavDisplay(backStack, state, onIntent)
                }
                MessageBar(
                    message = state.message,
                    onDismiss = remember(onIntent) { { onIntent(AppIntent.DismissMessage) } },
                    modifier = Modifier.align(Alignment.BottomCenter),
                )
                AppDialogHost(dialog = state.dialog, onIntent = onIntent)
            }
        }
    }
}

@Composable
private fun AppNavDisplay(backStack: NavBackStack<AppKey>, state: AppState, onIntent: (AppIntent) -> Unit) {
    val transform = pageFade()
    NavDisplay(
        backStack = backStack,
        onBack = { onIntent(AppIntent.Back) },
        transitionSpec = { transform },
        popTransitionSpec = { transform },
        predictivePopTransitionSpec = { transform },
        entryProvider = entryProvider {
            entry<AppKey.Stations> { StationsScreen(state, onIntent) }
            entry<AppKey.Favorites> { FavoritesScreen(state, onIntent) }
            entry<AppKey.Settings> { SettingsScreen(state, onIntent) }
            entry<AppKey.About> { AboutScreen() }
            entry<AppKey.NowPlaying> { NowPlayingScreen(state, onIntent) }
        },
    )
}

private val PageFadeEasing = CubicBezierEasing(0.25f, 0.1f, 0.25f, 1f)

private fun pageFade(): ContentTransform =
    fadeIn(tween(150, easing = PageFadeEasing)) togetherWith fadeOut(tween(150, easing = PageFadeEasing))
