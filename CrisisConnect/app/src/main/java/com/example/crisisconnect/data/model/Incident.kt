package com.example.crisisconnect.data.model

data class Incident(
    val id: String,
    val title: String,
    val type: String,
    val location: String,
    val reporter: String,
    val status: String,
    val description: String,
    val severity: AlertSeverity,
    val lastUpdated: String
)

