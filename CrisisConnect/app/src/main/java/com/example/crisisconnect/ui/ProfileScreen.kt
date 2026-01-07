package com.example.crisisconnect.ui.screens

import androidx.compose.foundation.Image
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.crisisconnect.R
import com.example.crisisconnect.data.AdminRepository
import com.example.crisisconnect.data.ProfileRepository
import com.example.crisisconnect.data.SessionManager
import com.example.crisisconnect.data.model.UserRole
import com.example.crisisconnect.ui.theme.PurpleStart
import kotlinx.coroutines.launch

@Composable
fun ProfileScreen(navController: NavController) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    SessionManager.initialize(context)
    
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var organization by remember { mutableStateOf("") }
    var currentRole by remember { mutableStateOf<UserRole?>(null) }
    var showDialog by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var statusMessage by remember { mutableStateOf<String?>(null) }
    var isCurrentUserAdmin by remember { mutableStateOf(false) }
    var isProfileAdmin by remember { mutableStateOf(false) }
    
    // Load profile on init
    LaunchedEffect(Unit) {
        val userId = SessionManager.getUserId()
        if (userId != null) {
            try {
                // Check if current user is admin
                isCurrentUserAdmin = AdminRepository.isAdmin(userId)
                
                val profile = ProfileRepository.getProfile(userId)
                profile?.let {
                    name = it.full_name ?: ""
                    phone = it.phone ?: ""
                    organization = it.organization ?: ""
                    isProfileAdmin = it.is_admin == true
                    // Convert role string to UserRole enum
                    currentRole = try {
                        UserRole.valueOf(it.role?.uppercase() ?: "CITIZEN")
                    } catch (e: Exception) {
                        UserRole.CITIZEN
                    }
                }
            } catch (e: Exception) {
                statusMessage = "Failed to load profile: ${e.localizedMessage}"
            }
        } else {
            statusMessage = "Please log in to view your profile."
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Image(
            painter = painterResource(id = R.drawable.ic_profile),
            contentDescription = "profile",
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
        )

        Spacer(Modifier.height(16.dp))

        currentRole?.let { role ->
            RoleToggle(
                currentRole = role,
                onRoleChange = { newRole ->
                    currentRole = newRole
                }
            )
            Spacer(Modifier.height(16.dp))
        }

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Full Name") },
            modifier = Modifier.fillMaxWidth(),
            colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.Black,
                unfocusedTextColor = Color.Black
            )
        )

        Spacer(Modifier.height(10.dp))

        OutlinedTextField(
            value = phone,
            onValueChange = { phone = it },
            label = { Text("Phone") },
            modifier = Modifier.fillMaxWidth(),
            colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.Black,
                unfocusedTextColor = Color.Black
            )
        )

        Spacer(Modifier.height(10.dp))

        OutlinedTextField(
            value = organization,
            onValueChange = { organization = it },
            label = { Text("Organization") },
            modifier = Modifier.fillMaxWidth(),
            colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.Black,
                unfocusedTextColor = Color.Black
            )
        )

        Spacer(Modifier.height(24.dp))

        statusMessage?.let {
            Text(
                it,
                color = if (it.contains("success", true)) Color(0xFF1B5E20) else Color(0xFFD32F2F),
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }

        Button(
            onClick = {
                scope.launch {
                    isLoading = true
                    statusMessage = null
                    try {
                        val userId = SessionManager.getUserId()
                        if (userId == null) {
                            statusMessage = "Please log in to update your profile."
                            isLoading = false
                            return@launch
                        }
                        
                        // Check permission: Non-admins cannot update admin profiles
                        if (isProfileAdmin && !isCurrentUserAdmin) {
                            statusMessage = "You do not have permission to update admin profiles."
                            isLoading = false
                            return@launch
                        }
                        
                        // TODO: Get location coordinates from location picker
                        ProfileRepository.updateProfile(
                            userId = userId,
                            fullName = name.takeIf { it.isNotBlank() },
                            phone = phone.takeIf { it.isNotBlank() },
                            locationLat = null, // TODO: Get from location picker
                            locationLon = null, // TODO: Get from location picker
                            organization = organization.takeIf { it.isNotBlank() }
                            // Note: Role updates are handled separately via AdminRepository if needed
                        )
                        statusMessage = "Profile updated successfully!"
                        showDialog = true
                    } catch (e: Exception) {
                        statusMessage = e.localizedMessage ?: "Failed to update profile."
                    } finally {
                        isLoading = false
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading,
            colors = ButtonDefaults.buttonColors(containerColor = PurpleStart)
        ) {
            Text(if (isLoading) "Saving..." else "Save Changes", color = Color.White, fontSize = 16.sp)
        }

        Spacer(Modifier.height(12.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(Color(0xFFF7F7FB))
        ) {
            Column(Modifier.padding(16.dp)) {
                Text("Security & Account", fontWeight = FontWeight.SemiBold, color = PurpleStart)
                Spacer(Modifier.height(8.dp))
                Text("• Enable MFA for authority access\n• Keep verified contact details updated\n• Assign backup responder for your role")
            }
        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            confirmButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("Great")
                }
            },
            title = { Text("Profile updated") },
            text = { Text("Your contact details and role have been refreshed for upcoming alerts.") }
        )
    }
}

@Composable
private fun RoleToggle(currentRole: UserRole, onRoleChange: (UserRole) -> Unit) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(Color(0xFFF7F7FB))
    ) {
        Column(Modifier.padding(16.dp)) {
            Text("Role", color = PurpleStart, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                listOf(UserRole.CITIZEN, UserRole.NGO_WORKER, UserRole.AUTHORITY, UserRole.ADMIN, UserRole.RESPONDER)
                    .forEach { role ->
                        RoleChip(
                            role = role,
                            selected = currentRole == role,
                            onSelect = { onRoleChange(role) }
                        )
                    }
            }
        }
    }
}

@Composable
private fun RoleChip(role: UserRole, selected: Boolean, onSelect: () -> Unit) {
    Button(
        onClick = onSelect,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (selected) PurpleStart else Color.White,
            contentColor = if (selected) Color.White else Color.Black
        ),
        modifier = Modifier.height(36.dp),
        contentPadding = ButtonDefaults.ContentPadding
    ) {
        Text(role.name.substring(0, 3), fontSize = 12.sp)
    }
}
