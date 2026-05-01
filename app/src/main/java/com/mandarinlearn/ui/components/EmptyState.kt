// EmptyState.kt — Mandarin Learn
// Empty-state composable per UX_SPECIFICATION.md §3.8.
// Centered icon (56 dp), title (header-medium), body, optional CTA button.

package com.mandarinlearn.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mandarinlearn.ui.theme.MandarinLearnTheme
import com.mandarinlearn.ui.theme.SpacingM
import com.mandarinlearn.ui.theme.SpacingXs

/**
 * Empty-state screen component per UX spec §3.8.
 *
 * @param icon Icon to display (56 dp, must have contentDescription).
 * @param iconContentDescription Content description for the icon (TalkBack).
 * @param title Primary message (header-medium).
 * @param body Secondary body text (body-large).
 * @param actionContent Optional CTA composable (e.g. a MandarinPrimaryButton).
 * @param modifier Optional modifier.
 */
@Composable
fun EmptyState(
    icon: ImageVector,
    iconContentDescription: String,
    title: String,
    body: String,
    modifier: Modifier = Modifier,
    actionContent: @Composable (() -> Unit)? = null,
) {
    Column(
        modifier              = modifier.fillMaxSize(),
        horizontalAlignment   = Alignment.CenterHorizontally,
        verticalArrangement   = Arrangement.Center,
    ) {
        Icon(
            imageVector        = icon,
            contentDescription = iconContentDescription,
            tint               = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier           = Modifier.size(56.dp),   // UX spec §3.8
        )
        Spacer(modifier = Modifier.height(SpacingM))
        Text(
            text      = title,
            style     = MaterialTheme.typography.headlineMedium,
            color     = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(SpacingXs))
        Text(
            text      = body,
            style     = MaterialTheme.typography.bodyLarge,
            color     = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        if (actionContent != null) {
            Spacer(modifier = Modifier.height(SpacingM))
            actionContent()
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun EmptyStatePreview() {
    MandarinLearnTheme {
        EmptyState(
            icon                   = Icons.Filled.Inbox,
            iconContentDescription = "No results",
            title                  = "No words match",
            body                   = "Try a different search term.",
        )
    }
}
