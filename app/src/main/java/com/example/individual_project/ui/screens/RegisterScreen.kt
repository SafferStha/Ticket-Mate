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
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.individual_project.navigation.Screen
import com.example.individual_project.ui.theme.IndividualProjectTheme
import com.example.individual_project.ui.theme.TmBackground
import com.example.individual_project.ui.theme.TmBlue
import com.example.individual_project.ui.theme.TmError
import com.example.individual_project.ui.theme.TmLightBlue
import com.example.individual_project.ui.theme.TmNavyBlue
import com.example.individual_project.ui.theme.TmTextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(navController: NavController) {
    var fullName              by remember { mutableStateOf("") }
    var email                 by remember { mutableStateOf("") }
    var phone                 by remember { mutableStateOf("") }
    var password              by remember { mutableStateOf("") }
    var confirmPassword       by remember { mutableStateOf("") }
    var passwordVisible       by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    var acceptTerms           by remember { mutableStateOf(false) }

    var nameError            by remember { mutableStateOf("") }
    var emailError           by remember { mutableStateOf("") }
    var passwordError        by remember { mutableStateOf("") }
    var confirmPasswordError by remember { mutableStateOf("") }

    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = TmBlue,
        focusedLabelColor  = TmBlue,
        cursorColor        = TmBlue
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Create Account") },
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
                        text       = "Join TicketMate",
                        fontSize   = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color      = Color.White
                    )
                    Text(
                        text     = "Create your account to get started",
                        fontSize = 13.sp,
                        color    = TmLightBlue
                    )
                }
            }

            // ── Fields ──────────────────────────────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 24.dp)
            ) {
                // Full Name
                OutlinedTextField(
                    value         = fullName,
                    onValueChange = { fullName = it; nameError = "" },
                    label         = { Text("Full Name") },
                    leadingIcon   = { Icon(Icons.Default.Person, null, tint = TmBlue) },
                    isError       = nameError.isNotEmpty(),
                    supportingText = if (nameError.isNotEmpty()) {
                        { Text(nameError, color = TmError) }
                    } else null,
                    modifier   = Modifier.fillMaxWidth(),
                    shape      = RoundedCornerShape(12.dp),
                    colors     = fieldColors,
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Email
                OutlinedTextField(
                    value         = email,
                    onValueChange = { email = it; emailError = "" },
                    label         = { Text("Email Address") },
                    leadingIcon   = { Icon(Icons.Default.Email, null, tint = TmBlue) },
                    isError       = emailError.isNotEmpty(),
                    supportingText = if (emailError.isNotEmpty()) {
                        { Text(emailError, color = TmError) }
                    } else null,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    modifier   = Modifier.fillMaxWidth(),
                    shape      = RoundedCornerShape(12.dp),
                    colors     = fieldColors,
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Phone (optional)
                OutlinedTextField(
                    value         = phone,
                    onValueChange = { phone = it },
                    label         = { Text("Phone Number (Optional)") },
                    leadingIcon   = { Icon(Icons.Default.Phone, null, tint = TmBlue) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier   = Modifier.fillMaxWidth(),
                    shape      = RoundedCornerShape(12.dp),
                    colors     = fieldColors,
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Password
                OutlinedTextField(
                    value         = password,
                    onValueChange = { password = it; passwordError = "" },
                    label         = { Text("Password") },
                    leadingIcon   = { Icon(Icons.Default.Lock, null, tint = TmBlue) },
                    trailingIcon  = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = if (passwordVisible) Icons.Default.VisibilityOff
                                              else Icons.Default.Visibility,
                                contentDescription = null,
                                tint = TmTextSecondary
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
                    modifier   = Modifier.fillMaxWidth(),
                    shape      = RoundedCornerShape(12.dp),
                    colors     = fieldColors,
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Confirm Password
                OutlinedTextField(
                    value         = confirmPassword,
                    onValueChange = { confirmPassword = it; confirmPasswordError = "" },
                    label         = { Text("Confirm Password") },
                    leadingIcon   = { Icon(Icons.Default.Lock, null, tint = TmBlue) },
                    trailingIcon  = {
                        IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                            Icon(
                                imageVector = if (confirmPasswordVisible) Icons.Default.VisibilityOff
                                              else Icons.Default.Visibility,
                                contentDescription = null,
                                tint = TmTextSecondary
                            )
                        }
                    },
                    visualTransformation = if (confirmPasswordVisible) VisualTransformation.None
                                           else PasswordVisualTransformation(),
                    isError       = confirmPasswordError.isNotEmpty(),
                    supportingText = if (confirmPasswordError.isNotEmpty()) {
                        { Text(confirmPasswordError, color = TmError) }
                    } else null,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    modifier   = Modifier.fillMaxWidth(),
                    shape      = RoundedCornerShape(12.dp),
                    colors     = fieldColors,
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Terms checkbox
                Row(
                    verticalAlignment = Alignment.Top,
                    modifier          = Modifier.fillMaxWidth()
                ) {
                    Checkbox(
                        checked         = acceptTerms,
                        onCheckedChange = { acceptTerms = it },
                        colors          = CheckboxDefaults.colors(checkedColor = TmBlue)
                    )
                    Column(modifier = Modifier.padding(top = 12.dp)) {
                        Row {
                            Text("I agree to the ", color = TmTextSecondary, fontSize = 13.sp)
                            Text(
                                text       = "Terms & Conditions",
                                color      = TmBlue,
                                fontSize   = 13.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        Row {
                            Text("and ", color = TmTextSecondary, fontSize = 13.sp)
                            Text(
                                text       = "Privacy Policy",
                                color      = TmBlue,
                                fontSize   = 13.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Create Account button
                Button(
                    onClick = {
                        var valid = true
                        if (fullName.isBlank()) {
                            nameError = "Full name is required"; valid = false
                        }
                        if (email.isBlank()) {
                            emailError = "Email is required"; valid = false
                        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                            emailError = "Enter a valid email address"; valid = false
                        }
                        if (password.isBlank()) {
                            passwordError = "Password is required"; valid = false
                        } else if (password.length < 6) {
                            passwordError = "Minimum 6 characters"; valid = false
                        }
                        if (confirmPassword != password) {
                            confirmPasswordError = "Passwords do not match"; valid = false
                        }
                        if (valid && acceptTerms) {
                            navController.navigate(Screen.Dashboard.route) {
                                popUpTo(Screen.Login.route) { inclusive = true }
                            }
                        }
                    },
                    enabled  = acceptTerms,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    shape  = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = TmBlue)
                ) {
                    Text(
                        text       = "Create Account",
                        fontSize   = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Login link
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    Text(
                        text     = "Already have an account? ",
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

// ─────────────────────────────────────────────────────────────────────────────
//  Preview
// ─────────────────────────────────────────────────────────────────────────────

@Preview(name = "Register Screen", showBackground = true, showSystemUi = true)
@Composable
fun RegisterScreenPreview() {
    IndividualProjectTheme {
        RegisterScreen(navController = rememberNavController())
    }
}
