package com.example.crisisconnect.data

import androidx.compose.runtime.mutableStateListOf
import com.example.crisisconnect.data.model.Alert
import com.example.crisisconnect.data.model.AlertSeverity
import com.example.crisisconnect.data.model.AlertType
import com.example.crisisconnect.data.model.Incident
import com.example.crisisconnect.data.model.NotificationMessage
import com.example.crisisconnect.data.model.SafetyTip
import com.example.crisisconnect.data.model.Shelter
import com.example.crisisconnect.data.model.UserProfile
import com.example.crisisconnect.data.model.UserRole

object SampleDataProvider {

    val alerts = mutableStateListOf(
        Alert(
            id = "AL-100",
            title = "Flood Alert - Riverside",
            description = "Water levels rising near Riverside colony. Evacuate low-lying homes.",
            type = AlertType.FLOOD,
            severity = AlertSeverity.CRITICAL,
            location = "Riverside",
            issuedBy = "Disaster Authority",
            timestamp = "10:05 AM",
            verified = true
        ),
        Alert(
            id = "AL-102",
            title = "Heatwave Advisory",
            description = "Temperatures expected to reach 44°C. Stay hydrated and indoors.",
            type = AlertType.WEATHER,
            severity = AlertSeverity.MODERATE,
            location = "City Wide",
            issuedBy = "Met Office",
            timestamp = "08:40 AM"
        ),
        Alert(
            id = "AL-106",
            title = "Lightning Risk",
            description = "Severe lightning detected north of highway 18. Suspend outdoor events.",
            type = AlertType.WEATHER,
            severity = AlertSeverity.HIGH,
            location = "North District",
            issuedBy = "Weather API",
            timestamp = "09:20 AM",
            verified = true
        ),
        Alert(
            id = "AL-111",
            title = "Wildfire Contained",
            description = "Firefighters have contained the Pine Hills wildfire. Remain cautious.",
            type = AlertType.FIRE,
            severity = AlertSeverity.MODERATE,
            location = "Pine Hills",
            issuedBy = "Forest Dept.",
            timestamp = "07:55 AM",
            verified = true,
            acknowledged = true
        )
    )

    val incidents = mutableStateListOf(
        Incident(
            id = "IN-210",
            title = "Bridge Collapse Warning",
            type = "Infrastructure",
            location = "East Bridge",
            reporter = "Citizen",
            status = "Pending Review",
            description = "Visible cracks and swaying observed after tremors.",
            severity = AlertSeverity.HIGH,
            lastUpdated = "09:10 AM"
        ),
        Incident(
            id = "IN-214",
            title = "Fire Near Market",
            type = "Fire",
            location = "Downtown Market",
            reporter = "Responder",
            status = "Containment in progress",
            description = "Shops 12-16 impacted, responders on site.",
            severity = AlertSeverity.CRITICAL,
            lastUpdated = "09:45 AM"
        )
    )

    val shelters = listOf(
        Shelter(
            id = "SH-01",
            name = "Central High School Gym",
            address = "14 Main Street",
            capacity = 500,
            occupancy = 320,
            isOpen = true,
            distanceKm = 1.8,
            contact = "+92 300 1234567"
        ),
        Shelter(
            id = "SH-02",
            name = "Community Hall West",
            address = "78 Lake Road",
            capacity = 200,
            occupancy = 75,
            isOpen = true,
            distanceKm = 3.4,
            contact = "+92 300 9876543"
        ),
        Shelter(
            id = "SH-03",
            name = "Stadium Safe Zone",
            address = "Ring Road 5",
            capacity = 1200,
            occupancy = 1100,
            isOpen = false,
            distanceKm = 5.6,
            contact = "+92 311 0001122"
        )
    )

    val safetyTips = listOf(
        SafetyTip(
            category = "Earthquake",
            summary = "Drop, cover, hold and prepare go-bag.",
            steps = listOf(
                "Identify safe spots under sturdy furniture.",
                "Keep emergency kits accessible.",
                "After shaking stops, evacuate calmly using stairs."
            )
        ),
        SafetyTip(
            category = "Flood",
            summary = "Stay above water line and disconnect power.",
            steps = listOf(
                "Move to higher floors and avoid basements.",
                "Turn off main electricity supply.",
                "Do not drive through flooded roads."
            )
        ),
        SafetyTip(
            category = "Heatwave",
            summary = "Hydrate frequently and check on neighbors.",
            steps = listOf(
                "Limit outdoor work between 11AM - 4PM.",
                "Drink water every 30 minutes.",
                "Call emergency services if heatstroke symptoms appear."
            )
        )
    )

    val users = mutableStateListOf(
        UserProfile(
            id = "USR-01",
            full_name = "Ayesha Rafiq",
            phone = "+92 333 999111",
            organization = "Relief NGO",
            role = "NGO_WORKER",
            created_at = "2024-01-15T10:00:00Z",
            updated_at = null
        ),
        UserProfile(
            id = "USR-02",
            full_name = "Inspector Khan",
            phone = "+92 311 223344",
            organization = "City Police",
            role = "AUTHORITY",
            created_at = "2024-01-10T08:00:00Z",
            updated_at = null
        ),
        UserProfile(
            id = "USR-03",
            full_name = "Sara Malik",
            phone = "+92 300 555777",
            organization = "CrisisConnect",
            role = "ADMIN",
            created_at = "2024-01-01T12:00:00Z",
            updated_at = null
        )
    )

    val notificationHistory = mutableStateListOf(
        NotificationMessage(
            id = "NT-01",
            title = "Evacuation drill",
            body = "Mandatory drill at 3PM for riverside blocks.",
            channel = "SMS + Push",
            sentAt = "Yesterday",
            audience = "Riverside Residents"
        ),
        NotificationMessage(
            id = "NT-02",
            title = "Medical Camp",
            body = "Mobile medical camp operational at Community Hall.",
            channel = "Push",
            sentAt = "Today 07:00",
            audience = "Ward 9"
        )
    )
}

