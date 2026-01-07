package com.example.crisisconnect.ui.screens

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.crisisconnect.ui.theme.PurpleEnd
import com.example.crisisconnect.ui.theme.PurpleStart

@Composable
fun AIChatScreen(navController: NavController) {
    var userInput by remember { mutableStateOf("") }
    var messages by remember { mutableStateOf(listOf<Pair<Boolean, String>>()) }
    var voiceEnabled by remember { mutableStateOf(false) }
    var isListening by remember { mutableStateOf(false) }

    val typingAnim = rememberInfiniteTransition()
    val dotAlpha by typingAnim.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = androidx.compose.animation.core.tween(700, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF7F7FB))
            .padding(16.dp),
    ) {
        Text(
            "AI Assistant",
            fontSize = 28.sp,
            color = PurpleStart,
            style = MaterialTheme.typography.headlineSmall
        )

        Spacer(Modifier.height(8.dp))

        VoiceControlCard(
            voiceEnabled = voiceEnabled,
            isListening = isListening,
            onToggle = { voiceEnabled = it },
            onListenToggle = {
                if (voiceEnabled) {
                    isListening = !isListening
                    messages = messages + (false to if (isListening) "Listening for voice command..." else "Voice capture paused.")
                }
            }
        )

        Spacer(Modifier.height(12.dp))

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            messages.forEach { (isUser, msg) ->
                val gradient = if (isUser) Brush.horizontalGradient(listOf(PurpleStart, PurpleEnd)) else null
                val textColor = if (isUser) Color.White else Color.Black

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(6.dp),
                    contentAlignment = if (isUser) Alignment.CenterEnd else Alignment.CenterStart
                ) {
                    Card(
                        modifier = Modifier.fillMaxWidth(0.85f),
                        colors = if (isUser) CardDefaults.cardColors(Color.Transparent) else CardDefaults.cardColors(Color.White),
                        shape = RoundedCornerShape(18.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .background(
                                    brush = gradient ?: Brush.linearGradient(listOf(Color.White, Color.White)),
                                    shape = RoundedCornerShape(18.dp)
                                )
                                .padding(14.dp)
                        ) {
                            Text(msg, color = textColor, fontSize = 16.sp)
                        }
                    }
                }
            }

            if (messages.lastOrNull()?.first == false && messages.isNotEmpty()) {
                Text(
                    "...",
                    color = Color.Gray.copy(alpha = dotAlpha),
                    modifier = Modifier.padding(8.dp)
                )
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .padding(top = 8.dp)
                .shadow(8.dp, RoundedCornerShape(20.dp))
                .background(Color.White, RoundedCornerShape(20.dp))
                .padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            BasicTextField(
                value = userInput,
                onValueChange = { userInput = it },
                modifier = Modifier.weight(1f),
                decorationBox = { inner ->
                    if (userInput.isEmpty()) {
                        Text("Ask something…", color = Color.Gray)
                    }
                    inner()
                }
            )

            IconButton(
                onClick = {
                    if (!voiceEnabled) {
                        voiceEnabled = true
                    }
                    isListening = !isListening
                    messages = messages + (false to if (isListening) "Voice activation enabled." else "Voice capture stopped.")
                }
            ) {
                Icon(Icons.Default.Mic, contentDescription = "Voice", tint = if (isListening) PurpleEnd else Color.Gray)
            }

            IconButton(
                onClick = {
                    if (userInput.isNotEmpty()) {
                        messages = messages + (true to userInput)
                        messages = messages + (false to "Processing crisis response...")
                        userInput = ""
                    }
                }
            ) {
                Icon(Icons.Default.Send, "send", tint = PurpleStart)
            }
        }
    }
}

@Composable
private fun VoiceControlCard(
    voiceEnabled: Boolean,
    isListening: Boolean,
    onToggle: (Boolean) -> Unit,
    onListenToggle: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Voice activation & recognition", fontWeight = FontWeight.SemiBold)
                    Text(
                        if (voiceEnabled) "Say \"Send help\" or \"Share location\" to trigger automations."
                        else "Enable voice trigger for responders working hands-free.",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
                Switch(checked = voiceEnabled, onCheckedChange = onToggle)
            }
            if (voiceEnabled) {
                Spacer(Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        Icons.Default.Mic,
                        contentDescription = null,
                        tint = if (isListening) PurpleStart else Color.Gray,
                        modifier = Modifier.size(20.dp)
                    )
                    TextButton(onClick = onListenToggle) {
                        Text(if (isListening) "Stop listening" else "Start listening")
                    }
                }
            }
        }
    }
}

