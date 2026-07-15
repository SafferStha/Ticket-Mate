package com.example.individual_project.ui.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.individual_project.data.model.BookingStatus
import com.example.individual_project.data.model.PaymentStatus
import com.example.individual_project.data.model.User
import com.example.individual_project.domain.repository.BookingRepository
import com.example.individual_project.domain.repository.PaymentRepository
import com.example.individual_project.domain.repository.TicketRepository
import com.example.individual_project.domain.repository.UserRepository
import com.example.individual_project.utils.Resource
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

// ── Profile screen state ──────────────────────────────────────────────────────

data class ProfileUiState(
    val user              : User?   = null,
    val ticketCount       : Int     = 0,
    val favoriteCount     : Int     = 0,
    val totalBookings     : Int     = 0,
    val completedEvents   : Int     = 0,
    val cancelledBookings : Int     = 0,
    val totalSpent        : Double  = 0.0,
    val isLoading         : Boolean = false,
    val error             : String? = null
)

// ── Edit-profile screen state ─────────────────────────────────────────────────

data class EditProfileUiState(
    val name          : String  = "",
    val email         : String  = "",
    val profileImage  : String  = "",
    val isLoading     : Boolean = false,
    val isSaving      : Boolean = false,
    val saveSuccess   : Boolean = false,
    val error         : String? = null
)

// ── ProfileViewModel ──────────────────────────────────────────────────────────

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userRepository   : UserRepository,
    private val ticketRepository : TicketRepository,
    private val bookingRepository: BookingRepository,
    private val paymentRepository: PaymentRepository,
    private val firebaseAuth     : FirebaseAuth
) : ViewModel() {

    private val uid: String get() = firebaseAuth.currentUser?.uid ?: ""

    // ── Profile state ──────────────────────────────────────────────────────────
    private val _profileState = MutableStateFlow(ProfileUiState())
    val profileState: StateFlow<ProfileUiState> = _profileState.asStateFlow()

    // ── Edit-profile state ─────────────────────────────────────────────────────
    private val _editState = MutableStateFlow(EditProfileUiState())
    val editState: StateFlow<EditProfileUiState> = _editState.asStateFlow()

    init {
        if (uid.isNotBlank()) {
            loadProfile()
            loadEditProfile()
        }
    }

    // ── Load profile + stats in parallel ──────────────────────────────────────

    fun loadProfile() {
        viewModelScope.launch {
            _profileState.update { it.copy(isLoading = true, error = null) }

            val userJob      = async { userRepository.getUserProfile(uid) }
            val ticketsJob   = async { ticketRepository.getUserTickets(uid) }
            val favoritesJob = async { userRepository.getFavoriteEventIds(uid) }
            val bookingsJob  = async { bookingRepository.getUserBookings(uid) }
            val paymentsJob  = async { paymentRepository.fetchPaymentsByUser(uid) }

            val userResult      = userJob.await()
            val ticketsResult   = ticketsJob.await()
            val favoritesResult = favoritesJob.await()
            val bookingsResult  = bookingsJob.await()
            val paymentsResult  = paymentsJob.await()

            val user          = (userResult as? Resource.Success)?.data
            val ticketCount   = (ticketsResult as? Resource.Success)?.data
                ?.count { it.ticketStatus == "ACTIVE" } ?: 0
            val favoriteCount = (favoritesResult as? Resource.Success)?.data?.size ?: 0

            val bookings          = (bookingsResult as? Resource.Success)?.data ?: emptyList()
            val totalBookings     = bookings.size
            val completedEvents   = bookings.count { it.bookingStatus == BookingStatus.COMPLETED.name }
            val cancelledBookings = bookings.count { it.bookingStatus == BookingStatus.CANCELLED.name }

            val totalSpent = (paymentsResult as? Resource.Success)?.data
                ?.filter { it.paymentStatus == PaymentStatus.SUCCESS.name }
                ?.sumOf { it.totalAmount } ?: 0.0

            _profileState.update {
                it.copy(
                    user              = user,
                    ticketCount       = ticketCount,
                    favoriteCount     = favoriteCount,
                    totalBookings     = totalBookings,
                    completedEvents   = completedEvents,
                    cancelledBookings = cancelledBookings,
                    totalSpent        = totalSpent,
                    isLoading         = false,
                    error             = if (user == null) (userResult as? Resource.Error)?.message else null
                )
            }
        }
    }

    // ── Edit profile ───────────────────────────────────────────────────────────

    fun loadEditProfile() {
        viewModelScope.launch {
            _editState.update { it.copy(isLoading = true) }
            when (val result = userRepository.getUserProfile(uid)) {
                is Resource.Success -> _editState.update {
                    it.copy(
                        name         = result.data.name,
                        email        = result.data.email,
                        profileImage = result.data.profileImage,
                        isLoading    = false
                    )
                }
                is Resource.Error -> _editState.update {
                    it.copy(isLoading = false, error = result.message)
                }
                else -> Unit
            }
        }
    }

    fun onNameChange(name: String) {
        _editState.update { it.copy(name = name) }
    }

    fun uploadImageAndSave(imageUri: Uri) {
        if (_editState.value.isSaving) return
        viewModelScope.launch {
            _editState.update { it.copy(isSaving = true, error = null) }
            when (val urlResult = userRepository.uploadProfileImage(uid, imageUri)) {
                is Resource.Success -> {
                    _editState.update { it.copy(profileImage = urlResult.data) }
                    performSave()
                }
                is Resource.Error -> _editState.update {
                    it.copy(isSaving = false, error = urlResult.message)
                }
                else -> Unit
            }
        }
    }

    fun saveProfile() {
        val state = _editState.value
        if (state.isSaving) return
        if (state.name.isBlank()) {
            _editState.update { it.copy(error = "Name cannot be empty") }
            return
        }
        viewModelScope.launch {
            _editState.update { it.copy(isSaving = true, error = null) }
            performSave()
        }
    }

    /** Shared save call. Callers must already hold isSaving = true before invoking this. */
    private suspend fun performSave() {
        val state = _editState.value
        val updatedUser = User(
            uid          = uid,
            name         = state.name.trim(),
            email        = state.email,
            profileImage = state.profileImage
        )
        when (val result = userRepository.updateUserProfile(updatedUser)) {
            is Resource.Success -> {
                _editState.update { it.copy(isSaving = false, saveSuccess = true) }
                loadProfile() // refresh header stats
            }
            is Resource.Error -> _editState.update {
                it.copy(isSaving = false, error = result.message)
            }
            else -> Unit
        }
    }

    fun clearEditSuccess() {
        _editState.update { it.copy(saveSuccess = false) }
    }

    fun clearEditError() {
        _editState.update { it.copy(error = null) }
    }
}
