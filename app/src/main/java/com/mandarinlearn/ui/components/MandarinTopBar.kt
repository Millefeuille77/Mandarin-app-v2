// MandarinTopBar.kt — Mandarin Learn
// Reusable top app bar component per UX_SPECIFICATION.md §3.1.
// Height 64 dp, surface color, 0 dp elevation.
// Back arrow and action icon both have 56 dp touch targets (CLAUDE.md hard rule).

package com.mandarinlearn.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mandarinlearn.R
import com.mandarinlearn.ui.theme.MandarinLearnTheme
import com.mandarinlearn.ui.theme.TopBarHeight

/**
 * Standard top bar used across all screens.
 *
 * @param title Screen title text.
 * @param onNavigateBack If non-null, shows a back arrow that calls this lambda (56 dp target).
 * @param actionIcon Optional trailing icon composable (must have 56 dp touch target).
 * @param modifier Modifier for the top bar container.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MandarinTopBar(
    title: String,
    modifier: Modifier = Modifier,
    onNavigateBack: (() -> Unit)? = null,
    actionIcon: @Composable (() -> Unit)? = null,
) {
    TopAppBar(
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        },
        navigationIcon = {
            if (onNavigateBack != null) {
                // 56 dp touch target per CLAUDE.md accessibility hard rule.
                IconButton(
                    onClick = onNavigateBack,
                    modifier = Modifier.size(56.dp),
                ) {
                    Icon(
                        imageVector = Icons.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.content_desc_navigate_back),
                        tint = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }
        },
        actions = {
            if (actionIcon != null) {
                Box(modifier = Modifier.size(56.dp)) {
                    actionIcon()
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface,
        ),
        modifier = modifier
            .fillMaxWidth()
            .height(TopBarHeight),
    )
}

@Preview(showBackground = true)
@Composable
private fun MandarinTopBarPreview() {
    MandarinLearnTheme {
        MandarinTopBar(
            title = "Vocabulary",
            onNavigateBack = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun MandarinTopBarNoBackPreview() {
    MandarinLearnTheme {
        MandarinTopBar(title = "Mandarin Learn")
    }
}
