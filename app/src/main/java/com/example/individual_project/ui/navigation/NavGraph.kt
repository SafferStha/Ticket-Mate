package com.example.individual_project.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.individual_project.ui.screens.DashboardScreen
import com.example.individual_project.ui.screens.ForgotPasswordScreen
import com.example.individual_project.ui.screens.LoginScreen
import com.example.individual_project.ui.screens.RegisterScreen
import com.example.individual_project.ui.screens.SplashScreen
import com.example.individual_project.ui.screens.home.EventDetailScreen

@Composable
fun NavGraph(navController: NavHostController) {
    NavHost(
        navController    = navController,
        startDestination = Screen.Splash.route
    ) {
        // ── Auth flow ──────────────────────────────────────────────────────
        composable(Screen.Splash.route)         { SplashScreen(navController) }
        composable(Screen.Login.route)          { LoginScreen(navController) }
        composable(Screen.Register.route)       { RegisterScreen(navController) }
        composable(Screen.ForgotPassword.route) { ForgotPasswordScreen(navController) }

        // ── Main app ───────────────────────────────────────────────────────
        composable(Screen.Dashboard.route) { DashboardScreen(navController) }

        // ── Event detail ───────────────────────────────────────────────────
        composable(
            route     = Screen.EventDetail.route,
            arguments = listOf(navArgument("eventId") { type = NavType.StringType })
        ) {
            EventDetailScreen(navController = navController)
        }
    }
}
