package com.example.crisisconnect.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import com.example.crisisconnect.data.ProfileRepository
import com.example.crisisconnect.data.SessionManager
import com.example.crisisconnect.data.model.UserProfile
import com.example.crisisconnect.data.model.UserRole
import kotlinx.coroutines.launch

@Composable
fun ManageUsersScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    SessionManager.initialize(context)
    
    var users by remember { mutableStateOf<List<UserProfile>>(emptyList()) }
    var isAdmin by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        isLoading = true
        try {
            val userId = SessionManager.getUserId()
            if (userId != null) {
                isAdmin = AdminRepository.isAdmin(userId)
                if (isAdmin) {
                    // Get all users from profiles table
                    users = com.example.crisisconnect.data.network.SupabaseHttpClient.from<UserProfile>(
                        table = "profiles",
                        select = "*"
                    )
                }
            }
        } catch (e: Exception) {
            // Handle error
        } finally {
            isLoading = false
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
            Text("You need admin privileges to manage users.", color = Color.Black)
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Manage Users", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text("Activate, suspend or change roles instantly.", color = Color.Black)

        Spacer(Modifier.height(16.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(users, key = { it.id }) { user ->
                UserCard(user)
            }
        }
    }
}

@Composable
private fun UserCard(user: UserProfile) {
    val scope = rememberCoroutineScope()
    var fullName by remember { mutableStateOf(user.full_name ?: "") }
    var phone by remember { mutableStateOf(user.phone ?: "") }
    
    // Convert role string to UserRole enum, default to CITIZEN if invalid
    val initialRole = try {
        UserRole.valueOf(user.role?.uppercase() ?: "CITIZEN")
    } catch (e: Exception) {
        UserRole.CITIZEN
    }
    var selectedRole by remember { mutableStateOf(initialRole) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }

    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(user.full_name ?: "Unknown", fontWeight = FontWeight.SemiBold, fontSize = 18.sp)
            Text("${user.organization ?: ""} • ${user.phone ?: ""}", color = Color.Black, fontSize = 13.sp)
            
            Spacer(Modifier.height(8.dp))
            
            // Editable Full Name
            androidx.compose.material3.OutlinedTextField(
                value = fullName,
                onValueChange = { fullName = it },
                label = { Text("Full Name") },
                modifier = Modifier.fillMaxWidth(),
                colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.Black
                )
            )
            
            // Editable Phone
            androidx.compose.material3.OutlinedTextField(
                value = phone,
                onValueChange = { phone = it },
                label = { Text("Phone") },
                modifier = Modifier.fillMaxWidth(),
                colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.Black
                )
            )
            
            // Role Selector
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Role", fontWeight = FontWeight.SemiBold)
                RoleSelector(current = selectedRole, onChange = { selectedRole = it })
            }

            // Show error message if any
            errorMessage?.let {
                Text(
                    it,
                    color = Color(0xFFD32F2F),
                    fontSize = 12.sp,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }
            
            // Show success message if any
            successMessage?.let {
                Text(
                    it,
                    color = Color(0xFF2E7D32),
                    fontSize = 12.sp,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }

            Button(
                onClick = {
                    scope.launch {
                        isLoading = true
                        errorMessage = null
                        successMessage = null
                        try {
                            // Admin can update any user (including other admins)
                            AdminRepository.adminUpdateUser(
                                userId = user.id,
                                fullName = fullName.takeIf { it.isNotBlank() },
                                phone = phone.takeIf { it.isNotBlank() },
                                role = selectedRole.name // Convert enum to string
                            )
                            successMessage = "User updated successfully!"
                            // Refresh the user data
                            fullName = user.full_name ?: ""
                            phone = user.phone ?: ""
                            // Clear success message after 3 seconds
                            kotlinx.coroutines.delay(3000)
                            successMessage = null
                        } catch (e: Exception) {
                            // Show detailed error message
                            val errorMsg = e.message ?: e.localizedMessage ?: "Failed to update user"
                            errorMessage = "Error: $errorMsg"
                        } finally {
                            isLoading = false
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3949AB))
            ) {
                Text(if (isLoading) "Saving..." else "Save")
            }
        }
    }
}

@Composable
private fun RoleSelector(current: UserRole, onChange: (UserRole) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        listOf(UserRole.CITIZEN, UserRole.NGO_WORKER, UserRole.AUTHORITY, UserRole.ADMIN, UserRole.RESPONDER)
            .forEach { role ->
                Button(
                    onClick = { onChange(role) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (role == current) Color(0xFF3949AB) else Color(0xFFEDE7F6),
                        contentColor = if (role == current) Color.White else Color.Black
                    ),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(role.name.substring(0, 3), fontSize = 12.sp)
                }
            }
    }
}

