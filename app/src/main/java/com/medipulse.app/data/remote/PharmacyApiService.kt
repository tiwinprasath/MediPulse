package com.medipulse.app.data.remote

import kotlinx.serialization.Serializable
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

@Serializable
data class PrescriptionItemRequest(
    val medicineId: Int,
    val dosage: String,
    val frequency: String,
    val duration: String
)

@Serializable
data class PrescriptionRequest(
    val patientId: Int,
    val prescriberName: String,
    val notes: String? = null,
    val items: List<PrescriptionItemRequest>
)

@Serializable
data class PrescriptionDto(
    val id: Int,
    val patientId: Int,
    val prescriberName: String,
    val status: String,
    val notes: String? = null,
    val patientName: String? = null,
    val createdAt: String? = null
)

@Serializable
data class ForecastDto(
    val name: String,
    val category: String? = null,
    val totalStock: Int = 0,
    val shortageClass: String = "LOW"
)

interface PharmacyApiService {
    @GET("medicines")
    suspend fun getMedicines(@Header("Authorization") token: String): Response<List<MedicineDto>>

    @POST("medicines")
    suspend fun addMedicine(
        @Header("Authorization") token: String,
        @Body medicine: MedicineDto
    ): Response<MedicineDto>

    @GET("inventory")
    suspend fun getInventory(@Header("Authorization") token: String): Response<List<InventoryBatchDto>>

    @GET("suppliers")
    suspend fun getSuppliers(@Header("Authorization") token: String): Response<List<SupplierDto>>

    @POST("sales")
    suspend fun createSale(
        @Header("Authorization") token: String,
        @Body sale: SaleRequest
    ): Response<Map<String, Any>>

    @GET("prescriptions")
    suspend fun getPrescriptions(@Header("Authorization") token: String): Response<List<PrescriptionDto>>

    @POST("prescriptions")
    suspend fun createPrescription(
        @Header("Authorization") token: String,
        @Body prescription: PrescriptionRequest
    ): Response<Map<String, Any>>

    @PUT("prescriptions/{id}/status")
    suspend fun updatePrescriptionStatus(
        @Header("Authorization") token: String,
        @Path("id") id: Int,
        @Body body: Map<String, String>
    ): Response<Map<String, Any>>

    @GET("forecasts")
    suspend fun getForecasts(@Header("Authorization") token: String): Response<List<ForecastDto>>

    @GET("admin/audit-logs")
    suspend fun getAuditLogs(@Header("Authorization") token: String): Response<List<AuditLogDto>>

    @GET("admin/health")
    suspend fun getAdminHealth(@Header("Authorization") token: String): Response<HealthDto>

    @PUT("admin/users/{id}/role")
    suspend fun updateUserRole(
        @Header("Authorization") token: String,
        @Path("id") id: Int,
        @Body body: Map<String, String>
    ): Response<Map<String, Any>>
}
