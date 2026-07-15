package com.example.individual_project.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.individual_project.data.model.Event
import com.example.individual_project.data.model.SearchHistoryItem
import com.example.individual_project.domain.repository.EventRepository
import com.example.individual_project.domain.repository.SearchRepository
import com.example.individual_project.domain.repository.UserRepository
import com.example.individual_project.ui.model.FilterState
import com.example.individual_project.ui.model.UiState
import com.example.individual_project.utils.Resource
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val eventRepository  : EventRepository,
    private val searchRepository : SearchRepository,
    private val userRepository   : UserRepository,
    private val auth             : FirebaseAuth
) : ViewModel() {

    // ── Search state ────────────────────────────────────────────────────────────
    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    private val _searchResults = MutableStateFlow(UiState<List<Event>>())
    val searchResults: StateFlow<UiState<List<Event>>> = _searchResults.asStateFlow()

    // ── Favorites ─────────────────────────────────────────────────────────────
    private val _favoriteIds = MutableStateFlow<Set<String>>(emptySet())
    val favoriteIds: StateFlow<Set<String>> = _favoriteIds.asStateFlow()

    private val _favoriteLoadingIds = MutableStateFlow<Set<String>>(emptySet())
    val favoriteLoadingIds: StateFlow<Set<String>> = _favoriteLoadingIds.asStateFlow()

    // ── Discovery state ─────────────────────────────────────────────────────────
    private val _searchHistory = MutableStateFlow<List<SearchHistoryItem>>(emptyList())
    val searchHistory: StateFlow<List<SearchHistoryItem>> = _searchHistory.asStateFlow()

    private val _trendingKeywords = MutableStateFlow<List<String>>(emptyList())
    val trendingKeywords: StateFlow<List<String>> = _trendingKeywords.asStateFlow()

    // ── Filter state ────────────────────────────────────────────────────────────
    private val _filterState = MutableStateFlow(FilterState())
    val filterState: StateFlow<FilterState> = _filterState.asStateFlow()

    private val _isFilterSheetVisible = MutableStateFlow(false)
    val isFilterSheetVisible: StateFlow<Boolean> = _isFilterSheetVisible.asStateFlow()

    private var debounceJob: Job? = null
    private val userId get() = auth.currentUser?.uid

    init {
        loadSearchHistory()
        loadTrendingKeywords()
        loadFavoriteIds()
    }

    fun toggleFavorite(eventId: String) {
        val uid = userId ?: return
        if (eventId in _favoriteLoadingIds.value) return
        val wasFavorite = eventId in _favoriteIds.value
        viewModelScope.launch {
            _favoriteIds.update { if (wasFavorite) it - eventId else it + eventId }
            _favoriteLoadingIds.update { it + eventId }
            val result = eventRepository.toggleFavorite(eventId, uid)
            if (result is Resource.Error) {
                _favoriteIds.update { if (wasFavorite) it + eventId else it - eventId }
            }
            _favoriteLoadingIds.update { it - eventId }
        }
    }

    private fun loadFavoriteIds() {
        val uid = userId ?: return
        viewModelScope.launch {
            val result = userRepository.getFavoriteEventIds(uid)
            if (result is Resource.Success) _favoriteIds.value = result.data.toSet()
        }
    }

    // ── Query handling ──────────────────────────────────────────────────────────

    fun onQueryChange(query: String) {
        _query.value = query
        debounceJob?.cancel()
        if (query.isBlank()) {
            _searchResults.value = UiState()
            return
        }
        debounceJob = viewModelScope.launch {
            delay(300L)
            performSearch(query)
        }
    }

    // Called on IME action (keyboard search button). Skips debounce, saves to history.
    fun submitSearch(query: String = _query.value) {
        if (query.isBlank()) return
        debounceJob?.cancel()
        viewModelScope.launch {
            val uid = userId
            if (uid != null) {
                searchRepository.saveSearchHistory(uid, query.trim())
                refreshHistory(uid)
            }
            performSearch(query)
        }
    }

    fun selectHistoryItem(query: String) {
        _query.value = query
        submitSearch(query)
    }

    fun clearHistory() {
        val uid = userId ?: return
        viewModelScope.launch {
            searchRepository.clearSearchHistory(uid)
            _searchHistory.value = emptyList()
        }
    }

    // ── Filter handling ─────────────────────────────────────────────────────────

    fun showFilter()  { _isFilterSheetVisible.value = true  }
    fun hideFilter()  { _isFilterSheetVisible.value = false }

    fun applyFilter(filter: FilterState) {
        _filterState.value = filter
        _isFilterSheetVisible.value = false
        if (_query.value.isNotBlank()) {
            viewModelScope.launch { performSearch(_query.value) }
        }
    }

    fun clearFilter() {
        _filterState.value = FilterState()
        if (_query.value.isNotBlank()) {
            viewModelScope.launch { performSearch(_query.value) }
        }
    }

    // ── Internal ────────────────────────────────────────────────────────────────

    private suspend fun performSearch(query: String) {
        _searchResults.value = UiState(isLoading = true)
        _searchResults.value = when (val r = eventRepository.searchEvents(query)) {
            is Resource.Success -> UiState(data = applyFilters(r.data))
            is Resource.Error   -> UiState(error = r.message)
            else                -> UiState(error = "Search failed")
        }
    }

    private fun applyFilters(events: List<Event>): List<Event> {
        val f = _filterState.value
        return events.filter { e ->
            (f.selectedCategories.isEmpty() || e.category in f.selectedCategories) &&
            (f.selectedCity.isBlank()        || e.city.equals(f.selectedCity, ignoreCase = true)) &&
            e.price >= f.minPrice && e.price <= f.maxPrice
        }
    }

    private fun loadSearchHistory() {
        val uid = userId ?: return
        viewModelScope.launch { refreshHistory(uid) }
    }

    private suspend fun refreshHistory(uid: String) {
        val r = searchRepository.getSearchHistory(uid)
        if (r is Resource.Success) _searchHistory.value = r.data
    }

    private fun loadTrendingKeywords() {
        viewModelScope.launch {
            val r = searchRepository.getTrendingKeywords()
            if (r is Resource.Success) _trendingKeywords.value = r.data
        }
    }
}
