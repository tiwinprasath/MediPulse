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
                }
            } catch (e: Exception) {
                Timber.e(e, "Failed to load profile")
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
