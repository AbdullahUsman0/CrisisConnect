package com.example.crisisconnect.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.foundation.clickable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.crisisconnect.data.IncidentRepository
import com.example.crisisconnect.data.SessionManager
import com.example.crisisconnect.utils.MapUtils

@Composable
fun MyReportsScreen(navController: NavController) {
    val context = LocalContext.current
    SessionManager.initialize(context)
    
    var reports by remember { mutableStateOf<List<com.example.crisisconnect.data.IncidentReport>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        isLoading = true
        try {
            val userId = SessionManager.getUserId()
            if (userId != null) {
                reports = IncidentRepository.getUserIncidentReports(userId)
            }
        } catch (e: Exception) {
            // Handle error
        } finally {
            isLoading = false
        }
    }

    Column(modifier = Modifier.padding(16.dp)) {
        Text("My Reports", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text("Tap to open details and verify status.", color = Color.Black)

        Spacer(Modifier.height(16.dp))

        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator()
                    Spacer(Modifier.height(16.dp))
                    Text("Loading reports...", color = Color.Black)
                }
            }
        } else if (reports.isEmpty()) {
            // Empty state
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(32.dp)) {
                    Text("📋", fontSize = 64.sp)
                    Spacer(Modifier.height(16.dp))
                    Text("No Reports Yet", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                    Text("Report an incident to see it here", color = Color.Black, fontSize = 14.sp)
                    Spacer(Modifier.height(24.dp))
                    Button(
                        onClick = { navController.navigate("report") },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3949AB))
                    ) {
                        Text("Report Incident", color = Color.White)
                    }
                }
            }
        } else {
            LazyColumn {
                items(reports, key = { it.id }) { report ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        colors = CardDefaults.cardColors(Color.White),
                        onClick = { navController.navigate("incidentDetails") }
                    ) {
                        Column(Modifier.padding(16.dp)) {
                            Text(
                                text = report.description.take(80) + if (report.description.length > 80) "..." else "",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp
                            )
                            Spacer(Modifier.height(4.dp))
                            
                            // Show location if available (clickable to open in Google Maps)
                            if (report.lat != null && report.lon != null) {
                                Text(
                                    text = "📍 ${String.format("%.6f", report.lat)}, ${String.format("%.6f", report.lon)} (Tap to view in Maps)",
                                    color = Color(0xFF1976D2),
                                    fontSize = 12.sp,
                                    modifier = Modifier.clickable {
                                        MapUtils.openInGoogleMaps(
                                            context = context,
                                            lat = report.lat!!,
                                            lon = report.lon!!,
                                            label = report.description.take(50)
                                        )
                                    }
                                )
                            } else {
                                Text(
                                    text = "📍 Location not available",
                                    color = Color.Gray.copy(alpha = 0.6f),
                                    fontSize = 12.sp
                                )
                            }
                            
                            Spacer(Modifier.height(4.dp))
                            
                            // Status with color coding
                            val statusColor = when (report.status.lowercase()) {
                                "pending", "pending review" -> Color(0xFFD84315) // Orange
                                "verified", "verified & assigned" -> Color(0xFF2E7D32) // Green
                                "in progress", "containment in progress" -> Color(0xFF1976D2) // Blue
                                else -> Color(0xFF757575) // Grey
                            }
                            Text(
                                text = "Status: ${report.status}",
                                color = statusColor,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                            
                            // Show created date if available
                            if (!report.created_at.isNullOrBlank()) {
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text = "Reported: ${report.created_at.take(10)}",
                                    color = Color.Gray.copy(alpha = 0.7f),
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
