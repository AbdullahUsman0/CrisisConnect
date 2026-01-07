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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.crisisconnect.data.SettingsManager
import com.example.crisisconnect.ui.theme.AppThemeState
import com.example.crisisconnect.ui.theme.PurpleEnd
import com.example.crisisconnect.ui.theme.PurpleStart
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    SettingsManager.initialize(context)
    
    var notifications by remember { mutableStateOf(true) }
    var darkMode by remember { mutableStateOf(AppThemeState.darkMode) }
    var voiceActivation by remember { mutableStateOf(true) }
    var autoShareLocation by remember { mutableStateOf(false) }
    var alertThreshold by remember { mutableStateOf("Critical only") }
    
    // Load saved settings
    LaunchedEffect(Unit) {
        notifications = SettingsManager.pushNotifications.first()
        voiceActivation = SettingsManager.voiceActivation.first()
        autoShareLocation = SettingsManager.autoShareLocation.first()
        alertThreshold = SettingsManager.alertThreshold.first()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(PurpleStart, PurpleEnd)))
            .padding(20.dp)
    ) {
        Text("Control Center Settings", fontSize = 26.sp, color = Color.White, fontWeight = FontWeight.Bold)
        Text(
            "Choose who gets alerts, automation triggers and collaboration tools.",
            color = Color.White.copy(alpha = 0.9f)
        )

        Spacer(Modifier.height(20.dp))

        SettingSwitch("Enable Push Notifications", notifications) { 
            notifications = it
            scope.launch {
                SettingsManager.setPushNotifications(it)
            }
        }
        Spacer(Modifier.height(12.dp))

        SettingSwitch("Dark Mode", darkMode) {
            darkMode = it
            AppThemeState.darkMode = it
        }
        Spacer(Modifier.height(12.dp))

        SettingSwitch("Voice Activation", voiceActivation) { 
            voiceActivation = it
            scope.launch {
                SettingsManager.setVoiceActivation(it)
            }
        }
        Spacer(Modifier.height(12.dp))

        SettingSwitch("Auto Share Location with Family", autoShareLocation) { 
            autoShareLocation = it
            scope.launch {
                SettingsManager.setAutoShareLocation(it)
            }
        }
        Spacer(Modifier.height(16.dp))

        AlertThresholdCard(alertThreshold) { 
            alertThreshold = it
            scope.launch {
                SettingsManager.setAlertThreshold(it)
            }
        }
    }
}

@Composable
private fun SettingSwitch(title: String, value: Boolean, onChange: (Boolean) -> Unit) {
    Card(
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(0.9f))
    ) {
        Row(
            modifier = Modifier.padding(18.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(title, fontSize = 18.sp, color = Color.Black)
            Switch(checked = value, onCheckedChange = onChange)
        }
    }
}

@Composable
private fun AlertThresholdCard(current: String, onChange: (String) -> Unit) {
    val options = listOf("Critical only", "High & Critical", "All alerts")
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(Color.White.copy(alpha = 0.95f))
    ) {
        Column(Modifier.padding(16.dp)) {
            Text("Alert threshold", fontWeight = FontWeight.SemiBold, color = PurpleStart)
            Spacer(Modifier.height(6.dp))
            Text("Choose which severities trigger push + siren notifications.", color = Color.Gray, fontSize = 12.sp)
            options.forEach { option ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = current == option,
                        onClick = { onChange(option) }
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(option, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}
