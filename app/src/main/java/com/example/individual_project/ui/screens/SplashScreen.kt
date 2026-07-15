package com.example.individual_project.ui.screens

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.individual_project.auth.AuthState
import com.example.individual_project.ui.navigation.Screen
import com.example.individual_project.ui.theme.IndividualProjectTheme
import com.example.individual_project.ui.theme.TmBlue
import com.example.individual_project.ui.theme.TmDarkBlue
import com.example.individual_project.ui.theme.TmLightBlue
import com.example.individual_project.ui.theme.TmNavyBlue
import com.example.individual_project.ui.theme.TmSurface
import com.example.individual_project.ui.theme.TmTextHint
import com.example.individual_project.ui.viewmodel.AuthViewModel
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    navController         : NavController,
    viewModel              : AuthViewModel = hiltViewModel(),
    pendingDeepLinkEventId : String?        = null
) {
    var startAnimation  by remember { mutableStateOf(false) }
    var minTimeElapsed  by remember { mutableStateOf(false) }
    val authState       by viewModel.authState.collectAsState()

    val alpha by animateFloatAsState(
        targetValue   = if (startAnimation) 1f else 0f,
        animationSpec = tween(durationMillis = 900),
        label         = "alpha"
    )
    val scale by animateFloatAsState(
        targetValue   = if (startAnimation) 1f else 0.55f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness    = Spring.StiffnessMediumLow
        ),
        label = "scale"
    )

    // Enforce a minimum branding display time
    LaunchedEffect(Unit) {
        startAnimation = true
        delay(2000L)
        minTimeElapsed = true
    }

    // Navigate once both the minimum time has passed AND auth state is resolved
    LaunchedEffect(authState, minTimeElapsed) {
        if (!minTimeElapsed) return@LaunchedEffect
        if (authState == AuthState.Loading) return@LaunchedEffect

        val destination = when (authState) {
            AuthState.Authenticated    -> if (pendingDeepLinkEventId != null) {
                Screen.EventDetail.createRoute(pendingDeepLinkEventId)
            } else {
                Screen.Dashboard.route
            }
            AuthState.EmailNotVerified -> Screen.VerifyEmail.route
            else                       -> Screen.Login.route
        }
        navController.navigate(destination) {
            popUpTo(Screen.Splash.route) { inclusive = true }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(TmNavyBlue, TmDarkBlue, TmBlue)
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        // ── Centre content ────────────────────────────────────────────────────
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .alpha(alpha)
                .scale(scale)
        ) {
            Text(text = "🎫", fontSize = 90.sp)

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text          = "TicketMate",
                fontSize      = 44.sp,
                fontWeight    = FontWeight.ExtraBold,
                color         = TmSurface,
                letterSpacing = (-1).sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text          = "Your Events. Your Experience.",
                fontSize      = 15.sp,
                color         = TmLightBlue,
                textAlign     = TextAlign.Center,
                letterSpacing = 0.5.sp
            )

            Spacer(modifier = Modifier.height(40.dp))

            LinearProgressIndicator(
                modifier   = Modifier.alpha(0.6f),
                color      = TmLightBlue,
                trackColor = Color.White.copy(alpha = 0.15f)
            )
        }

        // ── Bottom copyright ──────────────────────────────────────────────────
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 48.dp)
                .alpha(alpha)
        ) {
            Text(
                text      = "© 2025 TicketMate Inc.",
                fontSize  = 12.sp,
                color     = TmTextHint,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Preview(name = "Splash Screen", showBackground = true, showSystemUi = true)
@Composable
fun SplashScreenPreview() {
    IndividualProjectTheme {
        SplashScreen(navController = rememberNavController())
    }
}
