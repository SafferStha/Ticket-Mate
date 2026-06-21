package com.example.individual_project.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.individual_project.domain.repository.AuthRepository
import com.example.individual_project.ui.model.UiState
import com.example.individual_project.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _loginState    = MutableStateFlow(UiState<Unit>())
    val loginState   : StateFlow<UiState<Unit>> = _loginState.asStateFlow()

    private val _registerState = MutableStateFlow(UiState<Unit>())
    val registerState: StateFlow<UiState<Unit>> = _registerState.asStateFlow()

    private val _resetState    = MutableStateFlow(UiState<Unit>())
    val resetState   : StateFlow<UiState<Unit>> = _resetState.asStateFlow()

    val isLoggedIn   : Boolean  get() = authRepository.isLoggedIn
    val currentUserId: String?  get() = authRepository.currentUserId

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _loginState.value = UiState(isLoading = true)
            _loginState.value = when (val r = authRepository.login(email, password)) {
                is Resource.Success -> UiState(data = Unit)
                is Resource.Error   -> UiState(error = r.message)
                else                -> UiState(error = "Unexpected error during login")
            }
        }
    }

    fun register(email: String, password: String, name: String, contact: String) {
        viewModelScope.launch {
            _registerState.value = UiState(isLoading = true)
            _registerState.value = when (val r = authRepository.register(email, password, name, contact)) {
                is Resource.Success -> UiState(data = Unit)
                is Resource.Error   -> UiState(error = r.message)
                else                -> UiState(error = "Unexpected error during registration")
            }
        }
    }

    fun sendPasswordReset(email: String) {
        viewModelScope.launch {
            _resetState.value = UiState(isLoading = true)
            _resetState.value = when (val r = authRepository.sendPasswordReset(email)) {
                is Resource.Success -> UiState(data = Unit)
                is Resource.Error   -> UiState(error = r.message)
                else                -> UiState(error = "Unexpected error sending reset email")
            }
        }
    }

    fun logout() = authRepository.logout()

    fun clearLoginState()    { _loginState.value    = UiState() }
    fun clearRegisterState() { _registerState.value = UiState() }
    fun clearResetState()    { _resetState.value    = UiState() }
}
