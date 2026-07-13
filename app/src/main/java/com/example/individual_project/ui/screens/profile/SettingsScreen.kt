package com.example.individual_project.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Policy
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.individual_project.ui.navigation.Screen
import com.example.individual_project.ui.theme.Spacing
import com.example.individual_project.ui.theme.TmBlue
import com.example.individual_project.ui.theme.TmError
import com.example.individual_project.ui.theme.TmNavyBlue
import com.example.individual_project.ui.viewmodel.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController,
    authViewModel: AuthViewModel = hiltViewModel()
) {
    var notificationsEnabled by remember { mutableStateOf(true) }
    var darkModeEnabled      by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        authViewModel.logoutEvent.collect {
            navController.navigate(Screen.Login.route) {
                popUpTo(0) { inclusive = true }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor             = TmNavyBlue,
                    titleContentColor          = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // ── Preferences ───────────────────────────────────────────────────
            item {
                Column(modifier = Modifier.padding(Spacing.screenHorizontal)) {
                    Spacer(modifier = Modifier.height(Spacing.md))
                    SettingsSectionLabel("PREFERENCES")
                    Spacer(modifier = Modifier.height(Spacing.sm))

                    Card(
                        shape     = MaterialTheme.shapes.large,
                        elevation = CardDefaults.cardElevation(2.dp),
                        colors    = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    ) {
                        SettingsToggleRow(
                            icon    = Icons.Default.Notifications,
                            label   = "Push Notifications",
                            checked = notificationsEnabled,
                            onToggle = { notificationsEnabled = it }
                        )
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = Spacing.cardPadding),
                            color    = MaterialTheme.colorScheme.outlineVariant
                        )
                        SettingsToggleRow(
                            icon    = Icons.Default.DarkMode,
                            label   = "Dark Mode",
                            checked = darkModeEnabled,
                            onToggle = { darkModeEnabled = it }
                        )
                    }
                }
            }

            // ── About ─────────────────────────────────────────────────────────
            item {
                Column(modifier = Modifier.padding(Spacing.screenHorizontal)) {
                    Spacer(modifier = Modifier.height(Spacing.lg))
                    SettingsSectionLabel("ABOUT")
                    Spacer(modifier = Modifier.height(Spacing.sm))

                    Card(
                        shape     = MaterialTheme.shapes.large,
                        elevation = CardDefaults.cardElevation(2.dp),
                        colors    = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    ) {
                        SettingsClickableRow(
                            icon  = Icons.Default.Policy,
                            label = "Privacy Policy",
                            value = null,
                            onClick = {}
                        )
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = Spacing.cardPadding),
                            color    = MaterialTheme.colorScheme.outlineVariant
                        )
                        SettingsClickableRow(
                            icon    = Icons.Default.Info,
                            label   = "App Version",
                            value   = "1.0.0",
                            onClick = {}
                        )
                    }
                }
            }

            // ── Account ───────────────────────────────────────────────────────
            item {
                Column(modifier = Modifier.padding(Spacing.screenHorizontal)) {
                    Spacer(modifier = Modifier.height(Spacing.lg))
                    SettingsSectionLabel("ACCOUNT")
                    Spacer(modifier = Modifier.height(Spacing.sm))

                    Card(
                        shape     = MaterialTheme.shapes.large,
                        elevation = CardDefaults.cardElevation(2.dp),
                        colors    = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Row(
                            modifier          = Modifier
                                .fillMaxWidth()
                                .clickable { authViewModel.logout() }
                                .padding(horizontal = Spacing.cardPadding, vertical = Spacing.md),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(Spacing.thumbnailSize - Spacing.lg)
                                    .clip(MaterialTheme.shapes.medium)
                                    .background(TmError.copy(alpha = 0.1f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.Logout, null,
                                    tint     = TmError,
                                    modifier = Modifier.size(Spacing.iconMd)
                                )
                            }
                            Text(
                                text       = "Sign Out",
                                style      = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                color      = TmError,
                                modifier   = Modifier.padding(start = Spacing.md)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(Spacing.xxl))
                }
            }
        }
    }
}

@Composable
private fun SettingsSectionLabel(label: String) {
    Text(
        text  = label,
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

@Composable
private fun SettingsToggleRow(
    icon    : ImageVector,
    label   : String,
    checked : Boolean,
    onToggle: (Boolean) -> Unit
) {
    Row(
        modifier          = Modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.cardPadding, vertical = Spacing.sm),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(Spacing.thumbnailSize - Spacing.lg)
                .clip(MaterialTheme.shapes.medium)
                .background(TmBlue.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                icon, null,
                tint     = TmBlue,
                modifier = Modifier.size(Spacing.iconMd)
            )
        }
        Text(
            text     = label,
            style    = MaterialTheme.typography.bodyMedium,
            modifier = Modifier
                .weight(1f)
                .padding(start = Spacing.md)
        )
        Switch(
            checked         = checked,
            onCheckedChange = onToggle,
            colors          = SwitchDefaults.colors(
                checkedThumbColor  = Color.White,
                checkedTrackColor  = TmBlue
            )
        )
    }
}

@Composable
private fun SettingsClickableRow(
    icon   : ImageVector,
    label  : String,
    value  : String?,
    onClick: () -> Unit
) {
    Row(
        modifier          = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = Spacing.cardPadding, vertical = Spacing.md),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(Spacing.thumbnailSize - Spacing.lg)
                .clip(MaterialTheme.shapes.medium)
                .background(TmBlue.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                icon, null,
                tint     = TmBlue,
                modifier = Modifier.size(Spacing.iconMd)
            )
        }
        Text(
            text     = label,
            style    = MaterialTheme.typography.bodyMedium,
            modifier = Modifier
                .weight(1f)
                .padding(start = Spacing.md)
        )
        if (value != null) {
            Text(
                text  = value,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
