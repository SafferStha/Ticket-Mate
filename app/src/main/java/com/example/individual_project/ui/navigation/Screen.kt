package com.example.individual_project.ui.navigation

sealed class Screen(val route: String) {

    // ─── Auth flow ────────────────────────────────────────────────────────────
    object Splash         : Screen("splash")
    object Onboarding     : Screen("onboarding")
    object Login          : Screen("login")
    object Register       : Screen("register")
    object ForgotPassword : Screen("forgot_password")
    object VerifyEmail    : Screen("verify_email")

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

    // ─── Booking flow ─────────────────────────────────────────────────────────
    object Booking : Screen("booking/{eventId}") {
        fun createRoute(eventId: String) = "booking/$eventId"
    }

    // eventId + quantity passed so ConfirmationScreen can create the booking
    object BookingConfirmation : Screen("booking_confirmation/{eventId}/{quantity}") {
        fun createRoute(eventId: String, quantity: Int) =
            "booking_confirmation/$eventId/$quantity"
    }

    object BookingSuccess : Screen("booking_success/{bookingId}") {
        fun createRoute(bookingId: String) = "booking_success/$bookingId"
    }

    object MyBookings : Screen("my_bookings")

    // ─── Payment flow ─────────────────────────────────────────────────────────
    object Checkout : Screen("checkout/{bookingId}") {
        fun createRoute(bookingId: String) = "checkout/$bookingId"
    }

    object PaymentSuccess : Screen("payment_success/{paymentId}") {
        fun createRoute(paymentId: String) = "payment_success/$paymentId"
    }

    object PaymentFailure : Screen("payment_failed/{bookingId}") {
        fun createRoute(bookingId: String) = "payment_failed/$bookingId"
    }

    // ─── Profile flow ─────────────────────────────────────────────────────────────
    object EditProfile    : Screen("edit_profile")

    object TicketDetail   : Screen("ticket_detail/{ticketId}") {
        fun createRoute(ticketId: String) = "ticket_detail/$ticketId"
    }

    object BookingHistory : Screen("booking_history")
    object Favorites      : Screen("favorites")
    object PaymentHistory : Screen("payment_history")
    object Settings       : Screen("settings")

    // ─── Legacy (unused) ──────────────────────────────────────────────────────
    object Categories  : Screen("categories")
}
