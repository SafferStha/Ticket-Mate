package com.example.individual_project

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.rememberNavController
import com.example.individual_project.data.model.ThemeMode
import com.example.individual_project.ui.navigation.NavGraph
import com.example.individual_project.ui.theme.IndividualProjectTheme
import com.example.individual_project.ui.viewmodel.SettingsViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    companion object {
        /** Intent extra carrying an eventId when the activity is launched from a local
         *  event-reminder notification tap -- see EventReminderWorker. Only handled on
         *  cold start (process launch); a tap while the app is already running just
         *  brings it to the foreground without navigating, to avoid racing the
         *  in-progress navigation state of whatever screen is currently showing. */
        const val EXTRA_DEEP_LINK_EVENT_ID = "deep_link_event_id"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val pendingDeepLinkEventId = intent?.getStringExtra(EXTRA_DEEP_LINK_EVENT_ID)

        setContent {
            val settingsViewModel: SettingsViewModel = hiltViewModel()
            val themeMode by settingsViewModel.themeMode.collectAsState()
            val darkTheme = when (themeMode) {
                ThemeMode.LIGHT  -> false
                ThemeMode.DARK   -> true
                ThemeMode.SYSTEM -> isSystemInDarkTheme()
            }

            val context = LocalContext.current
            val notificationPermissionLauncher = rememberLauncherForActivityResult(
                ActivityResultContracts.RequestPermission()
            ) { /* No follow-up needed either way -- the worker checks the grant itself. */ }

            LaunchedEffect(Unit) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                    ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
                        != PackageManager.PERMISSION_GRANTED
                ) {
                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }

            IndividualProjectTheme(darkTheme = darkTheme) {
                val navController = rememberNavController()
                NavGraph(
                    navController          = navController,
                    pendingDeepLinkEventId = pendingDeepLinkEventId
                )
            }
        }
    }
}
