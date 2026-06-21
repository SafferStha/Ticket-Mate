package com.example.individual_project

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.rememberNavController
import com.example.individual_project.ui.navigation.NavGraph
import com.example.individual_project.ui.theme.IndividualProjectTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            IndividualProjectTheme {
                val navController = rememberNavController()
                NavGraph(navController = navController)
            }
        }
    }
}
