package com.medipulse.app.data.remote

import kotlinx.serialization.Serializable

@Serializable
data class LoginRequest(
    val identifier: String,
    val password: String
)

@Serializable
data class LoginResponse(
    val accessToken: String,
    val refreshToken: String,
    val user: UserDto
)

@Serializable
data class RegisterRequest(
    val email: String,
    val phone: String,
    val password: String,
    val firstName: String,
    val lastName: String,
    val role: String,
    val gender: String? = null,
    val dob: String? = null,
    val addressLine1: String? = null,
    val city: String? = null,
    val state: String? = null,
    val postalCode: String? = null,
    val licenceNumber: String? = null
)

@Serializable
data class UserDto(
    val id: Int,
    val email: String,
    val phone: String? = null,
    val firstName: String,
    val lastName: String,
    val role: String,
    val status: String,
    val gender: String? = null,
    val dob: String? = null,
    val addressLine1: String? = null,
    val city: String? = null,
    val state: String? = null,
    val postalCode: String? = null,
    val country: String? = null,
    val avatarUrl: String? = null,
    val licenceNumber: String? = null,
    val department: String? = null,
    val isVerified: Boolean = false
)
