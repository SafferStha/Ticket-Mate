package com.example.individual_project.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Help
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow

import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.individual_project.navigation.Screen
import com.example.individual_project.ui.theme.IndividualProjectTheme
import com.example.individual_project.ui.theme.TmBackground
import com.example.individual_project.ui.theme.TmBlue
import com.example.individual_project.ui.theme.TmDarkBlue
import com.example.individual_project.ui.theme.TmDivider
import com.example.individual_project.ui.theme.TmError
import com.example.individual_project.ui.theme.TmGold
import com.example.individual_project.ui.theme.TmLightBlue
import com.example.individual_project.ui.theme.TmNavyBlue
import com.example.individual_project.ui.theme.TmSuccess
import com.example.individual_project.ui.theme.TmSurface
import com.example.individual_project.ui.theme.TmTextPrimary
import com.example.individual_project.ui.theme.TmTextSecondary

// ─────────────────────────────────────────────────────────────────────────────
//  Data
// ─────────────────────────────────────────────────────────────────────────────

data class Event(
    val id: Int,
    val title: String,
    val category: String,
    val date: String,
    val time: String,
    val venue: String,
    val location: String,
    val price: String,
    val emoji: String,
    val isFeatured: Boolean = false,
    val isLiked: Boolean = false
)

val sampleEvents = listOf(
    Event(1, "Taylor Swift | The Eras Tour",  "Music",   "Mar 15, 2025", "7:00 PM",  "SoFi Stadium",        "Los Angeles, CA",    "From \$250", "🎤", isFeatured = true),
    Event(2, "NBA Finals 2025",               "Sports",  "Jun 5, 2025",  "8:00 PM",  "Chase Center",        "San Francisco, CA",  "From \$180", "🏀", isFeatured = true),
    Event(3, "Hamilton – The Musical",        "Arts",    "Apr 20, 2025", "7:30 PM",  "Pantages Theatre",    "Hollywood, CA",      "From \$95",  "🎭"),
    Event(4, "Dave Chappelle Live",           "Comedy",  "Mar 28, 2025", "9:00 PM",  "Madison Square Garden","New York, NY",       "From \$75",  "😂"),
    Event(5, "Coachella 2025",               "Music",   "Apr 11, 2025", "12:00 PM", "Empire Polo Club",    "Indio, CA",          "From \$499", "🎸", isFeatured = true),
    Event(6, "UFC 310",                      "Sports",  "Dec 7, 2025",  "5:00 PM",  "T-Mobile Arena",      "Las Vegas, NV",      "From \$150", "🥊"),
    Event(7, "Disney On Ice",               "Family",  "Mar 10, 2025", "11:00 AM", "Crypto.com Arena",    "Los Angeles, CA",    "From \$45",  "❄️"),
    Event(8, "Ed Sheeran World Tour",        "Music",   "May 3, 2025",  "6:30 PM",  "Allegiant Stadium",   "Las Vegas, NV",      "From \$89",  "🎵"),
    Event(9, "The Metropolitan Opera",       "Arts",    "Apr 5, 2025",  "7:00 PM",  "Met Opera House",     "New York, NY",       "From \$65",  "🎼"),
    Event(10, "WWE Royal Rumble",            "Sports",  "Feb 1, 2025",  "7:00 PM",  "Lucas Oil Stadium",   "Indianapolis, IN",   "From \$55",  "🤼"),
)

val categories = listOf("All", "Music", "Sports", "Arts", "Comedy", "Family", "Theater", "Festival")

// ─────────────────────────────────────────────────────────────────────────────
//  Root screen
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun DashboardScreen(navController: NavController) {
    var selectedTab  by remember { mutableStateOf(0) }
    var searchQuery  by remember { mutableStateOf("") }
    var selectedCat  by remember { mutableStateOf("All") }

    data class NavItem(val icon: ImageVector, val label: String)

    val navItems = listOf(
        NavItem(Icons.Default.Home,               "Home"),
        NavItem(Icons.Default.Search,             "Search"),
        NavItem(Icons.Default.ConfirmationNumber, "Tickets"),
        NavItem(Icons.Default.Person,             "Profile"),
    )

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = TmSurface,
                tonalElevation = 8.dp
            ) {
                navItems.forEachIndexed { index, item ->
                    NavigationBarItem(
                        selected = selectedTab == index,
                        onClick  = { selectedTab = index },
                        icon     = { Icon(item.icon, contentDescription = item.label) },
                        label    = { Text(item.label, fontSize = 11.sp) },
                        colors   = NavigationBarItemDefaults.colors(
                            selectedIconColor   = TmBlue,
                            selectedTextColor   = TmBlue,
                            indicatorColor      = TmBlue.copy(alpha = 0.12f),
                            unselectedIconColor = TmTextSecondary,
                            unselectedTextColor = TmTextSecondary
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            when (selectedTab) {
                0 -> HomeTab(selectedCat, onCategoryChange = { selectedCat = it })
                1 -> SearchTab(searchQuery, onQueryChange = { searchQuery = it })
                2 -> MyTicketsTab()
                3 -> ProfileTab(navController)
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  Home tab
// ─────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeTab(selectedCategory: String, onCategoryChange: (String) -> Unit) {
    val featured  = sampleEvents.filter { it.isFeatured }
    val displayed = if (selectedCategory == "All") sampleEvents
                    else sampleEvents.filter { it.category == selectedCategory }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(TmBackground)
    ) {
        // Header
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Brush.verticalGradient(listOf(TmNavyBlue, TmDarkBlue)))
                    .padding(top = 48.dp, start = 20.dp, end = 20.dp, bottom = 20.dp)
            ) {
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Good Evening 👋", fontSize = 13.sp, color = TmLightBlue)
                        Text("John Doe", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        IconButton(onClick = {}) {
                            Icon(Icons.Default.Notifications, null, tint = Color.White)
                        }
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(TmBlue),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("JD", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                }
            }
        }

        // Search bar
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(TmNavyBlue)
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                OutlinedTextField(
                    value         = "",
                    onValueChange = {},
                    placeholder   = { Text("Search events, artists, venues...", fontSize = 13.sp) },
                    leadingIcon   = { Icon(Icons.Default.Search, null, tint = TmTextSecondary) },
                    modifier      = Modifier.fillMaxWidth(),
                    shape         = RoundedCornerShape(12.dp),
                    colors        = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor    = Color.Transparent,
                        focusedBorderColor      = TmBlue,
                        unfocusedContainerColor = Color.White,
                        focusedContainerColor   = Color.White
                    ),
                    singleLine = true
                )
            }
        }

        // Featured Events
        item {
            Column(modifier = Modifier.background(TmBackground)) {
                Row(
                    modifier              = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    Text("🔥 Featured Events", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = TmTextPrimary)
                    TextButton(onClick = {}) { Text("See All", color = TmBlue, fontSize = 13.sp) }
                }
                LazyRow(
                    contentPadding        = PaddingValues(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(featured) { event -> FeaturedEventCard(event) }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        // Categories
        item {
            Column(modifier = Modifier.padding(top = 20.dp)) {
                Text(
                    "Browse Categories",
                    fontSize   = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color      = TmTextPrimary,
                    modifier   = Modifier.padding(horizontal = 20.dp)
                )
                Spacer(modifier = Modifier.height(10.dp))
                LazyRow(
                    contentPadding        = PaddingValues(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(categories) { cat ->
                        CategoryChip(
                            category   = cat,
                            isSelected = selectedCategory == cat,
                            onClick    = { onCategoryChange(cat) }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
            }
        }

        // Section heading
        item {
            Row(
                modifier              = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Text("Trending Events", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = TmTextPrimary)
                TextButton(onClick = {}) { Text("See All", color = TmBlue, fontSize = 13.sp) }
            }
        }

        // Event list
        items(displayed) { event ->
            EventCard(
                event    = event,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)
            )
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  Reusable composables
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun FeaturedEventCard(event: Event) {
    val gradient = when (event.category) {
        "Music"  -> listOf(Color(0xFF1A0533), Color(0xFF6B21A8))
        "Sports" -> listOf(Color(0xFF0A1628), Color(0xFF1E40AF))
        "Arts"   -> listOf(Color(0xFF1A0A00), Color(0xFF92400E))
        else     -> listOf(TmNavyBlue, TmBlue)
    }

    Card(
        modifier  = Modifier
            .width(280.dp)
            .height(180.dp),
        shape     = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(8.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(gradient))
        ) {
            Column(
                modifier            = Modifier
                    .fillMaxSize()
                    .padding(18.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(event.emoji, fontSize = 38.sp)
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.White.copy(alpha = 0.2f))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(event.category, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                    }
                }
                Column {
                    Text(
                        text     = event.title,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color    = Color.White,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CalendarToday, null, tint = Color.White.copy(0.8f), modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("${event.date} • ${event.time}", fontSize = 11.sp, color = Color.White.copy(0.8f))
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier              = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment     = Alignment.CenterVertically
                    ) {
                        Text(event.price, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TmGold)
                        Text(event.location, fontSize = 11.sp, color = Color.White.copy(0.7f), maxLines = 1)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryChip(category: String, isSelected: Boolean, onClick: () -> Unit) {
    val emoji = when (category) {
        "All"      -> "✨"; "Music"    -> "🎵"; "Sports"   -> "⚽"
        "Arts"     -> "🎭"; "Comedy"   -> "😂"; "Family"   -> "👨‍👩‍👧"
        "Theater"  -> "🎪"; "Festival" -> "🎉"; else       -> "🎫"
    }
    FilterChip(
        selected = isSelected,
        onClick  = onClick,
        label    = { Text("$emoji $category", fontSize = 13.sp) },
        colors   = FilterChipDefaults.filterChipColors(
            selectedContainerColor = TmBlue,
            selectedLabelColor     = Color.White,
            containerColor         = TmSurface,
            labelColor             = TmTextPrimary
        )
    )
}

@Composable
fun EventCard(event: Event, modifier: Modifier = Modifier) {
    var liked by remember { mutableStateOf(event.isLiked) }

    Card(
        modifier  = modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(3.dp),
        colors    = CardDefaults.cardColors(containerColor = TmSurface)
    ) {
        Row(
            modifier          = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Emoji thumbnail
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(TmBlue.copy(0.08f)),
                contentAlignment = Alignment.Center
            ) {
                Text(event.emoji, fontSize = 30.sp)
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text       = event.title,
                    fontSize   = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color      = TmTextPrimary,
                    maxLines   = 2,
                    overflow   = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CalendarToday, null, tint = TmTextSecondary, modifier = Modifier.size(11.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("${event.date} • ${event.time}", fontSize = 12.sp, color = TmTextSecondary)
                }
                Spacer(modifier = Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.LocationOn, null, tint = TmTextSecondary, modifier = Modifier.size(11.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        "${event.venue}, ${event.location}",
                        fontSize = 12.sp, color = TmTextSecondary,
                        maxLines = 1, overflow = TextOverflow.Ellipsis
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    Text(event.price, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TmBlue)
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(TmBlue.copy(0.1f))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(event.category, fontSize = 10.sp, color = TmBlue, fontWeight = FontWeight.Medium)
                    }
                }
            }

            IconButton(onClick = { liked = !liked }) {
                Icon(
                    imageVector = if (liked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = "Like",
                    tint = if (liked) TmError else TmTextSecondary
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  Search tab
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun SearchTab(searchQuery: String, onQueryChange: (String) -> Unit) {
    val recentSearches = listOf("Taylor Swift", "NBA Finals", "Coachella", "Hamilton – Musical")
    val popularCats    = listOf(
        "🎵" to "Music", "⚽" to "Sports", "🎭" to "Arts",
        "😂" to "Comedy", "👨‍👩‍👧" to "Family", "🎪" to "Theater"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(TmBackground)
    ) {
        // Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.verticalGradient(listOf(TmNavyBlue, TmDarkBlue)))
                .padding(top = 48.dp, start = 20.dp, end = 20.dp, bottom = 20.dp)
        ) {
            Column {
                Text("Discover Events", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value         = searchQuery,
                    onValueChange = onQueryChange,
                    placeholder   = { Text("Search events, artists, venues...") },
                    leadingIcon   = { Icon(Icons.Default.Search, null, tint = TmTextSecondary) },
                    trailingIcon  = if (searchQuery.isNotEmpty()) {
                        { IconButton(onClick = { onQueryChange("") }) { Icon(Icons.Default.Clear, null, tint = TmTextSecondary) } }
                    } else null,
                    modifier      = Modifier.fillMaxWidth(),
                    shape         = RoundedCornerShape(12.dp),
                    colors        = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor    = Color.Transparent,
                        focusedBorderColor      = TmBlue,
                        unfocusedContainerColor = Color.White,
                        focusedContainerColor   = Color.White
                    ),
                    singleLine = true
                )
            }
        }

        // Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
        ) {
            if (searchQuery.isEmpty()) {
                // Recent searches
                Text("Recent Searches", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TmTextPrimary)
                Spacer(modifier = Modifier.height(10.dp))
                recentSearches.forEach { term ->
                    Row(
                        modifier          = Modifier
                            .fillMaxWidth()
                            .clickable { onQueryChange(term) }
                            .padding(vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.History, null, tint = TmTextSecondary, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(term, fontSize = 14.sp, color = TmTextPrimary, modifier = Modifier.weight(1f))
                        Icon(Icons.Default.ChevronRight, null, tint = TmTextSecondary, modifier = Modifier.size(18.dp))
                    }
                    HorizontalDivider(color = TmDivider)
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Popular Categories grid (2 per row)
                Text("Popular Categories", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TmTextPrimary)
                Spacer(modifier = Modifier.height(12.dp))
                popularCats.chunked(3).forEach { row ->
                    Row(
                        modifier              = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        row.forEach { (emoji, name) ->
                            Card(
                                modifier  = Modifier
                                    .weight(1f)
                                    .height(72.dp),
                                shape     = RoundedCornerShape(14.dp),
                                elevation = CardDefaults.cardElevation(2.dp),
                                colors    = CardDefaults.cardColors(containerColor = TmSurface),
                                onClick   = { onQueryChange(name) }
                            ) {
                                Column(
                                    modifier              = Modifier.fillMaxSize(),
                                    horizontalAlignment   = Alignment.CenterHorizontally,
                                    verticalArrangement   = Arrangement.Center
                                ) {
                                    Text(emoji, fontSize = 24.sp)
                                    Text(name, fontSize = 12.sp, color = TmTextPrimary, fontWeight = FontWeight.Medium)
                                }
                            }
                        }
                        // Pad incomplete rows
                        repeat(3 - row.size) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                }
            } else {
                val results = sampleEvents.filter {
                    it.title.contains(searchQuery, ignoreCase = true) ||
                    it.category.contains(searchQuery, ignoreCase = true) ||
                    it.venue.contains(searchQuery, ignoreCase = true) ||
                    it.location.contains(searchQuery, ignoreCase = true)
                }
                Text(
                    "${results.size} result${if (results.size == 1) "" else "s"} for \"$searchQuery\"",
                    fontSize = 13.sp, color = TmTextSecondary
                )
                Spacer(modifier = Modifier.height(14.dp))
                if (results.isEmpty()) {
                    Column(
                        modifier            = Modifier
                            .fillMaxWidth()
                            .padding(top = 48.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("🔍", fontSize = 52.sp)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("No events found", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TmTextPrimary)
                        Text("Try a different keyword", fontSize = 14.sp, color = TmTextSecondary)
                    }
                } else {
                    results.forEach { event ->
                        EventCard(event = event, modifier = Modifier.padding(vertical = 6.dp))
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  My Tickets tab
// ─────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyTicketsTab() {
    var tabIndex by remember { mutableStateOf(0) }

    val upcoming = listOf(
        Triple("🎤", "Taylor Swift | The Eras Tour",  "Mar 15, 2025 • 7:00 PM\nSoFi Stadium, Los Angeles, CA"),
        Triple("🏀", "NBA Finals 2025",               "Jun 5, 2025 • 8:00 PM\nChase Center, San Francisco, CA"),
    )
    val past = listOf(
        Triple("🎭", "Hamilton – The Musical",        "Jan 20, 2025 • 7:30 PM\nPantages Theatre, Hollywood, CA"),
        Triple("😂", "Kevin Hart: Reality Check",    "Dec 15, 2024 • 9:00 PM\nMadison Square Garden, New York, NY"),
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(TmBackground)
    ) {
        // Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.verticalGradient(listOf(TmNavyBlue, TmDarkBlue)))
                .padding(top = 48.dp, start = 20.dp, end = 20.dp, bottom = 20.dp)
        ) {
            Text("My Tickets", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }

        // Tab bar
        TabRow(
            selectedTabIndex = tabIndex,
            containerColor   = TmSurface,
            contentColor     = TmBlue
        ) {
            Tab(selected = tabIndex == 0, onClick = { tabIndex = 0 }, text = { Text("Upcoming") })
            Tab(selected = tabIndex == 1, onClick = { tabIndex = 1 }, text = { Text("Past") })
        }

        val tickets = if (tabIndex == 0) upcoming else past

        if (tickets.isEmpty()) {
            Column(
                modifier            = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text("🎫", fontSize = 64.sp)
                Spacer(modifier = Modifier.height(16.dp))
                Text("No tickets yet", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TmTextPrimary)
                Text("Your tickets will appear here", fontSize = 14.sp, color = TmTextSecondary)
            }
        } else {
            LazyColumn(
                modifier        = Modifier.fillMaxSize(),
                contentPadding  = PaddingValues(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(tickets) { (emoji, title, details) ->
                    TicketCard(emoji, title, details, isUpcoming = tabIndex == 0)
                }
            }
        }
    }
}

@Composable
fun TicketCard(emoji: String, title: String, details: String, isUpcoming: Boolean) {
    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(4.dp),
        colors    = CardDefaults.cardColors(containerColor = TmSurface)
    ) {
        Column {
            // Gradient header strip
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        if (isUpcoming) Brush.horizontalGradient(listOf(TmDarkBlue, TmBlue))
                        else Brush.horizontalGradient(listOf(Color(0xFF374151), Color(0xFF6B7280)))
                    )
                    .padding(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(emoji, fontSize = 32.sp)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            title,
                            fontSize   = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color      = Color.White,
                            maxLines   = 2,
                            overflow   = TextOverflow.Ellipsis
                        )
                        if (isUpcoming) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(TmSuccess.copy(0.25f))
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text("UPCOMING", fontSize = 10.sp, color = TmSuccess, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // Perforation effect
            Row(
                modifier          = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(modifier = Modifier.size(14.dp).clip(CircleShape).background(TmBackground))
                HorizontalDivider(modifier = Modifier.weight(1f), color = TmDivider, thickness = 1.dp)
                Box(modifier = Modifier.size(14.dp).clip(CircleShape).background(TmBackground))
            }

            // Ticket body
            Column(modifier = Modifier.padding(16.dp)) {
                details.split("\n").forEach { line ->
                    Text(line, fontSize = 13.sp, color = TmTextSecondary)
                }
                Spacer(modifier = Modifier.height(14.dp))
                if (isUpcoming) {
                    Button(
                        onClick  = {},
                        modifier = Modifier.fillMaxWidth(),
                        shape    = RoundedCornerShape(10.dp),
                        colors   = ButtonDefaults.buttonColors(containerColor = TmBlue)
                    ) {
                        Icon(Icons.Default.ConfirmationNumber, null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("View Ticket", fontWeight = FontWeight.Bold)
                    }
                } else {
                    OutlinedButton(
                        onClick  = {},
                        modifier = Modifier.fillMaxWidth(),
                        shape    = RoundedCornerShape(10.dp),
                        border   = BorderStroke(1.dp, TmBlue)
                    ) {
                        Text("Buy Again", color = TmBlue, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  Profile tab
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun ProfileTab(navController: NavController) {
    data class SettingsItem(val icon: ImageVector, val title: String, val subtitle: String)

    val settingsItems = listOf(
        SettingsItem(Icons.Default.Person,             "Personal Information", "Update your profile details"),
        SettingsItem(Icons.Default.Notifications,      "Notifications",        "Manage your alert preferences"),
        SettingsItem(Icons.Default.CreditCard,         "Payment Methods",      "Add or remove payment options"),
        SettingsItem(Icons.Default.LocationOn,         "Saved Locations",      "Manage your favourite venues"),
        SettingsItem(Icons.Default.Security,           "Security & Privacy",   "Password and security settings"),
        SettingsItem(Icons.Default.Settings,           "App Preferences",      "Language, theme and more"),
        SettingsItem(Icons.Default.Help,               "Help & Support",       "Get help or contact us"),
        SettingsItem(Icons.Default.Info,               "About TicketMate",     "Version 1.0.0"),
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(TmBackground)
    ) {
        // Profile header
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Brush.verticalGradient(listOf(TmNavyBlue, TmDarkBlue)))
                    .padding(top = 48.dp, start = 20.dp, end = 20.dp, bottom = 32.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier            = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .size(84.dp)
                            .clip(CircleShape)
                            .background(TmBlue),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("JD", fontSize = 30.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("John Doe",         fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    Text("john.doe@email.com", fontSize = 13.sp, color = TmLightBlue)
                    Spacer(modifier = Modifier.height(20.dp))

                    // Stats
                    Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                        StatBadge("12", "Events")
                        Box(modifier = Modifier.size(width = 1.dp, height = 36.dp).background(Color.White.copy(0.3f)))
                        StatBadge("5", "Tickets")
                        Box(modifier = Modifier.size(width = 1.dp, height = 36.dp).background(Color.White.copy(0.3f)))
                        StatBadge("8", "Favorites")
                    }
                }
            }
        }

        // Settings list
        item {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    "SETTINGS",
                    fontSize      = 12.sp,
                    fontWeight    = FontWeight.Bold,
                    color         = TmTextSecondary,
                    letterSpacing = 1.5.sp
                )
                Spacer(modifier = Modifier.height(12.dp))
                Card(
                    shape     = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(2.dp),
                    colors    = CardDefaults.cardColors(containerColor = TmSurface)
                ) {
                    settingsItems.forEachIndexed { index, item ->
                        Row(
                            modifier          = Modifier
                                .fillMaxWidth()
                                .clickable {}
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(TmBlue.copy(0.1f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(item.icon, null, tint = TmBlue, modifier = Modifier.size(20.dp))
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(item.title,    fontSize = 14.sp, fontWeight = FontWeight.Medium, color = TmTextPrimary)
                                Text(item.subtitle, fontSize = 12.sp, color = TmTextSecondary)
                            }
                            Icon(Icons.Default.ChevronRight, null, tint = TmTextSecondary)
                        }
                        if (index < settingsItems.lastIndex) {
                            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = TmDivider)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Sign out button
                OutlinedButton(
                    onClick  = {
                        navController.navigate(Screen.Login.route) {
                            popUpTo(Screen.Dashboard.route) { inclusive = true }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape  = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, TmError)
                ) {
                    Icon(Icons.Default.Logout, null, tint = TmError)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Sign Out", fontWeight = FontWeight.Bold, color = TmError, fontSize = 15.sp)
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun StatBadge(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
        Text(label, fontSize = 12.sp, color = TmLightBlue)
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  Previews
// ─────────────────────────────────────────────────────────────────────────────

/** Full Dashboard (Home tab visible by default) */
@Preview(name = "Dashboard – Home Tab", showBackground = true, showSystemUi = true)
@Composable
fun DashboardPreview() {
    IndividualProjectTheme {
        DashboardScreen(navController = rememberNavController())
    }
}

/** Home tab in isolation */
@Preview(name = "Home Tab", showBackground = true, showSystemUi = true)
@Composable
fun HomeTabPreview() {
    IndividualProjectTheme {
        HomeTab(selectedCategory = "All", onCategoryChange = {})
    }
}

/** Search tab – empty state */
@Preview(name = "Search Tab – Empty", showBackground = true, showSystemUi = true)
@Composable
fun SearchTabEmptyPreview() {
    IndividualProjectTheme {
        SearchTab(searchQuery = "", onQueryChange = {})
    }
}

/** Search tab – with query */
@Preview(name = "Search Tab – Results", showBackground = true, showSystemUi = true)
@Composable
fun SearchTabResultsPreview() {
    IndividualProjectTheme {
        SearchTab(searchQuery = "Music", onQueryChange = {})
    }
}

/** My Tickets tab */
@Preview(name = "My Tickets Tab", showBackground = true, showSystemUi = true)
@Composable
fun MyTicketsTabPreview() {
    IndividualProjectTheme {
        MyTicketsTab()
    }
}

/** Profile tab */
@Preview(name = "Profile Tab", showBackground = true, showSystemUi = true)
@Composable
fun ProfileTabPreview() {
    IndividualProjectTheme {
        ProfileTab(navController = rememberNavController())
    }
}

/** Single event card */
@Preview(name = "Event Card", showBackground = true)
@Composable
fun EventCardPreview() {
    IndividualProjectTheme {
        EventCard(
            event    = sampleEvents[0],
            modifier = Modifier.padding(16.dp)
        )
    }
}

/** Featured event card */
@Preview(name = "Featured Event Card", showBackground = true)
@Composable
fun FeaturedEventCardPreview() {
    IndividualProjectTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            FeaturedEventCard(event = sampleEvents[0])
        }
    }
}

/** Upcoming ticket card */
@Preview(name = "Ticket Card – Upcoming", showBackground = true)
@Composable
fun TicketCardUpcomingPreview() {
    IndividualProjectTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            TicketCard(
                emoji      = "🎤",
                title      = "Taylor Swift | The Eras Tour",
                details    = "Mar 15, 2025 • 7:00 PM\nSoFi Stadium, Los Angeles, CA",
                isUpcoming = true
            )
        }
    }
}

/** Past ticket card */
@Preview(name = "Ticket Card – Past", showBackground = true)
@Composable
fun TicketCardPastPreview() {
    IndividualProjectTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            TicketCard(
                emoji      = "🎭",
                title      = "Hamilton – The Musical",
                details    = "Jan 20, 2025 • 7:30 PM\nPantages Theatre, Hollywood, CA",
                isUpcoming = false
            )
        }
    }
}

/** Category chips row */
@Preview(name = "Category Chips", showBackground = true)
@Composable
fun CategoryChipsPreview() {
    IndividualProjectTheme {
        LazyRow(
            contentPadding        = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(categories) { cat ->
                CategoryChip(category = cat, isSelected = cat == "Music", onClick = {})
            }
        }
    }
}
