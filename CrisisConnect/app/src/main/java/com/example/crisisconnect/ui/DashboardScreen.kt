package com.example.crisisconnect.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Report
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.ShareLocation
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.crisisconnect.data.AlertRepository
import com.example.crisisconnect.data.IncidentRepository
import com.example.crisisconnect.data.SessionManager
import com.example.crisisconnect.data.model.AlertSeverity
import com.example.crisisconnect.ui.theme.PurpleEnd
import com.example.crisisconnect.ui.theme.PurpleStart
import kotlinx.coroutines.delay

@Composable
fun DashboardScreen(navController: NavController) {
    val context = LocalContext.current
    SessionManager.initialize(context)
    
    var alerts by remember { mutableStateOf<List<com.example.crisisconnect.data.DisasterAlert>>(emptyList()) }
    var incidents by remember { mutableStateOf<List<com.example.crisisconnect.data.IncidentReport>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    val criticalAlerts = alerts.count { 
        it.severity != null && (it.severity == "CRITICAL" || it.severity == "critical") 
    }
    // Count active incidents (verified, verified & assigned, in progress)
    val openIncidents = incidents.count { incident ->
        val status = incident.status.lowercase()
        status == "verified" || status.contains("verified") || status == "in progress"
    }
    val availableShelters = 0 // TODO: Load from shelters table
    
    // Load initial data
    LaunchedEffect(Unit) {
        val userId = SessionManager.getUserId()
        isLoading = true
        errorMessage = null
        if (userId != null) {
            try {
                // Load active disasters for user (within 5km and 1 day)
                alerts = AlertRepository.getActiveDisastersForUser(userId)
                // Load ALL verified incidents (not just user's own) for dashboard
                incidents = IncidentRepository.getVerifiedIncidents()
                errorMessage = null
            } catch (e: Exception) {
                // Only show error if it's not a serialization/empty response issue
                val errorMsg = e.localizedMessage ?: e.message ?: "Unknown error"
                if (errorMsg.contains("required for type") || errorMsg.contains("missing at path")) {
                    // RPC returned unexpected format - try fallback
                    try {
                        alerts = AlertRepository.getAllAlerts()
                        incidents = IncidentRepository.getVerifiedIncidents() // Still load incidents
                        errorMessage = null // Don't show error if fallback works
                    } catch (e2: Exception) {
                        // If fallback also fails, show a user-friendly message
                        errorMessage = "No active disasters found in your area"
                    }
                } else {
                    errorMessage = "Failed to load data: $errorMsg"
                }
            } finally {
                isLoading = false
            }
        } else {
            errorMessage = "Please log in to view dashboard"
            isLoading = false
        }
    }
    
    // Polling for active disasters and incidents every 2 minutes
    LaunchedEffect(Unit) {
        while (true) {
            delay(120000) // 2 minutes
            val userId = SessionManager.getUserId()
            if (userId != null) {
                try {
                    // Get active disasters within 5km and 1 day
                    val activeDisasters = AlertRepository.getActiveDisastersForUser(userId)
                    alerts = activeDisasters
                    // Refresh verified incidents count
                    incidents = IncidentRepository.getVerifiedIncidents()
                    // TODO: Show notifications for new alerts
                } catch (e: Exception) {
                    // Handle error silently - don't break polling
                    // Keep existing data if RPC fails
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {

        Text(
            "Operations Dashboard",
            style = MaterialTheme.typography.headlineSmall,
            color = Color.Black
        )
        Text(
            "Monitor disasters, responders and community safety in one view.",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Black
        )

        Spacer(Modifier.height(16.dp))

        // Loading state
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = PurpleStart)
                    Spacer(Modifier.height(16.dp))
                    Text("Loading dashboard...", color = Color.Black)
                }
            }
        } else {
            // Error state
            errorMessage?.let {
        Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE))
                ) {
                    Text(
                        it,
                        color = Color(0xFFD32F2F),
                        modifier = Modifier.padding(16.dp)
                    )
                }
                Spacer(Modifier.height(16.dp))
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                QuickStatCard(
                    title = "Critical Alerts",
                    value = "$criticalAlerts",
                    icon = Icons.Default.NotificationsActive,
                    onClick = { navController.navigate("alerts") },
                    modifier = Modifier.weight(1f)
                )
                QuickStatCard(
                    title = "Active Incidents",
                    value = "${incidents.size}",
                    icon = Icons.Default.Report,
                    onClick = { navController.navigate("myreports") },
                    modifier = Modifier.weight(1f)
                )
                QuickStatCard(
                    title = "Open Shelters",
                    value = "$availableShelters",
                    icon = Icons.Default.Place,
                    onClick = { navController.navigate("shelters") },
                    modifier = Modifier.weight(1f)
                )
            }

        Spacer(Modifier.height(20.dp))

        ActionCard(
            title = "Report Incident",
            description = "Submit verified incidents with media in less than a minute.",
            icon = Icons.Default.Report,
            onClick = { navController.navigate("report") }
        )

        Spacer(Modifier.height(14.dp))

        ActionCard(
            title = "Live Disaster Tracking",
            description = "View heat maps, hazard paths and responder status.",
            icon = Icons.Default.Map,
            onClick = { navController.navigate("map") }
        )

        Spacer(Modifier.height(14.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SmallActionCard("Shelters", "Safe zones & capacity") {
                navController.navigate("shelters")
            }
            SmallActionCard("Share Location", "Notify family & HQ") {
                navController.navigate("shareLocation")
            }
        }

        Spacer(Modifier.height(14.dp))

        AlertsTicker(navController, alerts)

        Spacer(Modifier.height(18.dp))

        Text("Quick Tools", fontWeight = FontWeight.SemiBold, color = PurpleStart)
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.padding(vertical = 12.dp)
        ) {
            item {
                QuickAction(Icons.Default.Chat, "AI Assistant") { navController.navigate("ai") }
            }
            item {
                QuickAction(Icons.Default.FilterList, "Filter Alerts") { navController.navigate("alerts") }
            }
            item {
                QuickAction(Icons.Default.Security, "Manage Users") { navController.navigate("manageUsers") }
            }
            item {
                QuickAction(Icons.Default.ShareLocation, "Broadcast") { navController.navigate("emergencyBroadcast") }
            }
            item {
                QuickAction(Icons.Default.Mic, "Voice Control") { navController.navigate("ai") }
            }
        }
        }
    }
}

@Composable
private fun AlertsTicker(
    navController: NavController,
    alerts: List<com.example.crisisconnect.data.DisasterAlert>
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { navController.navigate("alerts") },
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF2EEFF)),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text("Live Alerts Feed", fontWeight = FontWeight.SemiBold, color = PurpleStart)
            Spacer(Modifier.height(10.dp))
            alerts.take(3).forEach { alert ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(alert.title ?: "Alert", fontWeight = FontWeight.SemiBold, color = Color.Black)
                        Text(alert.message ?: "No description", maxLines = 2, color = Color.DarkGray, fontSize = 12.sp)
                    }
                    // Severity indicator
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .background(
                                when (alert.severity?.uppercase() ?: "") {
                                    "CRITICAL" -> Color(0xFFD32F2F)
                                    "HIGH" -> Color(0xFFFF6F00)
                                    "MODERATE" -> Color(0xFFFFC107)
                                    else -> Color(0xFF4CAF50)
                                },
                                CircleShape
                            )
                    )
                }
            }
        }
    }
}

@Composable
private fun SeverityDot(severity: AlertSeverity) {
    val color = when (severity) {
        AlertSeverity.LOW -> Color(0xFF4CAF50)
        AlertSeverity.MODERATE -> Color(0xFFFFC107)
        AlertSeverity.HIGH -> Color(0xFFFF7043)
        AlertSeverity.CRITICAL -> Color(0xFFD50000)
    }
    Canvas(modifier = Modifier
        .size(16.dp)
        .padding(start = 8.dp)) {
        drawCircle(color = color)
    }
}

@Composable
private fun QuickStatCard(
    title: String,
    value: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .height(110.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(6.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = title, tint = PurpleStart, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text(title, color = Color.Black, fontSize = 12.sp)
            }
            Text(value, fontSize = 26.sp, fontWeight = FontWeight.Bold, color = Color.Black)
            Text("Tap to open", color = Color.Black, fontSize = 11.sp)
        }
    }
}

@Composable
private fun ActionCard(
    title: String,
    description: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(18.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontSize = 20.sp, color = PurpleStart, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(6.dp))
                Text(description, color = Color.Black, fontSize = 14.sp)
            }
            Icon(icon, contentDescription = title, tint = PurpleEnd, modifier = Modifier.size(42.dp))
        }
    }
}

@Composable
fun QuickAction(icon: ImageVector, label: String, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .padding(4.dp)
            .clickable { onClick() }
    ) {
        Card(
            shape = RoundedCornerShape(18.dp),
            modifier = Modifier.size(72.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(6.dp)
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                Icon(icon, contentDescription = label, tint = PurpleStart, modifier = Modifier.size(32.dp))
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(label, color = Color.Black, fontSize = 13.sp)
    }
}

@Composable
fun RowScope.SmallActionCard(
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .weight(1f)
            .height(100.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(Color.White),
        elevation = CardDefaults.cardElevation(6.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(title, fontSize = 18.sp, color = PurpleStart, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(6.dp))
            Text(subtitle, color = Color.Black, fontSize = 13.sp)
        }
    }
}
