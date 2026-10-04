package dev.kdroid.musicradio.ui

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import dev.kdroid.musicradio.app.AppDialog
import dev.kdroid.musicradio.app.AppIntent
import musicradio.shared.generated.resources.Res
import musicradio.shared.generated.resources.dialog_cancel
import musicradio.shared.generated.resources.dialog_confirm
import musicradio.shared.generated.resources.settings_reset
import musicradio.shared.generated.resources.settings_reset_confirm
import org.jetbrains.compose.resources.stringResource

@Composable
fun AppDialogHost(dialog: AppDialog, onIntent: (AppIntent) -> Unit) {
    if (dialog !is AppDialog.ConfirmReset) return
    AlertDialog(
        onDismissRequest = { onIntent(AppIntent.DismissDialog) },
        title = { Text(stringResource(Res.string.settings_reset)) },
        text = { Text(stringResource(Res.string.settings_reset_confirm)) },
        confirmButton = {
            TextButton(onClick = { onIntent(AppIntent.ConfirmDialog) }) {
                Text(stringResource(Res.string.dialog_confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = { onIntent(AppIntent.DismissDialog) }) {
                Text(stringResource(Res.string.dialog_cancel))
            }
        },
    )
}
