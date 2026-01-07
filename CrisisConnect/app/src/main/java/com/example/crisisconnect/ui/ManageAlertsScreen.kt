package com.example.crisisconnect.ui.screens

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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.crisisconnect.data.AdminRepository
import com.example.crisisconnect.data.AlertRepository
import com.example.crisisconnect.data.IncidentRepository
import com.example.crisisconnect.data.SessionManager
import kotlinx.coroutines.launch

@Composable
fun ManageAlertsScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    SessionManager.initialize(context)
    
    var alerts by remember { mutableStateOf<List<com.example.crisisconnect.data.DisasterAlert>>(emptyList()) }
    var pendingIncidents by remember { mutableStateOf<List<com.example.crisisconnect.data.IncidentReport>>(emptyList()) }
    var isAdmin by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var statusMessage by remember { mutableStateOf<String?>(null) }
    
    fun refreshData() {
        scope.launch {
            isLoading = true
            try {
                val userId = SessionManager.getUserId()
                if (userId != null) {
                    isAdmin = AdminRepository.isAdmin(userId)
                    if (isAdmin) {
                        alerts = AlertRepository.getAllAlerts()
                        pendingIncidents = IncidentRepository.getPendingIncidents()
                    }
                }
            } catch (e: Exception) {
                // Handle error
            } finally {
                isLoading = false
            }
        }
    }
    
    LaunchedEffect(Unit) {
        refreshData()
    }
    
    // Refresh data immediately after approve/reject (handled in callbacks)
    
    if (!isAdmin) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("Admin access required", style = MaterialTheme.typography.headlineSmall)
            Text("You need admin privileges to manage alerts.", color = Color.Black)
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Manage Alerts & Incidents", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text("Approve/reject incidents and manage disaster alerts.", color = Color.Black)

        Spacer(Modifier.height(16.dp))
        
        statusMessage?.let {
            Text(
                it,
                color = if (it.contains("success", true)) Color(0xFF2E7D32) else Color(0xFFD32F2F),
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            // Pending Incidents Section
            if (pendingIncidents.isNotEmpty()) {
                item {
                    Text(
                        "⏳ Pending Incidents (${pendingIncidents.size})",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = Color(0xFFD84315),
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
                items(pendingIncidents, key = { it.id }) { incident ->
                    IncidentManageCard(
                        incident = incident,
                        onApproved = {
                            statusMessage = "Incident approved successfully!"
                            refreshData()
                            // Clear message after 3 seconds
                            scope.launch {
                                kotlinx.coroutines.delay(3000)
                                statusMessage = null
                            }
                        },
                        onRejected = {
                            statusMessage = "Incident rejected."
                            refreshData()
                            // Clear message after 3 seconds
                            scope.launch {
                                kotlinx.coroutines.delay(3000)
                                statusMessage = null
                            }
                        },
                        onError = { errorMsg ->
                            statusMessage = errorMsg
                        }
                    )
                }
            }
            
            // Disaster Alerts Section
            if (alerts.isNotEmpty()) {
                item {
                    Spacer(Modifier.height(16.dp))
                    Text(
                        "🚨 Disaster Alerts",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = Color(0xFF1976D2),
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
                items(alerts, key = { it.id ?: "" }) { alert ->
                    AlertManageCard(alert)
                }
            }
            
            if (pendingIncidents.isEmpty() && alerts.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("📋", fontSize = 64.sp)
                        Spacer(Modifier.height(16.dp))
                        Text("No Pending Items", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                        Text("All incidents are processed", color = Color.Black, fontSize = 14.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun IncidentManageCard(
    incident: com.example.crisisconnect.data.IncidentReport,
    onApproved: () -> Unit,
    onRejected: () -> Unit,
    onError: (String) -> Unit
) {
    val scope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(false) }

    Card(
        colors = CardDefaults.cardColors(Color(0xFFFFF5F5)),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                "Incident Report",
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp,
                color = Color(0xFFD84315)
            )
            
            Text(
                incident.description,
                color = Color.DarkGray,
                fontSize = 14.sp
            )
            
            if (incident.lat != null && incident.lon != null) {
                Text(
                    "📍 ${String.format("%.6f", incident.lat)}, ${String.format("%.6f", incident.lon)}",
                    color = Color.Black,
                    fontSize = 12.sp
                )
            } else {
                Text(
                    "📍 Location not available",
                    color = Color.Gray.copy(alpha = 0.6f),
                    fontSize = 12.sp
                )
            }
            
            Text(
                "Status: ${incident.status} • Reported: ${incident.created_at.take(10)}",
                color = Color.Gray,
                fontSize = 11.sp
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        scope.launch {
                            isLoading = true
                            try {
                                IncidentRepository.approveIncident(incident.id)
                                onApproved() // This will refresh data and show success message
                            } catch (e: Exception) {
                                // Show error message
                                onError("Error approving: ${e.localizedMessage}")
                            } finally {
                                isLoading = false
                            }
                        }
                    },
                    enabled = !isLoading,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))
                ) {
                    Text(if (isLoading) "Processing..." else "✅ Approve", color = Color.White, fontSize = 12.sp)
                }
                
                Button(
                    onClick = {
                        scope.launch {
                            isLoading = true
                            try {
                                IncidentRepository.rejectIncident(incident.id)
                                onRejected() // This will refresh data and show success message
                            } catch (e: Exception) {
                                // Show error message
                                onError("Error rejecting: ${e.localizedMessage}")
                            } finally {
                                isLoading = false
                            }
                        }
                    },
                    enabled = !isLoading,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F))
                ) {
                    Text(if (isLoading) "Processing..." else "❌ Reject", color = Color.White, fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
private fun AlertManageCard(alert: com.example.crisisconnect.data.DisasterAlert) {
    val scope = rememberCoroutineScope()
    var title by remember { mutableStateOf(alert.title ?: "") }
    var message by remember { mutableStateOf(alert.message ?: "") }
    var severity by remember { mutableStateOf(alert.severity ?: "") }
    var isLoading by remember { mutableStateOf(false) }

    Card(
        colors = CardDefaults.cardColors(Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(alert.title ?: "Alert", fontWeight = FontWeight.SemiBold, fontSize = 18.sp, color = Color(0xFFD84315))
            Text(alert.message ?: "No description", color = Color.Black)
            Text("Type: ${alert.disaster_type ?: "Unknown"} • Severity: ${severity.ifEmpty { "Unknown" }}", color = Color.DarkGray, fontSize = 12.sp)

            Button(
                onClick = {
                    scope.launch {
                        isLoading = true
                        try {
                            val alertId = alert.id
                            if (alertId != null) {
                                AdminRepository.adminUpdateAlert(
                                    alertId = alertId,
                                    title = title.ifEmpty { null },
                                    message = message.ifEmpty { null },
                                    severity = severity.ifEmpty { null }
                                )
                            }
                        } catch (e: Exception) {
                            // Handle error
                        } finally {
                            isLoading = false
                        }
                    }
                },
                enabled = !isLoading,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD84315))
            ) {
                Text(if (isLoading) "Updating..." else "Update Alert", color = Color.White)
            }
        }
    }
}

