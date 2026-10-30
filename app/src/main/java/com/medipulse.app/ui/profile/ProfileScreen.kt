package com.medipulse.app.ui.profile

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.medipulse.app.ui.components.MpButton

@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel,
    onLogout: () -> Unit
) {
    val user by viewModel.user.collectAsState()

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
        ) {
            Text(text = "Profile & Settings", style = MaterialTheme.colorScheme.displayLarge)
            Spacer(modifier = Modifier.height(16.dp))

            if (user != null) {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(text = "Name: ${user!!.firstName} ${user!!.lastName}", style = MaterialTheme.colorScheme.titleMedium)
                        Text(text = "Email: ${user!!.email}", style = MaterialTheme.colorScheme.bodyLarge)
                        Text(text = "Role: ${user!!.role}", style = MaterialTheme.colorScheme.bodyLarge)
                        Text(text = "Status: ${user!!.status}", style = MaterialTheme.colorScheme.bodyLarge)
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))
            MpButton(
                text = "Sign Out",
                onClick = { viewModel.logout(onLogout) },
                modifier = Modifier.fillMaxWidth(),
                isPrimary = false
            )
        }
    }
}
