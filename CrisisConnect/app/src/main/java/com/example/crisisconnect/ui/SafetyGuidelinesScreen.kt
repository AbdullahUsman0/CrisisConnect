package com.example.crisisconnect.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.crisisconnect.data.SampleDataProvider
import com.example.crisisconnect.data.model.SafetyTip

@Composable
fun SafetyGuidelinesScreen() {
    val tips = SampleDataProvider.safetyTips

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF9FAFF))
            .padding(16.dp)
    ) {
        Text("Safety Instructions", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text("Share with community members or print for shelters.", color = Color.Gray)

        Spacer(Modifier.height(12.dp))

        LazyColumn {
            items(tips) { tip ->
                SafetyCard(tip)
                Spacer(Modifier.height(12.dp))
            }
        }
    }
}

@Composable
private fun SafetyCard(tip: SafetyTip) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(Color.White),
        elevation = CardDefaults.cardElevation(2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(tip.category, fontWeight = FontWeight.SemiBold, fontSize = 18.sp, color = Color(0xFF3949AB))
            Text(tip.summary, color = Color.Gray, fontSize = 14.sp)
            Spacer(Modifier.height(8.dp))
            tip.steps.forEachIndexed { index, step ->
                Text("${index + 1}. $step", color = Color.Black, fontSize = 14.sp)
                Spacer(Modifier.height(4.dp))
            }
        }
    }
}

