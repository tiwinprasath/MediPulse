package com.medipulse.app.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.medipulse.app.ui.admin.AdminDashboardScreen
import com.medipulse.app.ui.admin.AdminViewModel
import com.medipulse.app.ui.analytics.AnalyticsScreen
import com.medipulse.app.ui.analytics.AnalyticsViewModel
import com.medipulse.app.ui.assistant.AssistantScreen
import com.medipulse.app.ui.auth.AuthViewModel
import com.medipulse.app.ui.auth.LoginScreen
import com.medipulse.app.ui.auth.RegistrationScreen
import com.medipulse.app.ui.catalog.CatalogScreen
import com.medipulse.app.ui.catalog.CatalogViewModel
import com.medipulse.app.ui.pos.PosScreen
import com.medipulse.app.ui.pos.PosViewModel
import com.medipulse.app.ui.profile.ProfileScreen
import com.medipulse.app.ui.profile.ProfileViewModel

@Composable
fun MediPulseNavGraph(
    navController: NavHostController = rememberNavController()
) {
    NavHost(navController = navController, startDestination = "login") {
        composable("login") {
            val viewModel: AuthViewModel = hiltViewModel()
            LoginScreen(
                viewModel = viewModel,
                onLoginSuccess = { role ->
                    val destination = when (role) {
                        "ADMIN" -> "admin_dashboard"
                        "MANAGER" -> "manager_dashboard"
                        "PHARMACIST" -> "pharmacist_dashboard"
                        else -> "patient_dashboard"
                    }
                    navController.navigate(destination) {
                        popUpTo("login") { inclusive = true }
                    }
                },
                onNavigateRegister = { navController.navigate("register") }
            )
        }
        composable("register") {
            val viewModel: AuthViewModel = hiltViewModel()
            RegistrationScreen(
                viewModel = viewModel,
                onRegisterSuccess = { navController.navigate("login") },
                onBackToLogin = { navController.popBackStack() }
            )
        }
        composable("admin_dashboard") {
            MainRoleScaffold(role = "Admin", navController = navController)
        }
        composable("manager_dashboard") {
            MainRoleScaffold(role = "Manager", navController = navController)
        }
        composable("pharmacist_dashboard") {
            MainRoleScaffold(role = "Pharmacist", navController = navController)
        }
        composable("patient_dashboard") {
            MainRoleScaffold(role = "Patient", navController = navController)
        }
    }
}

@Composable
fun MainRoleScaffold(role: String, navController: NavHostController) {
    var selectedTab by remember { mutableIntStateOf(0) }

    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Home, contentDescription = null) },
                    label = { Text("Home") },
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 }
                )
                if (role == "Admin") {
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.List, contentDescription = null) },
                        label = { Text("Admin") },
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 }
                    )
                } else {
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.List, contentDescription = null) },
                        label = { Text("Catalog") },
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 }
                    )
                }
                NavigationBarItem(
                    icon = { Icon(Icons.Default.ShoppingCart, contentDescription = null) },
                    label = { Text("POS") },
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Search, contentDescription = null) },
                    label = { Text("Analytics") },
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Person, contentDescription = null) },
                    label = { Text("AI") },
                    selected = selectedTab == 4,
                    onClick = { selectedTab = 4 }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Person, contentDescription = null) },
                    label = { Text("Profile") },
                    selected = selectedTab == 5,
                    onClick = { selectedTab = 5 }
                )
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
            when (selectedTab) {
                0 -> PlaceholderDashboard(role = role)
                1 -> if (role == "Admin") AdminDashboardScreen(viewModel = hiltViewModel<AdminViewModel>()) else CatalogScreen(viewModel = hiltViewModel<CatalogViewModel>())
                2 -> PosScreen(viewModel = hiltViewModel<PosViewModel>())
                3 -> AnalyticsScreen(viewModel = hiltViewModel<AnalyticsViewModel>())
                4 -> AssistantScreen()
                5 -> ProfileScreen(
                    viewModel = hiltViewModel<ProfileViewModel>(),
                    onLogout = {
                        navController.navigate("login") {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun PlaceholderDashboard(role: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = "$role Dashboard - MediPulse Active", style = MaterialTheme.colorScheme.displayLarge)
    }
}
