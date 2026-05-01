// MandarinBottomNav.kt — Mandarin Learn
// Bottom navigation bar with 4 tabs: Learn / Practice / Exam / Me.
// UX_SPECIFICATION.md §1.6 and §3.2: height 80 dp, icon 28 dp, label always visible.
// Each item has a contentDescription for TalkBack.

package com.mandarinlearn.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mandarinlearn.R
import com.mandarinlearn.navigation.Routes
import com.mandarinlearn.ui.theme.MandarinLearnTheme

/**
 * Bottom navigation tab descriptor.
 *
 * @param route The navigation route this tab maps to (must be a tab root from [Routes]).
 * @param labelResId String resource ID for the tab label.
 * @param icon Material icon for the tab.
 * @param contentDescResId String resource ID for the icon's contentDescription (TalkBack).
 */
data class BottomNavItem(
    val route: String,
    val labelResId: Int,
    val icon: ImageVector,
    val contentDescResId: Int,
)

/** The 4 bottom-nav tabs in order (UX spec §1.6). */
val bottomNavItems = listOf(
    BottomNavItem(
        route             = Routes.HOME,
        labelResId        = R.string.nav_learn,
        icon              = Icons.Filled.Home,
        contentDescResId  = R.string.content_desc_nav_learn,
    ),
    BottomNavItem(
        route             = Routes.PRACTICE,
        labelResId        = R.string.nav_practice,
        icon              = Icons.Filled.Book,
        contentDescResId  = R.string.content_desc_nav_practice,
    ),
    BottomNavItem(
        route             = Routes.EXAM_HUB,
        labelResId        = R.string.nav_exam,
        icon              = Icons.Filled.Edit,
        contentDescResId  = R.string.content_desc_nav_exam,
    ),
    BottomNavItem(
        route             = Routes.ME,
        labelResId        = R.string.nav_me,
        icon              = Icons.Filled.Person,
        contentDescResId  = R.string.content_desc_nav_me,
    ),
)

/**
 * Bottom navigation bar composable.
 *
 * @param selectedRoute The currently-selected tab route.
 * @param onTabSelected Called when the user taps a tab.
 * @param modifier Optional modifier.
 */
@Composable
fun MandarinBottomNav(
    selectedRoute: String,
    onTabSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    NavigationBar(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 3.dp,       // UX spec §1.4: Bottom nav 3 dp elevation
    ) {
        bottomNavItems.forEach { item ->
            val selected = selectedRoute == item.route
            NavigationBarItem(
                selected = selected,
                onClick  = { onTabSelected(item.route) },
                icon = {
                    Icon(
                        imageVector        = item.icon,
                        contentDescription = stringResource(item.contentDescResId),
                        // UX spec §3.2: icon 28 dp, selected = primary, unselected = onSurfaceVariant
                        modifier           = Modifier,
                    )
                },
                label = {
                    Text(
                        text  = stringResource(item.labelResId),
                        style = MaterialTheme.typography.labelLarge,
                    )
                },
                alwaysShowLabel = true,   // UX spec §1.6: label always visible
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor   = MaterialTheme.colorScheme.primary,
                    selectedTextColor   = MaterialTheme.colorScheme.primary,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    indicatorColor      = MaterialTheme.colorScheme.primaryContainer,
                ),
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun MandarinBottomNavPreview() {
    MandarinLearnTheme {
        MandarinBottomNav(
            selectedRoute = Routes.HOME,
            onTabSelected = {},
        )
    }
}
