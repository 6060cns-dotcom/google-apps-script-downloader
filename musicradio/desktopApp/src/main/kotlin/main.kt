import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.absolutePadding
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.rememberWindowState
import dev.kdroid.musicradio.App
import dev.kdroid.musicradio.domain.AccentColor
import dev.kdroid.musicradio.main.DesktopUpdate
import dev.kdroid.musicradio.main.LocalHostHasTitleBar
import dev.kdroid.musicradio.main.LocalWindowDrag
import dev.kdroid.musicradio.main.UpdateButton
import dev.kdroid.musicradio.main.UpdateRestartDialog
import dev.kdroid.musicradio.main.rememberDesktopUpdate
import dev.kdroid.musicradio.theme.rememberRadioColorScheme
import dev.nucleusframework.application.nucleusApplication
import dev.nucleusframework.core.runtime.Platform
import dev.nucleusframework.window.ControlButtonsDirection
import dev.nucleusframework.window.DecoratedWindowScope
import dev.nucleusframework.window.LocalWindowChromeInsets
import dev.nucleusframework.window.WindowAppearance
import dev.nucleusframework.window.WindowAppearanceMode
import dev.nucleusframework.window.WindowBackground
import dev.nucleusframework.window.WindowControls
import dev.nucleusframework.window.WindowScaffold
import dev.nucleusframework.window.macOSLargeCornerRadius
import dev.nucleusframework.window.material.MaterialDecoratedWindow
import dev.nucleusframework.window.windowDragArea
import musicradio.shared.generated.resources.Res
import musicradio.shared.generated.resources.app_icon
import org.jetbrains.compose.resources.painterResource

private val CHROME_HEIGHT = 44.dp

fun main(args: Array<String>) {
    nucleusApplication(args) {
        // The chrome sits outside App's own theme, so the app's resolved appearance has to come
        // back out here - otherwise the title bar keeps following the OS while the content goes
        // light or dark. Seeded from the OS; App overrides it as soon as the store has loaded.
        val systemDark = isSystemInDarkTheme()
        var dark by remember { mutableStateOf(systemDark) }
        // Same story for the interface language: Hebrew has to flip the window controls too, and
        // they sit outside App's own LocalLayoutDirection.
        var rtl by remember { mutableStateOf(false) }
        var accent by remember { mutableStateOf(AccentColor.Indigo) }
        val colors = rememberRadioColorScheme(accent, dark)
        val update = rememberDesktopUpdate()
        // The quiet path: a downloaded installer runs on the way out, so most people never see
        // the dialog at all - the app is simply newer the next time they open it.
        val quit = {
            update.installOnExit()
            exitApplication()
        }

        MaterialTheme(colorScheme = colors) {
            MaterialDecoratedWindow(
                onCloseRequest = quit,
                state = rememberWindowState(
                    position = WindowPosition(Alignment.Center),
                    width = 1180.dp,
                    height = 780.dp,
                ),
                title = "Music Radio",
                icon = if (Platform.Current == Platform.Windows) painterResource(Res.drawable.app_icon) else null,
                minimumSize = DpSize(420.dp, 560.dp),
            ) {
                val windowScope = this
                // The colour behind everything Compose does not paint (live resize frames, the
                // margin around a panel) and the appearance of the native surfaces.
                WindowBackground(colors.background)
                WindowAppearance(if (dark) WindowAppearanceMode.Dark else WindowAppearanceMode.Light)

                val direction = if (rtl) LayoutDirection.Rtl else LayoutDirection.Ltr
                WindowScaffold(
                    modifier = Modifier.macOSLargeCornerRadius(),
                    controlButtonsDirection = if (rtl) ControlButtonsDirection.Rtl else ControlButtonsDirection.Ltr,
                    titleBar = {
                        CompositionLocalProvider(LocalLayoutDirection provides direction) { windowScope.AppChrome(update) }
                    },
                ) {
                    Box(Modifier.fillMaxSize()) {
                        CompositionLocalProvider(
                            LocalHostHasTitleBar provides true,
                            // Lets the shared UI declare its own drag surfaces - the navigation
                            // rail - without depending on Nucleus.
                            LocalWindowDrag provides Modifier.windowDragArea(),
                        ) {
                            App(
                                // SideEffect: App reports the resolved appearance during
                                // composition, so the write has to land after it.
                                onThemeChange = { isDark -> SideEffect { dark = isDark } },
                                onLayoutDirectionChange = { isRtl -> SideEffect { rtl = isRtl } },
                                onAccentChange = { picked -> SideEffect { accent = picked } },
                                onQuit = quit,
                            )
                        }
                        // Outside the density and drag providers above: this is the host's own
                        // dialog, not part of the shared UI, and App knows nothing about it.
                        UpdateRestartDialog(update)
                    }
                }
            }
        }
    }
}

/**
 * Window chrome: the app title at the leading edge, caption buttons at the trailing one. The whole
 * strip is the drag surface - `WindowScaffold` makes nothing implicit.
 */
@Composable
private fun DecoratedWindowScope.AppChrome(update: DesktopUpdate) {
    val colors = MaterialTheme.colorScheme
    val insets = LocalWindowChromeInsets.current

    Box(
        Modifier
            .fillMaxWidth()
            .height(CHROME_HEIGHT)
            .background(colors.surfaceContainer)
            .windowDragArea(),
    ) {
        // controlsInsets keeps the title clear of the zones the platform owns: the macOS
        // traffic-lights over the leading edge, the KDE edge padding. Read as absolute values -
        // the scaffold already mirrored this reserve off controlButtonsDirection, so resolving it
        // again against an RTL layout would send it back under the title.
        val reserve = insets.controlsInsets
        Text(
            "Music Radio",
            Modifier
                .align(Alignment.CenterStart)
                .absolutePadding(
                    left = reserve.calculateLeftPadding(LayoutDirection.Ltr),
                    right = reserve.calculateRightPadding(LayoutDirection.Ltr),
                )
                .padding(start = 14.dp),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Medium,
            color = colors.onSurfaceVariant,
        )
        // macOS draws real traffic-lights, so nothing is placed there.
        val mac = Platform.Current == Platform.MacOS
        Row(
            Modifier.align(Alignment.CenterEnd).fillMaxHeight(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            UpdateButton(update)
            if (!mac) WindowControls(Modifier.fillMaxHeight())
        }
    }
}
