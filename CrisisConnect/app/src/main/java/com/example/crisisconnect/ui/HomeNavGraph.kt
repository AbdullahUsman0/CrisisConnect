package com.example.crisisconnect.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.crisisconnect.ui.screens.*

@Composable
fun HomeNavGraph(navController: NavHostController) {
    NavHost(navController = navController, startDestination = "dashboard") {
        composable("dashboard") { DashboardScreen(navController) }
        composable("map") { MapScreen() }
        composable("alerts") { EmergencyAlertsScreen(navController) }
        composable("profile") { ProfileScreen(navController) }
        // your implemented screens (AI, contacts, notifications, settings, about)
        composable("ai") { AIChatScreen(navController) }
        composable("contacts") { ContactsScreen(navController) }
        composable("notifications") { NotificationsScreen(navController) }
        composable("settings") { SettingsScreen() }
        composable("about") { AboutScreen() }
    }
}
