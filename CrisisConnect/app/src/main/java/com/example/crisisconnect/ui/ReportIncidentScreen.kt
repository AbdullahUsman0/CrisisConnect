package com.example.crisisconnect.ui.screens

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.rememberCoroutineScope
import androidx.navigation.NavController
import com.example.crisisconnect.data.IncidentRepository
import com.example.crisisconnect.data.LocationService
import com.example.crisisconnect.data.SessionManager
import com.example.crisisconnect.data.model.AlertSeverity
import com.example.crisisconnect.ui.theme.AppRed
import kotlinx.coroutines.launch

@Composable
fun ReportIncidentScreen(navController: NavController) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    SessionManager.initialize(context)
    
    val incidentTypes = listOf("Fire", "Flood", "Earthquake", "Medical", "Security", "Infrastructure")
    val severityLevels = AlertSeverity.entries

    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(incidentTypes.first()) }
    var severity by remember { mutableStateOf(AlertSeverity.MODERATE) }
    var reporter by remember { mutableStateOf("You") }
    var expanded by remember { mutableStateOf(false) }
    var showDialog by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var statusMessage by remember { mutableStateOf<String?>(null) }
    var hasLocationPermission by remember { mutableStateOf(LocationService.hasLocationPermission(context)) }
    var locationLat by remember { mutableStateOf<Double?>(null) }
    var locationLon by remember { mutableStateOf<Double?>(null) }

    // Location permission launcher
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        hasLocationPermission = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        
        if (hasLocationPermission) {
            // Get location after permission granted
            scope.launch {
                try {
                    val currentLocation = LocationService.getCurrentLocation(context)
                    currentLocation?.let {
                        locationLat = it.latitude
                        locationLon = it.longitude
                        location = "${it.latitude}, ${it.longitude}"
                    } ?: run {
                        // Try last known location if current location unavailable
                        val lastLocation = LocationService.getLastKnownLocation(context)
                        lastLocation?.let {
                            locationLat = it.latitude
                            locationLon = it.longitude
                            location = "${it.latitude}, ${it.longitude}"
                        }
                    }
                } catch (e: Exception) {
                    statusMessage = "Could not get location: ${e.message}"
                }
            }
        } else {
            statusMessage = "Location permission is required to report incidents accurately."
        }
    }

    // Get location if permission already granted (don't auto-request on load)
    LaunchedEffect(hasLocationPermission) {
        if (hasLocationPermission) {
            scope.launch {
                try {
                    val currentLocation = LocationService.getCurrentLocation(context)
                    currentLocation?.let {
                        locationLat = it.latitude
                        locationLon = it.longitude
                        location = "${it.latitude}, ${it.longitude}"
                    } ?: run {
                        val lastLocation = LocationService.getLastKnownLocation(context)
                        lastLocation?.let {
                            locationLat = it.latitude
                            locationLon = it.longitude
                            location = "${it.latitude}, ${it.longitude}"
                        }
                    }
                } catch (_: Exception) {
                    // Silent fail - user can still enter location manually
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(20.dp)
    ) {
        Text(
            "Report Incident",
            fontWeight = FontWeight.Bold,
            fontSize = 28.sp,
            color = AppRed
        )
        Text(
            "Captured reports will be routed to authorities for verification.",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.DarkGray
        )

        Spacer(Modifier.height(20.dp))

        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Incident Title") },
            modifier = Modifier.fillMaxWidth(),
            colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.Black,
                unfocusedTextColor = Color.Black
            )
        )

        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = location,
            onValueChange = { location = it },
            label = { Text("Location / Coordinates") },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Auto-filled from GPS or enter manually") },
            colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.Black,
                unfocusedTextColor = Color.Black
            )
        )
        
        if (!hasLocationPermission) {
            Text(
                "⚠️ Location permission required. Click 'Submit Report' to grant access.",
                color = Color(0xFFD32F2F),
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 4.dp)
            )
        } else if (locationLat == null || locationLon == null) {
            Text(
                "📍 Getting your location...",
                color = Color(0xFF1976D2),
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = reporter,
            onValueChange = { reporter = it },
            label = { Text("Reporter Name / Unit") },
            modifier = Modifier.fillMaxWidth(),
            colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.Black,
                unfocusedTextColor = Color.Black
            )
        )

        Spacer(Modifier.height(12.dp))

        Text("Incident Type", fontWeight = FontWeight.SemiBold)
        Button(
            onClick = { expanded = true },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 6.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF2F2F7)),
            contentPadding = ButtonDefaults.ContentPadding
        ) {
            Text(selectedType, color = Color.Black)
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            incidentTypes.forEach {
                DropdownMenuItem(
                    text = { Text(it) },
                    onClick = {
                        selectedType = it
                        expanded = false
                    }
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        Text("Severity", fontWeight = FontWeight.SemiBold)
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.padding(top = 6.dp)
        ) {
            severityLevels.forEach { level ->
                SeverityChip(
                    label = level.name.lowercase().replaceFirstChar { it.titlecase() },
                    selected = severity == level
                ) { severity = level }
            }
        }

        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("Detailed Description") },
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp),
            colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.Black,
                unfocusedTextColor = Color.Black
            )
        )

        Spacer(Modifier.height(20.dp))

        statusMessage?.let {
            Text(
                it,
                color = if (it.contains("success", true)) Color(0xFF1B5E20) else Color(0xFFD32F2F),
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }

        Button(
            onClick = {
                if (title.isNotBlank() && description.isNotBlank()) {
                    // Request permission first if not granted
                    if (!hasLocationPermission) {
                        locationPermissionLauncher.launch(
                            arrayOf(
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                            )
                        )
                        statusMessage = "Please grant location permission to submit report."
                    } else {
                        scope.launch {
                        // Check if we have location coordinates
                        if (locationLat == null || locationLon == null) {
                            // Try to get location
                            try {
                                val currentLocation = LocationService.getCurrentLocation(context)
                                if (currentLocation != null) {
                                    locationLat = currentLocation.latitude
                                    locationLon = currentLocation.longitude
                                    location = "${currentLocation.latitude}, ${currentLocation.longitude}"
                                } else {
                                    val lastLocation = LocationService.getLastKnownLocation(context)
                                    if (lastLocation != null) {
                                        locationLat = lastLocation.latitude
                                        locationLon = lastLocation.longitude
                                        location = "${lastLocation.latitude}, ${lastLocation.longitude}"
                                    } else {
                                        statusMessage = "Could not get location. Please enter coordinates manually or enable location services."
                                        return@launch
                                    }
                                }
                            } catch (e: Exception) {
                                statusMessage = "Location error: ${e.message}. Please enter coordinates manually."
                                return@launch
                            }
                        }
                        
                        // Submit the report
                        isLoading = true
                        statusMessage = null
                        try {
                            val userId = SessionManager.getUserId()
                            if (userId == null) {
                                statusMessage = "Please log in to submit a report."
                                isLoading = false
                                return@launch
                            }
                            
                            // Use GPS coordinates if available, otherwise try to parse from location string
                            val lat = locationLat ?: run {
                                // Try to parse from location string (format: "lat, lon")
                                location.split(",").firstOrNull()?.toDoubleOrNull() ?: 31.5204 // Default to Lahore
                            }
                            val lon = locationLon ?: run {
                                location.split(",").getOrNull(1)?.trim()?.toDoubleOrNull() ?: 74.3587 // Default to Lahore
                            }
                            
                            val fullDescription = if (title.isNotBlank()) {
                                "$title: $description"
                            } else {
                                description
                            }
                            
                            val reportId = IncidentRepository.addIncidentReport(
                                userId = userId,
                                description = fullDescription,
                                lat = lat,
                                lon = lon
                            )
                            statusMessage = "Report submitted successfully! ID: ${reportId.take(8)}..."
                            showDialog = true
                            title = ""
                            description = ""
                            location = ""
                            locationLat = null
                            locationLon = null
                        } catch (e: Exception) {
                            statusMessage = e.localizedMessage ?: "Failed to submit report. Please try again."
                        } finally {
                            isLoading = false
                        }
                        }
                    }
                } else {
                    statusMessage = "Please fill in all required fields (Title and Description)."
                }
            },
            enabled = !isLoading,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            colors = ButtonDefaults.buttonColors(containerColor = AppRed)
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = Color.White
                )
            } else {
                Text("Submit Report", color = Color.White, fontSize = 18.sp)
            }
        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            confirmButton = {
                TextButton(onClick = {
                    showDialog = false
                    navController.navigate("myreports")
                }) { Text("View Reports") }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) { Text("Close") }
            },
            title = { Text("Report submitted") },
            text = { Text("Your incident was shared with the command center for verification.") }
        )
    }
}

@Composable
private fun SeverityChip(label: String, selected: Boolean, onSelect: () -> Unit) {
    val container = if (selected) AppRed else Color(0xFFE8E8ED)
    val contentColor = if (selected) Color.White else Color.Black
    Button(
        onClick = onSelect,
        colors = ButtonDefaults.buttonColors(
            containerColor = container,
            contentColor = contentColor
        ),
        modifier = Modifier.height(36.dp),
        contentPadding = ButtonDefaults.ContentPadding
    ) {
        Text(label, fontSize = 13.sp)
    }
}
