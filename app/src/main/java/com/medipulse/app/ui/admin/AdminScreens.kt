package com.medipulse.app.ui.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun AdminDashboardScreen(
    viewModel: AdminViewModel
) {
    var selectedSubTab by remember { mutableIntStateOf(0) }
    val auditLogs by viewModel.auditLogs.collectAsState()
    val health by viewModel.health.collectAsState()
    val users by viewModel.users.collectAsState()
    val statusMsg by viewModel.statusMessage.collectAsState()

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            Text(text = "Admin Control Center", style = MaterialTheme.colorScheme.displayLarge)
            Spacer(modifier = Modifier.height(12.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                Button(onClick = { selectedSubTab = 0 }, enabled = selectedSubTab != 0) { Text("Audit Logs") }
                Button(onClick = { selectedSubTab = 1 }, enabled = selectedSubTab != 1) { Text("System Health") }
                Button(onClick = { selectedSubTab = 2 }, enabled = selectedSubTab != 2) { Text("Users & Roles") }
            }
            Spacer(modifier = Modifier.height(16.dp))

            if (!statusMsg.isNullOrBlank()) {
                Text(text = statusMsg!!, color = MaterialTheme.colorScheme.primary, style = MaterialTheme.colorScheme.bodyMedium)
                Spacer(modifier = Modifier.height(8.dp))
            }

            when (selectedSubTab) {
                0 -> {
                    Text(text = "Audit Log Viewer", style = MaterialTheme.colorScheme.titleLarge)
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(auditLogs) { log ->
                            Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(text = "Action: ${log.action}", style = MaterialTheme.colorScheme.titleMedium)
                                    Text(text = "Details: ${log.details ?: "N/A"}", style = MaterialTheme.colorScheme.bodyMedium)
                                    Text(text = "Time: ${log.createdAt}", style = MaterialTheme.colorScheme.bodySmall)
                                }
                            }
                        }
                    }
                }
                1 -> {
                    Text(text = "System Health Dashboard", style = MaterialTheme.colorScheme.titleLarge)
                    Spacer(modifier = Modifier.height(8.dp))
                    if (health != null) {
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(text = "Status: ${health!!.status}", style = MaterialTheme.colorScheme.titleMedium)
                                Text(text = "Database: ${health!!.database}", style = MaterialTheme.colorScheme.bodyLarge)
                                Text(text = "DB Time: ${health!!.dbTime ?: "N/A"}", style = MaterialTheme.colorScheme.bodyMedium)
                                Text(text = "Uptime: ${health!!.uptimeSeconds ?: 0.0} seconds", style = MaterialTheme.colorScheme.bodyMedium)
                                Text(text = "Timestamp: ${health!!.timestamp}", style = MaterialTheme.colorScheme.bodySmall)
                            }
                        }
                    } else {
                        Text(text = "Loading health status...", style = MaterialTheme.colorScheme.bodyLarge)
                    }
                }
                2 -> {
                    Text(text = "User Management & Role Updates", style = MaterialTheme.colorScheme.titleLarge)
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(users) { usr ->
                            Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(text = "${usr.firstName} ${usr.lastName} (${usr.email})", style = MaterialTheme.colorScheme.titleMedium)
                                    Text(text = "Current Role: ${usr.role} | Status: ${usr.status}", style = MaterialTheme.colorScheme.bodyMedium)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Button(onClick = { viewModel.updateUserRole(usr.id, "ADMIN") }, enabled = usr.role != "ADMIN") { Text("Make Admin") }
                                        Button(onClick = { viewModel.updateUserRole(usr.id, "MANAGER") }, enabled = usr.role != "MANAGER") { Text("Make Manager") }
                                        Button(onClick = { viewModel.updateUserRole(usr.id, "PHARMACIST") }, enabled = usr.role != "PHARMACIST") { Text("Make Pharmacist") }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
