package com.example.crisisconnect.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.ui.graphics.vector.ImageVector

sealed class BottomNavItem(val route: String, val title: String, val icon: ImageVector) {
    object Map : BottomNavItem("map", "Map", Icons.Default.Map)
    object Alerts : BottomNavItem("alerts", "Alerts", Icons.Default.Notifications)
    object Profile : BottomNavItem("profile", "Profile", Icons.Default.Person)

    companion object {
        val items = listOf(Map, Alerts, Profile)
    }
}
