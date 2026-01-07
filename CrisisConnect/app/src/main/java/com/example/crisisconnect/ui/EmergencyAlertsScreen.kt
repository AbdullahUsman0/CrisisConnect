package com.example.crisisconnect.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.foundation.clickable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import com.example.crisisconnect.data.AlertRepository
import com.example.crisisconnect.data.IncidentRepository
import com.example.crisisconnect.data.SessionManager
import com.example.crisisconnect.data.model.AlertSeverity
import com.example.crisisconnect.ui.theme.AppRed
import com.example.crisisconnect.ui.theme.TextPrimary
import com.example.crisisconnect.utils.MapUtils

@Composable
fun EmergencyAlertsScreen(navController: NavController) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    SessionManager.initialize(context)
    
    var alerts by remember { mutableStateOf<List<com.example.crisisconnect.data.DisasterAlert>>(emptyList()) }
    var verifiedIncidents by remember { mutableStateOf<List<com.example.crisisconnect.data.IncidentReport>>(emptyList()) }
    var pendingIncidents by remember { mutableStateOf<List<com.example.crisisconnect.data.IncidentReport>>(emptyList()) }
    var selectedType by remember { mutableStateOf<String?>(null) }
    var selectedSeverity by remember { mutableStateOf<String?>(null) }
    var onlyVerified by remember { mutableStateOf(true) }
    var isLoading by remember { mutableStateOf(false) }
    var refreshKey by remember { mutableStateOf(0) } // Key to trigger refresh

    // Load alerts and all incidents (verified and pending)
    LaunchedEffect(refreshKey) {
        isLoading = true
        try {
            alerts = AlertRepository.getAllAlerts()
            verifiedIncidents = IncidentRepository.getVerifiedIncidents()
            pendingIncidents = IncidentRepository.getPendingIncidents()
        } catch (e: Exception) {
            // Handle error
        } finally {
            isLoading = false
        }
    }
    
    // Auto-refresh every 10 seconds to catch new alerts
    LaunchedEffect(Unit) {
        while (true) {
            kotlinx.coroutines.delay(10000) // 10 seconds
            try {
                alerts = AlertRepository.getAllAlerts()
                verifiedIncidents = IncidentRepository.getVerifiedIncidents()
                pendingIncidents = IncidentRepository.getPendingIncidents()
            } catch (e: Exception) {
                // Handle error silently
            }
        }
    }

    // Filter incidents based on verification status
    val filteredIncidents = if (onlyVerified) {
        verifiedIncidents // Show only verified incidents
    } else {
        verifiedIncidents + pendingIncidents // Show all incidents (verified + pending/unverified)
    }

    // Filter disaster alerts (separate from incidents)
    // Make filtering case-insensitive and handle variations
    val filteredAlerts = alerts.filter { alert ->
        val typeMatch = if (selectedType == null) {
            true
        } else {
            val alertType = alert.disaster_type?.lowercase()?.trim() ?: ""
            val filterType = selectedType!!.lowercase().trim()
            // Exact match or contains match (for variations like "Fire" matching "Wildfire")
            alertType == filterType || alertType.contains(filterType) || filterType.contains(alertType)
        }
        val severityMatch = if (selectedSeverity == null) {
            true
        } else {
            alert.severity?.equals(selectedSeverity, ignoreCase = true) == true
        }
        typeMatch && severityMatch
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(16.dp)
    ) {
        
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator()
                    Spacer(Modifier.height(16.dp))
                        Text("Loading alerts...", color = Color.Black)
                }
            }
        } else {

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "Incidents & Alerts",
                    fontWeight = FontWeight.Bold,
                    fontSize = 28.sp,
                    color = AppRed
                )
                Text(
                    "View verified or pending incidents. Filter by type, severity, or jump to map.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Black
                )
            }
            // Refresh button
            androidx.compose.material3.IconButton(
                onClick = { refreshKey++ } // Trigger refresh
            ) {
                Icon(
                    Icons.Default.Refresh,
                    contentDescription = "Refresh alerts",
                    tint = AppRed
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        FilterRow(
            selectedType = selectedType,
            onTypeSelected = { type ->
                selectedType = if (selectedType == type) null else type
            },
            selectedSeverity = selectedSeverity,
            onSeveritySelected = { severity ->
                selectedSeverity = if (selectedSeverity == severity) null else severity
            },
            onlyVerified = onlyVerified,
            onVerifiedToggle = { onlyVerified = !onlyVerified },
            onOpenMap = { navController.navigate("map") }
        )

        Spacer(Modifier.height(16.dp))

        if (filteredIncidents.isEmpty() && filteredAlerts.isEmpty()) {
            // Empty state
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(32.dp)) {
                    Text("🚨", fontSize = 64.sp)
                    Spacer(Modifier.height(16.dp))
                    Text(
                        text = if (onlyVerified) "No Verified Incidents" else "No Incidents Found",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                    Text(
                        text = if (onlyVerified) "No verified incidents found" else "No incidents found (verified or pending)",
                        color = Color.Black,
                        fontSize = 14.sp
                    )
                }
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                // Show incidents based on verification filter
                // Note: Incidents don't have type/severity fields, so they're always shown regardless of type/severity filters
                // Type/severity filters only apply to disaster alerts
                if (filteredIncidents.isNotEmpty()) {
                    item {
                        Text(
                            text = if (onlyVerified) "✅ Verified Incidents (${filteredIncidents.size})" else "📋 All Incidents (${filteredIncidents.size})",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = if (onlyVerified) Color(0xFF2E7D32) else AppRed,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                    items(filteredIncidents, key = { it.id }) { incident ->
                        IncidentAlertCard(incident)
                    }
                }
                
                // Show disaster alerts (separate section) - these can be filtered by type/severity
                if (filteredAlerts.isNotEmpty()) {
                    item {
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "🌍 Disaster Alerts (${filteredAlerts.size})",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = AppRed,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                    items(filteredAlerts, key = { it.id ?: "" }) { alert ->
                        AlertCard(alert)
                    }
                } else if (alerts.isEmpty() && !isLoading) {
                    // Show message if no alerts found
                    item {
                        Spacer(Modifier.height(16.dp))
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                "No disaster alerts found",
                                fontWeight = FontWeight.SemiBold,
                                color = Color.Gray
                            )
                            Text(
                                "Emergency notifications will appear here after being sent",
                                fontSize = 12.sp,
                                color = Color.Gray,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                }
            }
        }
        }
    }
}

@Composable
    private fun FilterRow(
        selectedType: String?,
        onTypeSelected: (String) -> Unit,
        selectedSeverity: String?,
        onSeveritySelected: (String) -> Unit,
        onlyVerified: Boolean,
        onVerifiedToggle: () -> Unit,
        onOpenMap: () -> Unit
    ) {
        // Common disaster types - case-insensitive matching will handle variations
        val disasterTypes = listOf("Fire", "Flood", "Earthquake", "Medical", "Security", "Infrastructure", "Storm", "Wildfire", "Tornado")
        val severityLevels = listOf("LOW", "MODERATE", "HIGH", "CRITICAL")
    
    Column {
        Text("Filter by type", fontWeight = FontWeight.SemiBold, color = Color.Black)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            disasterTypes.forEach { type ->
                FilterChip(
                    label = type,
                    selected = selectedType == type,
                    onSelected = { onTypeSelected(type) }
                )
            }
        }

        Text("Severity", fontWeight = FontWeight.SemiBold)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            severityLevels.forEach { severity ->
                FilterChip(
                    label = severity.lowercase().replaceFirstChar { it.titlecase() },
                    selected = selectedSeverity?.equals(severity, ignoreCase = true) == true,
                    onSelected = { onSeveritySelected(severity) }
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            FilterChip(
                label = if (onlyVerified) "Verified Only" else "Include Unverified",
                selected = onlyVerified,
                onSelected = { onVerifiedToggle() }
            )
            AssistChip(
                onClick = onOpenMap,
                label = { Text("View on Map") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Place,
                        contentDescription = null
                    )
                }
            )
        }
    }
}

@Composable
private fun FilterChip(label: String, selected: Boolean, onSelected: () -> Unit) {
    AssistChip(
        onClick = onSelected,
        label = { Text(label) },
        colors = AssistChipDefaults.assistChipColors(
            containerColor = if (selected) Color(0xFFE1DBFF) else Color(0xFFF4F4F6),
            labelColor = if (selected) AppRed else Color.Black
        )
    )
}

@Composable
private fun AlertCard(alert: com.example.crisisconnect.data.DisasterAlert) {
    val indicatorColor = when (alert.severity?.uppercase() ?: "") {
        "CRITICAL" -> Color(0xFFD32F2F)
        "HIGH" -> Color(0xFFFF7043)
        "MODERATE" -> Color(0xFFFFC107)
        "LOW" -> Color(0xFF4CAF50)
        else -> Color(0xFF4CAF50)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(Color(0xFFFFF5F5)),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    alert.title ?: "Alert",
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
                Text(
                    alert.created_at?.take(10) ?: "Unknown date", 
                    color = Color.Black, 
                    fontSize = 12.sp
                )
            }

            Spacer(Modifier.height(4.dp))
            Text(alert.message ?: "No description", color = Color.DarkGray)

            Spacer(Modifier.height(8.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                StatusPill(text = alert.disaster_type ?: "Unknown", color = indicatorColor)
                StatusPill(text = alert.severity ?: "Unknown", color = indicatorColor.copy(alpha = 0.3f))
            }

            Spacer(Modifier.height(6.dp))
            val alertContext = LocalContext.current
            if (alert.lat != null && alert.lon != null) {
                Text(
                    text = "📍 Location: ${alert.location ?: "Unknown"} (Tap to view in Maps)",
                    color = Color(0xFF1976D2),
                    fontSize = 12.sp,
                    modifier = Modifier.clickable {
                        MapUtils.openInGoogleMaps(
                            context = alertContext,
                            lat = alert.lat!!,
                            lon = alert.lon!!,
                            label = alert.title ?: "Alert"
                        )
                    }
                )
            } else {
                Text(
                    text = "📍 Location: ${alert.location ?: "Unknown"}",
                    color = Color.Black,
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Composable
private fun StatusPill(text: String, color: Color) {
    Card(
        shape = CardDefaults.shape,
        colors = CardDefaults.cardColors(color.copy(alpha = 0.15f))
    ) {
        Text(
            text = text,
            color = color,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun IncidentAlertCard(incident: com.example.crisisconnect.data.IncidentReport) {
    val indicatorColor = when (incident.status.lowercase()) {
        "verified", "verified & assigned" -> Color(0xFF4CAF50) // Green
        "in progress" -> Color(0xFF1976D2) // Blue
        else -> Color(0xFF757575) // Grey
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(Color(0xFFF1F8E9)), // Light green background
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = incident.description.take(60) + if (incident.description.length > 60) "..." else "",
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
                Text(
                    text = incident.created_at.take(10),
                    color = Color.Black,
                    fontSize = 12.sp
                )
            }

            Spacer(Modifier.height(4.dp))
            val context = LocalContext.current
            if (incident.lat != null && incident.lon != null) {
                Text(
                    text = "📍 ${String.format("%.4f", incident.lat)}, ${String.format("%.4f", incident.lon)} (Tap to view in Maps)",
                    color = Color(0xFF1976D2),
                    fontSize = 12.sp,
                    modifier = Modifier.clickable {
                        MapUtils.openInGoogleMaps(
                            context = context,
                            lat = incident.lat!!,
                            lon = incident.lon!!,
                            label = incident.description.take(50)
                        )
                    }
                )
            } else {
                Text(
                    text = "📍 Location not available",
                    color = Color.DarkGray,
                    fontSize = 12.sp
                )
            }

            Spacer(Modifier.height(8.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                StatusPill(text = "Incident", color = indicatorColor)
                StatusPill(text = incident.status, color = indicatorColor.copy(alpha = 0.3f))
            }
        }
    }
}
