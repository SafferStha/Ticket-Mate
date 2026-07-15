package com.example.individual_project.ui.navigation

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.individual_project.auth.AuthState
import com.example.individual_project.ui.screens.DashboardScreen
import com.example.individual_project.ui.screens.admin.AdminDashboardScreen
import com.example.individual_project.ui.screens.admin.AdminEventFormScreen
import com.example.individual_project.ui.screens.ForgotPasswordScreen
import com.example.individual_project.ui.screens.LoginScreen
import com.example.individual_project.ui.screens.RegisterScreen
import com.example.individual_project.ui.screens.SplashScreen
import com.example.individual_project.ui.screens.VerifyEmailScreen
import com.example.individual_project.ui.screens.booking.BookingConfirmationScreen
import com.example.individual_project.ui.screens.booking.BookingScreen
import com.example.individual_project.ui.screens.booking.BookingSuccessScreen
import com.example.individual_project.ui.screens.booking.MyBookingsScreen
import com.example.individual_project.ui.screens.home.EventDetailScreen
import com.example.individual_project.ui.screens.payment.CheckoutScreen
import com.example.individual_project.ui.screens.payment.PaymentFailureScreen
import com.example.individual_project.ui.screens.payment.PaymentSuccessScreen
import com.example.individual_project.ui.screens.profile.BookingHistoryScreen
import com.example.individual_project.ui.screens.profile.ChangePasswordScreen
import com.example.individual_project.ui.screens.profile.EditProfileScreen
import com.example.individual_project.ui.screens.profile.FavoritesScreen
import com.example.individual_project.ui.screens.profile.LegalPlaceholderScreen
import com.example.individual_project.ui.screens.profile.MyTicketsScreen
import com.example.individual_project.ui.screens.profile.PaymentHistoryScreen
import com.example.individual_project.ui.screens.profile.SavedLocationsScreen
import com.example.individual_project.ui.screens.profile.SavedPaymentMethodsScreen
import com.example.individual_project.ui.screens.profile.SettingsScreen
import com.example.individual_project.ui.screens.profile.TicketDetailScreen
import com.example.individual_project.ui.viewmodel.AuthViewModel

private const val TRANSITION_DURATION = 300
private const val FADE_EXIT_DURATION  = 150

@Composable
fun NavGraph(
    navController         : NavHostController,
    pendingDeepLinkEventId: String? = null
) {
    NavHost(
        navController      = navController,
        startDestination   = Screen.Splash.route,
        enterTransition    = {
            slideInHorizontally(
                initialOffsetX = { it },
                animationSpec  = tween(TRANSITION_DURATION, easing = FastOutSlowInEasing)
            ) + fadeIn(tween(TRANSITION_DURATION))
        },
        exitTransition     = {
            slideOutHorizontally(
                targetOffsetX = { -it / 3 },
                animationSpec = tween(TRANSITION_DURATION, easing = FastOutSlowInEasing)
            ) + fadeOut(tween(FADE_EXIT_DURATION))
        },
        popEnterTransition = {
            slideInHorizontally(
                initialOffsetX = { -it / 3 },
                animationSpec  = tween(TRANSITION_DURATION, easing = FastOutSlowInEasing)
            ) + fadeIn(tween(TRANSITION_DURATION))
        },
        popExitTransition  = {
            slideOutHorizontally(
                targetOffsetX = { it },
                animationSpec = tween(TRANSITION_DURATION, easing = FastOutSlowInEasing)
            ) + fadeOut(tween(FADE_EXIT_DURATION))
        }
    ) {
        // ── Auth flow ────────────────────────────────────────────────────────
        composable(Screen.Splash.route) {
            SplashScreen(navController, pendingDeepLinkEventId = pendingDeepLinkEventId)
        }
        composable(Screen.Login.route)          { LoginScreen(navController) }
        composable(Screen.Register.route)       { RegisterScreen(navController) }
        composable(Screen.ForgotPassword.route) { ForgotPasswordScreen(navController) }
        composable(Screen.VerifyEmail.route)    { VerifyEmailScreen(navController) }

        // ── Protected: Main app ──────────────────────────────────────────────
        composable(Screen.Dashboard.route) {
            AuthGuard(navController) {
                DashboardScreen(navController)
            }
        }

        // ── Protected: Event detail ──────────────────────────────────────────
        composable(
            route     = Screen.EventDetail.route,
            arguments = listOf(navArgument("eventId") { type = NavType.StringType })
        ) {
            AuthGuard(navController) {
                EventDetailScreen(navController = navController)
            }
        }

        // ── Booking flow ─────────────────────────────────────────────────────
        composable(
            route     = Screen.Booking.route,
            arguments = listOf(navArgument("eventId") { type = NavType.StringType })
        ) {
            AuthGuard(navController) {
                BookingScreen(navController = navController)
            }
        }

        composable(
            route     = Screen.BookingConfirmation.route,
            arguments = listOf(
                navArgument("eventId")  { type = NavType.StringType },
                navArgument("quantity") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            AuthGuard(navController) {
                val eventId  = backStackEntry.arguments?.getString("eventId") ?: ""
                val quantity = backStackEntry.arguments?.getInt("quantity") ?: 1

                // Share the same ViewModel instance scoped to the booking/{eventId} back-stack entry.
                // We use remember here to safely cache the entry before it might be popped during transitions,
                // avoiding "No destination with route booking/{eventId}" crashes.
                val bookingEntry = remember(backStackEntry) {
                    navController.getBackStackEntry(Screen.Booking.route)
                }

                BookingConfirmationScreen(
                    navController = navController,
                    eventId       = eventId,
                    quantity      = quantity,
                    viewModel     = hiltViewModel(bookingEntry)
                )
            }
        }

        composable(
            route     = Screen.BookingSuccess.route,
            arguments = listOf(navArgument("bookingId") { type = NavType.StringType })
        ) {
            AuthGuard(navController) {
                BookingSuccessScreen(navController = navController)
            }
        }

        composable(Screen.MyBookings.route) {
            AuthGuard(navController) {
                MyBookingsScreen(navController = navController)
            }
        }

        // ── Payment flow ──────────────────────────────────────────────────────
        composable(
            route     = Screen.Checkout.route,
            arguments = listOf(navArgument("bookingId") { type = NavType.StringType })
        ) {
            AuthGuard(navController) {
                CheckoutScreen(navController = navController)
            }
        }

        composable(
            route     = Screen.PaymentSuccess.route,
            arguments = listOf(navArgument("paymentId") { type = NavType.StringType })
        ) {
            AuthGuard(navController) {
                PaymentSuccessScreen(navController = navController)
            }
        }

        composable(
            route     = Screen.PaymentFailure.route,
            arguments = listOf(navArgument("bookingId") { type = NavType.StringType })
        ) { backStackEntry ->
            AuthGuard(navController) {
                val bookingId = backStackEntry.arguments?.getString("bookingId") ?: ""
                PaymentFailureScreen(navController = navController, bookingId = bookingId)
            }
        }

        // ── Profile flow ──────────────────────────────────────────────────────
        composable(Screen.EditProfile.route) {
            AuthGuard(navController) {
                EditProfileScreen(navController = navController)
            }
        }

        composable(Screen.ChangePassword.route) {
            AuthGuard(navController) {
                ChangePasswordScreen(navController = navController)
            }
        }

        composable(Screen.MyTickets.route) {
            AuthGuard(navController) {
                MyTicketsScreen(navController = navController)
            }
        }

        composable(
            route     = Screen.TicketDetail.route,
            arguments = listOf(navArgument("ticketId") { type = NavType.StringType })
        ) {
            AuthGuard(navController) {
                TicketDetailScreen(navController = navController)
            }
        }

        composable(Screen.BookingHistory.route) {
            AuthGuard(navController) {
                BookingHistoryScreen(navController = navController)
            }
        }

        composable(Screen.Favorites.route) {
            AuthGuard(navController) {
                FavoritesScreen(navController = navController)
            }
        }

        composable(Screen.PaymentHistory.route) {
            AuthGuard(navController) {
                PaymentHistoryScreen(navController = navController)
            }
        }

        composable(Screen.Settings.route) {
            AuthGuard(navController) {
                SettingsScreen(navController = navController)
            }
        }

        composable(Screen.PrivacyPolicy.route) {
            AuthGuard(navController) {
                LegalPlaceholderScreen(navController = navController, title = "Privacy Policy")
            }
        }

        composable(Screen.SavedLocations.route) {
            AuthGuard(navController) {
                SavedLocationsScreen(navController = navController)
            }
        }

        composable(Screen.SavedPaymentMethods.route) {
            AuthGuard(navController) {
                SavedPaymentMethodsScreen(navController = navController)
            }
        }

        // ── Admin flow ───────────────────────────────────────────────────────
        composable(Screen.AdminDashboard.route) {
            AdminGuard(navController) {
                AdminDashboardScreen(navController = navController)
            }
        }

        composable(
            route     = Screen.AdminEventForm.route,
            arguments = listOf(navArgument("eventId") {
                type     = NavType.StringType
                nullable = true
            })
        ) {
            AdminGuard(navController) {
                AdminEventFormScreen(navController = navController)
            }
        }
    }
}

/**
 * Route guard for authenticated-only screens.
 *
 * Observes [AuthState] from the [AuthStateManager] singleton (via [AuthViewModel]).
 * - [AuthState.Loading]          → renders nothing until the state resolves
 * - [AuthState.Authenticated]    → renders [content]
 * - [AuthState.EmailNotVerified] → redirects to VerifyEmail, clears back stack
 * - [AuthState.Unauthenticated]  → redirects to Login, clears back stack
 *
 * Because [AuthStateManager] is backed by [FirebaseAuth.AuthStateListener], this guard
 * responds automatically to token expiry and logout without any manual polling.
 */
@Composable
private fun AuthGuard(
    navController: NavController,
    viewModel    : AuthViewModel = hiltViewModel(),
    content      : @Composable () -> Unit
) {
    val authState by viewModel.authState.collectAsState()

    LaunchedEffect(authState) {
        when (authState) {
            AuthState.Unauthenticated -> navController.navigate(Screen.Login.route) {
                popUpTo(0) { inclusive = true }
            }
            AuthState.EmailNotVerified -> navController.navigate(Screen.VerifyEmail.route) {
                popUpTo(0) { inclusive = true }
            }
            else -> Unit
        }
    }

    if (authState == AuthState.Authenticated) {
        content()
    }
}

/**
 * Route guard for admin-only screens. Requires both authentication AND the `admin` Firebase
 * Auth custom claim (see AdminStateManager). This is a UI convenience, not the actual security
 * boundary -- a user who bypasses this guard (or calls the repository directly) is still
 * rejected by database.rules.json, which independently checks the same claim server-side. A
 * non-admin who reaches this route (e.g. by deep link) is redirected to Dashboard, not shown
 * any admin content or even a flash of it.
 */
@Composable
private fun AdminGuard(
    navController: NavController,
    viewModel    : AuthViewModel = hiltViewModel(),
    content      : @Composable () -> Unit
) {
    val authState by viewModel.authState.collectAsState()
    val isAdmin    by viewModel.isAdmin.collectAsState()

    LaunchedEffect(authState, isAdmin) {
        when {
            authState == AuthState.Unauthenticated -> navController.navigate(Screen.Login.route) {
                popUpTo(0) { inclusive = true }
            }
            authState == AuthState.EmailNotVerified -> navController.navigate(Screen.VerifyEmail.route) {
                popUpTo(0) { inclusive = true }
            }
            authState == AuthState.Authenticated && !isAdmin -> navController.navigate(Screen.Dashboard.route) {
                popUpTo(0) { inclusive = true }
            }
            else -> Unit
        }
    }

    if (authState == AuthState.Authenticated && isAdmin) {
        content()
    }
}
