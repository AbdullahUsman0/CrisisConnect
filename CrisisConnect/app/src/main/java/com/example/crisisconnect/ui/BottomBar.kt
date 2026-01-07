package com.example.crisisconnect.ui.navigation

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.crisisconnect.ui.theme.PurpleStart

@Composable
fun BottomBar(navController: NavHostController) {
    val items = BottomNavItem.items
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    
    // Only show bottom bar on main screen and its child screens
    val showBottomBar = currentRoute in listOf("main", "map", "alerts", "profile", "dashboard")
    
    if (showBottomBar) {
        NavigationBar(containerColor = Color.White, contentColor = PurpleStart) {
            items.forEach { item ->
                NavigationBarItem(
                    selected = currentRoute == item.route,
                    onClick = {
                        // Don't navigate if already on that screen
                        if (currentRoute != item.route) {
                            navController.navigate(item.route) {
                                launchSingleTop = true
                                restoreState = true
                                popUpTo("main") { saveState = true }
                            }
                        }
                    },
                    icon = {
                        Icon(
                            item.icon, 
                            contentDescription = item.title, 
                            tint = if (currentRoute == item.route) PurpleStart else Color.Gray
                        )
                    },
                    label = { Text(item.title) },
                    alwaysShowLabel = false
                )
            }
        }
    }
}
