package com.example.individual_project.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import com.example.individual_project.ui.components.TmBottomNavigationBar
import com.example.individual_project.ui.navigation.Screen
import com.example.individual_project.ui.screens.home.HomeScreen
import com.example.individual_project.ui.screens.home.MyTicketsScreen
import com.example.individual_project.ui.screens.home.ProfileScreen
import com.example.individual_project.ui.screens.home.SearchScreen

/**
 * Thin coordinator — owns bottom-nav tab state only.
 * Each tab screen owns its own ViewModel via hiltViewModel().
 * Event click callbacks bubble up here so DashboardScreen controls outer navigation.
 */
@Composable
fun DashboardScreen(navController: NavController) {
    var selectedTab by remember { mutableStateOf(0) }

    val onEventClick: (String) -> Unit = { eventId ->
        navController.navigate(Screen.EventDetail.createRoute(eventId))
    }

    Scaffold(
        bottomBar = {
            TmBottomNavigationBar(
                selectedTab   = selectedTab,
                onTabSelected = { selectedTab = it }
            )
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            when (selectedTab) {
                0 -> HomeScreen(onEventClick = onEventClick)
                1 -> SearchScreen(onEventClick = onEventClick)
                2 -> MyTicketsScreen()
                3 -> ProfileScreen(navController = navController)
            }
        }
    }
}
