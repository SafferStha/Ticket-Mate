package com.example.individual_project.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.individual_project.auth.AuthState
import com.example.individual_project.ui.screens.DashboardScreen
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
import com.example.individual_project.ui.viewmodel.AuthViewModel

@Composable
fun NavGraph(navController: NavHostController) {
    NavHost(
        navController    = navController,
        startDestination = Screen.Splash.route
    ) {
        // ── Auth flow ────────────────────────────────────────────────────────
        composable(Screen.Splash.route)         { SplashScreen(navController) }
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
                BookingConfirmationScreen(
                    navController = navController,
                    eventId       = eventId,
                    quantity      = quantity
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
