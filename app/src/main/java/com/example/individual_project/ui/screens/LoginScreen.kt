package com.example.individual_project.ui.screens

import android.util.Patterns
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.individual_project.ui.components.PrimaryButton
import com.example.individual_project.ui.navigation.Screen
import com.example.individual_project.ui.theme.IndividualProjectTheme
import com.example.individual_project.ui.theme.Spacing
import com.example.individual_project.ui.theme.TmBlue
import com.example.individual_project.ui.theme.TmDivider
import com.example.individual_project.ui.theme.TmLightBlue
import com.example.individual_project.ui.theme.TmNavyBlue
import com.example.individual_project.ui.viewmodel.AuthViewModel

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

    // Navigate to Dashboard on successful login
    LaunchedEffect(loginState.data) {
        if (loginState.data != null) {
            viewModel.clearLoginState()
            navController.navigate(Screen.Dashboard.route) {
                popUpTo(Screen.Login.route) { inclusive = true }
            }
        }
    }

    fun validate(): Boolean {
        var ok = true
        emailError    = ""
        passwordError = ""
        if (email.isBlank()) {
            emailError = "Email is required"; ok = false
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailError = "Enter a valid email address"; ok = false
        }
        if (password.isBlank()) {
            passwordError = "Password is required"; ok = false
        } else if (password.length < 6) {
            passwordError = "Minimum 6 characters required"; ok = false
        }
        return ok
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
    ) {
        // ── Gradient header ──────────────────────────────────────────────
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

        // ── Form ─────────────────────────────────────────────────────────
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
                modifier        = Modifier.fillMaxWidth(),
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
                            imageVector = if (passwordVisible) Icons.Default.VisibilityOff
                                          else Icons.Default.Visibility,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
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
                modifier        = Modifier.fillMaxWidth(),
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
                    modifier = Modifier.align(Alignment.CenterEnd)
                ) {
                    Text(
                        text  = "Forgot Password?",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(Spacing.md))

            // Firebase error
            if (loginState.hasError) {
                Text(
                    text     = loginState.error ?: "",
                    color    = MaterialTheme.colorScheme.error,
                    style    = MaterialTheme.typography.bodySmall,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = Spacing.sm),
                    textAlign = TextAlign.Center
                )
            }

            // Sign In
            PrimaryButton(
                text      = "Sign In",
                isLoading = loginState.isLoading,
                onClick   = {
                    if (validate()) {
                        viewModel.login(email, password)
                    }
                }
            )

            Spacer(modifier = Modifier.height(Spacing.lg))

            // OR divider
            Row(
                modifier          = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                HorizontalDivider(modifier = Modifier.weight(1f), color = TmDivider)
                Text(
                    "  OR  ",
                    style     = MaterialTheme.typography.bodySmall,
                    color     = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
                HorizontalDivider(modifier = Modifier.weight(1f), color = TmDivider)
            }

            Spacer(modifier = Modifier.height(Spacing.md))

            // Social buttons (UI-only)
            Row(modifier = Modifier.fillMaxWidth()) {
                OutlinedButton(
                    onClick  = {},
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    shape  = MaterialTheme.shapes.medium,
                    border = BorderStroke(1.dp, TmDivider)
                ) {
                    Text(
                        "🔍  Google",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Spacer(modifier = Modifier.padding(horizontal = Spacing.sm))
                OutlinedButton(
                    onClick  = {},
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    shape  = MaterialTheme.shapes.medium,
                    border = BorderStroke(1.dp, TmDivider)
                ) {
                    Text(
                        "📘  Facebook",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

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
                TextButton(onClick = { navController.navigate(Screen.Register.route) }) {
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
