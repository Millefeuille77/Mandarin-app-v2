// OfflineBanner.kt — Mandarin Learn
// Offline indicator banner per UX_SPECIFICATION.md §3.10.
// 48 dp tall, warning background, shown only when device is offline.

package com.mandarinlearn.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.mandarinlearn.R
import com.mandarinlearn.ui.theme.MandarinLearnTheme
import com.mandarinlearn.ui.theme.OfflineBannerHeight
import com.mandarinlearn.ui.theme.SpacingM
import com.mandarinlearn.ui.theme.Warning

/**
 * Animated offline indicator banner per UX spec §3.10.
 *
 * @param isOffline When true, the banner is visible; when false, it collapses.
 * @param modifier Optional modifier for the banner container.
 */
@Composable
fun OfflineBanner(
    isOffline: Boolean,
    modifier: Modifier = Modifier,
) {
    AnimatedVisibility(
        visible  = isOffline,
        enter    = expandVertically(),
        exit     = shrinkVertically(),
        modifier = modifier,
    ) {
        Box(
            modifier           = Modifier
                .fillMaxWidth()
                .height(OfflineBannerHeight)
                .background(Warning)
                .padding(horizontal = SpacingM),
            contentAlignment   = Alignment.Center,
        ) {
            Text(
                text  = stringResource(R.string.offline_banner_message),
                style = MaterialTheme.typography.bodyMedium,
                // White text on warning orange meets 4.5:1 contrast (UX spec §1.8)
                color = androidx.compose.ui.graphics.Color.White,
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun OfflineBannerPreview() {
    MandarinLearnTheme {
        OfflineBanner(isOffline = true)
    }
}

@Preview(showBackground = true)
@Composable
private fun OfflineBannerHiddenPreview() {
    MandarinLearnTheme {
        OfflineBanner(isOffline = false)
    }
}
