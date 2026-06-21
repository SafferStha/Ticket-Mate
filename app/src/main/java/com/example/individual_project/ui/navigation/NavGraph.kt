package com.example.individual_project.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.individual_project.ui.screens.DashboardScreen
import com.example.individual_project.ui.screens.ForgotPasswordScreen
import com.example.individual_project.ui.screens.LoginScreen
import com.example.individual_project.ui.screens.RegisterScreen
import com.example.individual_project.ui.screens.SplashScreen

@Composable
fun NavGraph(navController: NavHostController) {
    NavHost(
        navController    = navController,
        startDestination = Screen.Splash.route
    ) {
        composable(Screen.Splash.route)         { SplashScreen(navController) }
        composable(Screen.Login.route)          { LoginScreen(navController) }
        composable(Screen.Register.route)       { RegisterScreen(navController) }
        composable(Screen.ForgotPassword.route) { ForgotPasswordScreen(navController) }
        composable(Screen.Dashboard.route)      { DashboardScreen(navController) }

        // Placeholders – wired in Phase 5 when ViewModels are ready
        // composable(Screen.Onboarding.route)   { OnboardingScreen(navController) }
        // composable(Screen.EventDetail.route)  { EventDetailScreen(navController, it.arguments?.getString("eventId")) }
        // composable(Screen.Booking.route)      { BookingScreen(navController, it.arguments?.getString("eventId")) }
        // composable(Screen.Checkout.route)     { CheckoutScreen(navController, it.arguments?.getString("bookingId")) }
        // composable(Screen.Categories.route)   { CategoriesScreen(navController) }
    }
}
