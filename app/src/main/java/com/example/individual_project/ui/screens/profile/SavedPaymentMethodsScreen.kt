package com.example.individual_project.ui.screens.profile

import androidx.compose.foundation.background
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.individual_project.data.model.SavedPaymentMethod
import com.example.individual_project.ui.components.EmptyState
import com.example.individual_project.ui.components.ErrorView
import com.example.individual_project.ui.components.LoadingView
import com.example.individual_project.ui.theme.Spacing
import com.example.individual_project.ui.theme.TmBlue
import com.example.individual_project.ui.theme.TmError
import com.example.individual_project.ui.theme.TmGold
import com.example.individual_project.ui.theme.TmNavyBlue
import com.example.individual_project.ui.viewmodel.PaymentMethod
import com.example.individual_project.ui.viewmodel.PaymentMethodFormState
import com.example.individual_project.ui.viewmodel.SavedPaymentMethodViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SavedPaymentMethodsScreen(
    navController: NavController,
    viewModel    : SavedPaymentMethodViewModel = hiltViewModel()
) {
    val state     by viewModel.uiState.collectAsState()
    val formState by viewModel.formState.collectAsState()
    val busyIds   by viewModel.busyIds.collectAsState()
    var pendingDeleteId by remember { mutableStateOf<String?>(null) }
    val snackbarHost = remember { SnackbarHostState() }

    LaunchedEffect(state.error) {
        state.error?.let {
            snackbarHost.showSnackbar(it)
            viewModel.clearError()
        }
    }

    formState?.let { form ->
        PaymentMethodFormDialog(
            form           = form,
            onProvider     = viewModel::onProviderChange,
            onDisplayName  = viewModel::onDisplayNameChange,
            onMasked       = viewModel::onMaskedIdentifierChange,
            onDismiss      = viewModel::dismissForm,
            onSubmit       = viewModel::submitForm
        )
    }

    pendingDeleteId?.let { id ->
        AlertDialog(
            onDismissRequest = { pendingDeleteId = null },
            title            = { Text("Remove this payment method?") },
            confirmButton    = {
                TextButton(onClick = {
                    viewModel.deleteMethod(id)
                    pendingDeleteId = null
                }) { Text("Remove", color = TmError) }
            },
            dismissButton = {
                TextButton(onClick = { pendingDeleteId = null }) { Text("Keep") }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Payment Methods") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.startAdd() }) {
                        Icon(Icons.Default.Add, contentDescription = "Add payment method", tint = Color.White)
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
                state.error != null && state.methods.isEmpty() -> ErrorView(
                    message = state.error ?: "Failed to load payment methods",
                    onRetry = { viewModel.loadMethods() }
                )
                state.methods.isEmpty() -> EmptyState(
                    emoji       = "💳",
                    title       = "No Payment Methods",
                    subtitle    = "This app uses demo payments only -- nothing here is a real card or charge",
                    actionLabel = "Add Method",
                    onAction    = { viewModel.startAdd() }
                )
                else -> LazyColumn(
                    modifier            = Modifier.fillMaxSize(),
                    contentPadding      = PaddingValues(Spacing.screenHorizontal),
                    verticalArrangement = Arrangement.spacedBy(Spacing.md)
                ) {
                    item { Spacer(modifier = Modifier.height(Spacing.sm)) }
                    items(state.methods, key = { it.id }) { method ->
                        PaymentMethodCard(
                            method       = method,
                            isBusy       = method.id in busyIds,
                            onDelete     = { pendingDeleteId = method.id },
                            onSetDefault = { viewModel.setDefaultMethod(method.id) }
                        )
                    }
                    item { Spacer(modifier = Modifier.height(Spacing.md)) }
                }
            }
        }
    }
}

@Composable
private fun PaymentMethodCard(
    method      : SavedPaymentMethod,
    isBusy      : Boolean,
    onDelete    : () -> Unit,
    onSetDefault: () -> Unit
) {
    Card(
        shape     = MaterialTheme.shapes.large,
        elevation = CardDefaults.cardElevation(2.dp),
        colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier          = Modifier.fillMaxWidth().padding(Spacing.md),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.CreditCard, null,
                tint     = TmBlue,
                modifier = Modifier.size(Spacing.iconLg)
            )
            Spacer(modifier = Modifier.width(Spacing.md))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text       = method.displayName,
                        style      = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    if (method.isDefault) {
                        Spacer(modifier = Modifier.width(Spacing.xs))
                        Text("DEFAULT", style = MaterialTheme.typography.labelSmall, color = TmGold, fontWeight = FontWeight.Bold)
                    }
                }
                Text(
                    text  = "${PaymentMethod.labelFor(method.provider)} •••• ${method.maskedIdentifier}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (isBusy) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
            } else {
                if (!method.isDefault) {
                    IconButton(onClick = onSetDefault) {
                        Icon(Icons.Default.RadioButtonUnchecked, contentDescription = "Set as default", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                } else {
                    Icon(Icons.Default.CheckCircle, contentDescription = "Default method", tint = TmGold)
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Remove", tint = TmError)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PaymentMethodFormDialog(
    form          : PaymentMethodFormState,
    onProvider    : (PaymentMethod) -> Unit,
    onDisplayName : (String) -> Unit,
    onMasked      : (String) -> Unit,
    onDismiss     : () -> Unit,
    onSubmit      : () -> Unit
) {
    AlertDialog(
        onDismissRequest = { if (!form.isSaving) onDismiss() },
        title = { Text("Add Payment Method (Demo)") },
        text = {
            Column {
                Row(horizontalArrangement = Arrangement.spacedBy(Spacing.xs)) {
                    PaymentMethod.entries.forEach { provider ->
                        FilterChip(
                            selected = form.provider == provider,
                            onClick  = { onProvider(provider) },
                            label    = { Text(provider.label, style = MaterialTheme.typography.labelSmall) },
                            enabled  = !form.isSaving
                        )
                    }
                }
                Spacer(modifier = Modifier.height(Spacing.sm))
                OutlinedTextField(
                    value         = form.displayName,
                    onValueChange = onDisplayName,
                    label         = { Text("Label (e.g. My Visa)") },
                    singleLine    = true,
                    enabled       = !form.isSaving,
                    modifier      = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(Spacing.sm))
                OutlinedTextField(
                    value           = form.maskedIdentifier,
                    onValueChange   = onMasked,
                    label           = { Text("Last 4 digits") },
                    singleLine      = true,
                    enabled         = !form.isSaving,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    supportingText  = { Text("Never enter a full card number -- this field only stores 4 digits") },
                    modifier        = Modifier.fillMaxWidth()
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
