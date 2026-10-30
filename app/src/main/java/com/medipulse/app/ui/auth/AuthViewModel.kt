package com.medipulse.app.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.medipulse.app.data.local.TokenManager
import com.medipulse.app.data.remote.AuthApiService
import com.medipulse.app.data.remote.RegisterRequest
import com.medipulse.app.data.remote.LoginRequest
import com.medipulse.app.util.Validators
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    data class Success(val role: String, val name: String) : AuthState()
    data class Error(val message: String) : AuthState()
}

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val apiService: AuthApiService,
    private val tokenManager: TokenManager
) : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    fun login(identifier: String, password: String) {
        if (identifier.isBlank() || password.isBlank()) {
            _authState.value = AuthState.Error("Please enter valid credentials")
            return
        }

        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                val response = apiService.login(LoginRequest(identifier.trim(), password))
                if (response.isSuccessful && response.body() != null) {
                    val body = response.body()!!
                    val user = body.user
                    tokenManager.saveTokens(
                        access = body.accessToken,
                        refresh = body.refreshToken,
                        role = user.role,
                        name = "${user.firstName} ${user.lastName}",
                        status = user.status
                    )
                    _authState.value = AuthState.Success(user.role, "${user.firstName} ${user.lastName}")
                } else {
                    val errorBody = response.errorBody()?.string() ?: "Invalid credentials"
                    _authState.value = AuthState.Error("Login failed: $errorBody")
                }
            } catch (e: Exception) {
                Timber.e(e, "Login exception")
                _authState.value = AuthState.Error("Network error: ${e.localizedMessage}")
            }
        }
    }

    fun register(
        email: String,
        phone: String,
        password: String,
        firstName: String,
        lastName: String,
        role: String,
        gender: String?,
        dob: String?,
        addressLine1: String?,
        city: String?,
        state: String?,
        postalCode: String?,
        licenceNumber: String?
    ) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                val formattedPhone = Validators.formatToE164(phone)
                val request = RegisterRequest(
                    email = email.trim().lowercase(),
                    phone = formattedPhone,
                    password = password,
                    firstName = firstName.trim(),
                    lastName = lastName.trim(),
                    role = role,
                    gender = gender,
                    dob = dob,
                    addressLine1 = addressLine1,
                    city = city,
                    state = state,
                    postalCode = postalCode,
                    licenceNumber = licenceNumber
                )
                val response = apiService.register(request)
                if (response.isSuccessful) {
                    _authState.value = AuthState.Success(role, "$firstName $lastName")
                } else {
                    val err = response.errorBody()?.string() ?: "Registration failed"
                    _authState.value = AuthState.Error(err)
                }
            } catch (e: Exception) {
                Timber.e(e, "Registration exception")
                _authState.value = AuthState.Error("Network error: ${e.localizedMessage}")
            }
        }
    }
}
