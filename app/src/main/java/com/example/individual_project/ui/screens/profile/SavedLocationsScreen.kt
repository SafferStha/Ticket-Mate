package com.example.individual_project.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.individual_project.data.model.SavedLocation
import com.example.individual_project.ui.components.EmptyState
import com.example.individual_project.ui.components.ErrorView
import com.example.individual_project.ui.components.LoadingView
import com.example.individual_project.ui.theme.Spacing
import com.example.individual_project.ui.theme.TmBlue
import com.example.individual_project.ui.theme.TmError
import com.example.individual_project.ui.theme.TmGold
import com.example.individual_project.ui.theme.TmNavyBlue
import com.example.individual_project.ui.viewmodel.SavedLocationFormState
import com.example.individual_project.ui.viewmodel.SavedLocationViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SavedLocationsScreen(
    navController: NavController,
    viewModel    : SavedLocationViewModel = hiltViewModel()
) {
    val state       by viewModel.uiState.collectAsState()
    val formState   by viewModel.formState.collectAsState()
    val busyIds     by viewModel.busyIds.collectAsState()
    var pendingDeleteId by remember { mutableStateOf<String?>(null) }
    val snackbarHost = remember { SnackbarHostState() }

    LaunchedEffect(state.error) {
        state.error?.let {
            snackbarHost.showSnackbar(it)
            viewModel.clearError()
        }
    }

    formState?.let { form ->
        LocationFormDialog(
            form       = form,
            onLabel    = viewModel::onLabelChange,
            onAddress  = viewModel::onAddressChange,
            onCity     = viewModel::onCityChange,
            onDismiss  = viewModel::dismissForm,
            onSubmit   = viewModel::submitForm
        )
    }

    pendingDeleteId?.let { id ->
        AlertDialog(
            onDismissRequest = { pendingDeleteId = null },
            title            = { Text("Delete this location?") },
            text             = { Text("This can't be undone.") },
            confirmButton    = {
                TextButton(onClick = {
                    viewModel.deleteLocation(id)
                    pendingDeleteId = null
                }) { Text("Delete", color = TmError) }
            },
            dismissButton = {
                TextButton(onClick = { pendingDeleteId = null }) { Text("Keep") }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Saved Locations") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.startAdd() }) {
                        Icon(Icons.Default.Add, contentDescription = "Add location", tint = Color.White)
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
                .background(MaterialTheme.colorScheme.background)
        ) {
            when {
                state.isLoading -> LoadingView()
                state.error != null && state.locations.isEmpty() -> ErrorView(
                    message = state.error ?: "Failed to load saved locations",
                    onRetry = { viewModel.loadLocations() }
                )
                state.locations.isEmpty() -> EmptyState(
                    emoji       = "📍",
                    title       = "No Saved Locations",
                    subtitle    = "Save addresses you use often for faster checkout",
                    actionLabel = "Add Location",
                    onAction    = { viewModel.startAdd() }
                )
                else -> LazyColumn(
                    modifier            = Modifier.fillMaxSize(),
                    contentPadding      = PaddingValues(Spacing.screenHorizontal),
                    verticalArrangement = Arrangement.spacedBy(Spacing.md)
                ) {
                    item { Spacer(modifier = Modifier.height(Spacing.sm)) }
                    items(state.locations, key = { it.id }) { location ->
                        SavedLocationCard(
                            location    = location,
                            isBusy      = location.id in busyIds,
                            onEdit      = { viewModel.startEdit(location) },
                            onDelete    = { pendingDeleteId = location.id },
                            onSetDefault = { viewModel.setDefaultLocation(location.id) }
                        )
                    }
                    item { Spacer(modifier = Modifier.height(Spacing.md)) }
                }
            }
        }
    }
}

@Composable
private fun SavedLocationCard(
    location    : SavedLocation,
    isBusy      : Boolean,
    onEdit      : () -> Unit,
    onDelete    : () -> Unit,
    onSetDefault: () -> Unit
) {
    Card(
        shape     = MaterialTheme.shapes.large,
        elevation = CardDefaults.cardElevation(2.dp),
        colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier          = Modifier
                .fillMaxWidth()
                .padding(Spacing.md),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.LocationOn, null,
                tint     = TmBlue,
                modifier = Modifier.size(Spacing.iconLg)
            )
            Spacer(modifier = Modifier.width(Spacing.md))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text       = location.label,
                        style      = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    if (location.isDefault) {
                        Spacer(modifier = Modifier.width(Spacing.xs))
                        Text(
                            text  = "DEFAULT",
                            style = MaterialTheme.typography.labelSmall,
                            color = TmGold,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Text(
                    text  = "${location.address}, ${location.city}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (isBusy) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
            } else {
                if (!location.isDefault) {
                    IconButton(onClick = onSetDefault) {
                        Icon(
                            Icons.Default.RadioButtonUnchecked, contentDescription = "Set as default",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    Icon(
                        Icons.Default.CheckCircle, contentDescription = "Default location",
                        tint = TmGold
                    )
                }
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit", tint = TmBlue)
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = TmError)
                }
            }
        }
    }
}

@Composable
private fun LocationFormDialog(
    form      : SavedLocationFormState,
    onLabel   : (String) -> Unit,
    onAddress : (String) -> Unit,
    onCity    : (String) -> Unit,
    onDismiss : () -> Unit,
    onSubmit  : () -> Unit
) {
    AlertDialog(
        onDismissRequest = { if (!form.isSaving) onDismiss() },
        title = { Text(if (form.editingId != null) "Edit Location" else "Add Location") },
        text = {
            Column {
                OutlinedTextField(
                    value         = form.label,
                    onValueChange = onLabel,
                    label         = { Text("Label (e.g. Home, Work)") },
                    singleLine    = true,
                    enabled       = !form.isSaving,
                    modifier      = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(Spacing.sm))
                OutlinedTextField(
                    value         = form.address,
                    onValueChange = onAddress,
                    label         = { Text("Address") },
                    singleLine    = true,
                    enabled       = !form.isSaving,
                    modifier      = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(Spacing.sm))
                OutlinedTextField(
                    value         = form.city,
                    onValueChange = onCity,
                    label         = { Text("City") },
                    singleLine    = true,
                    enabled       = !form.isSaving,
                    modifier      = Modifier.fillMaxWidth()
                )
                if (form.error != null) {
                    Spacer(modifier = Modifier.height(Spacing.sm))
                    Text(form.error, color = TmError, style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onSubmit, enabled = !form.isSaving) {
                if (form.isSaving) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                } else {
                    Text("Save")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !form.isSaving) { Text("Cancel") }
        }
    )
}
