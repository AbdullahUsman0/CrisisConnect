package com.example.crisisconnect.data.model

data class NotificationMessage(
    val id: String,
    val title: String,
    val body: String,
    val channel: String,
    val sentAt: String,
    val audience: String
)

