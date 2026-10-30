package com.medipulse.app.data.remote

import kotlinx.serialization.Serializable

@Serializable
data class AuditLogDto(
    val id: Int,
    val userId: Int? = null,
    val action: String,
    val details: String? = null,
    val createdAt: String? = null
)

@Serializable
data class HealthDto(
    val status: String,
    val database: String,
    val dbTime: String? = null,
    val uptimeSeconds: Double? = null,
    val timestamp: String
)
