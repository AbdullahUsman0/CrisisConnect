package com.example.crisisconnect.data.model

import kotlinx.serialization.Serializable

enum class UserRole { CITIZEN, NGO_WORKER, AUTHORITY, ADMIN, RESPONDER }

@Serializable
data class UserProfile(
    val id: String,
    val full_name: String? = null,
    val phone: String? = null,
    val location: String? = null, // PostGIS POINT as WKT
    val organization: String? = null,
    val role: String? = null, // Stored as string in Supabase, convert to UserRole enum
    val is_admin: Boolean? = null, // Admin flag
    val created_at: String? = null,
    val updated_at: String? = null
)

