package com.example.individual_project.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.individual_project.ui.components.PrimaryButton
import com.example.individual_project.ui.navigation.Screen
import com.example.individual_project.ui.testtags.TestTags
import com.example.individual_project.ui.theme.IndividualProjectTheme
import com.example.individual_project.ui.theme.Spacing
import com.example.individual_project.ui.theme.TmBlue
import com.example.individual_project.ui.theme.TmLightBlue
import com.example.individual_project.ui.theme.TmNavyBlue
import com.example.individual_project.ui.viewmodel.AuthViewModel
import com.example.individual_project.utils.Validation

@Composable
fun LoginScreen(
    navController: NavController,
    viewModel    : AuthViewModel = hiltViewModel()
) {
    var email           by remember { mutableStateOf("") }
    var password        by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var emailError      by remember { mutableStateOf("") }
    var passwordError   by remember { mutableStateOf("") }

    val loginState by viewModel.loginState.collectAsState()

    // Navigate after successful login — route depends on email verification status
    LaunchedEffect(loginState.data) {
        if (loginState.data != null) {
            viewModel.clearLoginState()
            val destination = if (viewModel.isEmailVerified) Screen.Dashboard.route
                              else Screen.VerifyEmail.route
            navController.navigate(destination) {
                popUpTo(Screen.Login.route) { inclusive = true }
            }
        }
    }

    fun validate(): Boolean {
        emailError    = Validation.validateEmail(email) ?: ""
        passwordError = if (password.isBlank()) "Password is required." else ""
        return emailError.isEmpty() && passwordError.isEmpty()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
    ) {
        // ── Gradient header ────────────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(250.dp)
                .background(Brush.verticalGradient(listOf(TmNavyBlue, TmBlue))),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = "🎫", fontSize = 62.sp)
                Spacer(modifier = Modifier.height(Spacing.sm))
                Text(
                    text       = "TicketMate",
                    style      = MaterialTheme.typography.headlineLarge,
                    color      = Color.White,
                    fontWeight = FontWeight.ExtraBold
                )
                Spacer(modifier = Modifier.height(Spacing.xs))
                Text(
                    text  = "Sign in to continue",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TmLightBlue
                )
            }
        }

        // ── Form ──────────────────────────────────────────────────────────
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Spacing.lg, vertical = Spacing.xl)
        ) {
            Text(
                text  = "Welcome Back!",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(Spacing.xs))
            Text(
                text  = "Login to discover amazing events",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(Spacing.lg))

            // Email field
            OutlinedTextField(
                value         = email,
                onValueChange = { email = it; emailError = "" },
                label         = { Text("Email Address") },
                leadingIcon   = {
                    Icon(Icons.Default.Email, null, tint = MaterialTheme.colorScheme.primary)
                },
                isError        = emailError.isNotEmpty(),
                supportingText = if (emailError.isNotEmpty()) {
                    { Text(emailError, color = MaterialTheme.colorScheme.error) }
                } else null,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier        = Modifier
                    .fillMaxWidth()
                    .testTag(TestTags.LOGIN_EMAIL),
                shape           = MaterialTheme.shapes.medium,
                colors          = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    focusedLabelColor  = MaterialTheme.colorScheme.primary,
                    cursorColor        = MaterialTheme.colorScheme.primary
                ),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(Spacing.md))

            // Password field
            OutlinedTextField(
                value         = password,
                onValueChange = { password = it; passwordError = "" },
                label         = { Text("Password") },
                leadingIcon   = {
                    Icon(Icons.Default.Lock, null, tint = MaterialTheme.colorScheme.primary)
                },
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector        = if (passwordVisible) Icons.Default.VisibilityOff
                                                 else Icons.Default.Visibility,
                            contentDescription = null,
                            tint               = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                visualTransformation = if (passwordVisible) VisualTransformation.None
                                       else PasswordVisualTransformation(),
                isError        = passwordError.isNotEmpty(),
                supportingText = if (passwordError.isNotEmpty()) {
                    { Text(passwordError, color = MaterialTheme.colorScheme.error) }
                } else null,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier        = Modifier
                    .fillMaxWidth()
                    .testTag(TestTags.LOGIN_PASSWORD),
                shape           = MaterialTheme.shapes.medium,
                colors          = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    focusedLabelColor  = MaterialTheme.colorScheme.primary,
                    cursorColor        = MaterialTheme.colorScheme.primary
                ),
                singleLine = true
            )

            // Forgot password link
            Box(modifier = Modifier.fillMaxWidth()) {
                TextButton(
                    onClick  = { navController.navigate(Screen.ForgotPassword.route) },
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .testTag(TestTags.LOGIN_FORGOT_PASSWORD)
                ) {
                    Text(
                        text  = "Forgot Password?",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(Spacing.sm))

            // Firebase error (mapped — no raw exception messages)
            if (loginState.hasError) {
                Text(
                    text      = loginState.error ?: "",
                    color     = MaterialTheme.colorScheme.error,
                    style     = MaterialTheme.typography.bodySmall,
                    modifier  = Modifier
                        .fillMaxWidth()
                        .padding(bottom = Spacing.sm)
                        .testTag(TestTags.LOGIN_ERROR),
                    textAlign = TextAlign.Center
                )
            }

            // Sign In
            PrimaryButton(
                text      = "Sign In",
                isLoading = loginState.isLoading,
                modifier  = Modifier.testTag(TestTags.LOGIN_SUBMIT),
                onClick   = { if (validate()) viewModel.login(email, password) }
            )

            Spacer(modifier = Modifier.height(Spacing.xl))

            // Sign up link
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = androidx.compose.foundation.layout.Arrangement.Center,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Text(
                    text  = "Don't have an account? ",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                TextButton(
                    onClick  = { navController.navigate(Screen.Register.route) },
                    modifier = Modifier.testTag(TestTags.LOGIN_REGISTER)
                ) {
                    Text(
                        text       = "Sign Up",
                        style      = MaterialTheme.typography.labelLarge,
                        color      = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(Spacing.lg))
        }
    }
}

@Preview(name = "Login Screen", showBackground = true, showSystemUi = true)
@Composable
private fun LoginScreenPreview() {
    IndividualProjectTheme {
        LoginScreen(navController = rememberNavController())
    }
}
