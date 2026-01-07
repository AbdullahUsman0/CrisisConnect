package com.example.crisisconnect.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.crisisconnect.ui.theme.AppRed
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

@Composable
fun HelpSupportScreen(navController: NavController) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Help & Support", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = AppRed)
        Spacer(Modifier.height(12.dp))

        Text("If you need immediate help call local emergency services.", style = MaterialTheme.typography.bodyLarge)
        Spacer(Modifier.height(16.dp))

        Text("Support Channels:", fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(8.dp))
        Text("- In-app chat (AI Assistant)\n- Email: support@crisisconnect.app\n- Hotline: +123 456 789")
    }
}
