package com.example.individual_project.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.individual_project.ui.navigation.Screen
import com.example.individual_project.ui.theme.IndividualProjectTheme
import com.example.individual_project.ui.theme.TmBackground
import com.example.individual_project.ui.theme.TmBlue
import com.example.individual_project.ui.theme.TmDivider
import com.example.individual_project.ui.theme.TmError
import com.example.individual_project.ui.theme.TmLightBlue
import com.example.individual_project.ui.theme.TmNavyBlue
import com.example.individual_project.ui.theme.TmSurface
import com.example.individual_project.ui.theme.TmTextPrimary
import com.example.individual_project.ui.theme.TmTextSecondary

@Composable
fun LoginScreen(navController: NavController) {
    var email           by remember { mutableStateOf("") }
    var password        by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var emailError      by remember { mutableStateOf("") }
    var passwordError   by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(TmBackground)
            .verticalScroll(rememberScrollState())
    ) {
        // ── Gradient header ─────────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(250.dp)
                .background(Brush.verticalGradient(listOf(TmNavyBlue, TmBlue))),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = "🎫", fontSize = 62.sp)
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text       = "TicketMate",
                    fontSize   = 32.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color      = Color.White,
                    letterSpacing = (-0.5).sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text     = "Sign in to continue",
                    fontSize = 14.sp,
                    color    = TmLightBlue
                )
            }
        }

        // ── Form card ────────────────────────────────────────────────────
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(top = 32.dp)
        ) {
            Text(
                text       = "Welcome Back!",
                fontSize   = 24.sp,
                fontWeight = FontWeight.Bold,
                color      = TmTextPrimary
            )
            Text(
                text     = "Login to discover amazing events",
                fontSize = 14.sp,
                color    = TmTextSecondary,
                modifier = Modifier.padding(top = 4.dp)
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Email
            OutlinedTextField(
                value         = email,
                onValueChange = { email = it; emailError = "" },
                label         = { Text("Email Address") },
                leadingIcon   = {
                    Icon(Icons.Default.Email, contentDescription = null, tint = TmBlue)
                },
                isError       = emailError.isNotEmpty(),
                supportingText = if (emailError.isNotEmpty()) {
                    { Text(emailError, color = TmError) }
                } else null,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier      = Modifier.fillMaxWidth(),
                shape         = RoundedCornerShape(12.dp),
                colors        = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = TmBlue,
                    focusedLabelColor  = TmBlue,
                    cursorColor        = TmBlue
                ),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Password
            OutlinedTextField(
                value         = password,
                onValueChange = { password = it; passwordError = "" },
                label         = { Text("Password") },
                leadingIcon   = {
                    Icon(Icons.Default.Lock, contentDescription = null, tint = TmBlue)
                },
                trailingIcon  = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector  = if (passwordVisible) Icons.Default.VisibilityOff
                                           else Icons.Default.Visibility,
                            contentDescription = null,
                            tint         = TmTextSecondary
                        )
                    }
                },
                visualTransformation = if (passwordVisible) VisualTransformation.None
                                       else PasswordVisualTransformation(),
                isError       = passwordError.isNotEmpty(),
                supportingText = if (passwordError.isNotEmpty()) {
                    { Text(passwordError, color = TmError) }
                } else null,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier      = Modifier.fillMaxWidth(),
                shape         = RoundedCornerShape(12.dp),
                colors        = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = TmBlue,
                    focusedLabelColor  = TmBlue,
                    cursorColor        = TmBlue
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
                        text       = "Forgot Password?",
                        color      = TmBlue,
                        fontWeight = FontWeight.Medium,
                        fontSize   = 13.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Sign In button
            Button(
                onClick = {
                    var valid = true
                    if (email.isBlank()) {
                        emailError = "Email is required"; valid = false
                    } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                        emailError = "Enter a valid email address"; valid = false
                    }
                    if (password.isBlank()) {
                        passwordError = "Password is required"; valid = false
                    } else if (password.length < 6) {
                        passwordError = "Minimum 6 characters required"; valid = false
                    }
                    if (valid) {
                        navController.navigate(Screen.Dashboard.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape  = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = TmBlue)
            ) {
                Text(
                    text       = "Sign In",
                    fontSize   = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Divider
            Row(
                modifier            = Modifier.fillMaxWidth(),
                verticalAlignment   = Alignment.CenterVertically
            ) {
                HorizontalDivider(modifier = Modifier.weight(1f), color = TmDivider)
                Text("  OR  ", color = TmTextSecondary, fontSize = 13.sp)
                HorizontalDivider(modifier = Modifier.weight(1f), color = TmDivider)
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Social buttons
            Row(
                modifier            = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick  = { /* Google sign-in */ },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    shape  = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, TmDivider)
                ) {
                    Text("🔍  Google", fontSize = 13.sp, color = TmTextPrimary)
                }
                OutlinedButton(
                    onClick  = { /* Facebook sign-in */ },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    shape  = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, TmDivider)
                ) {
                    Text("📘  Facebook", fontSize = 13.sp, color = TmTextPrimary)
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Register link
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Text(
                    text     = "Don't have an account? ",
                    color    = TmTextSecondary,
                    fontSize = 14.sp
                )
                TextButton(onClick = { navController.navigate(Screen.Register.route) }) {
                    Text(
                        text       = "Sign Up",
                        color      = TmBlue,
                        fontWeight = FontWeight.Bold,
                        fontSize   = 14.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  Preview
// ─────────────────────────────────────────────────────────────────────────────

@Preview(name = "Login Screen", showBackground = true, showSystemUi = true)
@Composable
fun LoginScreenPreview() {
    IndividualProjectTheme {
        LoginScreen(navController = rememberNavController())
    }
}
