package com.example.crisisconnect.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.crisisconnect.ui.screens.*

@Composable
fun MainNavGraph(navController: NavHostController, modifier: Modifier = Modifier) {

    NavHost(
        navController = navController,
        startDestination = "dashboard",
        modifier = modifier
    ) {

        composable("dashboard") { DashboardScreen(navController) }
        composable("report") { ReportIncidentScreen(navController) }
        composable("alerts") { EmergencyAlertsScreen(navController) }
        composable("profile") { ProfileScreen(navController) }


        composable("settings") { SettingsScreen() }
        composable("myreports") { MyReportsScreen(navController) }
        composable("incidentDetails") { IncidentDetailsScreen(navController) }
        composable("map") { MapScreen() }
        composable("ai") { AIChatScreen(navController) }
        composable("contacts") { ContactsScreen(navController) }
    }
}
