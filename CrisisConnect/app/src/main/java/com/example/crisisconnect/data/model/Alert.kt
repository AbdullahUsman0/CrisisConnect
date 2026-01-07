package com.example.crisisconnect.data.model

enum class AlertSeverity { LOW, MODERATE, HIGH, CRITICAL }
enum class AlertType { EARTHQUAKE, FLOOD, WEATHER, FIRE, HEALTH, SECURITY }

data class Alert(
    val id: String,
    val title: String,
    val description: String,
    val type: AlertType,
    val severity: AlertSeverity,
    val location: String,
    val issuedBy: String,
    val timestamp: String,
    val verified: Boolean = false,
    val acknowledged: Boolean = false
)

