package com.example.individual_project.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.individual_project.data.model.Booking
import com.example.individual_project.data.model.Event
import com.example.individual_project.domain.repository.BookingRepository
import com.example.individual_project.domain.repository.EventRepository
import com.example.individual_project.utils.Resource
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class BookingUiState(
    val event          : Event?  = null,
    val quantity       : Int     = 1,
    val totalPrice     : Double  = 0.0,
    val isLoadingEvent : Boolean = false,
    val isBooking      : Boolean = false,
    val eventError     : String? = null,
    val bookingError   : String? = null,
    val bookingId      : String? = null   // non-null = booking confirmed
)

@HiltViewModel
class BookingViewModel @Inject constructor(
    private val eventRepository   : EventRepository,
    private val bookingRepository  : BookingRepository,
    private val firebaseAuth       : FirebaseAuth,
    savedStateHandle               : SavedStateHandle
) : ViewModel() {

    private val eventId : String = savedStateHandle.get<String>("eventId") ?: ""
    private val userId  : String get() = firebaseAuth.currentUser?.uid ?: ""

    private val _uiState = MutableStateFlow(BookingUiState())
    val uiState: StateFlow<BookingUiState> = _uiState.asStateFlow()

    init {
        if (eventId.isNotBlank()) loadEvent()
    }

    fun loadEvent() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingEvent = true, eventError = null) }
            when (val result = eventRepository.fetchEventDetails(eventId)) {
                is Resource.Success -> _uiState.update { state ->
                    state.copy(
                        event          = result.data,
                        isLoadingEvent = false,
                        totalPrice     = result.data.price * state.quantity
                    )
                }
                is Resource.Error   -> _uiState.update {
                    it.copy(isLoadingEvent = false, eventError = result.message)
                }
                else -> Unit
            }
        }
    }

    fun increaseQuantity() {
        val state = _uiState.value
        val max   = state.event?.availableSeats ?: 1
        if (state.quantity >= max) return
        val newQty = state.quantity + 1
        _uiState.update { it.copy(quantity = newQty, totalPrice = (it.event?.price ?: 0.0) * newQty) }
    }

    fun decreaseQuantity() {
        val state = _uiState.value
        if (state.quantity <= 1) return
        val newQty = state.quantity - 1
        _uiState.update { it.copy(quantity = newQty, totalPrice = (it.event?.price ?: 0.0) * newQty) }
    }

    fun confirmBooking() {
        val state = _uiState.value
        val event = state.event ?: return
        if (userId.isBlank()) {
            _uiState.update { it.copy(bookingError = "You must be logged in to book tickets") }
            return
        }
        if (state.quantity > event.availableSeats) {
            _uiState.update { it.copy(bookingError = "Not enough seats available") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isBooking = true, bookingError = null) }

            val booking = Booking(
                userId         = userId,
                eventId        = event.id,
                eventTitle     = event.title,
                eventImage     = event.imageUrl,
                venue          = "${event.venue}, ${event.city}",
                date           = event.date,
                quantity       = state.quantity,
                pricePerTicket = event.price,
                totalPrice     = state.totalPrice
            )

            when (val result = bookingRepository.bookTicket(booking)) {
                is Resource.Success -> _uiState.update {
                    it.copy(isBooking = false, bookingId = result.data)
                }
                is Resource.Error   -> _uiState.update {
                    it.copy(isBooking = false, bookingError = result.message)
                }
                else -> Unit
            }
        }
    }

    fun clearBookingError() {
        _uiState.update { it.copy(bookingError = null) }
    }

    fun resetBookingResult() {
        _uiState.update { it.copy(bookingId = null, bookingError = null) }
    }
}
