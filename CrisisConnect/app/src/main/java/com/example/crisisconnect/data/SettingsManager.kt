package com.example.crisisconnect.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

private val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

object SettingsManager {
    private var dataStore: DataStore<Preferences>? = null
    
    private val PUSH_NOTIFICATIONS_KEY = booleanPreferencesKey("push_notifications")
    private val VOICE_ACTIVATION_KEY = booleanPreferencesKey("voice_activation")
    private val AUTO_SHARE_LOCATION_KEY = booleanPreferencesKey("auto_share_location")
    private val ALERT_THRESHOLD_KEY = stringPreferencesKey("alert_threshold")
    
    fun initialize(context: Context) {
        dataStore = context.settingsDataStore
    }
    
    suspend fun setPushNotifications(enabled: Boolean) {
        dataStore?.edit { preferences ->
            preferences[PUSH_NOTIFICATIONS_KEY] = enabled
        }
    }
    
    val pushNotifications: Flow<Boolean> = dataStore?.data?.map { preferences ->
        preferences[PUSH_NOTIFICATIONS_KEY] ?: true
    } ?: flowOf(true)
    
    suspend fun setVoiceActivation(enabled: Boolean) {
        dataStore?.edit { preferences ->
            preferences[VOICE_ACTIVATION_KEY] = enabled
        }
    }
    
    val voiceActivation: Flow<Boolean> = dataStore?.data?.map { preferences ->
        preferences[VOICE_ACTIVATION_KEY] ?: true
    } ?: flowOf(true)
    
    suspend fun setAutoShareLocation(enabled: Boolean) {
        dataStore?.edit { preferences ->
            preferences[AUTO_SHARE_LOCATION_KEY] = enabled
        }
    }
    
    val autoShareLocation: Flow<Boolean> = dataStore?.data?.map { preferences ->
        preferences[AUTO_SHARE_LOCATION_KEY] ?: false
    } ?: flowOf(false)
    
    suspend fun setAlertThreshold(threshold: String) {
        dataStore?.edit { preferences ->
            preferences[ALERT_THRESHOLD_KEY] = threshold
        }
    }
    
    val alertThreshold: Flow<String> = dataStore?.data?.map { preferences ->
        preferences[ALERT_THRESHOLD_KEY] ?: "Critical only"
    } ?: flowOf("Critical only")
}

