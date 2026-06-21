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
import com.example.individual_project.ui.screens.home.HomeScreen
import com.example.individual_project.ui.screens.home.MyTicketsScreen
import com.example.individual_project.ui.screens.home.ProfileScreen
import com.example.individual_project.ui.screens.home.SearchScreen

/**
 * Thin coordinator screen — only owns tab selection state and bottom nav.
 * Each tab is a standalone screen composable with its own state.
 * Phase 5: inject ViewModels here and pass state down to tab screens.
 */
@Composable
fun DashboardScreen(navController: NavController) {
    var selectedTab      by remember { mutableStateOf(0) }
    var searchQuery      by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }

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
                0 -> HomeScreen(
                    selectedCategory = selectedCategory,
                    onCategoryChange = { selectedCategory = it }
                )
                1 -> SearchScreen(
                    searchQuery   = searchQuery,
                    onQueryChange = { searchQuery = it }
                )
                2 -> MyTicketsScreen()
                3 -> ProfileScreen(navController = navController)
            }
        }
    }
}
