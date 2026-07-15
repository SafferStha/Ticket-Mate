package com.example.individual_project.ui.screens.admin

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.individual_project.ui.components.LoadingView
import com.example.individual_project.ui.theme.Spacing
import com.example.individual_project.ui.theme.TmBlue
import com.example.individual_project.ui.theme.TmError
import com.example.individual_project.ui.theme.TmNavyBlue
import com.example.individual_project.ui.viewmodel.AdminEventViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminEventFormScreen(
    navController: NavController,
    viewModel    : AdminEventViewModel = hiltViewModel()
) {
    val form by viewModel.formState.collectAsState()
    val snackbarHost = remember { SnackbarHostState() }
    val isEditing = form.editingId.isNotBlank()

    LaunchedEffect(form.saveSuccess) {
        if (form.saveSuccess) {
            snackbarHost.showSnackbar(if (isEditing) "Event updated" else "Event created")
            navController.popBackStack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEditing) "Edit Event" else "Create Event") },
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
        if (form.isLoading) {
            LoadingView()
            return@Scaffold
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Spacing.screenHorizontal)
        ) {
            Spacer(modifier = Modifier.height(Spacing.lg))

            OutlinedTextField(
                value = form.title, onValueChange = { v -> viewModel.onFieldChange { it.copy(title = v) } },
                label = { Text("Title *") }, singleLine = true, enabled = !form.isSaving,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(Spacing.sm))

            OutlinedTextField(
                value = form.description, onValueChange = { v -> viewModel.onFieldChange { it.copy(description = v) } },
                label = { Text("Description") }, enabled = !form.isSaving,
                modifier = Modifier.fillMaxWidth(), minLines = 3
            )
            Spacer(modifier = Modifier.height(Spacing.sm))

            OutlinedTextField(
                value = form.category, onValueChange = { v -> viewModel.onFieldChange { it.copy(category = v) } },
                label = { Text("Category (e.g. Concerts, Sports)") }, singleLine = true, enabled = !form.isSaving,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(Spacing.sm))

            OutlinedTextField(
                value = form.venue, onValueChange = { v -> viewModel.onFieldChange { it.copy(venue = v) } },
                label = { Text("Venue *") }, singleLine = true, enabled = !form.isSaving,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(Spacing.sm))

            OutlinedTextField(
                value = form.city, onValueChange = { v -> viewModel.onFieldChange { it.copy(city = v) } },
                label = { Text("City *") }, singleLine = true, enabled = !form.isSaving,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(Spacing.sm))

            Row {
                OutlinedTextField(
                    value = form.date, onValueChange = { v -> viewModel.onFieldChange { it.copy(date = v) } },
                    label = { Text("Date * (yyyy-MM-dd)") }, singleLine = true, enabled = !form.isSaving,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(Spacing.sm))
                OutlinedTextField(
                    value = form.time, onValueChange = { v -> viewModel.onFieldChange { it.copy(time = v) } },
                    label = { Text("Time (HH:mm)") }, singleLine = true, enabled = !form.isSaving,
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(modifier = Modifier.height(Spacing.sm))

            OutlinedTextField(
                value = form.imageUrl, onValueChange = { v -> viewModel.onFieldChange { it.copy(imageUrl = v) } },
                label = { Text("Image URL") }, singleLine = true, enabled = !form.isSaving,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(Spacing.sm))

            Row {
                OutlinedTextField(
                    value = form.price, onValueChange = { v -> viewModel.onFieldChange { it.copy(price = v) } },
                    label = { Text("Price *") }, singleLine = true, enabled = !form.isSaving,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(Spacing.sm))
                OutlinedTextField(
                    value = form.availableSeats, onValueChange = { v -> viewModel.onFieldChange { it.copy(availableSeats = v) } },
                    label = { Text("Seats *") }, singleLine = true, enabled = !form.isSaving,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(modifier = Modifier.height(Spacing.sm))

            OutlinedTextField(
                value = form.organizer, onValueChange = { v -> viewModel.onFieldChange { it.copy(organizer = v) } },
                label = { Text("Organizer") }, singleLine = true, enabled = !form.isSaving,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(Spacing.sm))

            Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                Checkbox(
                    checked = form.featured,
                    onCheckedChange = { v -> viewModel.onFieldChange { it.copy(featured = v) } },
                    enabled = !form.isSaving
                )
                Text("Featured event", style = MaterialTheme.typography.bodyMedium)
            }

            if (form.error != null) {
                Spacer(modifier = Modifier.height(Spacing.sm))
                Text(form.error ?: "", color = TmError, style = MaterialTheme.typography.bodySmall)
            }

            Spacer(modifier = Modifier.height(Spacing.lg))

            Button(
                onClick = { viewModel.submitForm() },
                enabled = !form.isSaving,
                modifier = Modifier.fillMaxWidth().height(Spacing.buttonHeight),
                shape = MaterialTheme.shapes.medium,
                colors = ButtonDefaults.buttonColors(containerColor = TmBlue)
            ) {
                if (form.isSaving) {
                    CircularProgressIndicator(modifier = Modifier.size(Spacing.iconMd), color = Color.White, strokeWidth = 2.dp)
                } else {
                    Text(if (isEditing) "Save Changes" else "Create Event")
                }
            }

            Spacer(modifier = Modifier.height(Spacing.xl))
        }
    }
}
