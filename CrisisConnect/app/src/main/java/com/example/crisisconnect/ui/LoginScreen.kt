package com.example.crisisconnect.ui.screens

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import com.example.crisisconnect.R
import com.example.crisisconnect.data.AuthRepository
import com.example.crisisconnect.data.SessionManager
import com.example.crisisconnect.ui.theme.PurpleEnd
import com.example.crisisconnect.ui.theme.PurpleStart
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(navController: NavController) {
    val context = LocalContext.current
    
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var statusMessage by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    
    // Initialize SessionManager
    SessionManager.initialize(context)

    // Animated gradient background for a subtle live-wallpaper feel
    val transition = rememberInfiniteTransition(label = "loginGradient")
    val shift by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = androidx.compose.animation.core.tween(5000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "gradientShift"
    )
    val backgroundGradient = Brush.verticalGradient(
        colors = listOf(PurpleStart, Color(0xFF5C6BC0), PurpleEnd),
        startY = 0f,
        endY = 1200f * (1f + shift)
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundGradient)
            .padding(26.dp),
        contentAlignment = Alignment.Center
    ) {

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(18.dp, RoundedCornerShape(22.dp))
                .background(Color.White.copy(alpha = 0.88f), RoundedCornerShape(22.dp))
                .padding(30.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Image(
                painter = painterResource(id = R.drawable.ic_logo),
                contentDescription = null,
                modifier = Modifier.size(110.dp)
            )

            Spacer(Modifier.height(10.dp))

            Text(
                "Welcome Back",
                fontSize = 30.sp,
                fontWeight = FontWeight.SemiBold,
                color = PurpleStart
            )

            Spacer(Modifier.height(26.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                enabled = !isLoading,
                colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.Black
                )
            )

            Spacer(Modifier.height(16.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                visualTransformation = if (passVisible)
                    VisualTransformation.None else PasswordVisualTransformation(),
                colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.Black
                ),
                trailingIcon = {
                    IconButton(onClick = { passVisible = !passVisible }) {
                        Icon(
                            if (passVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = null
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                enabled = !isLoading
            )

            statusMessage?.let {
                Text(
                    it,
                    color = Color(0xFFD32F2F),
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            Spacer(Modifier.height(10.dp))

            TextButton(onClick = { navController.navigate("forgot") }) {
                Text("Forgot Password?", color = PurpleStart)
            }

            Spacer(Modifier.height(20.dp))

            Button(
                onClick = {
                    if (email.isBlank() || password.isBlank()) {
                        statusMessage = "Email and password are required."
                        return@Button
                    }
                    scope.launch {
                        isLoading = true
                        statusMessage = null
                        try {
                            // Ensure SessionManager is initialized
                            SessionManager.initialize(context)
                            
                            val authResponse = AuthRepository.signIn(email.trim(), password)
                            
                            // Save session
                            authResponse.access_token?.let { token ->
                                authResponse.user?.id?.let { userId ->
                                    try {
                                        SessionManager.saveSession(
                                            accessToken = token,
                                            userId = userId,
                                            refreshToken = authResponse.refresh_token
                                        )
                                        // Navigate only after successful session save
                                        try {
                                            navController.navigate("main") {
                                                popUpTo("login") { inclusive = true }
                                            }
                                        } catch (navException: Exception) {
                                            statusMessage = "Navigation failed: ${navException.localizedMessage}"
                                        }
                                    } catch (e: Exception) {
                                        statusMessage = "Failed to save session: ${e.localizedMessage}"
                                    }
                                } ?: run {
                                    statusMessage = "User ID not found in response"
                                }
                            } ?: run {
                                statusMessage = "Access token not found in response"
                            }
                        } catch (e: Exception) {
                            statusMessage = e.localizedMessage ?: "Unable to sign in. Please check your credentials."
                        } finally {
                            isLoading = false
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PurpleStart
                ),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        color = Color.White,
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(22.dp)
                    )
                } else {
                    Text("Login", color = Color.White, fontSize = 18.sp)
                }
            }

            Spacer(Modifier.height(16.dp))

            TextButton(onClick = { navController.navigate("register") }) {
                Text("Don’t have an account? Register", color = PurpleStart)
            }
        }
    }
}

