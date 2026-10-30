package com.medipulse.app.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.medipulse.app.ui.components.MpButton
import com.medipulse.app.ui.components.MpTextField

@Composable
fun LoginScreen(
    viewModel: AuthViewModel,
    onLoginSuccess: (String) -> Unit,
    onNavigateRegister: () -> Unit
) {
    var identifier by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val authState by viewModel.authState.collectAsState()

    LaunchedEffect(authState) {
        if (authState is AuthState.Success) {
            val role = (authState as AuthState.Success).role
            onLoginSuccess(role)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary,
                        MaterialTheme.colorScheme.secondary
                    )
                )
            )
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
                .align(Alignment.TopCenter),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(32.dp))
            Text(
                text = "MediPulse",
                style = MaterialTheme.colorScheme.displayLarge,
                color = MaterialTheme.colorScheme.onPrimary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Care. Stock. Simplified.",
                style = MaterialTheme.colorScheme.titleMedium,
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.9f)
            )
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.75f)
                .align(Alignment.BottomCenter),
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Welcome Back",
                    style = MaterialTheme.colorScheme.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(24.dp))

                MpTextField(
                    value = identifier,
                    onValueChange = { identifier = it },
                    label = "Email or Phone",
                    leadingIcon = Icons.Default.Email
                )
                Spacer(modifier = Modifier.height(16.dp))

                MpTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = "Password",
                    leadingIcon = Icons.Default.Lock,
                    visualTransformation = PasswordVisualTransformation()
                )
                Spacer(modifier = Modifier.height(24.dp))

                if (authState is AuthState.Error) {
                    Text(
                        text = (authState as AuthState.Error).message,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.colorScheme.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }

                MpButton(
                    text = if (authState is AuthState.Loading) "Signing in..." else "Sign In",
                    onClick = { viewModel.login(identifier, password) },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = authState !is AuthState.Loading
                )
                Spacer(modifier = Modifier.height(16.dp))

                TextButton(onClick = onNavigateRegister) {
                    Text(text = "Don't have an account? Register")
                }
            }
        }
    }
}
