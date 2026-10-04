package dev.kdroid.musicradio.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import musicradio.shared.generated.resources.Res
import musicradio.shared.generated.resources.noto_sans_hebrew_bold
import musicradio.shared.generated.resources.noto_sans_hebrew_medium
import musicradio.shared.generated.resources.noto_sans_hebrew_regular
import musicradio.shared.generated.resources.noto_sans_hebrew_semibold
import org.jetbrains.compose.resources.Font

/**
 * Noto Sans Hebrew on every platform, bundled rather than borrowed from the system.
 *
 * The web canvas had to carry it - a browser has no system Hebrew face to fall back on - and
 * shipping it everywhere else is what keeps the app looking like one app: the system default is
 * Roboto on Android, Segoe UI on Windows and SF on macOS, so the same screen used to render in
 * four different faces. The file also covers Latin and the French accents, so the English and
 * French interfaces travel with it.
 *
 * Anything outside its 464 codepoints - a track title arriving off a stream in Cyrillic, say -
 * still falls back to the system face, so this narrows the typography without narrowing what the
 * app can display.
 */
@Composable
internal fun radioTypography(): Typography {
    val regular = Font(Res.font.noto_sans_hebrew_regular, FontWeight.Normal)
    val medium = Font(Res.font.noto_sans_hebrew_medium, FontWeight.Medium)
    val semibold = Font(Res.font.noto_sans_hebrew_semibold, FontWeight.SemiBold)
    val bold = Font(Res.font.noto_sans_hebrew_bold, FontWeight.Bold)
    val family = remember(regular, medium, semibold, bold) {
        FontFamily(regular, medium, semibold, bold)
    }
    return remember(family) { Typography().withFontFamily(family) }
}

private fun Typography.withFontFamily(fontFamily: FontFamily): Typography = copy(
    displayLarge = displayLarge.copy(fontFamily = fontFamily),
    displayMedium = displayMedium.copy(fontFamily = fontFamily),
    displaySmall = displaySmall.copy(fontFamily = fontFamily),
    headlineLarge = headlineLarge.copy(fontFamily = fontFamily),
    headlineMedium = headlineMedium.copy(fontFamily = fontFamily),
    headlineSmall = headlineSmall.copy(fontFamily = fontFamily),
    titleLarge = titleLarge.copy(fontFamily = fontFamily),
    titleMedium = titleMedium.copy(fontFamily = fontFamily),
    titleSmall = titleSmall.copy(fontFamily = fontFamily),
    bodyLarge = bodyLarge.copy(fontFamily = fontFamily),
    bodyMedium = bodyMedium.copy(fontFamily = fontFamily),
    bodySmall = bodySmall.copy(fontFamily = fontFamily),
    labelLarge = labelLarge.copy(fontFamily = fontFamily),
    labelMedium = labelMedium.copy(fontFamily = fontFamily),
    labelSmall = labelSmall.copy(fontFamily = fontFamily),
)
