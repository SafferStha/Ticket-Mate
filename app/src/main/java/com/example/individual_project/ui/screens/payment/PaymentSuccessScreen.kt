package com.example.individual_project.ui.screens.payment

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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material.icons.filled.Receipt
import com.example.individual_project.ui.viewmodel.PaymentMethod
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.example.individual_project.data.model.Payment
import com.example.individual_project.domain.repository.BookingRepository
import com.example.individual_project.domain.repository.PaymentRepository
import com.example.individual_project.ui.navigation.Screen
import com.example.individual_project.ui.theme.Spacing
import com.example.individual_project.ui.theme.TmBlue
import com.example.individual_project.ui.theme.TmGold
import com.example.individual_project.ui.theme.TmSuccess
import com.example.individual_project.utils.PriceFormatter
import com.example.individual_project.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

// ── ViewModel ─────────────────────────────────────────────────────────────────

data class PaymentSuccessUiState(
    val payment      : Payment? = null,
    val eventTitle   : String   = "",
    val venue        : String   = "",
    val date         : String   = "",
    val quantity     : Int      = 0,
    val isLoading    : Boolean  = false,
    val error        : String?  = null
)

@HiltViewModel
class PaymentSuccessViewModel @Inject constructor(
    private val paymentRepository : PaymentRepository,
    private val bookingRepository : BookingRepository,
    savedStateHandle              : SavedStateHandle
) : ViewModel() {

    private val paymentId : String = savedStateHandle.get<String>("paymentId") ?: ""

    private val _state = MutableStateFlow(PaymentSuccessUiState())
    val state: StateFlow<PaymentSuccessUiState> = _state.asStateFlow()

    init {
        if (paymentId.isNotBlank()) load()
    }

    fun load() {
        viewModelScope.launch {
            _state.value = PaymentSuccessUiState(isLoading = true)

            val paymentResult = paymentRepository.fetchPayment(paymentId)
            if (paymentResult is Resource.Error) {
                _state.value = PaymentSuccessUiState(error = paymentResult.message)
                return@launch
            }
            val payment = (paymentResult as Resource.Success).data

            // Load booking to get event details for the summary
            val bookingResult = bookingRepository.getBookingById(payment.bookingId)
            val booking = (bookingResult as? Resource.Success)?.data

            _state.value = PaymentSuccessUiState(
                payment    = payment,
                eventTitle = booking?.eventTitle ?: "",
                venue      = booking?.venue      ?: "",
                date       = booking?.date       ?: "",
                quantity   = booking?.quantity   ?: 0
            )
        }
    }
}

// ── Screen ────────────────────────────────────────────────────────────────────

@Composable
fun PaymentSuccessScreen(
    navController : NavController,
    viewModel     : PaymentSuccessViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    Scaffold { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            when {
                state.isLoading -> CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )

                state.error != null -> Column(
                    modifier            = Modifier
                        .fillMaxSize()
                        .padding(Spacing.screenHorizontal),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text      = state.error!!,
                        style     = MaterialTheme.typography.bodyMedium,
                        color     = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(Spacing.md))
                    Button(onClick = { viewModel.load() }) { Text("Retry") }
                }

                state.payment != null -> {
                    val payment = state.payment!!
                    Column(
                        modifier            = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = Spacing.screenHorizontal),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Spacer(modifier = Modifier.height(Spacing.xxl))

                        // ── Success icon ───────────────────────────────────────
                        Box(
                            modifier         = Modifier
                                .size(100.dp)
                                .clip(CircleShape)
                                .background(TmSuccess.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint     = TmSuccess,
                                modifier = Modifier.size(64.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(Spacing.lg))
                        Text(
                            text       = "Payment Successful!",
                            style      = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            textAlign  = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(Spacing.xs))
                        Text(
                            text      = "Your tickets have been confirmed. Enjoy the event!",
                            style     = MaterialTheme.typography.bodyMedium,
                            color     = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(Spacing.xl))

                        // ── Payment ID chip ────────────────────────────────────
                        Surface(
                            shape          = MaterialTheme.shapes.medium,
                            color          = TmBlue.copy(alpha = 0.1f),
                            tonalElevation = 0.dp
                        ) {
                            Row(
                                modifier          = Modifier.padding(horizontal = Spacing.md, vertical = Spacing.sm),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Receipt, null, tint = TmBlue, modifier = Modifier.size(Spacing.iconMd))
                                Spacer(modifier = Modifier.width(Spacing.sm))
                                Column {
                                    Text(
                                        text  = "Payment ID",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text       = payment.id,
                                        style      = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Medium,
                                        color      = TmBlue
                                    )
                                }
                                Spacer(modifier = Modifier.width(Spacing.lg))
                                Icon(Icons.Default.ConfirmationNumber, null, tint = TmBlue, modifier = Modifier.size(Spacing.iconMd))
                                Spacer(modifier = Modifier.width(Spacing.sm))
                                Column {
                                    Text(
                                        text  = "Booking ID",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text       = payment.bookingId,
                                        style      = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Medium,
                                        color      = TmBlue
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(Spacing.xl))

                        // ── Summary card ───────────────────────────────────────
                        Surface(
                            modifier      = Modifier.fillMaxWidth(),
                            shape         = MaterialTheme.shapes.large,
                            color         = MaterialTheme.colorScheme.surface,
                            tonalElevation = 2.dp
                        ) {
                            Column(modifier = Modifier.padding(Spacing.md)) {
                                if (state.eventTitle.isNotBlank()) {
                                    Text(
                                        text       = state.eventTitle,
                                        style      = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(Spacing.sm))
                                }
                                if (state.venue.isNotBlank())    PaySuccessRow("Venue",    state.venue)
                                if (state.date.isNotBlank())     PaySuccessRow("Date",     state.date)
                                if (state.quantity > 0) PaySuccessRow(
                                    "Tickets", "${state.quantity} ticket${if (state.quantity > 1) "s" else ""}"
                                )
                                HorizontalDivider(modifier = Modifier.padding(vertical = Spacing.sm))
                                PaySuccessRow("Method", PaymentMethod.labelFor(payment.paymentMethod))
                                PaySuccessRow(
                                    label      = "Total Paid",
                                    value      = PriceFormatter.format(payment.totalAmount),
                                    valueColor = TmGold,
                                    bold       = true
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(Spacing.xxl))

                        // ── CTAs ───────────────────────────────────────────────
                        Button(
                            onClick  = {
                                navController.navigate(Screen.MyTickets.route) {
                                    popUpTo(Screen.Dashboard.route) { inclusive = false }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(Spacing.buttonHeight),
                            shape  = MaterialTheme.shapes.medium,
                            colors = ButtonDefaults.buttonColors(containerColor = TmBlue)
                        ) {
                            Text("View My Tickets", style = MaterialTheme.typography.labelLarge)
                        }

                        Spacer(modifier = Modifier.height(Spacing.sm))

                        OutlinedButton(
                            onClick  = {
                                navController.navigate(Screen.Dashboard.route) {
                                    popUpTo(Screen.Dashboard.route) { inclusive = true }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(Spacing.buttonHeight),
                            shape = MaterialTheme.shapes.medium
                        ) {
                            Text("Back to Home", style = MaterialTheme.typography.labelLarge)
                        }

                        Spacer(modifier = Modifier.height(Spacing.xl))
                    }
                }
            }
        }
    }
}

@Composable
private fun PaySuccessRow(
    label      : String,
    value      : String,
    valueColor : Color   = Color.Unspecified,
    bold       : Boolean = false
) {
    Row(
        modifier              = Modifier
            .fillMaxWidth()
            .padding(vertical = Spacing.xs),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment     = Alignment.CenterVertically
    ) {
        Text(
            text  = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text       = value,
            style      = MaterialTheme.typography.bodySmall,
            color      = if (valueColor == Color.Unspecified) MaterialTheme.colorScheme.onSurface else valueColor,
            fontWeight = if (bold) FontWeight.SemiBold else FontWeight.Normal
        )
    }
}
