package com.example.individual_project.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.individual_project.data.model.Event
import com.example.individual_project.domain.repository.EventRepository
import com.example.individual_project.domain.repository.UserRepository
import com.example.individual_project.ui.model.UiState
import com.example.individual_project.utils.Resource
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EventDetailViewModel @Inject constructor(
    private val eventRepository : EventRepository,
    private val userRepository  : UserRepository,
    private val firebaseAuth    : FirebaseAuth,
    savedStateHandle            : SavedStateHandle
) : ViewModel() {

    private val eventId : String = savedStateHandle.get<String>("eventId") ?: ""
    private val userId  : String get() = firebaseAuth.currentUser?.uid ?: ""

    // ── Event ──────────────────────────────────────────────────────────────────
    private val _eventState = MutableStateFlow(UiState<Event>())
    val eventState: StateFlow<UiState<Event>> = _eventState.asStateFlow()

    // ── Related events ─────────────────────────────────────────────────────────
    private val _relatedEventsState = MutableStateFlow(UiState<List<Event>>())
    val relatedEventsState: StateFlow<UiState<List<Event>>> = _relatedEventsState.asStateFlow()

    // ── Favorites ──────────────────────────────────────────────────────────────
    private val _isFavorite = MutableStateFlow(false)
    val isFavorite: StateFlow<Boolean> = _isFavorite.asStateFlow()

    // Prevents double-taps while Firebase write is in-flight
    private val _favoriteLoading = MutableStateFlow(false)
    val favoriteLoading: StateFlow<Boolean> = _favoriteLoading.asStateFlow()

    // ── Favorites for the "You May Also Like" carousel ───────────────────────
    private val _relatedFavoriteIds = MutableStateFlow<Set<String>>(emptySet())
    val relatedFavoriteIds: StateFlow<Set<String>> = _relatedFavoriteIds.asStateFlow()

    private val _relatedFavoriteLoadingIds = MutableStateFlow<Set<String>>(emptySet())
    val relatedFavoriteLoadingIds: StateFlow<Set<String>> = _relatedFavoriteLoadingIds.asStateFlow()

    init {
        if (eventId.isNotBlank()) {
            loadEvent()
            if (userId.isNotBlank()) {
                checkFavoriteStatus()
                loadRelatedFavoriteIds()
            }
        }
    }

    /** Optimistically flips a related event's favorite state, rolling back on failure. */
    fun toggleRelatedFavorite(relatedEventId: String) {
        if (userId.isBlank() || relatedEventId in _relatedFavoriteLoadingIds.value) return
        val wasFavorite = relatedEventId in _relatedFavoriteIds.value
        viewModelScope.launch {
            _relatedFavoriteIds.update { if (wasFavorite) it - relatedEventId else it + relatedEventId }
            _relatedFavoriteLoadingIds.update { it + relatedEventId }
            val result = eventRepository.toggleFavorite(relatedEventId, userId)
            if (result is Resource.Error) {
                _relatedFavoriteIds.update { if (wasFavorite) it + relatedEventId else it - relatedEventId }
            }
            _relatedFavoriteLoadingIds.update { it - relatedEventId }
        }
    }

    private fun loadRelatedFavoriteIds() {
        viewModelScope.launch {
            val result = userRepository.getFavoriteEventIds(userId)
            if (result is Resource.Success) _relatedFavoriteIds.value = result.data.toSet()
        }
    }

    // ── Public: called on error-retry ──────────────────────────────────────────
    fun loadEvent() {
        viewModelScope.launch {
            _eventState.value = UiState(isLoading = true)
            val result = eventRepository.fetchEventDetails(eventId)
            _eventState.value = when (result) {
                is Resource.Success -> UiState(data = result.data)
                is Resource.Error   -> UiState(error = result.message)
                else                -> UiState(error = "Unexpected error loading event")
            }
            if (result is Resource.Success && result.data.category.isNotBlank()) {
                loadRelatedEvents(result.data.category)
            }
        }
    }

    // ── Optimistic toggle: flips local state immediately, reverts on failure ───
    fun toggleFavorite() {
        if (userId.isBlank() || _favoriteLoading.value) return
        val previous = _isFavorite.value
        viewModelScope.launch {
            _isFavorite.value    = !previous   // optimistic flip
            _favoriteLoading.value = true
            val result = eventRepository.toggleFavorite(eventId, userId)
            if (result is Resource.Error) {
                _isFavorite.value = previous   // revert on failure
            }
            _favoriteLoading.value = false
        }
    }

    // ── Private helpers ────────────────────────────────────────────────────────
    private fun loadRelatedEvents(category: String) {
        viewModelScope.launch {
            _relatedEventsState.value = UiState(isLoading = true)
            _relatedEventsState.value = when (val r = eventRepository.filterByCategory(category)) {
                is Resource.Success -> UiState(data = r.data.filter { it.id != eventId })
                is Resource.Error   -> UiState(error = r.message)
                else                -> UiState(error = "Failed to load related events")
            }
        }
    }

    private fun checkFavoriteStatus() {
        viewModelScope.launch {
            val result = eventRepository.isFavorite(eventId, userId)
            if (result is Resource.Success) {
                _isFavorite.value = result.data
            }
        }
    }
}
