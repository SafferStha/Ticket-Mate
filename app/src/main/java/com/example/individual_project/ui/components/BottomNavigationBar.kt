package com.example.individual_project.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

private data class NavDestination(val icon: ImageVector, val label: String)

private val destinations = listOf(
    NavDestination(Icons.Default.Home,               "Home"),
    NavDestination(Icons.Default.Search,             "Search"),
    NavDestination(Icons.Default.ConfirmationNumber, "Tickets"),
    NavDestination(Icons.Default.Person,             "Profile"),
)

@Composable
fun TmBottomNavigationBar(
    selectedTab   : Int,
    onTabSelected : (Int) -> Unit
) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp
    ) {
        destinations.forEachIndexed { index, dest ->
            NavigationBarItem(
                selected = selectedTab == index,
                onClick  = { onTabSelected(index) },
                icon     = {
                    Icon(
                        imageVector        = dest.icon,
                        contentDescription = dest.label
                    )
                },
                label  = {
                    Text(
                        text  = dest.label,
                        style = MaterialTheme.typography.labelSmall
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor   = MaterialTheme.colorScheme.primary,
                    selectedTextColor   = MaterialTheme.colorScheme.primary,
                    indicatorColor      = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
    }
}
