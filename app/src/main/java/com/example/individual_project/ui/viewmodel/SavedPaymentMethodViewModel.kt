package com.example.individual_project.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.individual_project.data.model.SavedPaymentMethod
import com.example.individual_project.domain.repository.SavedPaymentMethodRepository
import com.example.individual_project.utils.Resource
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SavedPaymentMethodsUiState(
    val methods   : List<SavedPaymentMethod> = emptyList(),
    val isLoading : Boolean = false,
    val error     : String? = null
)

data class PaymentMethodFormState(
    val provider         : PaymentMethod = PaymentMethod.CARD,
    val displayName      : String        = "",
    val maskedIdentifier : String        = "",
    val isSaving         : Boolean       = false,
    val error            : String?       = null
)

@HiltViewModel
class SavedPaymentMethodViewModel @Inject constructor(
    private val repository  : SavedPaymentMethodRepository,
    private val firebaseAuth: FirebaseAuth
) : ViewModel() {

    private val uid: String get() = firebaseAuth.currentUser?.uid ?: ""

    private val _uiState = MutableStateFlow(SavedPaymentMethodsUiState())
    val uiState: StateFlow<SavedPaymentMethodsUiState> = _uiState.asStateFlow()

    private val _formState = MutableStateFlow<PaymentMethodFormState?>(null)
    val formState: StateFlow<PaymentMethodFormState?> = _formState.asStateFlow()

    private val _busyIds = MutableStateFlow<Set<String>>(emptySet())
    val busyIds: StateFlow<Set<String>> = _busyIds.asStateFlow()

    init {
        loadMethods()
    }

    fun loadMethods() {
        if (uid.isBlank()) {
            _uiState.value = SavedPaymentMethodsUiState(error = "Not logged in")
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            when (val result = repository.getSavedPaymentMethods(uid)) {
                is Resource.Success -> _uiState.update { it.copy(methods = result.data, isLoading = false) }
                is Resource.Error   -> _uiState.update { it.copy(isLoading = false, error = result.message) }
                else -> Unit
            }
        }
    }

    // ── Add form ──────────────────────────────────────────────────────────────

    fun startAdd() { _formState.value = PaymentMethodFormState() }
    fun dismissForm() { _formState.value = null }

    fun onProviderChange(provider: PaymentMethod) { _formState.update { it?.copy(provider = provider, error = null) } }
    fun onDisplayNameChange(value: String)        { _formState.update { it?.copy(displayName = value, error = null) } }

    /** Only ever accepts exactly 4 digits -- this is the structural guarantee that a full
     *  card number can never be typed into (or stored from) this field. */
    fun onMaskedIdentifierChange(value: String) {
        val digitsOnly = value.filter { it.isDigit() }.take(4)
        _formState.update { it?.copy(maskedIdentifier = digitsOnly, error = null) }
    }

    fun submitForm() {
        val form = _formState.value ?: return
        if (form.isSaving) return
        if (form.displayName.isBlank()) {
            _formState.update { it?.copy(error = "Give this method a name, e.g. \"My Visa\"") }
            return
        }
        if (form.maskedIdentifier.length != 4) {
            _formState.update { it?.copy(error = "Enter the last 4 digits") }
            return
        }

        viewModelScope.launch {
            _formState.update { it?.copy(isSaving = true, error = null) }
            val method = SavedPaymentMethod(
                userId           = uid,
                provider         = form.provider.key,
                displayName      = form.displayName.trim(),
                maskedIdentifier = form.maskedIdentifier
            )
            when (val result = repository.addSavedPaymentMethod(method)) {
                is Resource.Error -> _formState.update { it?.copy(isSaving = false, error = result.message) }
                else -> {
                    _formState.value = null
                    loadMethods()
                }
            }
        }
    }

    // ── Delete / set default ─────────────────────────────────────────────────

    fun deleteMethod(methodId: String) {
        if (methodId in _busyIds.value) return
        viewModelScope.launch {
            _busyIds.update { it + methodId }
            when (val result = repository.deleteSavedPaymentMethod(uid, methodId)) {
                is Resource.Error -> _uiState.update { it.copy(error = result.message) }
                else              -> loadMethods()
            }
            _busyIds.update { it - methodId }
        }
    }

    fun setDefaultMethod(methodId: String) {
        if (methodId in _busyIds.value) return
        viewModelScope.launch {
            _busyIds.update { it + methodId }
            when (val result = repository.setDefaultPaymentMethod(uid, methodId)) {
                is Resource.Error -> _uiState.update { it.copy(error = result.message) }
                else              -> loadMethods()
            }
            _busyIds.update { it - methodId }
        }
    }

    fun clearError() { _uiState.update { it.copy(error = null) } }
}
