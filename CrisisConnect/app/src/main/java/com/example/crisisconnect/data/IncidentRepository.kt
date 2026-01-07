package com.example.crisisconnect.data

import com.example.crisisconnect.data.network.SupabaseHttpClient
import kotlinx.serialization.Serializable

/**
 * Repository for managing incident reports.
 */
object IncidentRepository {

    /**
     * Add a new incident report.
     * RPC: add_incident_report
     * Parameters: p_user_id, p_description, p_lat, p_lon
     * Note: RPC may return just the UUID string, so we handle both cases
     */
    suspend fun addIncidentReport(
        userId: String,
        description: String,
        lat: Double,
        lon: Double
    ): String {
        // RPC returns UUID string, not full object
        val response = SupabaseHttpClient.rpc<String>(
            functionName = "add_incident_report",
            params = mapOf(
                "p_user_id" to userId,
                "p_description" to description,
                "p_lat" to lat,
                "p_lon" to lon
            )
        )
        return response
    }

    /**
     * Auto-verify an incident report.
     * RPC: auto_verify_incident_report
     */
    suspend fun autoVerifyIncidentReport(reportId: String) {
        SupabaseHttpClient.rpc<Unit>(
            functionName = "auto_verify_incident_report",
            params = mapOf("p_report_id" to reportId)
        )
    }

    /**
     * Get all incident reports for the current user.
     */
    suspend fun getUserIncidentReports(userId: String): List<IncidentReport> {
        return SupabaseHttpClient.from<IncidentReport>(
            table = "incident_reports",
            select = "*",
            filter = "user_id=eq.$userId"
        )
    }

    /**
     * Get all verified/approved incidents (for map and alerts display).
     * Only incidents with status "verified", "verified & assigned", or "in progress" are shown.
     */
    suspend fun getVerifiedIncidents(): List<IncidentReport> {
        // Use or operator with properly encoded values for PostgREST
        // Format: or=(condition1,condition2,condition3)
        // PostgREST needs %20 for spaces, not +, so we manually encode
        val verifiedStatus = "verified"
        val verifiedAssignedStatus = "verified%20%26%20assigned" // "verified & assigned" encoded
        val inProgressStatus = "in%20progress" // "in progress" encoded
        
        return SupabaseHttpClient.from<IncidentReport>(
            table = "incident_reports",
            select = "*",
            filter = "or=(status.eq.$verifiedStatus,status.eq.$verifiedAssignedStatus,status.eq.$inProgressStatus)"
        )
    }

    /**
     * Get all pending incident reports (for admin approval).
     */
    suspend fun getPendingIncidents(): List<IncidentReport> {
        return SupabaseHttpClient.from<IncidentReport>(
            table = "incident_reports",
            select = "*",
            filter = "status=eq.pending"
        )
    }

    /**
     * Admin approve incident (change status to verified).
     */
    suspend fun approveIncident(reportId: String) {
        SupabaseHttpClient.update<IncidentReport>(
            table = "incident_reports",
            filter = "id=eq.$reportId",
            data = mapOf("status" to "verified")
        )
    }

    /**
     * Admin reject incident.
     */
    suspend fun rejectIncident(reportId: String) {
        SupabaseHttpClient.update<IncidentReport>(
            table = "incident_reports",
            filter = "id=eq.$reportId",
            data = mapOf("status" to "rejected")
        )
    }
}

@Serializable
data class IncidentReportResponse(
    val id: String,
    val user_id: String,
    val disaster_event_id: String?,
    val description: String,
    val lat: Double,
    val lon: Double,
    val status: String,
    val created_at: String
)

@Serializable
data class IncidentReport(
    val id: String,
    val user_id: String,
    val disaster_event_id: String? = null,
    val description: String,
    val lat: Double? = null,
    val lon: Double? = null,
    val status: String,
    val created_at: String,
    val updated_at: String? = null
)

