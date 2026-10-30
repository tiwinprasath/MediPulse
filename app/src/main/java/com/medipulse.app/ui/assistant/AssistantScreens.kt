package com.medipulse.app.ui.assistant

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.medipulse.app.ui.components.MpButton

@Composable
fun AssistantScreen() {
    var messages by remember {
        mutableStateOf(
            listOf(
                "Assistant: Hello! I am your Pharmacy Assistant. Ask me about medicine availability or dosage instructions."
            )
        )
    }
    var inputQuery by remember { mutableStateOf("") }

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            Text(text = "Pharmacy Assistant", style = MaterialTheme.colorScheme.displayLarge)
            Spacer(modifier = Modifier.height(8.dp))

            Surface(
                color = MaterialTheme.colorScheme.errorContainer,
                shape = MaterialTheme.shapes.small
            ) {
                Text(
                    text = "Medical Disclaimer: For informational use only. Does not replace professional medical advice, diagnosis, or prescription.",
                    style = MaterialTheme.colorScheme.labelSmall,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.padding(8.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(modifier = Modifier.weight(1f)) {
                items(messages) { msg ->
                    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                        Text(text = msg, modifier = Modifier.padding(12.dp), style = MaterialTheme.colorScheme.bodyLarge)
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = inputQuery,
                    onValueChange = { inputQuery = it },
                    label = { Text("Ask about medicine...") },
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                MpButton(
                    text = "Send",
                    onClick = {
                        if (inputQuery.isNotBlank()) {
                            val userMsg = "You: $inputQuery"
                            val reply = if (inputQuery.contains("Paracetamol", ignoreCase = true)) {
                                "Assistant: Paracetamol 650 is currently In Stock (Batch BAT-P650-01, Price: ₹30)."
                            } else {
                                "Assistant: I can check catalog availability for medicines. Please consult your physician for medical advice."
                            }
                            messages = messages + userMsg + reply
                            inputQuery = ""
                        }
                    }
                )
            }
        }
    }
}
