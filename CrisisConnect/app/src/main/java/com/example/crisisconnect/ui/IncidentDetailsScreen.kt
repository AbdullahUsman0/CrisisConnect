package com.example.crisisconnect.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.crisisconnect.data.IncidentRepository
import com.example.crisisconnect.data.SessionManager
import com.example.crisisconnect.ui.theme.AppRed
import com.example.crisisconnect.utils.MapUtils

@Composable
fun IncidentDetailsScreen(navController: NavController) {
    val context = LocalContext.current
    SessionManager.initialize(context)
    
    // For now, get the first user report (you can enhance this to accept incident ID via navigation)
    var incidentReport by remember { mutableStateOf<com.example.crisisconnect.data.IncidentReport?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    
    LaunchedEffect(Unit) {
        try {
            val userId = SessionManager.getUserId()
            if (userId != null) {
                val reports = IncidentRepository.getUserIncidentReports(userId)
                incidentReport = reports.firstOrNull()
            }
        } catch (e: Exception) {
            // Handle error
        } finally {
            isLoading = false
        }
    }
    
    if (isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator()
                Spacer(Modifier.height(16.dp))
                Text("Loading incident details...", color = Color.Black)
            }
        }
        return
    }
    
    val incident = incidentReport
    if (incident == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(32.dp)) {
                Text("📋", fontSize = 64.sp)
                Spacer(Modifier.height(16.dp))
                Text("No Incident Found", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                Text("Unable to load incident details", color = Color.Black, fontSize = 14.sp)
                Spacer(Modifier.height(24.dp))
                TextButton(onClick = { navController.popBackStack() }) {
                    Text("Go Back")
                }
            }
        }
        return
    }
    
    var verified by remember { mutableStateOf(incident.status.contains("verified", true)) }
    var responderDispatched by remember { mutableStateOf(false) }
    var shareWithAuthorities by remember { mutableStateOf(true) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {

        Text("Incident Details", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text("ID ${incident.id.take(8)} • ${incident.created_at.take(10)}", color = Color.Black)

        Spacer(Modifier.height(16.dp))

        DetailCard(label = "Status", value = incident.status)
        DetailCard(label = "Description", value = incident.description)
        
        // Location card with "View on Map" button
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Location", color = Color.Black, fontSize = 12.sp)
                        if (incident.lat != null && incident.lon != null) {
                            Text(
                                text = "📍 ${String.format("%.6f", incident.lat)}, ${String.format("%.6f", incident.lon)}",
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF1976D2)
                            )
                        } else {
                            Text(
                                text = "📍 Location not available",
                                fontWeight = FontWeight.SemiBold,
                                color = Color.Black
                            )
                        }
                    }
                    if (incident.lat != null && incident.lon != null) {
                        Button(
                            onClick = { navController.navigate("map") },
                            colors = ButtonDefaults.buttonColors(containerColor = AppRed),
                            modifier = Modifier.padding(start = 8.dp)
                        ) {
                            Icon(Icons.Default.Map, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("View on Map", fontSize = 12.sp)
                        }
                    }
                }
            }
        }
        
        // Also make location clickable to open in Google Maps
        if (incident.lat != null && incident.lon != null) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)
                    .clickable {
                        MapUtils.openInGoogleMaps(
                            context = context,
                            lat = incident.lat!!,
                            lon = incident.lon!!,
                            label = incident.description.take(50)
                        )
                    },
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color(0xFF1976D2))
                        Spacer(Modifier.width(8.dp))
                        Text("Open in Google Maps", color = Color(0xFF1976D2), fontWeight = FontWeight.Medium)
                    }
                    Icon(Icons.Default.Map, contentDescription = null, tint = Color(0xFF1976D2))
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        Card(colors = CardDefaults.cardColors(containerColor = Color(0xFFF6F6FA))) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Verification Checklist", fontWeight = FontWeight.SemiBold)
                CheckboxRow("Evidence reviewed", verified) { verified = it }
                CheckboxRow("Responder dispatched", responderDispatched) { responderDispatched = it }
                CheckboxRow("Share with Authorities", shareWithAuthorities) { shareWithAuthorities = it }
            }
        }

        Spacer(Modifier.height(20.dp))

        Button(
            onClick = {
                // Save verification logic can be added here
                navController.popBackStack()
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = AppRed)
        ) {
            Text("Save verification")
        }

        TextButton(onClick = { navController.popBackStack() }) {
            Text("Back")
        }
    }
}

@Composable
private fun DetailCard(label: String, value: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(label, color = Color.Black, fontSize = 12.sp)
            Text(value, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun CheckboxRow(label: String, checked: Boolean, onChecked: (Boolean) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Checkbox(checked = checked, onCheckedChange = onChecked)
        Spacer(Modifier.width(8.dp))
        Text(label)
    }
}

