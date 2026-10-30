package com.medipulse.app.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.medipulse.app.data.local.TokenManager
import com.medipulse.app.data.remote.AuthApiService
import com.medipulse.app.data.remote.UserDto
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val apiService: AuthApiService,
    private val tokenManager: TokenManager
) : ViewModel() {

    private val _user = MutableStateFlow<UserDto?>(null)
    val user: StateFlow<UserDto?> = _user.asStateFlow()

    private val _pendingUsers = MutableStateFlow<List<UserDto>>(emptyList())
    val pendingUsers: StateFlow<List<UserDto>> = _pendingUsers.asStateFlow()

    init {
        loadProfile()
    }

    fun loadProfile() {
        viewModelScope.launch {
            try {
                val token = tokenManager.accessToken.first() ?: return@launch
                val response = apiService.getProfile("Bearer $token")
                if (response.isSuccessful && response.body() != null) {
                    _user.value = response.body()
                    if (response.body()?.role == "ADMIN") {
                        loadPendingUsers(token)
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "Failed to load profile")
            }
        }
    }

    private fun loadPendingUsers(token: String) {
        viewModelScope.launch {
            try {
                val response = apiService.getPendingUsers("Bearer $token")
                if (response.isSuccessful && response.body() != null) {
                    _pendingUsers.value = response.body()!!.filter { it.status == "PENDING" }
                }
            } catch (e: Exception) {
                Timber.e(e, "Failed to load pending users")
            }
        }
    }

    fun updateUserStatus(userId: Int, status: String) {
        viewModelScope.launch {
            try {
                val token = tokenManager.accessToken.first() ?: return@launch
                val response = apiService.updateUserStatus("Bearer $token", userId, mapOf("status" to status))
                if (response.isSuccessful) {
                    loadPendingUsers(token)
                }
            } catch (e: Exception) {
                Timber.e(e, "Failed to update user status")
            }
        }
    }

    fun logout(onLoggedOut: () -> Unit) {
        viewModelScope.launch {
            tokenManager.clearSession()
            onLoggedOut()
        }
    }
}
