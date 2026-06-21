package com.example.individual_project.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.individual_project.data.model.Booking
import com.example.individual_project.domain.repository.BookingRepository
import com.example.individual_project.domain.repository.PaymentRepository
import com.example.individual_project.utils.PaymentBreakdown
import com.example.individual_project.utils.PaymentCalculator
import com.example.individual_project.utils.Resource
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PaymentUiState(
    val booking          : Booking?          = null,
    val breakdown        : PaymentBreakdown? = null,
    val selectedMethod   : String            = PaymentMethod.CASH.key,
    val isLoadingBooking : Boolean           = false,
    val isProcessing     : Boolean           = false,
    val bookingError     : String?           = null,
    val paymentId        : String?           = null,  // non-null = payment succeeded
    val paymentFailed    : Boolean           = false,
    val error            : String?           = null
)

enum class PaymentMethod(val key: String, val label: String) {
    CARD  ("CARD",   "Credit / Debit Card"),
    ESEWA ("ESEWA",  "eSewa"),
    KHALTI("KHALTI", "Khalti"),
    CASH  ("CASH",   "Cash (Simulation)")
}

@HiltViewModel
class PaymentViewModel @Inject constructor(
    private val paymentRepository : PaymentRepository,
    private val bookingRepository : BookingRepository,
    private val firebaseAuth      : FirebaseAuth,
    savedStateHandle              : SavedStateHandle
) : ViewModel() {

    private val bookingId : String = savedStateHandle.get<String>("bookingId") ?: ""
    private val userId    : String get() = firebaseAuth.currentUser?.uid ?: ""

    private val _uiState = MutableStateFlow(PaymentUiState())
    val uiState: StateFlow<PaymentUiState> = _uiState.asStateFlow()

    init {
        if (bookingId.isNotBlank()) loadBooking()
    }

    fun loadBooking() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingBooking = true, bookingError = null) }
            when (val result = bookingRepository.getBookingById(bookingId)) {
                is Resource.Success -> {
                    val booking   = result.data
                    val breakdown = PaymentCalculator.calculate(
                        ticketPrice = booking.pricePerTicket,
                        quantity    = booking.quantity
                    )
                    _uiState.update {
                        it.copy(
                            booking          = booking,
                            breakdown        = breakdown,
                            isLoadingBooking = false
                        )
                    }
                }
                is Resource.Error   -> _uiState.update {
                    it.copy(isLoadingBooking = false, bookingError = result.message)
                }
                else -> Unit
            }
        }
    }

    fun selectPaymentMethod(method: PaymentMethod) {
        _uiState.update { it.copy(selectedMethod = method.key) }
    }

    fun processPayment() {
        val state     = _uiState.value
        val breakdown = state.breakdown ?: return
        if (userId.isBlank()) {
            _uiState.update { it.copy(error = "You must be logged in to pay") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isProcessing = true, error = null, paymentFailed = false) }

            when (val result = paymentRepository.processPayment(
                bookingId     = bookingId,
                userId        = userId,
                subtotal      = breakdown.subtotal,
                tax           = breakdown.tax,
                serviceFee    = breakdown.serviceFee,
                discount      = breakdown.discount,
                totalAmount   = breakdown.total,
                paymentMethod = state.selectedMethod
            )) {
                is Resource.Success -> _uiState.update {
                    it.copy(isProcessing = false, paymentId = result.data)
                }
                is Resource.Error   -> _uiState.update {
                    it.copy(isProcessing = false, paymentFailed = true, error = result.message)
                }
                else -> Unit
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    fun resetPaymentResult() {
        _uiState.update { it.copy(paymentId = null, paymentFailed = false, error = null) }
    }
}
