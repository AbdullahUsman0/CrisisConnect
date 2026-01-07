package com.example.crisisconnect.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.crisisconnect.ui.theme.PurpleStart
import com.example.crisisconnect.ui.theme.PurpleEnd

@Composable
fun AboutScreen() {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(PurpleStart, PurpleEnd)))
            .padding(20.dp)
    ) {

        Text("About", color = Color.White, fontSize = 26.sp)

        Spacer(Modifier.height(20.dp))

        Card(
            shape = androidx.compose.foundation.shape.RoundedCornerShape(18.dp),
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.9f))
        ) {
            Text(
                "CrisisConnect helps people report emergencies, get updates and reach help quickly. Built for safety and rapid response.",
                modifier = Modifier.padding(22.dp),
                color = Color.Black,
                fontSize = 17.sp
            )
        }
    }
}
