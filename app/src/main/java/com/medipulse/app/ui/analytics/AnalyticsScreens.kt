package com.medipulse.app.ui.analytics

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.medipulse.app.data.local.TokenManager
import com.medipulse.app.data.remote.ForecastDto
import com.medipulse.app.data.remote.PharmacyApiService
import com.medipulse.app.ui.components.StatusChip
import com.medipulse.app.ui.components.StatusType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class AnalyticsViewModel @Inject constructor(
    private val apiService: PharmacyApiService,
    private val tokenManager: TokenManager
) : ViewModel() {
    private val _forecasts = MutableStateFlow<List<ForecastDto>>(emptyList())
    val forecasts: StateFlow<List<ForecastDto>> = _forecasts.asStateFlow()

    init {
        loadForecasts()
    }

    fun loadForecasts() {
        viewModelScope.launch {
            try {
                val token = tokenManager.accessToken.first() ?: return@launch
                val response = apiService.getForecasts("Bearer $token")
                if (response.isSuccessful && response.body() != null) {
                    _forecasts.value = response.body()!!
                }
            } catch (e: Exception) {
                Timber.e(e, "Failed to load forecasts")
            }
        }
    }
}

@Composable
fun AnalyticsScreen(
    viewModel: AnalyticsViewModel
) {
    val forecasts by viewModel.forecasts.collectAsState()

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            Text(text = "Demand Forecasts & Shortages", style = MaterialTheme.colorScheme.displayLarge)
            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(forecasts) { forecast ->
                    val statusType = when (forecast.shortageClass) {
                        "CRITICAL" -> StatusType.ERROR
                        "MODERATE" -> StatusType.WARNING
                        else -> StatusType.SUCCESS
                    }

                    Card(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = forecast.name, style = MaterialTheme.colorScheme.titleMedium)
                                StatusChip(text = forecast.shortageClass, statusType = statusType)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = "Category: ${forecast.category ?: "General"} | Stock: ${forecast.totalStock}", style = MaterialTheme.colorScheme.bodyMedium)
                        }
                    }
                }
            }
        }
    }
}
