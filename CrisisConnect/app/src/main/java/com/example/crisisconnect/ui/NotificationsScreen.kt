package com.example.crisisconnect.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.crisisconnect.data.SampleDataProvider
import com.example.crisisconnect.data.model.NotificationMessage
import com.example.crisisconnect.ui.theme.PurpleEnd
import com.example.crisisconnect.ui.theme.PurpleStart

@Composable
fun NotificationsScreen(navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(PurpleStart, PurpleEnd)))
            .padding(18.dp)
    ) {
        Text("Emergency Notifications", color = Color.White, fontSize = 26.sp, fontWeight = FontWeight.Bold)
        Text("Sent alerts, SMS broadcasts and NGO updates", color = Color.White.copy(alpha = 0.8f))

        Spacer(Modifier.height(20.dp))

        Button(
            onClick = { navController.navigate("emergencyBroadcast") },
            colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = PurpleStart),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Compose Broadcast")
        }

        Spacer(Modifier.height(20.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(SampleDataProvider.notificationHistory, key = { it.id }) { notif ->
                NotificationItem(notif)
            }
        }
    }
}

@Composable
private fun NotificationItem(notification: NotificationMessage) {
    Card(
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.95f))
    ) {
        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(notification.title, fontWeight = FontWeight.SemiBold, color = PurpleStart)
                Text(notification.sentAt, color = Color.Gray, fontSize = 12.sp)
            }
            Text(notification.body, color = Color.Black)
            Text("Channel: ${notification.channel} • Audience: ${notification.audience}", color = Color.Gray, fontSize = 12.sp)
        }
    }
}
