package dev.kdroid.musicradio.main

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Switch
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
import androidx.compose.ui.unit.dp
import dev.kdroid.musicradio.app.AppIntent
import dev.kdroid.musicradio.app.AppState
import dev.kdroid.musicradio.domain.AccentColor
import dev.kdroid.musicradio.domain.StartupPlay
import dev.kdroid.musicradio.domain.Stations
import dev.kdroid.musicradio.domain.ThemeMode
import dev.kdroid.musicradio.domain.UiLanguage
import dev.kdroid.musicradio.platform.wakeEventLog
import dev.kdroid.musicradio.platform.wakeListenerSupported
import dev.kdroid.musicradio.ui.SectionHeader
import dev.kdroid.musicradio.ui.SettingBlock
import dev.kdroid.musicradio.ui.SettingRow
import musicradio.shared.generated.resources.Res
import musicradio.shared.generated.resources.dialog_dismiss
import musicradio.shared.generated.resources.language_system
import musicradio.shared.generated.resources.settings_accent
import musicradio.shared.generated.resources.settings_appearance
import musicradio.shared.generated.resources.settings_data
import musicradio.shared.generated.resources.settings_language
import musicradio.shared.generated.resources.settings_playback
import musicradio.shared.generated.resources.settings_reset
import musicradio.shared.generated.resources.settings_reset_desc
import musicradio.shared.generated.resources.settings_show_news
import musicradio.shared.generated.resources.settings_show_news_desc
import musicradio.shared.generated.resources.settings_startup
import musicradio.shared.generated.resources.settings_startup_desc
import musicradio.shared.generated.resources.settings_theme
import musicradio.shared.generated.resources.settings_wake
import musicradio.shared.generated.resources.settings_wake_desc
import musicradio.shared.generated.resources.startup_chosen
import musicradio.shared.generated.resources.startup_last
import musicradio.shared.generated.resources.startup_off
import musicradio.shared.generated.resources.theme_dark
import musicradio.shared.generated.resources.theme_light
import musicradio.shared.generated.resources.theme_system
import musicradio.shared.generated.resources.wake_log
import musicradio.shared.generated.resources.wake_log_empty
import org.jetbrains.compose.resources.stringResource

@Composable
fun SettingsScreen(state: AppState, onIntent: (AppIntent) -> Unit, modifier: Modifier = Modifier) {
    val settings = state.data.settings
    var picking by remember { mutableStateOf<PickTarget?>(null) }
    var showWakeLog by remember { mutableStateOf(false) }
    if (showWakeLog) {
        AlertDialog(
            onDismissRequest = { showWakeLog = false },
            title = { Text(stringResource(Res.string.wake_log)) },
            text = {
                Text(
                    wakeEventLog().ifBlank { stringResource(Res.string.wake_log_empty) },
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    style = MaterialTheme.typography.bodySmall,
                )
            },
            confirmButton = {
                TextButton(onClick = { showWakeLog = false }) { Text(stringResource(Res.string.dialog_dismiss)) }
            },
        )
    }
    picking?.let { target ->
        ChannelPickerDialog(
            onPick = { channelId ->
                onIntent(
                    when (target) {
                        PickTarget.Startup -> AppIntent.SetStartupChannel(channelId)
                        PickTarget.Wake -> AppIntent.SetWakeChannel(channelId)
                    },
                )
                picking = null
            },
            onDismiss = { picking = null },
        )
    }
    Column(
        modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 24.dp, vertical = 20.dp),
    ) {
        Column(Modifier.widthIn(max = 720.dp)) {
            SectionHeader(stringResource(Res.string.settings_appearance))

            SettingBlock(stringResource(Res.string.settings_theme)) {
                ThemePicker(
                    current = settings.theme,
                    onPick = { onIntent(AppIntent.SetTheme(it)) },
                    modifier = Modifier.widthIn(max = 420.dp).fillMaxWidth(),
                )
            }
            SettingRow(stringResource(Res.string.settings_accent)) {
                AccentPicker(settings.accent) { onIntent(AppIntent.SetAccent(it)) }
            }
            SettingRow(stringResource(Res.string.settings_language)) {
                LanguagePicker(
                    language = if (settings.uiLanguageAuto) null else settings.uiLanguage,
                    onPick = { onIntent(AppIntent.SetUiLanguage(it)) },
                )
            }

            HorizontalDivider(Modifier.padding(vertical = 16.dp), color = MaterialTheme.colorScheme.outlineVariant)
            SectionHeader(stringResource(Res.string.settings_playback))

            SettingRow(
                stringResource(Res.string.settings_show_news),
                subtitle = stringResource(Res.string.settings_show_news_desc),
            ) {
                Switch(checked = settings.showNews, onCheckedChange = { onIntent(AppIntent.SetShowNews(it)) })
            }
            SettingBlock(
                stringResource(Res.string.settings_startup),
                subtitle = stringResource(Res.string.settings_startup_desc),
            ) {
                StartupPicker(
                    current = settings.startupPlay,
                    onPick = { mode ->
                        // "Chosen" with nothing chosen yet has nothing to play, so it opens the picker.
                        if (mode == StartupPlay.Chosen && Stations.channel(settings.startupChannelId) == null) {
                            picking = PickTarget.Startup
                        } else {
                            onIntent(AppIntent.SetStartupPlay(mode))
                        }
                    },
                    modifier = Modifier.widthIn(max = 420.dp).fillMaxWidth(),
                )
                if (settings.startupPlay == StartupPlay.Chosen) {
                    OutlinedButton(onClick = { picking = PickTarget.Startup }) {
                        Text(channelLabel(settings.startupChannelId))
                    }
                }
            }
            if (wakeListenerSupported) {
                SettingRow(
                    stringResource(Res.string.settings_wake),
                    subtitle = stringResource(Res.string.settings_wake_desc),
                ) {
                    Switch(
                        checked = settings.playOnWake,
                        onCheckedChange = { on ->
                            if (on && Stations.channel(settings.wakeChannelId) == null) {
                                picking = PickTarget.Wake
                            } else {
                                onIntent(AppIntent.SetPlayOnWake(on))
                            }
                        },
                    )
                }
                if (settings.playOnWake) {
                    OutlinedButton(onClick = { picking = PickTarget.Wake }) {
                        Text(channelLabel(settings.wakeChannelId))
                    }
                    TextButton(onClick = { showWakeLog = true }) { Text(stringResource(Res.string.wake_log)) }
                }
            }

            HorizontalDivider(Modifier.padding(vertical = 16.dp), color = MaterialTheme.colorScheme.outlineVariant)
            SectionHeader(stringResource(Res.string.settings_data))

            SettingRow(
                stringResource(Res.string.settings_reset),
                subtitle = stringResource(Res.string.settings_reset_desc),
            ) {
                Button(
                    onClick = { onIntent(AppIntent.ResetApp) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer,
                    ),
                ) {
                    Text(stringResource(Res.string.settings_reset))
                }
            }
        }
    }
}

private enum class PickTarget { Startup, Wake }

@Composable
private fun StartupPicker(current: StartupPlay, onPick: (StartupPlay) -> Unit, modifier: Modifier = Modifier) {
    SingleChoiceSegmentedButtonRow(modifier) {
        StartupPlay.entries.forEachIndexed { index, mode ->
            SegmentedButton(
                selected = current == mode,
                onClick = { onPick(mode) },
                shape = SegmentedButtonDefaults.itemShape(index, StartupPlay.entries.size),
            ) {
                Text(
                    when (mode) {
                        StartupPlay.Off -> stringResource(Res.string.startup_off)
                        StartupPlay.LastPlayed -> stringResource(Res.string.startup_last)
                        StartupPlay.Chosen -> stringResource(Res.string.startup_chosen)
                    },
                    maxLines = 1,
                    softWrap = false,
                )
            }
        }
    }
}

@Composable
private fun ThemePicker(current: ThemeMode, onPick: (ThemeMode) -> Unit, modifier: Modifier = Modifier) {
    SingleChoiceSegmentedButtonRow(modifier) {
        ThemeMode.entries.forEachIndexed { index, mode ->
            SegmentedButton(
                selected = current == mode,
                onClick = { onPick(mode) },
                shape = SegmentedButtonDefaults.itemShape(index, ThemeMode.entries.size),
            ) {
                Text(
                    when (mode) {
                        ThemeMode.System -> stringResource(Res.string.theme_system)
                        ThemeMode.Light -> stringResource(Res.string.theme_light)
                        ThemeMode.Dark -> stringResource(Res.string.theme_dark)
                    },
                    maxLines = 1,
                    softWrap = false,
                )
            }
        }
    }
}

@Composable
private fun AccentPicker(current: AccentColor, onPick: (AccentColor) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
        AccentColor.entries.forEach { accent ->
            val selected = accent == current
            Box(
                Modifier
                    .size(if (selected) 32.dp else 26.dp)
                    .clip(CircleShape)
                    .background(accent.seed)
                    .border(
                        width = if (selected) 3.dp else 0.dp,
                        color = MaterialTheme.colorScheme.onSurface,
                        shape = CircleShape,
                    )
                    .clickable { onPick(accent) },
            )
        }
    }
}

@Composable
private fun LanguagePicker(language: UiLanguage?, onPick: (UiLanguage?) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        OutlinedButton(onClick = { expanded = true }) {
            Text(language?.label ?: stringResource(Res.string.language_system))
            Icon(Icons.Outlined.ExpandMore, null, Modifier.padding(start = 6.dp).size(18.dp))
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            DropdownMenuItem(
                text = { Text(stringResource(Res.string.language_system)) },
                onClick = {
                    expanded = false
                    onPick(null)
                },
            )
            UiLanguage.entries.forEach { entry ->
                DropdownMenuItem(
                    text = { Text(entry.label) },
                    onClick = {
                        expanded = false
                        onPick(entry)
                    },
                )
            }
        }
    }
}
