package com.example.crisisconnect.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.crisisconnect.data.AuthRepository
import com.example.crisisconnect.ui.theme.PurpleStart
import com.example.crisisconnect.ui.theme.PurpleEnd
import kotlinx.coroutines.launch
import androidx.compose.foundation.layout.height

@Composable
fun ForgotPasswordScreen(navController: NavController) {

    var email by remember { mutableStateOf("") }
    var statusMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(PurpleStart, PurpleEnd)))
            .padding(26.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(16.dp, RoundedCornerShape(24.dp))
                .background(Color.White.copy(alpha = 0.85f), RoundedCornerShape(24.dp))
                .padding(30.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Text("Reset Password", fontSize = 24.sp, color = PurpleStart)

            Spacer(Modifier.height(18.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Enter your Email") },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading,
                colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.Black
                )
            )

            statusMessage?.let {
                Text(
                    it,
                    color = if (it.contains("sent", true)) Color(0xFF1B5E20) else Color(0xFFD32F2F),
                    modifier = Modifier.padding(top = 12.dp)
                )
            }

            Spacer(Modifier.height(20.dp))

            Button(
                onClick = {
                    if (email.isBlank()) {
                        statusMessage = "Please enter your email."
                        return@Button
                    }
                    scope.launch {
                        isLoading = true
                        statusMessage = null
                        try {
                            AuthRepository.resetPassword(email.trim())
                            statusMessage = "Reset link sent! Check your inbox."
                            navController.navigate("login")
                        } catch (e: Exception) {
                            statusMessage = e.localizedMessage ?: "Unable to send reset email."
                        } finally {
                            isLoading = false
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PurpleStart),
                enabled = !isLoading
            ) {
                Text(if (isLoading) "Sending..." else "Send Reset Link", color = Color.White)
            }
        }
    }
}
