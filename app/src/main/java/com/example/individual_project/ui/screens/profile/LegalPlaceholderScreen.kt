package com.example.individual_project.ui.screens.profile

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.individual_project.ui.theme.Spacing
import com.example.individual_project.ui.theme.TmNavyBlue

/**
 * Placeholder destination for legal content (Privacy Policy, Terms) that hasn't been written
 * yet. Exists so the Settings link goes somewhere real instead of doing nothing -- and so
 * nobody mistakes this generic notice for actual legal text, which no one should fabricate.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LegalPlaceholderScreen(
    navController: NavController,
    title        : String
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor             = TmNavyBlue,
                    titleContentColor          = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(Spacing.screenHorizontal)
        ) {
            Spacer(modifier = Modifier.height(Spacing.lg))
            Text(
                text  = "$title content has not been added yet.",
                style = MaterialTheme.typography.bodyLarge
            )
            Spacer(modifier = Modifier.height(Spacing.sm))
            Text(
                text  = "Contact support for details.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
