package com.medipulse.app.ui.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.medipulse.app.data.local.TokenManager
import com.medipulse.app.data.remote.AuditLogDto
import com.medipulse.app.data.remote.AuthApiService
import com.medipulse.app.data.remote.HealthDto
import com.medipulse.app.data.remote.PharmacyApiService
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
class AdminViewModel @Inject constructor(
    private val apiService: PharmacyApiService,
    private val tokenManager: TokenManager,
    private val authApiService: AuthApiService
) : ViewModel() {

    private val _auditLogs = MutableStateFlow<List<AuditLogDto>>(emptyList())
    val auditLogs: StateFlow<List<AuditLogDto>> = _auditLogs.asStateFlow()

    private val _health = MutableStateFlow<HealthDto?>(null)
    val health: StateFlow<HealthDto?> = _health.asStateFlow()

    private val _users = MutableStateFlow<List<UserDto>>(emptyList())
    val users: StateFlow<List<UserDto>> = _users.asStateFlow()

    private val _statusMessage = MutableStateFlow<String?>(null)
    val statusMessage: StateFlow<String?> = _statusMessage.asStateFlow()

    init {
        loadAuditLogs()
        loadHealth()
        loadUsers()
    }

    fun loadAuditLogs() {
        viewModelScope.launch {
            try {
                val token = tokenManager.accessToken.first() ?: return@launch
                val response = apiService.getAuditLogs("Bearer $token")
                if (response.isSuccessful && response.body() != null) {
                    _auditLogs.value = response.body()!!
                }
            } catch (e: Exception) {
                Timber.e(e, "Failed to load audit logs")
            }
        }
    }

    fun loadHealth() {
        viewModelScope.launch {
            try {
                val token = tokenManager.accessToken.first() ?: return@launch
                val response = apiService.getAdminHealth("Bearer $token")
                if (response.isSuccessful && response.body() != null) {
                    _health.value = response.body()!!
                }
            } catch (e: Exception) {
                Timber.e(e, "Failed to load health check")
                _health.value = HealthDto(status = "unhealthy", database = "disconnected", timestamp = java.util.Date().toString())
            }
        }
    }

    fun loadUsers() {
        viewModelScope.launch {
            try {
                val token = tokenManager.accessToken.first() ?: return@launch
                val response = authApiService.getPendingUsers("Bearer $token")
                if (response.isSuccessful && response.body() != null) {
                    _users.value = response.body()!!
                }
            } catch (e: Exception) {
                Timber.e(e, "Failed to load users")
            }
        }
    }

    fun updateUserRole(userId: Int, newRole: String) {
        viewModelScope.launch {
            try {
                val token = tokenManager.accessToken.first() ?: return@launch
                val response = apiService.updateUserRole("Bearer $token", userId, mapOf("role" to newRole))
                if (response.isSuccessful) {
                    _statusMessage.value = "Role updated successfully to $newRole"
                    loadUsers()
                } else {
                    _statusMessage.value = "Failed: ${response.errorBody()?.string()}"
                }
            } catch (e: Exception) {
                Timber.e(e, "Role update exception")
                _statusMessage.value = "Error: ${e.localizedMessage}"
            }
        }
    }
}
