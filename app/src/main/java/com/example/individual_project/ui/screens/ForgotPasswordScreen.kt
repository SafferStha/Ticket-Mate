package com.example.individual_project.ui.screens

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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.individual_project.ui.theme.IndividualProjectTheme
import com.example.individual_project.ui.theme.TmBackground
import com.example.individual_project.ui.theme.TmBlue
import com.example.individual_project.ui.theme.TmError
import com.example.individual_project.ui.theme.TmLightBlue
import com.example.individual_project.ui.theme.TmNavyBlue
import com.example.individual_project.ui.theme.TmSuccess
import com.example.individual_project.ui.theme.TmTextPrimary
import com.example.individual_project.ui.theme.TmTextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForgotPasswordScreen(navController: NavController) {
    var email      by remember { mutableStateOf("") }
    var emailError by remember { mutableStateOf("") }
    var emailSent  by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Reset Password") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor         = TmNavyBlue,
                    titleContentColor      = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(TmBackground)
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            // Sub-header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Brush.verticalGradient(listOf(TmNavyBlue, TmBlue)))
                    .padding(horizontal = 24.dp, vertical = 20.dp)
            ) {
                Column {
                    Text(
                        text       = "Forgot Password?",
                        fontSize   = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color      = Color.White
                    )
                    Text(
                        text     = "No worries, we'll send you reset instructions.",
                        fontSize = 13.sp,
                        color    = TmLightBlue,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (emailSent) {
                    // ── Success state ─────────────────────────────────────
                    Spacer(modifier = Modifier.height(32.dp))

                    Box(
                        modifier = Modifier
                            .background(
                                color = TmSuccess.copy(alpha = 0.1f),
                                shape = RoundedCornerShape(100.dp)
                            )
                            .padding(28.dp)
                    ) {
                        Text(text = "📧", fontSize = 60.sp)
                    }

                    Spacer(modifier = Modifier.height(28.dp))

                    Text(
                        text       = "Check Your Email",
                        fontSize   = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color      = TmTextPrimary
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text      = "We've sent a password reset link to",
                        fontSize  = 14.sp,
                        color     = TmTextSecondary,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text       = email,
                        fontSize   = 14.sp,
                        color      = TmBlue,
                        fontWeight = FontWeight.SemiBold,
                        textAlign  = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text      = "Didn't receive the email? Check your spam folder or try another email address.",
                        fontSize  = 13.sp,
                        color     = TmTextSecondary,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(36.dp))

                    Button(
                        onClick  = { navController.popBackStack() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp),
                        shape  = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = TmBlue)
                    ) {
                        Text(
                            text       = "Back to Sign In",
                            fontSize   = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    TextButton(onClick = { emailSent = false; email = "" }) {
                        Text(
                            text  = "Try a different email",
                            color = TmBlue
                        )
                    }

                } else {
                    // ── Input state ───────────────────────────────────────
                    Spacer(modifier = Modifier.height(32.dp))

                    Text(text = "🔐", fontSize = 72.sp, textAlign = TextAlign.Center)

                    Spacer(modifier = Modifier.height(20.dp))

                    Text(
                        text       = "Enter the email address associated with your account and we'll send you a link to reset your password.",
                        fontSize   = 14.sp,
                        color      = TmTextSecondary,
                        textAlign  = TextAlign.Center,
                        modifier   = Modifier.padding(horizontal = 8.dp)
                    )

                    Spacer(modifier = Modifier.height(32.dp))

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
                        modifier   = Modifier.fillMaxWidth(),
                        shape      = RoundedCornerShape(12.dp),
                        colors     = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = TmBlue,
                            focusedLabelColor  = TmBlue,
                            cursorColor        = TmBlue
                        ),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = {
                            when {
                                email.isBlank() ->
                                    emailError = "Email is required"
                                !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() ->
                                    emailError = "Enter a valid email address"
                                else -> emailSent = true
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp),
                        shape  = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = TmBlue)
                    ) {
                        Text(
                            text       = "Send Reset Link",
                            fontSize   = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment     = Alignment.CenterVertically
                    ) {
                        Text(
                            text     = "Remember your password? ",
                            color    = TmTextSecondary,
                            fontSize = 14.sp
                        )
                        TextButton(onClick = { navController.popBackStack() }) {
                            Text(
                                text       = "Sign In",
                                color      = TmBlue,
                                fontWeight = FontWeight.Bold,
                                fontSize   = 14.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  Previews
// ─────────────────────────────────────────────────────────────────────────────

/** Input state (default) */
@Preview(name = "Forgot Password – Input", showBackground = true, showSystemUi = true)
@Composable
fun ForgotPasswordInputPreview() {
    IndividualProjectTheme {
        ForgotPasswordScreen(navController = rememberNavController())
    }
}
