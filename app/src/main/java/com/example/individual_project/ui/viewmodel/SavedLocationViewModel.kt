package com.example.individual_project.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.individual_project.data.model.SavedLocation
import com.example.individual_project.domain.repository.SavedLocationRepository
import com.example.individual_project.utils.Resource
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SavedLocationsUiState(
    val locations : List<SavedLocation> = emptyList(),
    val isLoading  : Boolean = false,
    val error      : String? = null
)

/** State for the add/edit form dialog. A null [editingId] means "creating a new location". */
data class SavedLocationFormState(
    val editingId : String? = null,
    val label     : String  = "",
    val address   : String  = "",
    val city      : String  = "",
    val isSaving  : Boolean = false,
    val error     : String? = null
)

@HiltViewModel
class SavedLocationViewModel @Inject constructor(
    private val repository  : SavedLocationRepository,
    private val firebaseAuth: FirebaseAuth
) : ViewModel() {

    private val uid: String get() = firebaseAuth.currentUser?.uid ?: ""

    private val _uiState = MutableStateFlow(SavedLocationsUiState())
    val uiState: StateFlow<SavedLocationsUiState> = _uiState.asStateFlow()

    private val _formState = MutableStateFlow<SavedLocationFormState?>(null)
    val formState: StateFlow<SavedLocationFormState?> = _formState.asStateFlow()

    // Location ids currently mid-mutation (delete / set-default) -- guards double-taps.
    private val _busyIds = MutableStateFlow<Set<String>>(emptySet())
    val busyIds: StateFlow<Set<String>> = _busyIds.asStateFlow()

    init {
        loadLocations()
    }

    fun loadLocations() {
        if (uid.isBlank()) {
            _uiState.value = SavedLocationsUiState(error = "Not logged in")
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            when (val result = repository.getSavedLocations(uid)) {
                is Resource.Success -> _uiState.update { it.copy(locations = result.data, isLoading = false) }
                is Resource.Error   -> _uiState.update { it.copy(isLoading = false, error = result.message) }
                else -> Unit
            }
        }
    }

    // ── Add/edit form ─────────────────────────────────────────────────────────

    fun startAdd() { _formState.value = SavedLocationFormState() }

    fun startEdit(location: SavedLocation) {
        _formState.value = SavedLocationFormState(
            editingId = location.id,
            label     = location.label,
            address   = location.address,
            city      = location.city
        )
    }

    fun dismissForm() { _formState.value = null }

    fun onLabelChange(value: String)   { _formState.update { it?.copy(label = value, error = null) } }
    fun onAddressChange(value: String) { _formState.update { it?.copy(address = value, error = null) } }
    fun onCityChange(value: String)    { _formState.update { it?.copy(city = value, error = null) } }

    fun submitForm() {
        val form = _formState.value ?: return
        if (form.isSaving) return
        if (form.label.isBlank() || form.address.isBlank() || form.city.isBlank()) {
            _formState.update { it?.copy(error = "Label, address, and city are all required") }
            return
        }
        // Prevent an obvious duplicate (same label already saved) rather than silently
        // allowing an ever-growing list of near-identical entries.
        val isDuplicate = _uiState.value.locations.any {
            it.id != form.editingId && it.label.equals(form.label.trim(), ignoreCase = true)
        }
        if (isDuplicate) {
            _formState.update { it?.copy(error = "You already have a location saved with this name") }
            return
        }

        viewModelScope.launch {
            _formState.update { it?.copy(isSaving = true, error = null) }
            val location = SavedLocation(
                id      = form.editingId ?: "",
                userId  = uid,
                label   = form.label.trim(),
                address = form.address.trim(),
                city    = form.city.trim()
            )
            val result = if (form.editingId != null) {
                repository.updateSavedLocation(location)
            } else {
                repository.addSavedLocation(location)
            }
            when (result) {
                is Resource.Error -> _formState.update { it?.copy(isSaving = false, error = result.message) }
                else -> {
                    _formState.value = null
                    loadLocations()
                }
            }
        }
    }

    // ── Delete / set default ─────────────────────────────────────────────────

    fun deleteLocation(locationId: String) {
        if (locationId in _busyIds.value) return
        viewModelScope.launch {
            _busyIds.update { it + locationId }
            when (val result = repository.deleteSavedLocation(uid, locationId)) {
                is Resource.Error -> _uiState.update { it.copy(error = result.message) }
                else              -> loadLocations()
            }
            _busyIds.update { it - locationId }
        }
    }

    fun setDefaultLocation(locationId: String) {
        if (locationId in _busyIds.value) return
        viewModelScope.launch {
            _busyIds.update { it + locationId }
            when (val result = repository.setDefaultLocation(uid, locationId)) {
                is Resource.Error -> _uiState.update { it.copy(error = result.message) }
                else              -> loadLocations()
            }
            _busyIds.update { it - locationId }
        }
    }

    fun clearError() { _uiState.update { it.copy(error = null) } }
}
