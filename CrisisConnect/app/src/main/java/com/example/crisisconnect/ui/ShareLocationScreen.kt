package com.example.crisisconnect.ui.screens

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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

@Composable
fun ShareLocationScreen() {
    val context = LocalContext.current
    var message by remember { mutableStateOf("I'm safe. Track me via CrisisConnect live location.") }
    var coordinates by remember { mutableStateOf("33.6844° N, 73.0479° E") }
    var recipients by remember { mutableStateOf("Family Group, Command Center") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(18.dp)
    ) {
        Text("Share Location", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text("Send live coordinates with a custom message to responders or family.", color = Color.Gray)

        Spacer(Modifier.height(16.dp))

        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(Color(0xFFF7F7FB))
        ) {
            Column(Modifier.padding(16.dp)) {
                Text("Last GPS lock", fontWeight = FontWeight.SemiBold)
                Text(coordinates, fontSize = 20.sp, color = Color(0xFF1B5E20))
            }
        }

        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = recipients,
            onValueChange = { recipients = it },
            label = { Text("Recipients") },
            modifier = Modifier.fillMaxWidth(),
            colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.Black,
                unfocusedTextColor = Color.Black
            )
        )

        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = message,
            onValueChange = { message = it },
            label = { Text("Message") },
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp),
            colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.Black,
                unfocusedTextColor = Color.Black
            )
        )

        Spacer(Modifier.height(20.dp))

        Button(
            onClick = {
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, "Location: $coordinates\n$message")
                }
                context.startActivity(Intent.createChooser(intent, "Share via"))
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3949AB))
        ) {
            Text("Share with ${recipients}", color = Color.White)
        }
    }
}

