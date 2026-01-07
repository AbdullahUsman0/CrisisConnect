package com.example.crisisconnect.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.ShareLocation
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.crisisconnect.ui.theme.PurpleEnd
import com.example.crisisconnect.ui.theme.PurpleStart

@Composable
fun ContactsScreen(navController: NavController) {
    val contacts = listOf(
        Triple("Rescue Command", "1122", "Rescue & evacuation"),
        Triple("Police Control", "15", "Crowd control, security"),
        Triple("Ambulance Dispatch", "115", "Medical emergencies"),
        Triple("Fire Brigade", "16", "Urban fire response")
    )
    val context = LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(PurpleStart, PurpleEnd)))
            .padding(20.dp)
    ) {
        Column {
            Text("Emergency Contacts", fontSize = 26.sp, color = Color.White)
            Text("Tap to call or send live location with one tap.", color = Color.White.copy(alpha = 0.9f))

            Spacer(Modifier.height(20.dp))

            contacts.forEach { (title, number, duty) ->
                ContactCard(
                    title = title,
                    number = number,
                    duty = duty,
                    onCall = {
                        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$number"))
                        context.startActivity(intent)
                    },
                    onShare = {
                        navController.navigate("shareLocation")
                    }
                )
                Spacer(Modifier.height(14.dp))
            }
        }
    }
}

@Composable
private fun ContactCard(
    title: String,
    number: String,
    duty: String,
    onCall: () -> Unit,
    onShare: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(10.dp, RoundedCornerShape(18.dp)),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.95f))
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(title, fontSize = 20.sp, color = PurpleStart, style = MaterialTheme.typography.titleMedium)
            Text(duty, color = Color.Gray, fontSize = 13.sp)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Dial $number", color = Color.Black.copy(alpha = 0.8f))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Icon(
                        imageVector = Icons.Default.ShareLocation,
                        contentDescription = "share location",
                        tint = PurpleStart,
                        modifier = Modifier
                            .size(28.dp)
                            .clickable { onShare() }
                    )
                    Icon(
                        imageVector = Icons.Default.Call,
                        contentDescription = "call",
                        tint = PurpleStart,
                        modifier = Modifier
                            .size(32.dp)
                            .clickable { onCall() }
                    )
                }
            }
        }
    }
}
