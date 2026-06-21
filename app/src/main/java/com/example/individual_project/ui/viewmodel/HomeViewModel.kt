package com.example.individual_project.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.individual_project.data.model.Event
import com.example.individual_project.domain.repository.EventRepository
import com.example.individual_project.ui.model.UiState
import com.example.individual_project.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val eventRepository: EventRepository
) : ViewModel() {

    // ── Featured events (horizontal carousel) ──────────────────────────────────
    private val _featuredState = MutableStateFlow(UiState<List<Event>>())
    val featuredState: StateFlow<UiState<List<Event>>> = _featuredState.asStateFlow()

    // ── All / category-filtered events (vertical list) ─────────────────────────
    private val _eventsState = MutableStateFlow(UiState<List<Event>>())
    val eventsState: StateFlow<UiState<List<Event>>> = _eventsState.asStateFlow()

    // ── Categories ─────────────────────────────────────────────────────────────
    val categories: StateFlow<List<String>> = MutableStateFlow(
        listOf("All", "Concerts", "Sports", "Theater", "Comedy", "Family", "Festivals")
    ).asStateFlow()

    private val _selectedCategory = MutableStateFlow("All")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    // ── Search ─────────────────────────────────────────────────────────────────
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _searchResults = MutableStateFlow(UiState<List<Event>>())
    val searchResults: StateFlow<UiState<List<Event>>> = _searchResults.asStateFlow()

    init {
        loadFeaturedEvents()
        loadAllEvents()
    }

    // ── Public actions ─────────────────────────────────────────────────────────

    fun selectCategory(category: String) {
        _selectedCategory.value = category
        if (category == "All") loadAllEvents() else loadByCategory(category)
    }

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
        if (query.isBlank()) {
            _searchResults.value = UiState()
        } else {
            searchEvents(query)
        }
    }

    fun refresh() {
        loadFeaturedEvents()
        if (_selectedCategory.value == "All") loadAllEvents()
        else loadByCategory(_selectedCategory.value)
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

    private fun searchEvents(query: String) {
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
