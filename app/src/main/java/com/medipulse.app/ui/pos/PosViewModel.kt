package com.medipulse.app.ui.pos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.medipulse.app.data.local.TokenManager
import com.medipulse.app.data.remote.MedicineDto
import com.medipulse.app.data.remote.PharmacyApiService
import com.medipulse.app.data.remote.SaleItemRequest
import com.medipulse.app.data.remote.SaleRequest
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class PosViewModel @Inject constructor(
    private val apiService: PharmacyApiService,
    private val tokenManager: TokenManager
) : ViewModel() {

    private val _medicines = MutableStateFlow<List<MedicineDto>>(emptyList())
    val medicines: StateFlow<List<MedicineDto>> = _medicines.asStateFlow()

    private val _cart = MutableStateFlow<Map<MedicineDto, Int>>(emptyMap())
    val cart: StateFlow<Map<MedicineDto, Int>> = _cart.asStateFlow()

    private val _saleStatus = MutableStateFlow<String?>(null)
    val saleStatus: StateFlow<String?> = _saleStatus.asStateFlow()

    init {
        loadMedicines()
    }

    fun loadMedicines() {
        viewModelScope.launch {
            try {
                val token = tokenManager.accessToken.first() ?: return@launch
                val response = apiService.getMedicines("Bearer $token")
                if (response.isSuccessful && response.body() != null) {
                    _medicines.value = response.body()!!
                }
            } catch (e: Exception) {
                Timber.e(e, "Failed to load medicines for POS")
            }
        }
    }

    fun addToCart(medicine: MedicineDto) {
        val current = _cart.value.toMutableMap()
        val qty = current[medicine] ?: 0
        current[medicine] = qty + 1
        _cart.value = current
    }

    fun removeFromCart(medicine: MedicineDto) {
        val current = _cart.value.toMutableMap()
        val qty = current[medicine] ?: 0
        if (qty > 1) {
            current[medicine] = qty - 1
        } else {
            current.remove(medicine)
        }
        _cart.value = current
    }

    fun checkout(customerName: String, discount: Double) {
        viewModelScope.launch {
            try {
                val token = tokenManager.accessToken.first() ?: return@launch
                val items = _cart.value.map { SaleItemRequest(medicineId = it.key.id, quantity = it.value) }
                val request = SaleRequest(items = items, customerName = customerName, discountAmount = discount)
                val response = apiService.createSale("Bearer $token", request)
                if (response.isSuccessful) {
                    _saleStatus.value = "Sale successful! ID: ${response.body()?.get("saleId")}"
                    _cart.value = emptyMap()
                } else {
                    _saleStatus.value = "Checkout failed: ${response.errorBody()?.string()}"
                }
            } catch (e: Exception) {
                Timber.e(e, "Checkout exception")
                _saleStatus.value = "Error: ${e.localizedMessage}"
            }
        }
    }
}
