package com.medipulse.app.ui.catalog

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.medipulse.app.data.local.TokenManager
import com.medipulse.app.data.remote.MedicineDto
import com.medipulse.app.data.remote.PharmacyApiService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class CatalogViewModel @Inject constructor(
    private val apiService: PharmacyApiService,
    private val tokenManager: TokenManager
) : ViewModel() {

    private val _medicines = MutableStateFlow<List<MedicineDto>>(emptyList())
    val medicines: StateFlow<List<MedicineDto>> = _medicines.asStateFlow()

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
                Timber.e(e, "Failed to load medicines")
            }
        }
    }
}
