package com.example.individual_project.ui.navigation

sealed class Screen(val route: String) {

    // ─── Auth flow ────────────────────────────────────────────────────────────
    object Splash         : Screen("splash")
    object Onboarding     : Screen("onboarding")
    object Login          : Screen("login")
    object Register       : Screen("register")
    object ForgotPassword : Screen("forgot_password")

    // ─── Main app (inside bottom nav) ─────────────────────────────────────────
    object Dashboard      : Screen("dashboard")     // hosts bottom nav tabs
    object Home           : Screen("home")
    object Search         : Screen("search")
    object MyTickets      : Screen("my_tickets")
    object Profile        : Screen("profile")

    // ─── Detail / flow screens ────────────────────────────────────────────────
    object EventDetail    : Screen("event_detail/{eventId}") {
        fun createRoute(eventId: String) = "event_detail/$eventId"
    }
    object Booking        : Screen("booking/{eventId}") {
        fun createRoute(eventId: String) = "booking/$eventId"
    }
    object Checkout       : Screen("checkout/{bookingId}") {
        fun createRoute(bookingId: String) = "checkout/$bookingId"
    }
    object Categories     : Screen("categories")
}
