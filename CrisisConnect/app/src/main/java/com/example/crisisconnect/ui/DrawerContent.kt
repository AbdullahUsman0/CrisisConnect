package com.example.crisisconnect.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowCircleUp
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.crisisconnect.ui.theme.PurpleStart

@Composable
fun DrawerContent(
    onNavigate: (String) -> Unit,
    onLogout: () -> Unit
) {
    ModalDrawerSheet(
        modifier = Modifier
            .width(260.dp)
            .fillMaxHeight()
            .background(Color.White)
    ) {
        Spacer(Modifier.height(12.dp))

        Card(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Text("Crisis Operator", color = PurpleStart, style = MaterialTheme.typography.titleMedium)
                Text("ops@crisisconnect.app", color = Color.Gray, fontSize = 14.sp)
            }
        }

        Spacer(Modifier.height(8.dp))

        DrawerItem("Dashboard", Icons.Default.Map) { onNavigate("main") }
        DrawerItem("Profile", Icons.Default.Person) { onNavigate("profile") }
        DrawerItem("Settings", Icons.Default.Settings) { onNavigate("settings") }
        DrawerItem("Emergency Contacts", Icons.Default.Phone) { onNavigate("contacts") }
        DrawerItem("AI Assistant", Icons.Default.Chat) { onNavigate("ai") }
        DrawerItem("Notifications", Icons.Default.Notifications) { onNavigate("notifications") }
        DrawerItem("Shelters & Safe Zones", Icons.Default.Map) { onNavigate("shelters") }
        DrawerItem("Safety Instructions", Icons.Default.Info) { onNavigate("safety") }
        DrawerItem("Manage Users", Icons.Default.Security) { onNavigate("manageUsers") }
        DrawerItem("Manage Alerts", Icons.Default.ArrowCircleUp) { onNavigate("manageAlerts") }

        Spacer(Modifier.height(26.dp))

        DrawerItem("Logout", Icons.Default.ExitToApp, Color.Red) { onLogout() }
    }
}

fun ModalDrawerSheet(modifier: androidx.compose.ui.Modifier, content: () -> Unit) {}

@Composable
private fun DrawerItem(
    title: String,
    icon: ImageVector,
    color: Color = PurpleStart,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = title, tint = color)
        Spacer(modifier = Modifier.width(16.dp))
        Text(text = title, fontSize = 16.sp, color = color, style = MaterialTheme.typography.bodyMedium)
    }
}
