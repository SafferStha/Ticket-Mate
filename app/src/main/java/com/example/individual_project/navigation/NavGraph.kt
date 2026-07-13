package com.example.individual_project.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.individual_project.screens.ChangePasswordScreen
import com.example.individual_project.screens.DashboardScreen
import com.example.individual_project.screens.ForgotPasswordScreen
import com.example.individual_project.screens.LoginScreen
import com.example.individual_project.screens.OtpVerificationScreen
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
        
        composable(
            route = "otp_verification/{email}",
            arguments = listOf(navArgument("email") { type = NavType.StringType })
        ) { backStackEntry ->
            val email = backStackEntry.arguments?.getString("email") ?: ""
            OtpVerificationScreen(
                navController = navController,
                email = email,
                onVerificationSuccess = {
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }
        
        composable(Screen.ChangePassword.route) { ChangePasswordScreen(navController) }
    }
}
