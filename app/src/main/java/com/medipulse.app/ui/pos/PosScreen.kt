package com.medipulse.app.ui.pos

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.medipulse.app.ui.components.MpButton

@Composable
fun PosScreen(
    viewModel: PosViewModel
) {
    val medicines by viewModel.medicines.collectAsState()
    val cart by viewModel.cart.collectAsState()
    val saleStatus by viewModel.saleStatus.collectAsState()
    var customerName by remember { mutableStateOf("Walk-in") }

    val subtotal = cart.entries.sumOf { it.key.price * it.value }
    val taxTotal = cart.entries.sumOf { it.key.price * it.value * (it.key.taxPct / 100.0) }
    val total = subtotal + taxTotal

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Row(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            Column(modifier = Modifier.weight(1f).fillMaxHeight()) {
                Text(text = "POS Billing", style = MaterialTheme.colorScheme.displayLarge)
                Spacer(modifier = Modifier.height(16.dp))
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(medicines) { med ->
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            onClick = { viewModel.addToCart(med) }
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(text = med.name, style = MaterialTheme.colorScheme.titleMedium)
                                Text(text = "₹${med.price} | ${med.dosageForm}", style = MaterialTheme.colorScheme.bodyMedium)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f).fillMaxHeight()) {
                Text(text = "Current Cart", style = MaterialTheme.colorScheme.titleLarge)
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = customerName,
                    onValueChange = { customerName = it },
                    label = { Text("Customer Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))

                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(cart.entries.toList()) { entry ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = entry.key.name, style = MaterialTheme.colorScheme.bodyLarge)
                                Text(text = "Qty: ${entry.value} x ₹${entry.key.price}", style = MaterialTheme.colorScheme.bodyMedium)
                            }
                            Row {
                                Button(onClick = { viewModel.removeFromCart(entry.key) }) { Text("-") }
                                Spacer(modifier = Modifier.width(4.dp))
                                Button(onClick = { viewModel.addToCart(entry.key) }) { Text("+") }
                            }
                        }
                    }
                }

                Divider()
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "Subtotal: ₹%.2f".format(subtotal), style = MaterialTheme.colorScheme.bodyLarge)
                Text(text = "Tax: ₹%.2f".format(taxTotal), style = MaterialTheme.colorScheme.bodyLarge)
                Text(text = "Total: ₹%.2f".format(total), style = MaterialTheme.colorScheme.titleLarge, color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(12.dp))

                if (saleStatus != null) {
                    Text(text = saleStatus!!, color = MaterialTheme.colorScheme.primary, style = MaterialTheme.colorScheme.bodyMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                }

                MpButton(
                    text = "Complete Checkout",
                    onClick = { viewModel.checkout(customerName, 0.0) },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = cart.isNotEmpty()
                )
            }
        }
    }
}
