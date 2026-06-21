package com.example.individual_project.ui.model

/**
 * Static data used ONLY by MyTicketsScreen until Phase 4 wires real Booking/Ticket data.
 * Do NOT use for HomeScreen or SearchScreen — those consume live Firebase data via ViewModels.
 */

val sampleUpcomingTickets = listOf(
    Triple("🎤", "Taylor Swift | The Eras Tour", "Mar 15, 2025 • 7:00 PM\nSoFi Stadium, Los Angeles, CA"),
    Triple("🏀", "NBA Finals 2025",              "Jun 5, 2025 • 8:00 PM\nChase Center, San Francisco, CA"),
)

val samplePastTickets = listOf(
    Triple("🎭", "Hamilton – The Musical",     "Jan 20, 2025 • 7:30 PM\nPantages Theatre, Hollywood, CA"),
    Triple("😂", "Kevin Hart: Reality Check", "Dec 15, 2024 • 9:00 PM\nMadison Square Garden, New York, NY"),
)
