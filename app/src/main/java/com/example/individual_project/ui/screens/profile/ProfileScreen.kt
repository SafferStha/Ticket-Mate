package com.example.individual_project.ui.screens.profile

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BookOnline
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.individual_project.ui.components.ErrorView
import com.example.individual_project.ui.components.LoadingView
import com.example.individual_project.ui.components.ProfileAvatar
import com.example.individual_project.ui.navigation.Screen
import com.example.individual_project.ui.theme.Spacing
import com.example.individual_project.ui.theme.TmBlue
import com.example.individual_project.ui.theme.TmDarkBlue
import com.example.individual_project.ui.theme.TmError
import com.example.individual_project.ui.theme.TmLightBlue
import com.example.individual_project.ui.theme.TmNavyBlue
import com.example.individual_project.ui.viewmodel.AuthViewModel
import com.example.individual_project.ui.viewmodel.ProfileViewModel
import com.example.individual_project.utils.DateFormatter
import com.example.individual_project.utils.PriceFormatter

private data class QuickAction(
    val icon   : ImageVector,
    val label  : String,
    val route  : String
)

@Composable
fun ProfileScreen(
    navController    : NavController,
    profileViewModel : ProfileViewModel = hiltViewModel(),
    authViewModel    : AuthViewModel    = hiltViewModel()
) {
    val state     by profileViewModel.profileState.collectAsState()
    val authState by authViewModel.authState.collectAsState()
    val isAdmin   by authViewModel.isAdmin.collectAsState()

    // AuthGuard in NavGraph handles redirection after logout automatically.
    // This LaunchedEffect handles the one-shot channel event emitted by logout().
    LaunchedEffect(Unit) {
        authViewModel.logoutEvent.collect {
            navController.navigate(Screen.Login.route) {
                popUpTo(0) { inclusive = true }
            }
        }
    }

    // Re-fetch stats every time this destination resumes, so numbers stay accurate after
    // completing a booking/payment or (un)favoriting an event on a screen pushed on top of it.
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) profileViewModel.loadProfile()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val quickActions = buildList {
        add(QuickAction(Icons.Default.QrCode,     "My Tickets",      Screen.MyTickets.route))
        add(QuickAction(Icons.Default.BookOnline, "Booking History", Screen.BookingHistory.route))
        add(QuickAction(Icons.Default.Favorite,   "Favorites",       Screen.Favorites.route))
        add(QuickAction(Icons.Default.CreditCard, "Payment History", Screen.PaymentHistory.route))
        add(QuickAction(Icons.Default.Settings,   "Settings",        Screen.Settings.route))
        if (isAdmin) {
            add(QuickAction(Icons.Default.Edit, "Admin · Manage Events", Screen.AdminDashboard.route))
        }
    }

    when {
        state.isLoading -> LoadingView()
        state.error != null && state.user == null -> ErrorView(
            message = state.error!!,
            onRetry = { profileViewModel.loadProfile() }
        )
        else -> {
            val user      = state.user
            val name      = user?.name?.ifBlank { "User" }      ?: "User"
            val email     = user?.email?.ifBlank { "" }          ?: ""
            val imageUrl  = user?.profileImage?.ifBlank { null } ?: null
            val initials  = name.split(" ")
                .take(2)
                .mapNotNull { it.firstOrNull()?.uppercaseChar() }
                .joinToString("")
                .ifBlank { "U" }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
            ) {
                // ── Header ────────────────────────────────────────────────────
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
                            // Profile image or initials avatar
                            Box(
                                modifier         = Modifier.size(Spacing.avatarLg),
                                contentAlignment = Alignment.Center
                            ) {
                                if (imageUrl != null) {
                                    AsyncImage(
                                        model             = imageUrl,
                                        contentDescription = "Profile picture",
                                        modifier          = Modifier
                                            .size(Spacing.avatarLg)
                                            .clip(CircleShape),
                                        contentScale      = ContentScale.Crop
                                    )
                                } else {
                                    ProfileAvatar(
                                        initials = initials,
                                        size     = Spacing.avatarLg
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(Spacing.sm))

                            // Edit profile button
                            Box(
                                modifier = Modifier
                                    .clip(MaterialTheme.shapes.small)
                                    .background(Color.White.copy(alpha = 0.15f))
                                    .clickable {
                                        navController.navigate(Screen.EditProfile.route)
                                    }
                                    .padding(horizontal = Spacing.sm, vertical = Spacing.xs)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.Edit, null,
                                        tint     = Color.White,
                                        modifier = Modifier.size(Spacing.iconSm)
                                    )
                                    Spacer(modifier = Modifier.width(Spacing.xs))
                                    Text(
                                        text  = "Edit Profile",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.White
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(Spacing.sm))

                            Text(
                                text       = name,
                                style      = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color      = Color.White
                            )
                            Spacer(modifier = Modifier.height(Spacing.xs))
                            Text(
                                text  = email,
                                style = MaterialTheme.typography.bodySmall,
                                color = TmLightBlue
                            )
                            if (user != null && user.createdAt > 0L) {
                                Spacer(modifier = Modifier.height(Spacing.xs))
                                Text(
                                    text  = "Member since ${DateFormatter.formatMemberSince(user.createdAt)}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TmLightBlue
                                )
                            }

                            Spacer(modifier = Modifier.height(Spacing.lg))

                            // ── Stats ───────────────────────────────────────
                            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.md)) {
                                StatBadge(value = "${state.ticketCount}",   label = "Tickets")
                                StatDivider()
                                StatBadge(value = "${state.totalBookings}", label = "Bookings")
                                StatDivider()
                                StatBadge(value = "${state.favoriteCount}", label = "Favorites")
                                StatDivider()
                                StatBadge(value = PriceFormatter.formatShort(state.totalSpent), label = "Spent")
                            }
                        }
                    }
                }

                // ── Quick actions ─────────────────────────────────────────────
                item {
                    Column(modifier = Modifier.padding(Spacing.screenHorizontal)) {
                        Spacer(modifier = Modifier.height(Spacing.md))
                        Text(
                            text  = "QUICK ACTIONS",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(Spacing.md))

                        Card(
                            shape     = MaterialTheme.shapes.large,
                            elevation = CardDefaults.cardElevation(2.dp),
                            colors    = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            )
                        ) {
                            quickActions.forEachIndexed { index, action ->
                                QuickActionRow(
                                    icon    = action.icon,
                                    label   = action.label,
                                    onClick = { navController.navigate(action.route) }
                                )
                                if (index < quickActions.lastIndex) {
                                    HorizontalDivider(
                                        modifier = Modifier.padding(horizontal = Spacing.cardPadding),
                                        color    = MaterialTheme.colorScheme.outlineVariant
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(Spacing.md))

                        // ── Logout ─────────────────────────────────────────
                        Card(
                            shape     = MaterialTheme.shapes.large,
                            elevation = CardDefaults.cardElevation(2.dp),
                            colors    = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            )
                        ) {
                            QuickActionRow(
                                icon       = Icons.Default.Logout,
                                label      = "Sign Out",
                                tint       = TmError,
                                labelColor = TmError,
                                onClick    = { authViewModel.logout() }
                            )
                        }

                        Spacer(modifier = Modifier.height(Spacing.xxl))
                    }
                }
            }
        }
    }
}

@Composable
private fun QuickActionRow(
    icon       : ImageVector,
    label      : String,
    tint       : Color       = TmBlue,
    labelColor : Color       = Color.Unspecified,
    onClick    : () -> Unit
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
                .background(tint.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                icon, null,
                tint     = tint,
                modifier = Modifier.size(Spacing.iconMd)
            )
        }
        Spacer(modifier = Modifier.width(Spacing.md))
        Text(
            text     = label,
            style    = MaterialTheme.typography.bodyMedium,
            color    = if (labelColor == Color.Unspecified)
                           MaterialTheme.colorScheme.onSurface
                       else labelColor,
            modifier = Modifier.weight(1f)
        )
        Icon(
            Icons.Default.ChevronRight, null,
            tint     = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(Spacing.iconLg)
        )
    }
}

@Composable
private fun StatDivider() {
    Box(
        modifier = Modifier
            .size(width = 1.dp, height = 36.dp)
            .background(Color.White.copy(alpha = 0.3f))
    )
}

@Composable
private fun StatBadge(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text       = value,
            style      = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color      = Color.White
        )
        Text(
            text  = label,
            style = MaterialTheme.typography.bodySmall,
            color = TmLightBlue
        )
    }
}
