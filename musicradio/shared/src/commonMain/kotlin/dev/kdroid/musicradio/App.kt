package dev.kdroid.musicradio

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.LayoutDirection
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.kdroid.musicradio.app.AppViewModel
import dev.kdroid.musicradio.app.RootScreen
import dev.kdroid.musicradio.di.AppGraph
import dev.kdroid.musicradio.di.createAppGraph
import dev.kdroid.musicradio.domain.AccentColor
import dev.kdroid.musicradio.domain.ThemeMode
import dev.kdroid.musicradio.platform.ProvideAppLocale
import dev.kdroid.musicradio.theme.RadioTheme

@Composable
fun App(
    graph: AppGraph? = null,
    provided: AppViewModel? = null,
    onThemeChange: @Composable (isDark: Boolean) -> Unit = {},
    /** Window chrome lives outside this composable and needs the resolved direction too. */
    onLayoutDirectionChange: @Composable (isRtl: Boolean) -> Unit = {},
    /** …and the accent, so a host-drawn title bar is coloured by the same scheme as the content. */
    onAccentChange: @Composable (accent: AccentColor) -> Unit = {},
    onQuit: () -> Unit = {},
) {
    val appGraph = graph ?: remember { createAppGraph() }
    val vm = provided ?: viewModel { appGraph.viewModelFactory.create(onQuit) }
    val state by vm.state.collectAsState()
    val systemDark = isSystemInDarkTheme()
    val settings = state.data.settings
    val isDark = when (settings.theme) {
        ThemeMode.System -> systemDark
        ThemeMode.Light -> false
        ThemeMode.Dark -> true
    }
    onThemeChange(isDark)
    onAccentChange(settings.accent)
    val language = settings.uiLanguage
    onLayoutDirectionChange(language.rtl)
    val direction = if (language.rtl) LayoutDirection.Rtl else LayoutDirection.Ltr
    ProvideAppLocale(language.code) {
        RadioTheme(accent = settings.accent, isDark = isDark) {
            CompositionLocalProvider(LocalLayoutDirection provides direction) {
                RootScreen(state = state, backStack = vm.backStack, onIntent = vm::onIntent)
            }
        }
    }
}

@Preview
@Composable
private fun AppPreview() {
    App()
}
