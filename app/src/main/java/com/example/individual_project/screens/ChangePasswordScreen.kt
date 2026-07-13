package com.example.individual_project.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import com.example.individual_project.repo.UserRepoImpl
import com.example.individual_project.ui.theme.IndividualProjectTheme
import com.example.individual_project.ui.theme.TmBackground
import com.example.individual_project.ui.theme.TmBlue
import com.example.individual_project.ui.theme.TmError
import com.example.individual_project.ui.theme.TmNavyBlue
import com.example.individual_project.ui.theme.TmSuccess
import com.example.individual_project.ui.theme.TmTextPrimary
import com.example.individual_project.ui.theme.TmTextSecondary
import com.example.individual_project.viewmodel.UserViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChangePasswordScreen(navController: NavController) {
    var oldPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    var oldPasswordVisibility by remember { mutableStateOf(false) }
    var newPasswordVisibility by remember { mutableStateOf(false) }
    var confirmPasswordVisibility by remember { mutableStateOf(false) }

    var oldPasswordError by remember { mutableStateOf("") }
    var newPasswordError by remember { mutableStateOf("") }
    var confirmPasswordError by remember { mutableStateOf("") }

    var isLoading by remember { mutableStateOf(false) }
    var changeSuccess by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val userViewModel = remember { UserViewModel(UserRepoImpl()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Change Password") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = TmNavyBlue,
                    titleContentColor = Color.White,
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
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (changeSuccess) {
                // Success state
                Spacer(modifier = Modifier.height(60.dp))

                Box(
                    modifier = Modifier
                        .background(
                            color = TmSuccess.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(100.dp)
                        )
                        .padding(28.dp)
                ) {
                    Text(text = "✓", fontSize = 60.sp, color = TmSuccess)
                }

                Spacer(modifier = Modifier.height(28.dp))

                Text(
                    text = "Password Changed Successfully",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = TmTextPrimary,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Your password has been updated. You can now log in with your new password.",
                    fontSize = 14.sp,
                    color = TmTextSecondary,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(36.dp))

                Button(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = TmBlue)
                ) {
                    Text(
                        text = "Done",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            } else {
                // Input state
                Spacer(modifier = Modifier.height(20.dp))

                Box(
                    modifier = Modifier
                        .background(
                            color = TmBlue.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(100.dp)
                        )
                        .padding(24.dp)
                ) {
                    Icon(
                        Icons.Default.Lock,
                        contentDescription = null,
                        tint = TmBlue,
                        modifier = Modifier.size(48.dp)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Update Your Password",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = TmTextPrimary,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Enter your current password and choose a new one.",
                    fontSize = 14.sp,
                    color = TmTextSecondary,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Old Password Field
                OutlinedTextField(
                    value = oldPassword,
                    onValueChange = {
                        oldPassword = it
                        oldPasswordError = ""
                    },
                    label = { Text("Current Password") },
                    visualTransformation = if (oldPasswordVisibility)
                        VisualTransformation.None
                    else
                        PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = {
                            oldPasswordVisibility = !oldPasswordVisibility
                        }) {
                            Icon(
                                if (oldPasswordVisibility)
                                    Icons.Default.Visibility
                                else
                                    Icons.Default.VisibilityOff,
                                contentDescription = null
                            )
                        }
                    },
                    isError = oldPasswordError.isNotEmpty(),
                    supportingText = if (oldPasswordError.isNotEmpty()) {
                        { Text(oldPasswordError, color = TmError) }
                    } else null,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = TmBlue,
                        focusedLabelColor = TmBlue,
                        cursorColor = TmBlue
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(16.dp))

                // New Password Field
                OutlinedTextField(
                    value = newPassword,
                    onValueChange = {
                        newPassword = it
                        newPasswordError = ""
                    },
                    label = { Text("New Password") },
                    visualTransformation = if (newPasswordVisibility)
                        VisualTransformation.None
                    else
                        PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = {
                            newPasswordVisibility = !newPasswordVisibility
                        }) {
                            Icon(
                                if (newPasswordVisibility)
                                    Icons.Default.Visibility
                                else
                                    Icons.Default.VisibilityOff,
                                contentDescription = null
                            )
                        }
                    },
                    isError = newPasswordError.isNotEmpty(),
                    supportingText = if (newPasswordError.isNotEmpty()) {
                        { Text(newPasswordError, color = TmError) }
                    } else null,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = TmBlue,
                        focusedLabelColor = TmBlue,
                        cursorColor = TmBlue
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Confirm Password Field
                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = {
                        confirmPassword = it
                        confirmPasswordError = ""
                    },
                    label = { Text("Confirm New Password") },
                    visualTransformation = if (confirmPasswordVisibility)
                        VisualTransformation.None
                    else
                        PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = {
                            confirmPasswordVisibility = !confirmPasswordVisibility
                        }) {
                            Icon(
                                if (confirmPasswordVisibility)
                                    Icons.Default.Visibility
                                else
                                    Icons.Default.VisibilityOff,
                                contentDescription = null
                            )
                        }
                    },
                    isError = confirmPasswordError.isNotEmpty(),
                    supportingText = if (confirmPasswordError.isNotEmpty()) {
                        { Text(confirmPasswordError, color = TmError) }
                    } else null,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = TmBlue,
                        focusedLabelColor = TmBlue,
                        cursorColor = TmBlue
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = {
                        // Validation
                        var hasError = false

                        if (oldPassword.isBlank()) {
                            oldPasswordError = "Current password is required"
                            hasError = true
                        }

                        if (newPassword.isBlank()) {
                            newPasswordError = "New password is required"
                            hasError = true
                        } else if (newPassword.length < 6) {
                            newPasswordError = "Password must be at least 6 characters"
                            hasError = true
                        }

                        if (confirmPassword.isBlank()) {
                            confirmPasswordError = "Please confirm your password"
                            hasError = true
                        } else if (newPassword != confirmPassword) {
                            confirmPasswordError = "Passwords don't match"
                            hasError = true
                        }

                        if (oldPassword == newPassword) {
                            newPasswordError = "New password must be different from current"
                            hasError = true
                        }

                        if (!hasError) {
                            isLoading = true
                            userViewModel.changePassword(oldPassword, newPassword) { success, message ->
                                isLoading = false
                                if (success) {
                                    changeSuccess = true
                                    Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                                } else {
                                    oldPasswordError = message
                                    Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                                }
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = TmBlue),
                    enabled = !isLoading
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            text = "Update Password",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Preview(name = "Change Password", showBackground = true, showSystemUi = true)
@Composable
fun ChangePasswordPreview() {
    IndividualProjectTheme {
        ChangePasswordScreen(navController = rememberNavController())
    }
}
