package com.example.individual_project.ui.screens.profile

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.individual_project.ui.components.LoadingView
import com.example.individual_project.ui.components.ProfileAvatar
import com.example.individual_project.ui.theme.Spacing
import com.example.individual_project.ui.theme.TmBlue
import com.example.individual_project.ui.theme.TmNavyBlue
import com.example.individual_project.ui.viewmodel.ProfileViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    navController : NavController,
    viewModel     : ProfileViewModel = hiltViewModel()
) {
    val state        by viewModel.editState.collectAsState()
    val snackbarHost = remember { SnackbarHostState() }

    // Image picker
    val imageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { viewModel.uploadImageAndSave(it) }
    }

    // Navigate back on save success
    LaunchedEffect(state.saveSuccess) {
        if (state.saveSuccess) {
            snackbarHost.showSnackbar("Profile updated successfully")
            viewModel.clearEditSuccess()
            navController.popBackStack()
        }
    }

    // Show errors in snackbar
    LaunchedEffect(state.error) {
        state.error?.let {
            snackbarHost.showSnackbar(it)
            viewModel.clearEditError()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Profile") },
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
        when {
            state.isLoading -> LoadingView()
            else -> {
                Column(
                    modifier            = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = Spacing.screenHorizontal),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(Spacing.xl))

                    // ── Profile image picker ───────────────────────────────────
                    Box(
                        modifier         = Modifier.size(100.dp),
                        contentAlignment = Alignment.BottomEnd
                    ) {
                        if (state.profileImage.isNotBlank()) {
                            AsyncImage(
                                model             = state.profileImage,
                                contentDescription = "Profile picture",
                                modifier          = Modifier
                                    .size(100.dp)
                                    .clip(CircleShape)
                                    .border(2.dp, TmBlue, CircleShape),
                                contentScale      = ContentScale.Crop
                            )
                        } else {
                            val initials = state.name.split(" ")
                                .take(2)
                                .mapNotNull { it.firstOrNull()?.uppercaseChar() }
                                .joinToString("")
                                .ifBlank { "U" }
                            ProfileAvatar(
                                initials = initials,
                                size     = 100.dp
                            )
                        }

                        Box(
                            modifier         = Modifier
                                .size(30.dp)
                                .clip(CircleShape)
                                .background(TmBlue)
                                .clickable(
                                    enabled = !state.isSaving,
                                    onClick = { imageLauncher.launch("image/*") }
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.CameraAlt, null,
                                tint     = Color.White,
                                modifier = Modifier.size(Spacing.iconSm)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(Spacing.xs))
                    Text(
                        text  = "Tap to change photo",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(Spacing.xl))

                    // ── Name field ────────────────────────────────────────────
                    OutlinedTextField(
                        value         = state.name,
                        onValueChange = { viewModel.onNameChange(it) },
                        label         = { Text("Full Name") },
                        singleLine    = true,
                        enabled       = !state.isSaving,
                        modifier      = Modifier.fillMaxWidth(),
                        shape         = MaterialTheme.shapes.medium,
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Words,
                            imeAction      = ImeAction.Done
                        )
                    )

                    Spacer(modifier = Modifier.height(Spacing.md))

                    // ── Email (read-only) ─────────────────────────────────────
                    OutlinedTextField(
                        value         = state.email,
                        onValueChange = {},
                        label         = { Text("Email") },
                        singleLine    = true,
                        readOnly      = true,
                        enabled       = false,
                        modifier      = Modifier.fillMaxWidth(),
                        shape         = MaterialTheme.shapes.medium,
                        supportingText = {
                            Text(
                                text  = "Email cannot be changed",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    )

                    Spacer(modifier = Modifier.height(Spacing.xl))

                    // ── Save button ───────────────────────────────────────────
                    Button(
                        onClick  = { viewModel.saveProfile() },
                        enabled  = !state.isSaving,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(Spacing.buttonHeight),
                        shape  = MaterialTheme.shapes.medium,
                        colors = ButtonDefaults.buttonColors(containerColor = TmBlue)
                    ) {
                        if (state.isSaving) {
                            CircularProgressIndicator(
                                modifier    = Modifier.size(Spacing.iconMd),
                                color       = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("Save Changes", style = MaterialTheme.typography.labelLarge)
                        }
                    }

                    Spacer(modifier = Modifier.height(Spacing.xl))
                }
            }
        }
    }
}
