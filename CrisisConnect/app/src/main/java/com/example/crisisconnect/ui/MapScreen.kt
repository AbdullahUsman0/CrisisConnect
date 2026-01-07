package com.example.crisisconnect.ui.screens

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CheckCircleOutline
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Route
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.clickable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.crisisconnect.data.IncidentRepository
import com.example.crisisconnect.data.LocationService
import com.example.crisisconnect.data.SampleDataProvider
import com.example.crisisconnect.data.SessionManager
import com.example.crisisconnect.data.IncidentReport
import com.example.crisisconnect.ui.theme.PurpleEnd
import com.example.crisisconnect.ui.theme.PurpleStart
import com.example.crisisconnect.utils.MapUtils
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun MapScreen() {
    val context = LocalContext.current
    SessionManager.initialize(context)
    
    var incidents by remember { mutableStateOf<List<com.example.crisisconnect.data.IncidentReport>>(emptyList()) }
    val shelters = SampleDataProvider.shelters

    var hasLocationPermission by remember { mutableStateOf(LocationService.hasLocationPermission(context)) }
    var userLocation by remember { mutableStateOf<LatLng?>(null) }
    var isLoadingIncidents by remember { mutableStateOf(false) }
    var selectedIncidentIds by remember { mutableStateOf<Set<String>>(emptySet()) } // Track selected incidents
    var showOnlySelected by remember { mutableStateOf(false) } // Toggle to show only selected
    
    // Load verified incidents from Supabase
    LaunchedEffect(Unit) {
        isLoadingIncidents = true
        try {
            incidents = IncidentRepository.getVerifiedIncidents()
        } catch (e: Exception) {
            // Handle error - keep empty list
            // Error is handled silently to prevent crashes
        } finally {
            isLoadingIncidents = false
        }
    }
    
    // Refresh incidents periodically (every 30 seconds)
    LaunchedEffect(Unit) {
        while (true) {
            kotlinx.coroutines.delay(30000) // 30 seconds
            try {
                val refreshedIncidents = IncidentRepository.getVerifiedIncidents()
                incidents = refreshedIncidents
            } catch (e: Exception) {
                // Silent fail - keep existing incidents
            }
        }
    }

    // Default location (Lahore, Pakistan) if user location not available
    val defaultLocation = LatLng(31.5204, 74.3587)
    val initialLocation = userLocation ?: defaultLocation

    // Request location permission
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        hasLocationPermission = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true

        if (hasLocationPermission) {
            // Get user location after permission granted
            CoroutineScope(Dispatchers.IO).launch {
                if (LocationService.hasLocationPermission(context)) {
                    try {
                        val location = LocationService.getCurrentLocation(context)
                        location?.let {
                            userLocation = LatLng(it.latitude, it.longitude)
                        }
                    } catch (e: SecurityException) {
                        // Permission was revoked, handle gracefully
                    }
                }
            }
        }
    }

    // Request permission on first load
    LaunchedEffect(Unit) {
        if (!hasLocationPermission) {
            locationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        } else {
            // Get location if permission already granted
            CoroutineScope(Dispatchers.IO).launch {
                if (LocationService.hasLocationPermission(context)) {
                    try {
                        val location = LocationService.getCurrentLocation(context)
                        location?.let {
                            userLocation = LatLng(it.latitude, it.longitude)
                        }
                    } catch (e: SecurityException) {
                        // Permission was revoked, handle gracefully
                    }
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(PurpleStart, PurpleEnd)))
            .padding(20.dp)
    ) {
        Text("Live Disaster Tracking", fontSize = 26.sp, color = Color.White, fontWeight = FontWeight.Bold)
        Text(
            "Overlay verified alerts, responder routes and nearby shelters in one map.",
            color = Color.White.copy(alpha = 0.85f),
            fontSize = 14.sp
        )

        Spacer(Modifier.height(16.dp))

        CommandCard()

        Spacer(Modifier.height(18.dp))

        // Google Maps
        Card(
            shape = RoundedCornerShape(18.dp),
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .shadow(10.dp, RoundedCornerShape(18.dp)),
            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.96f))
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                // Filter incidents for map display based on selection
                val incidentsForMap = if (showOnlySelected && selectedIncidentIds.isNotEmpty()) {
                    incidents.filter { it.id in selectedIncidentIds }
                } else {
                    incidents
                }
                
                GoogleMapView(
                    initialLocation = initialLocation,
                    incidents = incidentsForMap,
                    shelters = shelters,
                    userLocation = userLocation,
                    selectedIncidentIds = selectedIncidentIds,
                    showOnlySelected = showOnlySelected
                )
            }
        }

        Spacer(Modifier.height(14.dp))

        val incidentsWithLocation = incidents.filter { it.lat != null && it.lon != null }
        val totalIncidents = incidents.size
        
        // Filter incidents based on selection
        val displayedIncidents = if (showOnlySelected && selectedIncidentIds.isNotEmpty()) {
            incidents.filter { it.id in selectedIncidentIds }
        } else {
            incidents
        }
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Active Incidents (${displayedIncidents.size}/${totalIncidents})", 
                color = Color.White, 
                fontWeight = FontWeight.SemiBold, 
                fontSize = 16.sp
            )
            // Toggle button to show only selected
            if (selectedIncidentIds.isNotEmpty()) {
                androidx.compose.material3.IconButton(
                    onClick = { showOnlySelected = !showOnlySelected }
                ) {
                    Icon(
                        if (showOnlySelected) Icons.Default.CheckCircle else Icons.Default.CheckCircleOutline,
                        contentDescription = if (showOnlySelected) "Show all" else "Show selected only",
                        tint = Color.White
                    )
                }
            }
        }
        
        if (displayedIncidents.isEmpty()) {
            Text(
                if (showOnlySelected) "No selected incidents to display" else "No active incidents found",
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 12.sp,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        } else {
            // Show all incidents in the list (even without location), but only show location on map if available
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(displayedIncidents.take(5), key = { it.id }) { incident ->
                    IncidentReportRow(
                        incident = incident,
                        isSelected = incident.id in selectedIncidentIds,
                        onToggleSelect = { incidentId ->
                            selectedIncidentIds = if (incidentId in selectedIncidentIds) {
                                selectedIncidentIds - incidentId
                            } else {
                                selectedIncidentIds + incidentId
                            }
                        }
                    )
                }
            }
            // Show info if some incidents are missing location
            val displayedWithoutLocation = displayedIncidents.count { it.lat == null || it.lon == null }
            if (displayedWithoutLocation > 0) {
                Text(
                    "$displayedWithoutLocation incident(s) missing location (created before GPS tracking or location not captured)",
                    color = Color.White.copy(alpha = 0.6f),
                    fontSize = 10.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        Text("Nearby Safe Zones", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(shelters, key = { it.id }) { shelter ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.92f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(shelter.name, fontWeight = FontWeight.SemiBold, color = PurpleStart)
                            Text("${shelter.address} • ${shelter.distanceKm} km", fontSize = 12.sp)
                        }
                        Text("${shelter.capacity - shelter.occupancy} spots",
                            color = if (shelter.isOpen) Color(0xFF4CAF50) else Color(0xFFD32F2F))
                    }
                }
            }
        }
    }
}

@Composable
fun GoogleMapView(
    initialLocation: LatLng,
    incidents: List<com.example.crisisconnect.data.IncidentReport>,
    shelters: List<com.example.crisisconnect.data.model.Shelter>,
    userLocation: LatLng?,
    selectedIncidentIds: Set<String> = emptySet(),
    showOnlySelected: Boolean = false
) {
    var mapLoaded by remember { mutableStateOf(false) }
    var mapError by remember { mutableStateOf<String?>(null) }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(initialLocation, 12f)
    }

    // Remove timeout - let map load naturally
    // Map will show loading state until onMapLoaded is called

    // Update camera when user location changes
    LaunchedEffect(userLocation) {
        userLocation?.let {
            cameraPositionState.animate(
                CameraUpdateFactory.newLatLngZoom(it, 14f)
            )
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Always render the map (it needs to be rendered to initialize)
        GoogleMap(
                    modifier = Modifier.fillMaxSize(),
                    cameraPositionState = cameraPositionState,
                    uiSettings = com.google.maps.android.compose.MapUiSettings(
                        zoomControlsEnabled = true,
                        myLocationButtonEnabled = true,
                        compassEnabled = true
                    ),
                    properties = com.google.maps.android.compose.MapProperties(
                        isMyLocationEnabled = userLocation != null
                    ),
                    onMapLoaded = {
                        mapLoaded = true
                        mapError = null
                    },
                    onMapClick = { },
                    onMyLocationButtonClick = { false },
                    onMyLocationClick = { false }
                ) {
                    // User location marker
                    userLocation?.let {
                        Marker(
                            state = MarkerState(position = it),
                            title = "Your Location"
                        )
                    }

                    // Incident markers - only show verified incidents with coordinates
                    // Filter based on selection if enabled
                    val incidentsToShow = if (showOnlySelected && selectedIncidentIds.isNotEmpty()) {
                        incidents.filter { it.id in selectedIncidentIds && it.lat != null && it.lon != null }
                    } else {
                        incidents.filter { it.lat != null && it.lon != null }
                    }
                    
                    incidentsToShow.forEach { incident ->
                        val incidentLocation = LatLng(incident.lat!!, incident.lon!!)
                        val isSelected = incident.id in selectedIncidentIds
                        Marker(
                            state = MarkerState(position = incidentLocation),
                            title = incident.description.take(50),
                            snippet = "Status: ${incident.status}${if (isSelected) " [Tracked]" else ""}"
                        )
                    }

                    // Shelter markers
                    shelters.forEach { shelter ->
                        // Using sample coordinates - replace with actual shelter coordinates from Supabase
                        val shelterLocation = LatLng(
                            31.5204 + (Math.random() - 0.5) * 0.1,
                            74.3587 + (Math.random() - 0.5) * 0.1
                        )
                        Marker(
                            state = MarkerState(position = shelterLocation),
                            title = shelter.name,
                            snippet = "${shelter.address} • ${shelter.capacity - shelter.occupancy} spots available"
                        )
                    }
                }
        
        // Overlay loading state if map not loaded yet
        if (!mapLoaded && mapError == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    androidx.compose.material3.CircularProgressIndicator(color = PurpleStart)
                    Spacer(Modifier.height(16.dp))
                    Text("Loading map...", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
        
        // Overlay error state if map failed to load (only show if there's an actual error, not just loading)
        if (mapError != null && !mapError!!.contains("taking too long")) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.7f)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(16.dp)) {
                    Icon(Icons.Default.Map, contentDescription = null, tint = Color.White, modifier = Modifier.size(48.dp))
                    Spacer(Modifier.height(16.dp))
                    Text("Map failed to load", color = Color.White, fontWeight = FontWeight.Bold)
                    Text(mapError ?: "Please check your internet connection", color = Color.White, fontSize = 12.sp)
                    Spacer(Modifier.height(8.dp))
                    Text("Check Google Maps API key in local.properties", color = Color.White.copy(alpha = 0.8f), fontSize = 10.sp)
                }
            }
        }
    }
}

@Composable
fun CommandCard() {
    Card(
        shape = RoundedCornerShape(18.dp),
        modifier = Modifier
            .fillMaxWidth()
            .shadow(12.dp, RoundedCornerShape(18.dp)),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.92f))
    ) {
        Row(
            modifier = Modifier.padding(18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Command Center Feed", color = PurpleStart, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                Text(
                    "Location locked near Civic Center. Responders 6 min away.",
                    color = Color.Black.copy(alpha = 0.7f),
                    fontSize = 14.sp
                )
            }
            Icon(Icons.Default.LocationOn, contentDescription = "loc", tint = PurpleStart)
        }
    }
}

@Composable
fun IncidentReportRow(
    incident: com.example.crisisconnect.data.IncidentReport,
    isSelected: Boolean = false,
    onToggleSelect: (String) -> Unit = {}
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.9f)),
        modifier = Modifier.clickable { onToggleSelect(incident.id) }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                // Selection checkbox
                Icon(
                    if (isSelected) Icons.Default.CheckCircle else Icons.Default.CheckCircleOutline,
                    contentDescription = if (isSelected) "Selected" else "Not selected",
                    tint = if (isSelected) Color(0xFF4CAF50) else Color.Gray,
                    modifier = Modifier
                        .size(20.dp)
                        .clickable { onToggleSelect(incident.id) }
                )
                Spacer(Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = incident.description.take(50) + if (incident.description.length > 50) "..." else "",
                        fontWeight = FontWeight.SemiBold,
                        color = PurpleStart
                    )
                    val context = LocalContext.current
                    if (incident.lat != null && incident.lon != null) {
                        Text(
                            text = "📍 ${String.format("%.4f", incident.lat)}, ${String.format("%.4f", incident.lon)} (Tap to view)",
                            fontSize = 12.sp,
                            color = Color(0xFF1976D2),
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
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Route, contentDescription = "route", tint = PurpleStart)
                Spacer(Modifier.width(6.dp))
                Text("Status: ${incident.status}", fontSize = 12.sp, color = Color(0xFFD84315))
            }
        }
    }
}
