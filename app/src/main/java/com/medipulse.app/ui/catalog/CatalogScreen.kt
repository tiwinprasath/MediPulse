package com.medipulse.app.ui.catalog

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun CatalogScreen(
    viewModel: CatalogViewModel
) {
    val medicines by viewModel.medicines.collectAsState()
    var searchQuery by remember { mutableStateOf("") }

    val filteredMedicines = medicines.filter {
        it.name.contains(searchQuery, ignoreCase = true) ||
                (it.genericName != null && it.genericName.contains(searchQuery, ignoreCase = true))
    }

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            Text(text = "Medicine Catalog", style = MaterialTheme.colorScheme.displayLarge)
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Search medicines...") },
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium
            )
            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(filteredMedicines) { med ->
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(text = med.name, style = MaterialTheme.colorScheme.titleMedium)
                            Text(text = "Generic: ${med.genericName ?: "N/A"} | Brand: ${med.brand ?: "N/A"}", style = MaterialTheme.colorScheme.bodyMedium)
                            Text(text = "Dosage: ${med.dosageForm ?: "Tablet"} (${med.strength ?: ""})", style = MaterialTheme.colorScheme.bodyMedium)
                            Text(text = "Price: ₹${med.price} | Tax: ${med.taxPct}%", style = MaterialTheme.colorScheme.bodyLarge, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }
        }
    }
}
