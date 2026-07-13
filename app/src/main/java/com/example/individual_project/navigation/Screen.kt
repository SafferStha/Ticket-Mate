package com.example.individual_project.navigation

sealed class Screen(val route: String) {
    object Splash        : Screen("splash")
    object Login         : Screen("login")
    object Register      : Screen("register")
    object ForgotPassword: Screen("forgot_password")
    object Dashboard     : Screen("dashboard")
}
