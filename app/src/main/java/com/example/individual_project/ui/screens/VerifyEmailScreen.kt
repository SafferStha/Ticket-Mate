package com.example.individual_project.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.individual_project.auth.AuthState
import com.example.individual_project.ui.navigation.Screen
import com.example.individual_project.ui.theme.Spacing
import com.example.individual_project.ui.theme.TmBlue
import com.example.individual_project.ui.theme.TmNavyBlue
import com.example.individual_project.ui.viewmodel.AuthViewModel

@Composable
fun VerifyEmailScreen(
    navController: NavController,
    viewModel    : AuthViewModel = hiltViewModel()
) {
    val authState      by viewModel.authState.collectAsState()
    val verifyState    by viewModel.verifyEmailState.collectAsState()

    // Firebase AuthStateListener fires when user verifies — navigate automatically
    LaunchedEffect(authState) {
        if (authState == AuthState.Authenticated) {
            viewModel.clearVerifyEmailState()
            navController.navigate(Screen.Dashboard.route) {
                popUpTo(0) { inclusive = true }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // ── Header ──────────────────────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
                .background(Brush.verticalGradient(listOf(TmNavyBlue, TmBlue))),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = "📧", fontSize = 64.sp)
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text       = "Verify Your Email",
                    style      = MaterialTheme.typography.headlineMedium,
                    color      = Color.White,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }

        // ── Body ─────────────────────────────────────────────────────────────
        Column(
            modifier            = Modifier.padding(horizontal = Spacing.lg),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(Spacing.xl))

            Text(
                text      = "A verification link has been sent to your email address.",
                style     = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color     = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(Spacing.sm))

            Text(
                text      = "Please open your inbox and click the link to activate your account. Once verified, this screen will update automatically.",
                style     = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color     = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(Spacing.xl))

            // Feedback messages
            if (verifyState.hasError) {
                Text(
                    text      = verifyState.error ?: "",
                    color     = MaterialTheme.colorScheme.error,
                    style     = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center,
                    modifier  = Modifier.padding(bottom = Spacing.sm)
                )
            }

            if (verifyState.isSuccess) {
                Text(
                    text      = "Verification email sent! Check your inbox.",
                    color     = MaterialTheme.colorScheme.primary,
                    style     = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center,
                    modifier  = Modifier.padding(bottom = Spacing.sm)
                )
            }

            // Primary CTA — poll Firebase for updated verification status
            Button(
                onClick  = { viewModel.refreshVerificationStatus() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape  = MaterialTheme.shapes.medium,
                colors = ButtonDefaults.buttonColors(containerColor = TmBlue)
            ) {
                Text(
                    text       = "I've Verified My Email",
                    fontWeight = FontWeight.Bold,
                    fontSize   = 16.sp,
                    color      = Color.White
                )
            }

            Spacer(modifier = Modifier.height(Spacing.md))

            // Secondary CTA — resend verification email
            OutlinedButton(
                onClick  = { viewModel.resendVerificationEmail() },
                enabled  = !verifyState.isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = MaterialTheme.shapes.medium
            ) {
                if (verifyState.isLoading) {
                    CircularProgressIndicator(
                        modifier    = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color       = TmBlue
                    )
                } else {
                    Text(
                        text       = "Resend Verification Email",
                        fontWeight = FontWeight.Medium,
                        fontSize   = 16.sp,
                        color      = TmBlue
                    )
                }
            }

            Spacer(modifier = Modifier.height(Spacing.lg))

            // Sign Out — takes user back to Login and clears back stack
            TextButton(
                onClick = {
                    viewModel.logout()
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            ) {
                Text(
                    text  = "Sign Out",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.labelMedium
                )
            }
        }
    }
}
