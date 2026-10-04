package dev.kdroid.musicradio.main

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier

/** `true` when the host draws its own title bar, so the app must not repeat the brand strip. */
val LocalHostHasTitleBar = staticCompositionLocalOf { false }

/**
 * A surface the window can be dragged by. Empty everywhere except the desktop host, which supplies
 * Nucleus's drag modifier without the shared UI having to depend on Nucleus.
 */
val LocalWindowDrag = staticCompositionLocalOf<Modifier> { Modifier }

/** `true` on a phone-width window: one pane at a time, bottom navigation, player on its own screen. */
val LocalCompactLayout = compositionLocalOf { false }
