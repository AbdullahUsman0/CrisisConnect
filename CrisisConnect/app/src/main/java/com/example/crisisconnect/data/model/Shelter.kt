package com.example.crisisconnect.data.model

data class Shelter(
    val id: String,
    val name: String,
    val address: String,
    val capacity: Int,
    val occupancy: Int,
    val isOpen: Boolean,
    val distanceKm: Double,
    val contact: String
)

