package com.example.individual_project.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.example.individual_project.ui.components.EmptyState
import com.example.individual_project.ui.components.TicketCard
import com.example.individual_project.ui.model.samplePastTickets
import com.example.individual_project.ui.model.sampleUpcomingTickets
import com.example.individual_project.ui.theme.Spacing
import com.example.individual_project.ui.theme.TmDarkBlue
import com.example.individual_project.ui.theme.TmNavyBlue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyTicketsScreen() {
    var tabIndex by remember { mutableStateOf(0) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.verticalGradient(listOf(TmNavyBlue, TmDarkBlue)))
                .padding(
                    top    = Spacing.headerPaddingTop,
                    start  = Spacing.screenHorizontal,
                    end    = Spacing.screenHorizontal,
                    bottom = Spacing.md
                )
        ) {
            Text(
                text  = "My Tickets",
                style = MaterialTheme.typography.headlineSmall,
                color = Color.White
            )
        }

        // Upcoming / Past tabs
        TabRow(
            selectedTabIndex = tabIndex,
            containerColor   = MaterialTheme.colorScheme.surface,
            contentColor     = MaterialTheme.colorScheme.primary
        ) {
            Tab(
                selected = tabIndex == 0,
                onClick  = { tabIndex = 0 },
                text     = {
                    Text(
                        "Upcoming",
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            )
            Tab(
                selected = tabIndex == 1,
                onClick  = { tabIndex = 1 },
                text     = {
                    Text(
                        "Past",
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            )
        }

        val tickets = if (tabIndex == 0) sampleUpcomingTickets else samplePastTickets

        if (tickets.isEmpty()) {
            EmptyState(
                emoji    = "🎫",
                title    = "No tickets yet",
                subtitle = if (tabIndex == 0) "Your upcoming tickets will appear here"
                           else "Past tickets will appear here"
            )
        } else {
            LazyColumn(
                modifier            = Modifier.fillMaxSize(),
                contentPadding      = PaddingValues(Spacing.screenHorizontal),
                verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(Spacing.md)
            ) {
                item { Spacer(modifier = Modifier.height(Spacing.xs)) }
                items(tickets) { (emoji, title, details) ->
                    TicketCard(
                        emoji      = emoji,
                        title      = title,
                        details    = details,
                        isUpcoming = tabIndex == 0
                    )
                }
                item { Spacer(modifier = Modifier.height(Spacing.md)) }
            }
        }
    }
}
