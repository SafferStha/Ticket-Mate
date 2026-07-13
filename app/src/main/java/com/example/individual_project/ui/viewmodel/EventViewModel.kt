package com.example.individual_project.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
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
class EventViewModel @Inject constructor(
    private val eventRepository : EventRepository,
    savedStateHandle            : SavedStateHandle
) : ViewModel() {

    private val eventId: String = savedStateHandle.get<String>("eventId") ?: ""

    private val _eventState = MutableStateFlow(UiState<Event>())
    val eventState: StateFlow<UiState<Event>> = _eventState.asStateFlow()

    private val _relatedEventsState = MutableStateFlow(UiState<List<Event>>())
    val relatedEventsState: StateFlow<UiState<List<Event>>> = _relatedEventsState.asStateFlow()

    init {
        if (eventId.isNotBlank()) loadEvent()
    }

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
}
