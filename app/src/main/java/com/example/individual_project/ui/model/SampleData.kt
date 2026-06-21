package com.example.individual_project.ui.model

/**
 * Temporary dummy data used while Firebase integration is pending (Phase 6).
 * Replace every reference to sampleEvents/sampleCategories with live ViewModel state in Phase 6.
 */
val sampleEvents = listOf(
    EventUiModel("1",  "Taylor Swift | The Eras Tour", "Music",   "Mar 15, 2025", "7:00 PM",  "SoFi Stadium",         "Los Angeles, CA",   "From \$250", "🎤", isFeatured = true),
    EventUiModel("2",  "NBA Finals 2025",              "Sports",  "Jun 5, 2025",  "8:00 PM",  "Chase Center",         "San Francisco, CA", "From \$180", "🏀", isFeatured = true),
    EventUiModel("3",  "Hamilton – The Musical",       "Arts",    "Apr 20, 2025", "7:30 PM",  "Pantages Theatre",     "Hollywood, CA",     "From \$95",  "🎭"),
    EventUiModel("4",  "Dave Chappelle Live",          "Comedy",  "Mar 28, 2025", "9:00 PM",  "Madison Square Garden","New York, NY",      "From \$75",  "😂"),
    EventUiModel("5",  "Coachella 2025",               "Music",   "Apr 11, 2025", "12:00 PM", "Empire Polo Club",     "Indio, CA",         "From \$499", "🎸", isFeatured = true),
    EventUiModel("6",  "UFC 310",                      "Sports",  "Dec 7, 2025",  "5:00 PM",  "T-Mobile Arena",       "Las Vegas, NV",     "From \$150", "🥊"),
    EventUiModel("7",  "Disney On Ice",                "Family",  "Mar 10, 2025", "11:00 AM", "Crypto.com Arena",     "Los Angeles, CA",   "From \$45",  "❄️"),
    EventUiModel("8",  "Ed Sheeran World Tour",        "Music",   "May 3, 2025",  "6:30 PM",  "Allegiant Stadium",    "Las Vegas, NV",     "From \$89",  "🎵"),
    EventUiModel("9",  "The Metropolitan Opera",       "Arts",    "Apr 5, 2025",  "7:00 PM",  "Met Opera House",      "New York, NY",      "From \$65",  "🎼"),
    EventUiModel("10", "WWE Royal Rumble",             "Sports",  "Feb 1, 2025",  "7:00 PM",  "Lucas Oil Stadium",    "Indianapolis, IN",  "From \$55",  "🤼"),
)

val sampleCategories = listOf("All", "Music", "Sports", "Arts", "Comedy", "Family", "Theater", "Festival")

val sampleUpcomingTickets = listOf(
    Triple("🎤", "Taylor Swift | The Eras Tour", "Mar 15, 2025 • 7:00 PM\nSoFi Stadium, Los Angeles, CA"),
    Triple("🏀", "NBA Finals 2025",              "Jun 5, 2025 • 8:00 PM\nChase Center, San Francisco, CA"),
)

val samplePastTickets = listOf(
    Triple("🎭", "Hamilton – The Musical",     "Jan 20, 2025 • 7:30 PM\nPantages Theatre, Hollywood, CA"),
    Triple("😂", "Kevin Hart: Reality Check", "Dec 15, 2024 • 9:00 PM\nMadison Square Garden, New York, NY"),
)
