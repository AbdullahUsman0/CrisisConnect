package com.example.crisisconnect.data

import com.example.crisisconnect.data.model.UserProfile
import com.example.crisisconnect.data.network.SupabaseHttpClient

/**
 * Repository for managing user profiles.
 */
object ProfileRepository {

    /**
     * Update user profile with name, phone, location, and organization.
     * Note: Role is not updated here as it may not exist in the profiles table.
     * Use AdminRepository.adminUpdateUser() for role updates.
     * Location is stored as PostGIS POINT format: 'POINT(lon lat)'
     */
    suspend fun updateProfile(
        userId: String,
        fullName: String? = null,
        phone: String? = null,
        locationLat: Double? = null,
        locationLon: Double? = null,
        organization: String? = null
    ): UserProfile {
        val locationWkt = if (locationLat != null && locationLon != null) {
            "POINT($locationLon $locationLat)"
        } else {
            null
        }

        val updateData = mutableMapOf<String, Any?>()
        fullName?.let { updateData["full_name"] = it }
        phone?.let { updateData["phone"] = it }
        locationWkt?.let { updateData["location"] = it }
        organization?.let { updateData["organization"] = it }
        // Note: role is not included as it may not exist in the profiles table

        val result = SupabaseHttpClient.update<UserProfile>(
            table = "profiles",
            filter = "id=eq.$userId",
            data = updateData
        )
        
        return result.firstOrNull() ?: throw Exception("Profile update failed")
    }

    /**
     * Get user profile by ID.
     */
    suspend fun getProfile(userId: String): UserProfile? {
        val profiles = SupabaseHttpClient.from<UserProfile>(
            table = "profiles",
            select = "*",
            filter = "id=eq.$userId"
        )
        return profiles.firstOrNull()
    }
}

// UserProfile is defined in data.model.UserProfile.kt

