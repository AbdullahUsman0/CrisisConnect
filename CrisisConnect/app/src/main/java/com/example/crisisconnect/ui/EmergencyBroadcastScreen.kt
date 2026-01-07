package com.example.crisisconnect.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Alignment
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.crisisconnect.data.AdminRepository
import com.example.crisisconnect.data.SessionManager
import kotlinx.coroutines.launch

@Composable
fun EmergencyBroadcastScreen() {
    val context = LocalContext.current
    SessionManager.initialize(context)
    
    var title by remember { mutableStateOf("Emergency Alert") }
    var body by remember { mutableStateOf("Flood waters rising near Riverside. Evacuate to Central Shelter.") }
    var severity by remember { mutableStateOf("CRITICAL") }
    var isAdmin by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    
    // Note: audience and channel are not used in the API call, but kept for UI if needed
    // var audience by remember { mutableStateOf("Residents within 5km") }
    // var channel by remember { mutableStateOf("Push + SMS") }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    
    LaunchedEffect(Unit) {
        val userId = SessionManager.getUserId()
        if (userId != null) {
            try {
                isAdmin = AdminRepository.isAdmin(userId)
            } catch (e: Exception) {
                // Handle error
            }
        }
    }
    
    if (!isAdmin) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("Admin access required", style = MaterialTheme.typography.headlineSmall)
            Text("You need admin privileges to send emergency broadcasts.", color = Color.Black)
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(16.dp)
    ) {
        Text("Send Emergency Notification", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text("Dispatch targeted alerts to communities and partner agencies.", color = Color.Black)

        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = title, 
            onValueChange = { title = it }, 
            label = { Text("Title") }, 
            modifier = Modifier.fillMaxWidth(),
            colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.Black,
                unfocusedTextColor = Color.Black
            )
        )
        Spacer(Modifier.height(10.dp))
        OutlinedTextField(
            value = body,
            onValueChange = { body = it },
            label = { Text("Message") },
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp),
            colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.Black,
                unfocusedTextColor = Color.Black
            )
        )
        Spacer(Modifier.height(10.dp))
        OutlinedTextField(
            value = severity,
            onValueChange = { severity = it },
            label = { Text("Severity (LOW, MODERATE, HIGH, CRITICAL)") },
            modifier = Modifier.fillMaxWidth(),
            colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.Black,
                unfocusedTextColor = Color.Black
            )
        )

        Spacer(Modifier.height(20.dp))

        Button(
            onClick = {
                scope.launch {
                    isLoading = true
                    try {
                        AdminRepository.sendEmergencyNotifications(
                            disasterEventId = null, // Can be linked to a disaster event if available
                            title = title,
                            message = body,
                            radiusMeters = 5000 // Default radius
                        )
                        snackbarHostState.showSnackbar("Broadcast sent successfully!")
                        title = ""
                        body = ""
                    } catch (e: Exception) {
                        snackbarHostState.showSnackbar("Failed to send broadcast: ${e.localizedMessage}")
                    } finally {
                        isLoading = false
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD84315))
        ) {
            Text(if (isLoading) "Sending..." else "Send Notification", color = Color.White, fontSize = 18.sp)
        }

        Spacer(Modifier.height(12.dp))

        SnackbarHost(hostState = snackbarHostState)
    }
}

