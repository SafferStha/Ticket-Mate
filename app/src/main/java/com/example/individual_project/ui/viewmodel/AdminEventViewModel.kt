package com.example.individual_project.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.individual_project.auth.AdminStateManager
import com.example.individual_project.data.model.Event
import com.example.individual_project.domain.repository.EventRepository
import com.example.individual_project.ui.model.UiState
import com.example.individual_project.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

// ── Admin dashboard (event list) ────────────────────────────────────────────

data class AdminDashboardUiState(
    val events   : List<Event> = emptyList(),
    val isLoading: Boolean     = false,
    val error    : String?     = null
)

// ── Admin event create/edit form ────────────────────────────────────────────

data class AdminEventFormState(
    val editingId      : String  = "",     // blank = creating a new event
    val title           : String  = "",
    val description      : String  = "",
    val category         : String  = "",
    val venue            : String  = "",
    val city              : String  = "",
    val date              : String  = "",   // e.g. "2026-08-20" -- see DateFormatter for accepted formats
    val time               : String  = "",   // e.g. "18:30"
    val imageUrl          : String  = "",
    val price             : String  = "0",
    val availableSeats    : String  = "0",
    val organizer         : String  = "",
    val featured          : Boolean = false,
    val isLoading         : Boolean = false,
    val isSaving          : Boolean = false,
    val saveSuccess       : Boolean = false,
    val error             : String? = null
)

@HiltViewModel
class AdminEventViewModel @Inject constructor(
    private val eventRepository  : EventRepository,
    private val adminStateManager: AdminStateManager,
    savedStateHandle              : SavedStateHandle
) : ViewModel() {

    private val editingEventId: String? = savedStateHandle.get<String>("eventId")

    // ── Dashboard ─────────────────────────────────────────────────────────────
    private val _dashboardState = MutableStateFlow(AdminDashboardUiState())
    val dashboardState: StateFlow<AdminDashboardUiState> = _dashboardState.asStateFlow()

    // ── Form ──────────────────────────────────────────────────────────────────
    private val _formState = MutableStateFlow(AdminEventFormState(editingId = editingEventId ?: ""))
    val formState: StateFlow<AdminEventFormState> = _formState.asStateFlow()

    init {
        loadEvents()
        if (!editingEventId.isNullOrBlank()) loadEventForEdit(editingEventId)
    }

    fun loadEvents() {
        viewModelScope.launch {
            _dashboardState.update { it.copy(isLoading = true, error = null) }
            when (val result = eventRepository.fetchEvents()) {
                is Resource.Success -> _dashboardState.update { it.copy(events = result.data, isLoading = false) }
                is Resource.Error   -> _dashboardState.update { it.copy(isLoading = false, error = result.message) }
                else -> Unit
            }
        }
    }

    fun deleteEvent(eventId: String) {
        if (!adminStateManager.isAdmin.value) return
        viewModelScope.launch {
            when (val result = eventRepository.deleteEvent(eventId)) {
                is Resource.Error -> _dashboardState.update { it.copy(error = result.message) }
                else              -> loadEvents()
            }
        }
    }

    private fun loadEventForEdit(eventId: String) {
        viewModelScope.launch {
            _formState.update { it.copy(isLoading = true) }
            when (val result = eventRepository.fetchEventDetails(eventId)) {
                is Resource.Success -> {
                    val e = result.data
                    _formState.update {
                        it.copy(
                            title           = e.title,
                            description     = e.description,
                            category        = e.category,
                            venue           = e.venue,
                            city            = e.city,
                            date            = e.date,
                            time            = e.time,
                            imageUrl        = e.imageUrl,
                            price           = e.price.toString(),
                            availableSeats  = e.availableSeats.toString(),
                            organizer       = e.organizer,
                            featured        = e.featured,
                            isLoading       = false
                        )
                    }
                }
                is Resource.Error -> _formState.update { it.copy(isLoading = false, error = result.message) }
                else -> Unit
            }
        }
    }

    fun onFieldChange(update: (AdminEventFormState) -> AdminEventFormState) {
        _formState.update { update(it).copy(error = null) }
    }

    fun submitForm() {
        if (!adminStateManager.isAdmin.value) {
            _formState.update { it.copy(error = "Admin privileges required.") }
            return
        }
        val form = _formState.value
        if (form.isSaving) return

        if (form.title.isBlank() || form.venue.isBlank() || form.city.isBlank() || form.date.isBlank()) {
            _formState.update { it.copy(error = "Title, venue, city, and date are required.") }
            return
        }
        val price = form.price.toDoubleOrNull()
        if (price == null || price < 0) {
            _formState.update { it.copy(error = "Price must be a non-negative number.") }
            return
        }
        val seats = form.availableSeats.toIntOrNull()
        if (seats == null || seats < 0) {
            _formState.update { it.copy(error = "Available seats must be a non-negative whole number.") }
            return
        }

        viewModelScope.launch {
            _formState.update { it.copy(isSaving = true, error = null) }
            val event = Event(
                id             = form.editingId,
                title          = form.title.trim(),
                description    = form.description.trim(),
                category       = form.category.trim(),
                venue          = form.venue.trim(),
                city           = form.city.trim(),
                date           = form.date.trim(),
                time           = form.time.trim(),
                imageUrl       = form.imageUrl.trim(),
                price          = price,
                availableSeats = seats,
                organizer      = form.organizer.trim(),
                featured       = form.featured
            )
            val result = if (form.editingId.isNotBlank()) {
                eventRepository.updateEvent(event)
            } else {
                eventRepository.createEvent(event)
            }
            when (result) {
                is Resource.Error -> _formState.update { it.copy(isSaving = false, error = result.message) }
                else              -> _formState.update { it.copy(isSaving = false, saveSuccess = true) }
            }
        }
    }
}
