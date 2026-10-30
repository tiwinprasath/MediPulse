package com.medipulse.app.data.remote

import kotlinx.serialization.Serializable

@Serializable
data class MedicineDto(
    val id: Int,
    val name: String,
    val genericName: String? = null,
    val brand: String? = null,
    val manufacturer: String? = null,
    val category: String? = null,
    val strength: String? = null,
    val dosageForm: String? = null,
    val price: Double,
    val taxPct: Double = 12.0,
    val barcode: String? = null,
    val storageConditions: String? = null,
    val description: String? = null
)

@Serializable
data class InventoryBatchDto(
    val id: Int,
    val medicineId: Int,
    val batchNumber: String,
    val quantity: Int,
    val expiryDate: String,
    val supplierId: Int? = null,
    val reorderThreshold: Int = 20,
    val medicineName: String? = null,
    val dosageForm: String? = null,
    val supplierName: String? = null
)

@Serializable
data class SupplierDto(
    val id: Int,
    val name: String,
    val contactPerson: String? = null,
    val phone: String? = null,
    val email: String? = null,
    val leadTimeDays: Int = 3,
    val reliabilityPct: Double = 95.0
)

@Serializable
data class SaleItemRequest(
    val medicineId: Int,
    val quantity: Int
)

@Serializable
data class SaleRequest(
    val items: List<SaleItemRequest>,
    val customerName: String? = null,
    val discountAmount: Double = 0.0
)
