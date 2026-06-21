package com.example.individual_project.ui.screens.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Help
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.individual_project.ui.components.ProfileAvatar
import com.example.individual_project.ui.navigation.Screen
import com.example.individual_project.ui.theme.Spacing
import com.example.individual_project.ui.theme.TmDarkBlue
import com.example.individual_project.ui.theme.TmError
import com.example.individual_project.ui.theme.TmLightBlue
import com.example.individual_project.ui.theme.TmNavyBlue

private data class SettingsItem(
    val icon     : ImageVector,
    val title    : String,
    val subtitle : String
)

private val settingsItems = listOf(
    SettingsItem(Icons.Default.Person,        "Personal Information", "Update your profile details"),
    SettingsItem(Icons.Default.Notifications, "Notifications",        "Manage your alert preferences"),
    SettingsItem(Icons.Default.CreditCard,    "Payment Methods",      "Add or remove payment options"),
    SettingsItem(Icons.Default.LocationOn,    "Saved Locations",      "Manage your favourite venues"),
    SettingsItem(Icons.Default.Security,      "Security & Privacy",   "Password and security settings"),
    SettingsItem(Icons.Default.Settings,      "App Preferences",      "Language, theme and more"),
    SettingsItem(Icons.Default.Help,          "Help & Support",       "Get help or contact us"),
    SettingsItem(Icons.Default.Info,          "About TicketMate",     "Version 1.0.0"),
)

@Composable
fun ProfileScreen(
    navController : NavController,
    userName      : String = "John Doe",
    userEmail     : String = "john.doe@email.com",
    userInitials  : String = "JD",
    eventsCount   : Int    = 12,
    ticketsCount  : Int    = 5,
    favoritesCount: Int    = 8
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // ── Profile header ─────────────────────────────────────────────────
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Brush.verticalGradient(listOf(TmNavyBlue, TmDarkBlue)))
                    .padding(
                        top    = Spacing.headerPaddingTop,
                        start  = Spacing.screenHorizontal,
                        end    = Spacing.screenHorizontal,
                        bottom = Spacing.xl
                    )
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier            = Modifier.fillMaxWidth()
                ) {
                    ProfileAvatar(
                        initials = userInitials,
                        size     = Spacing.avatarLg
                    )
                    Spacer(modifier = Modifier.height(Spacing.md))
                    Text(
                        text  = userName,
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(Spacing.xs))
                    Text(
                        text  = userEmail,
                        style = MaterialTheme.typography.bodySmall,
                        color = TmLightBlue
                    )
                    Spacer(modifier = Modifier.height(Spacing.lg))

                    // Stats
                    Row(horizontalArrangement = Arrangement.spacedBy(Spacing.lg)) {
                        StatBadge(value = "$eventsCount",  label = "Events")
                        Box(modifier = Modifier.size(width = 1.dp, height = 36.dp).background(Color.White.copy(0.3f)))
                        StatBadge(value = "$ticketsCount", label = "Tickets")
                        Box(modifier = Modifier.size(width = 1.dp, height = 36.dp).background(Color.White.copy(0.3f)))
                        StatBadge(value = "$favoritesCount", label = "Favorites")
                    }
                }
            }
        }

        // ── Settings list ─────────────────────────────────────────────────
        item {
            Column(modifier = Modifier.padding(Spacing.screenHorizontal)) {
                Spacer(modifier = Modifier.height(Spacing.md))
                Text(
                    text  = "SETTINGS",
                    style = MaterialTheme.typography.labelSmall.copy(
                        letterSpacing = androidx.compose.ui.unit.TextUnit(
                            1.5f,
                            androidx.compose.ui.unit.TextUnitType.Sp
                        )
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(Spacing.md))

                Card(
                    shape     = MaterialTheme.shapes.large,
                    elevation = CardDefaults.cardElevation(2.dp),
                    colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    settingsItems.forEachIndexed { index, item ->
                        Row(
                            modifier          = Modifier
                                .fillMaxWidth()
                                .clickable {}
                                .padding(horizontal = Spacing.cardPadding, vertical = Spacing.md),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(Spacing.thumbnailSize - Spacing.lg)
                                    .clip(MaterialTheme.shapes.medium)
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    item.icon, null,
                                    tint     = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(Spacing.iconMd)
                                )
                            }
                            Spacer(modifier = Modifier.width(Spacing.md))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text  = item.title,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text  = item.subtitle,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Icon(
                                Icons.Default.ChevronRight, null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        if (index < settingsItems.lastIndex) {
                            HorizontalDivider(
                                modifier = Modifier.padding(horizontal = Spacing.cardPadding),
                                color    = MaterialTheme.colorScheme.outlineVariant
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(Spacing.lg))

                // Sign out button
                OutlinedButton(
                    onClick  = {
                        navController.navigate(Screen.Login.route) {
                            popUpTo(Screen.Dashboard.route) { inclusive = true }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(Spacing.buttonHeight),
                    shape  = MaterialTheme.shapes.medium,
                    border = BorderStroke(1.dp, TmError)
                ) {
                    Icon(Icons.Default.Logout, null, tint = TmError)
                    Spacer(modifier = Modifier.width(Spacing.sm))
                    Text(
                        "Sign Out",
                        style = MaterialTheme.typography.labelLarge,
                        color = TmError
                    )
                }
                Spacer(modifier = Modifier.height(Spacing.lg))
            }
        }
    }
}

@Composable
private fun StatBadge(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text  = value,
            style = MaterialTheme.typography.titleMedium,
            color = Color.White
        )
        Text(
            text  = label,
            style = MaterialTheme.typography.bodySmall,
            color = TmLightBlue
        )
    }
}
