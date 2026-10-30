package com.medipulse.app.data.remote

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface AuthApiService {
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<Map<String, String>>

    @GET("me")
    suspend fun getProfile(@Header("Authorization") token: String): Response<UserDto>

    @PUT("me")
    suspend fun updateProfile(
        @Header("Authorization") token: String,
        @Body request: Map<String, String?>
    ): Response<Map<String, String>>

    @GET("admin/users")
    suspend fun getPendingUsers(@Header("Authorization") token: String): Response<List<UserDto>>

    @PUT("admin/users/{id}/status")
    suspend fun updateUserStatus(
        @Header("Authorization") token: String,
        @Path("id") id: Int,
        @Body body: Map<String, String>
    ): Response<Map<String, String>>
}
