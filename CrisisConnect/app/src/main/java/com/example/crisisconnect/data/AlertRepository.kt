package com.example.crisisconnect.data

import com.example.crisisconnect.data.network.SupabaseHttpClient
import kotlinx.serialization.Serializable

/**
 * Repository for managing disaster alerts and notifications.
 */
object AlertRepository {

    /**
     * Get active disasters for user within 5km and 1 day.
     * RPC: get_active_disasters_for_user
     * Returns disasters within 5km radius and within last 24 hours.
     * This should be called every 2 minutes (polling).
     */
    suspend fun getActiveDisastersForUser(userId: String): List<DisasterAlert> {
        return try {
            val result = SupabaseHttpClient.rpc<List<DisasterAlert>>(
                functionName = "get_active_disasters_for_user",
                params = mapOf("p_user_id" to userId)
            )
            // Filter out any null/invalid entries
            result.filter { it.id != null || it.disaster_type != null }
        } catch (e: Exception) {
            // If RPC fails or returns unexpected format, return empty list
            emptyList()
        }
    }

    /**
     * Check for disasters in radius and create notifications.
     * RPC: check_disasters_and_notify
     * This should be called every 2 minutes (polling).
     * @deprecated Use getActiveDisastersForUser instead
     */
    @Deprecated("Use getActiveDisastersForUser instead")
    suspend fun checkDisastersAndNotify(userUuid: String): List<DisasterAlert> {
        return SupabaseHttpClient.rpc<List<DisasterAlert>>(
            functionName = "check_disasters_and_notify",
            params = mapOf("user_uuid" to userUuid)
        )
    }

    /**
     * Filter alerts by disaster type.
     * RPC: filter_alerts_by_type
     */
    suspend fun filterAlertsByType(disasterType: String): List<DisasterAlert> {
        return SupabaseHttpClient.rpc<List<DisasterAlert>>(
            functionName = "filter_alerts_by_type",
            params = mapOf("p_disaster_type" to disasterType)
        )
    }

    /**
     * Get all active alerts.
     */
    suspend fun getAllAlerts(): List<DisasterAlert> {
        return SupabaseHttpClient.from<DisasterAlert>(
            table = "disaster_alerts",
            select = "*",
            filter = null // Get all alerts, ordered by created_at desc
        )
    }
    
    /**
     * Create a disaster alert directly in the database.
     * This can be used if the RPC function doesn't create alerts.
     */
    suspend fun createAlert(
        title: String,
        message: String,
        disasterType: String? = null,
        severity: String? = null,
        location: String? = null,
        lat: Double? = null,
        lon: Double? = null
    ): DisasterAlert {
        val alertData = mutableMapOf<String, Any?>(
            "title" to title,
            "message" to message
        )
        disasterType?.let { alertData["disaster_type"] = it }
        severity?.let { alertData["severity"] = it }
        location?.let { alertData["location"] = it }
        lat?.let { alertData["lat"] = it }
        lon?.let { alertData["lon"] = it }
        
        // Insert into disaster_alerts table
        val result = SupabaseHttpClient.rpc<List<DisasterAlert>>(
            functionName = "create_disaster_alert",
            params = alertData
        )
        return result.firstOrNull() ?: throw Exception("Failed to create alert")
    }
}

@Serializable
data class DisasterAlert(
    val id: String? = null,
    val title: String? = null,
    val message: String? = null,
    val disaster_type: String? = null,
    val severity: String? = null,
    val location: String? = null,
    val lat: Double? = null,
    val lon: Double? = null,
    val created_at: String? = null,
    val expires_at: String? = null
)

