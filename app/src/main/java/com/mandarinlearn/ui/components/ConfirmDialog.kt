// ConfirmDialog.kt — Mandarin Learn
// Reusable confirmation dialog (e.g. quit exam, reset progress, mark as read).
// Used by FlashcardScreen (quit session), ExamScreen (quit exam), SettingsScreen (reset progress).

package com.mandarinlearn.ui.components

import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mandarinlearn.R
import com.mandarinlearn.ui.theme.MandarinLearnTheme

/**
 * Standard confirmation dialog.
 *
 * @param title Dialog title (header-medium size via M3 AlertDialog default).
 * @param message Body text explaining the action.
 * @param confirmText Label for the confirm (destructive) action.
 * @param dismissText Label for the cancel / dismiss action.
 * @param onConfirm Called when user taps the confirm button.
 * @param onDismiss Called when user taps cancel or outside the dialog.
 * @param modifier Optional modifier.
 */
@Composable
fun ConfirmDialog(
    title: String,
    message: String,
    confirmText: String,
    dismissText: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text  = title,
                style = MaterialTheme.typography.headlineMedium,
            )
        },
        text = {
            Text(
                text  = message,
                style = MaterialTheme.typography.bodyLarge,
            )
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                modifier = Modifier.defaultMinSize(minWidth = 56.dp, minHeight = 56.dp),
            ) {
                Text(
                    text  = confirmText,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.error,
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.defaultMinSize(minWidth = 56.dp, minHeight = 56.dp),
            ) {
                Text(
                    text  = dismissText,
                    style = MaterialTheme.typography.labelLarge,
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.surface,
        modifier       = modifier,
    )
}

@Preview(showBackground = true)
@Composable
private fun ConfirmDialogPreview() {
    MandarinLearnTheme {
        ConfirmDialog(
            title       = "Quit exam?",
            message     = "Your progress will be lost.",
            confirmText = "Quit",
            dismissText = "Cancel",
            onConfirm   = {},
            onDismiss   = {},
        )
    }
}
