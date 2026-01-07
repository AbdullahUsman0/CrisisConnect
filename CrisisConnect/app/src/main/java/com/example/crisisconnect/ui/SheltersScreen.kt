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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.crisisconnect.data.SampleDataProvider
import com.example.crisisconnect.data.model.Shelter
import com.example.crisisconnect.ui.theme.PurpleStart

@Composable
fun SheltersScreen() {
    var onlyOpen by remember { mutableStateOf(true) }
    var distance by remember { mutableStateOf(5f) }

    val shelters = SampleDataProvider.shelters.filter {
        (!onlyOpen || it.isOpen) && it.distanceKm <= distance
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(16.dp)
    ) {
        Text("Safe Zones & Shelters", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text("Filter by availability, distance and capacity to guide evacuees quickly.", color = Color.Gray)

        Spacer(Modifier.height(12.dp))

        Card(colors = CardDefaults.cardColors(Color(0xFFF7F7FB))) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Show only open shelters")
                    Switch(checked = onlyOpen, onCheckedChange = { onlyOpen = it })
                }

                Text("Max distance ${"%.1f".format(distance)} km")
                Slider(
                    value = distance,
                    onValueChange = { distance = it },
                    valueRange = 1f..15f
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(shelters, key = { it.id }) { shelter ->
                ShelterCard(shelter)
            }
        }
    }
}

@Composable
private fun ShelterCard(shelter: Shelter) {
    Card(
        colors = CardDefaults.cardColors(Color.White),
        elevation = CardDefaults.cardElevation(3.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(shelter.name, fontWeight = FontWeight.SemiBold, color = PurpleStart)
            Text(shelter.address, color = Color.Gray)
            Spacer(Modifier.height(6.dp))
            Text("Capacity ${shelter.capacity} • Occupied ${shelter.occupancy}")
            Text("Distance ${shelter.distanceKm} km • Contact ${shelter.contact}", color = Color.DarkGray, fontSize = 12.sp)
            if (!shelter.isOpen) {
                Text("Currently closed for maintenance", color = Color(0xFFD32F2F), fontSize = 12.sp)
            }
        }
    }
}

