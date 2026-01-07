package com.example.crisisconnect.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast

/**
 * Utility functions for opening locations in Google Maps.
 */
object MapUtils {
    
    /**
     * Open Google Maps with the given coordinates.
     * @param context Android context
     * @param lat Latitude
     * @param lon Longitude
     * @param label Optional label for the location
     */
    fun openInGoogleMaps(context: Context, lat: Double, lon: Double, label: String? = null) {
        try {
            // Create URI for Google Maps
            val uri = if (label != null) {
                // Use geo URI with label
                Uri.parse("geo:$lat,$lon?q=$lat,$lon(${Uri.encode(label)})")
            } else {
                // Use geo URI without label
                Uri.parse("geo:$lat,$lon?q=$lat,$lon")
            }
            
            // Try to open in Google Maps app
            val mapIntent = Intent(Intent.ACTION_VIEW, uri).apply {
                setPackage("com.google.android.apps.maps")
            }
            
            if (mapIntent.resolveActivity(context.packageManager) != null) {
                context.startActivity(mapIntent)
            } else {
                // Fallback to web browser if Google Maps app not installed
                val webUri = Uri.parse("https://www.google.com/maps/search/?api=1&query=$lat,$lon")
                val webIntent = Intent(Intent.ACTION_VIEW, webUri)
                context.startActivity(webIntent)
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Could not open location in maps", Toast.LENGTH_SHORT).show()
        }
    }
    
    /**
     * Open Google Maps with coordinates from a string (format: "lat, lon").
     * @param context Android context
     * @param coordinates String in format "lat, lon" or "lat,lon"
     * @param label Optional label for the location
     */
    fun openInGoogleMapsFromString(context: Context, coordinates: String, label: String? = null) {
        try {
            val parts = coordinates.split(",").map { it.trim() }
            if (parts.size >= 2) {
                val lat = parts[0].toDoubleOrNull()
                val lon = parts[1].toDoubleOrNull()
                
                if (lat != null && lon != null) {
                    openInGoogleMaps(context, lat, lon, label)
                } else {
                    Toast.makeText(context, "Invalid coordinates", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(context, "Invalid coordinate format", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Could not open location in maps", Toast.LENGTH_SHORT).show()
        }
    }
}

