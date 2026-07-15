package com.example.individual_project.ui.viewmodel

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
class HomeViewModel @Inject constructor(
    private val eventRepository : EventRepository,
    private val userRepository  : UserRepository,
    private val auth            : FirebaseAuth
) : ViewModel() {

    private val userId: String get() = auth.currentUser?.uid ?: ""

    // ── Favorites (shared across every list on this screen) ─────────────────────
    private val _favoriteIds = MutableStateFlow<Set<String>>(emptySet())
    val favoriteIds: StateFlow<Set<String>> = _favoriteIds.asStateFlow()

    private val _favoriteLoadingIds = MutableStateFlow<Set<String>>(emptySet())
    val favoriteLoadingIds: StateFlow<Set<String>> = _favoriteLoadingIds.asStateFlow()

    // ── Featured events (horizontal carousel) ──────────────────────────────────
    private val _featuredState = MutableStateFlow(UiState<List<Event>>())
    val featuredState: StateFlow<UiState<List<Event>>> = _featuredState.asStateFlow()

    // ── Trending Now (horizontal carousel) ─────────────────────────────────────
    private val _trendingState = MutableStateFlow(UiState<List<Event>>())
    val trendingState: StateFlow<UiState<List<Event>>> = _trendingState.asStateFlow()

    // ── Recommended for You (horizontal carousel, shown when non-empty) ─────────
    private val _recommendedState = MutableStateFlow(UiState<List<Event>>())
    val recommendedState: StateFlow<UiState<List<Event>>> = _recommendedState.asStateFlow()

    // ── Near You (horizontal carousel, driven by selectedCity) ─────────────────
    private val _nearbyState = MutableStateFlow(UiState<List<Event>>())
    val nearbyState: StateFlow<UiState<List<Event>>> = _nearbyState.asStateFlow()

    private val _selectedCity = MutableStateFlow("")
    val selectedCity: StateFlow<String> = _selectedCity.asStateFlow()

    val availableCities = listOf("Kathmandu", "Pokhara", "Lalitpur", "Butwal", "Biratnagar")

    // ── All / category-filtered events (vertical list) ─────────────────────────
    private val _eventsState = MutableStateFlow(UiState<List<Event>>())
    val eventsState: StateFlow<UiState<List<Event>>> = _eventsState.asStateFlow()

    // ── Categories ─────────────────────────────────────────────────────────────
    val categories: StateFlow<List<String>> = MutableStateFlow(
        listOf("All", "Concerts", "Sports", "Theater", "Comedy", "Family", "Festivals")
    ).asStateFlow()

    private val _selectedCategory = MutableStateFlow("All")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    // ── Search (kept for HomeScreen search bar redirect awareness) ─────────────
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _searchResults = MutableStateFlow(UiState<List<Event>>())
    val searchResults: StateFlow<UiState<List<Event>>> = _searchResults.asStateFlow()

    init {
        loadFeaturedEvents()
        loadAllEvents()
        loadTrendingEvents()
        loadRecommendedEvents()
        loadFavoriteIds()
    }

    // ── Public actions ─────────────────────────────────────────────────────────

    /** Optimistically flips [eventId]'s favorite state, rolling back on failure. Shared by
     *  every list on this screen since they all read from the same [favoriteIds] set. */
    fun toggleFavorite(eventId: String) {
        if (userId.isBlank() || eventId in _favoriteLoadingIds.value) return
        val wasFavorite = eventId in _favoriteIds.value
        viewModelScope.launch {
            _favoriteIds.update { if (wasFavorite) it - eventId else it + eventId }
            _favoriteLoadingIds.update { it + eventId }
            val result = eventRepository.toggleFavorite(eventId, userId)
            if (result is Resource.Error) {
                _favoriteIds.update { if (wasFavorite) it + eventId else it - eventId }
            }
            _favoriteLoadingIds.update { it - eventId }
        }
    }

    private fun loadFavoriteIds() {
        if (userId.isBlank()) return
        viewModelScope.launch {
            val result = userRepository.getFavoriteEventIds(userId)
            if (result is Resource.Success) _favoriteIds.value = result.data.toSet()
        }
    }

    fun selectCategory(category: String) {
        _selectedCategory.value = category
        if (category == "All") loadAllEvents() else loadByCategory(category)
    }

    fun selectCity(city: String) {
        _selectedCity.value = if (_selectedCity.value == city) "" else city
        if (_selectedCity.value.isNotEmpty()) {
            loadNearbyEvents(_selectedCity.value)
        } else {
            _nearbyState.value = UiState()
        }
    }

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
        if (query.isBlank()) {
            _searchResults.value = UiState()
        } else {
            viewModelScope.launch {
                _searchResults.value = UiState(isLoading = true)
                _searchResults.value = when (val r = eventRepository.searchEvents(query)) {
                    is Resource.Success -> UiState(data = r.data)
                    is Resource.Error   -> UiState(error = r.message)
                    else                -> UiState(error = "Search failed")
                }
            }
        }
    }

    fun refresh() {
        loadFeaturedEvents()
        loadTrendingEvents()
        loadRecommendedEvents()
        loadFavoriteIds()
        if (_selectedCity.value.isNotEmpty()) loadNearbyEvents(_selectedCity.value)
        if (_selectedCategory.value == "All") loadAllEvents() else loadByCategory(_selectedCategory.value)
    }

    // ── Private loaders ────────────────────────────────────────────────────────

    private fun loadFeaturedEvents() {
        viewModelScope.launch {
            _featuredState.value = UiState(isLoading = true)
            _featuredState.value = when (val r = eventRepository.fetchFeaturedEvents()) {
                is Resource.Success -> UiState(data = r.data)
                is Resource.Error   -> UiState(error = r.message)
                else                -> UiState(error = "Unexpected error loading featured events")
            }
        }
    }

    private fun loadTrendingEvents() {
        viewModelScope.launch {
            _trendingState.value = UiState(isLoading = true)
            _trendingState.value = when (val r = eventRepository.getTrendingEvents()) {
                is Resource.Success -> UiState(data = r.data)
                is Resource.Error   -> UiState(error = r.message)
                else                -> UiState(error = "Unexpected error loading trending events")
            }
        }
    }

    private fun loadRecommendedEvents() {
        val uid = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            _recommendedState.value = UiState(isLoading = true)
            _recommendedState.value = when (val r = eventRepository.getRecommendedEvents(uid)) {
                is Resource.Success -> UiState(data = r.data)
                is Resource.Error   -> UiState(error = r.message)
                else                -> UiState(error = "Unexpected error loading recommendations")
            }
        }
    }

    private fun loadNearbyEvents(city: String) {
        viewModelScope.launch {
            _nearbyState.value = UiState(isLoading = true)
            _nearbyState.value = when (val r = eventRepository.getEventsByCity(city)) {
                is Resource.Success -> UiState(data = r.data)
                is Resource.Error   -> UiState(error = r.message)
                else                -> UiState(error = "Unexpected error loading nearby events")
            }
        }
    }

    private fun loadAllEvents() {
        viewModelScope.launch {
            _eventsState.value = UiState(isLoading = true)
            _eventsState.value = when (val r = eventRepository.fetchEvents()) {
                is Resource.Success -> UiState(data = r.data)
                is Resource.Error   -> UiState(error = r.message)
                else                -> UiState(error = "Unexpected error loading events")
            }
        }
    }

    private fun loadByCategory(category: String) {
        viewModelScope.launch {
            _eventsState.value = UiState(isLoading = true)
            _eventsState.value = when (val r = eventRepository.filterByCategory(category)) {
                is Resource.Success -> UiState(data = r.data)
                is Resource.Error   -> UiState(error = r.message)
                else                -> UiState(error = "Unexpected error")
            }
        }
    }
}
