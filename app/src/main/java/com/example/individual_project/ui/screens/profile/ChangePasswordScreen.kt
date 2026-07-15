package com.example.individual_project.ui.screens.profile

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.individual_project.ui.theme.Spacing
import com.example.individual_project.ui.theme.TmBlue
import com.example.individual_project.ui.theme.TmError
import com.example.individual_project.ui.theme.TmNavyBlue
import com.example.individual_project.ui.viewmodel.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChangePasswordScreen(
    navController: NavController,
    viewModel    : AuthViewModel = hiltViewModel()
) {
    var currentPassword by remember { mutableStateOf("") }
    var newPassword     by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var showCurrent     by remember { mutableStateOf(false) }
    var showNew         by remember { mutableStateOf(false) }
    var confirmError    by remember { mutableStateOf("") }

    val state        by viewModel.changePasswordState.collectAsState()
    val snackbarHost = remember { SnackbarHostState() }

    LaunchedEffect(state.isSuccess) {
        if (state.isSuccess) {
            snackbarHost.showSnackbar("Password updated successfully")
            viewModel.clearChangePasswordState()
            navController.popBackStack()
        }
    }

    LaunchedEffect(state.error) {
        state.error?.let {
            snackbarHost.showSnackbar(it)
            viewModel.clearChangePasswordState()
        }
    }

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
                    containerColor             = TmNavyBlue,
                    titleContentColor          = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHost) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Spacing.screenHorizontal)
        ) {
            Spacer(modifier = Modifier.height(Spacing.xl))

            Text(
                text  = "Enter your current password, then choose a new one.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(Spacing.xl))

            OutlinedTextField(
                value           = currentPassword,
                onValueChange   = { currentPassword = it },
                label           = { Text("Current Password") },
                singleLine      = true,
                enabled         = !state.isLoading,
                leadingIcon     = { Icon(Icons.Default.Lock, contentDescription = null) },
                trailingIcon    = {
                    IconButton(onClick = { showCurrent = !showCurrent }) {
                        Icon(
                            if (showCurrent) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = if (showCurrent) "Hide password" else "Show password"
                        )
                    }
                },
                visualTransformation = if (showCurrent) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier        = Modifier.fillMaxWidth(),
                shape           = MaterialTheme.shapes.medium
            )

            Spacer(modifier = Modifier.height(Spacing.md))

            OutlinedTextField(
                value           = newPassword,
                onValueChange   = { newPassword = it; confirmError = "" },
                label           = { Text("New Password") },
                singleLine      = true,
                enabled         = !state.isLoading,
                leadingIcon     = { Icon(Icons.Default.Lock, contentDescription = null) },
                trailingIcon    = {
                    IconButton(onClick = { showNew = !showNew }) {
                        Icon(
                            if (showNew) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = if (showNew) "Hide password" else "Show password"
                        )
                    }
                },
                visualTransformation = if (showNew) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                supportingText  = { Text("At least 6 characters") },
                modifier        = Modifier.fillMaxWidth(),
                shape           = MaterialTheme.shapes.medium
            )

            Spacer(modifier = Modifier.height(Spacing.md))

            OutlinedTextField(
                value           = confirmPassword,
                onValueChange   = { confirmPassword = it; confirmError = "" },
                label           = { Text("Confirm New Password") },
                singleLine      = true,
                enabled         = !state.isLoading,
                isError         = confirmError.isNotEmpty(),
                supportingText  = if (confirmError.isNotEmpty()) {
                    { Text(confirmError, color = TmError) }
                } else null,
                leadingIcon     = { Icon(Icons.Default.Lock, contentDescription = null) },
                visualTransformation = if (showNew) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier        = Modifier.fillMaxWidth(),
                shape           = MaterialTheme.shapes.medium
            )

            Spacer(modifier = Modifier.height(Spacing.xl))

            Button(
                onClick = {
                    when {
                        currentPassword.isBlank() -> confirmError = "Current password is required"
                        newPassword.length < 6    -> confirmError = "New password must be at least 6 characters"
                        newPassword != confirmPassword -> confirmError = "Passwords do not match"
                        else -> viewModel.changePassword(currentPassword, newPassword)
                    }
                },
                enabled  = !state.isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(Spacing.buttonHeight),
                shape  = MaterialTheme.shapes.medium,
                colors = ButtonDefaults.buttonColors(containerColor = TmBlue)
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(
                        modifier    = Modifier.size(Spacing.iconMd),
                        color       = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Update Password", style = MaterialTheme.typography.labelLarge)
                }
            }

            Spacer(modifier = Modifier.height(Spacing.xl))
        }
    }
}
