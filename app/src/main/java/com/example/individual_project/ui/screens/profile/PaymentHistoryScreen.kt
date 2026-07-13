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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.example.individual_project.data.model.Payment
import com.example.individual_project.data.model.PaymentStatus
import com.example.individual_project.domain.repository.PaymentRepository
import com.example.individual_project.ui.components.EmptyState
import com.example.individual_project.ui.components.ErrorView
import com.example.individual_project.ui.components.LoadingView
import com.example.individual_project.ui.model.UiState
import com.example.individual_project.ui.theme.Spacing
import com.example.individual_project.ui.theme.TmBlue
import com.example.individual_project.ui.theme.TmError
import com.example.individual_project.ui.theme.TmGold
import com.example.individual_project.ui.theme.TmNavyBlue
import com.example.individual_project.ui.theme.TmSuccess
import com.example.individual_project.ui.theme.TmWarning
import com.example.individual_project.utils.PriceFormatter
import com.example.individual_project.utils.Resource
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

// ── ViewModel ─────────────────────────────────────────────────────────────────

@HiltViewModel
class PaymentHistoryViewModel @Inject constructor(
    private val paymentRepository: PaymentRepository,
    private val firebaseAuth     : FirebaseAuth
) : ViewModel() {

    private val uid: String get() = firebaseAuth.currentUser?.uid ?: ""

    private val _state = MutableStateFlow(UiState<List<Payment>>())
    val state: StateFlow<UiState<List<Payment>>> = _state.asStateFlow()

    init {
        loadPayments()
    }

    fun loadPayments() {
        if (uid.isBlank()) {
            _state.value = UiState(error = "Not logged in")
            return
        }
        viewModelScope.launch {
            _state.value = UiState(isLoading = true)
            _state.value = when (val r = paymentRepository.fetchPaymentsByUser(uid)) {
                is Resource.Success -> UiState(data = r.data)
                is Resource.Error   -> UiState(error = r.message)
                else                -> UiState(error = "Failed to load payment history")
            }
        }
    }
}

// ── Screen ────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentHistoryScreen(
    navController: NavController,
    viewModel    : PaymentHistoryViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Payment History") },
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
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            when {
                state.isLoading -> LoadingView()
                state.hasError  -> ErrorView(
                    message = state.error ?: "Failed to load payments",
                    onRetry = { viewModel.loadPayments() }
                )
                state.data != null -> {
                    val payments = state.data!!
                    if (payments.isEmpty()) {
                        EmptyState(
                            emoji    = "💳",
                            title    = "No Payments Yet",
                            subtitle = "Your payment history will appear here"
                        )
                    } else {
                        LazyColumn(
                            modifier            = Modifier.fillMaxSize(),
                            contentPadding      = PaddingValues(Spacing.screenHorizontal),
                            verticalArrangement = Arrangement.spacedBy(Spacing.md)
                        ) {
                            item { Spacer(modifier = Modifier.height(Spacing.sm)) }
                            items(payments, key = { it.id }) { payment ->
                                PaymentHistoryCard(payment = payment)
                            }
                            item { Spacer(modifier = Modifier.height(Spacing.md)) }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PaymentHistoryCard(payment: Payment) {
    val statusColor = when (payment.paymentStatus) {
        PaymentStatus.SUCCESS.name  -> TmSuccess
        PaymentStatus.FAILED.name   -> TmError
        PaymentStatus.REFUNDED.name -> TmWarning
        else                        -> TmBlue
    }

    Surface(
        modifier        = Modifier.fillMaxWidth(),
        shape           = MaterialTheme.shapes.large,
        color           = MaterialTheme.colorScheme.surface,
        tonalElevation  = 2.dp,
        shadowElevation = 1.dp
    ) {
        Column(modifier = Modifier.padding(Spacing.md)) {
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = MaterialTheme.shapes.medium,
                        color = TmBlue.copy(alpha = 0.1f)
                    ) {
                        Icon(
                            Icons.Default.Receipt, null,
                            tint     = TmBlue,
                            modifier = Modifier
                                .padding(Spacing.sm)
                                .size(Spacing.iconMd)
                        )
                    }
                    Column(modifier = Modifier.padding(start = Spacing.md)) {
                        Text(
                            text       = payment.paymentMethod,
                            style      = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text  = formatDate(payment.transactionDate),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text       = PriceFormatter.format(payment.totalAmount),
                        style      = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color      = TmGold
                    )
                    Surface(
                        shape = MaterialTheme.shapes.small,
                        color = statusColor.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text     = payment.paymentStatus,
                            style    = MaterialTheme.typography.labelSmall,
                            color    = statusColor,
                            modifier = Modifier.padding(
                                horizontal = Spacing.sm,
                                vertical   = Spacing.xxs
                            )
                        )
                    }
                }
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = Spacing.sm),
                color    = MaterialTheme.colorScheme.outlineVariant
            )

            Text(
                text  = "Ref: ${payment.id.take(20)}…",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private fun formatDate(epochMs: Long): String {
    if (epochMs == 0L) return ""
    return SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
        .format(Date(epochMs))
}
