package com.example.individual_project.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.individual_project.screens.DashboardScreen
import com.example.individual_project.screens.ForgotPasswordScreen
import com.example.individual_project.screens.LoginScreen
import com.example.individual_project.screens.RegisterScreen
import com.example.individual_project.screens.SplashScreen

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
    }
}
