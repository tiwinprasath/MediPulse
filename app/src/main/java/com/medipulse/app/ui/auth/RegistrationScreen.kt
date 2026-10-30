package com.medipulse.app.ui.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.medipulse.app.ui.components.MpButton
import com.medipulse.app.ui.components.MpTextField

@Composable
fun RegistrationScreen(
    viewModel: AuthViewModel,
    onRegisterSuccess: () -> Unit,
    onBackToLogin: () -> Unit
) {
    var step by remember { mutableIntStateOf(1) }
    var role by remember { mutableStateOf("PATIENT") }
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var addressLine1 by remember { mutableStateOf("") }
    var licenceNumber by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    val authState by viewModel.authState.collectAsState()

    LaunchedEffect(authState) {
        if (authState is AuthState.Success) {
            onRegisterSuccess()
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(24.dp))
            Text(text = "MediPulse Registration (Step $step/5)", style = MaterialTheme.colorScheme.titleLarge)
            Spacer(modifier = Modifier.height(24.dp))

            when (step) {
                1 -> {
                    Text(text = "Select Account Type", style = MaterialTheme.colorScheme.titleMedium)
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                        Button(onClick = { role = "PATIENT" }, enabled = role != "PATIENT") { Text("Patient") }
                        Button(onClick = { role = "PHARMACIST" }, enabled = role != "PHARMACIST") { Text("Pharmacist") }
                        Button(onClick = { role = "MANAGER" }, enabled = role != "MANAGER") { Text("Manager") }
                    }
                    Spacer(modifier = Modifier.height(32.dp))
                    MpButton(text = "Next", onClick = { step = 2 }, modifier = Modifier.fillMaxWidth())
                }
                2 -> {
                    MpTextField(value = firstName, onValueChange = { firstName = it }, label = "First Name")
                    Spacer(modifier = Modifier.height(16.dp))
                    MpTextField(value = lastName, onValueChange = { lastName = it }, label = "Last Name")
                    Spacer(modifier = Modifier.height(32.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        MpButton(text = "Back", onClick = { step = 1 }, isPrimary = false)
                        MpButton(text = "Next", onClick = { step = 3 })
                    }
                }
                3 -> {
                    MpTextField(value = email, onValueChange = { email = it }, label = "Email")
                    Spacer(modifier = Modifier.height(16.dp))
                    MpTextField(value = phone, onValueChange = { phone = it }, label = "Phone (+91...)")
                    Spacer(modifier = Modifier.height(16.dp))
                    MpTextField(value = addressLine1, onValueChange = { addressLine1 = it }, label = "Address")
                    Spacer(modifier = Modifier.height(32.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        MpButton(text = "Back", onClick = { step = 2 }, isPrimary = false)
                        MpButton(text = "Next", onClick = { step = if (role != "PATIENT") 4 else 5 })
                    }
                }
                4 -> {
                    if (role != "PATIENT") {
                        MpTextField(value = licenceNumber, onValueChange = { licenceNumber = it }, label = "Licence Number / Employee ID")
                        Spacer(modifier = Modifier.height(32.dp))
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        MpButton(text = "Back", onClick = { step = 3 }, isPrimary = false)
                        MpButton(text = "Next", onClick = { step = 5 })
                    }
                }
                5 -> {
                    MpTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = "Password (min 8 chars, 1 upper, 1 digit, 1 special)",
                        visualTransformation = PasswordVisualTransformation()
                    )
                    Spacer(modifier = Modifier.height(32.dp))
                    if (authState is AuthState.Error) {
                        Text(text = (authState as AuthState.Error).message, color = MaterialTheme.colorScheme.error)
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        MpButton(text = "Back", onClick = { step = if (role != "PATIENT") 4 else 3 }, isPrimary = false)
                        MpButton(
                            text = if (authState is AuthState.Loading) "Submitting..." else "Submit Registration",
                            onClick = {
                                viewModel.register(
                                    email = email,
                                    phone = phone,
                                    password = password,
                                    firstName = firstName,
                                    lastName = lastName,
                                    role = role,
                                    gender = "Other",
                                    dob = "2000-01-01",
                                    addressLine1 = addressLine1,
                                    city = "City",
                                    state = "State",
                                    postalCode = "123456",
                                    licenceNumber = licenceNumber.ifBlank { null }
                                )
                            },
                            enabled = authState !is AuthState.Loading
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
            TextButton(onClick = onBackToLogin) {
                Text("Already have an account? Sign In")
            }
        }
    }
}
