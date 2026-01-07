package com.example.crisisconnect.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "session")

object SessionManager {
    private const val ACCESS_TOKEN_KEY = "access_token"
    private const val USER_ID_KEY = "user_id"
    private const val REFRESH_TOKEN_KEY = "refresh_token"

    private var dataStore: DataStore<Preferences>? = null

    fun initialize(context: Context) {
        if (dataStore == null) {
            dataStore = context.dataStore
        }
    }

    suspend fun saveSession(accessToken: String, userId: String, refreshToken: String? = null) {
        val store = dataStore
        if (store == null) {
            throw IllegalStateException("SessionManager not initialized. Call initialize(context) first.")
        }
        store.edit { preferences ->
            preferences[stringPreferencesKey(ACCESS_TOKEN_KEY)] = accessToken
            preferences[stringPreferencesKey(USER_ID_KEY)] = userId
            refreshToken?.let {
                preferences[stringPreferencesKey(REFRESH_TOKEN_KEY)] = it
            }
        }
    }

    suspend fun getAccessToken(): String? {
        return dataStore?.data?.first()?.get(stringPreferencesKey(ACCESS_TOKEN_KEY))
    }

    suspend fun getUserId(): String? {
        return dataStore?.data?.first()?.get(stringPreferencesKey(USER_ID_KEY))
    }

    suspend fun getRefreshToken(): String? {
        return dataStore?.data?.first()?.get(stringPreferencesKey(REFRESH_TOKEN_KEY))
    }

    val userIdFlow: Flow<String?> = dataStore?.data?.map { preferences ->
        preferences[stringPreferencesKey(USER_ID_KEY)]
    } ?: kotlinx.coroutines.flow.flowOf(null)

    suspend fun clearSession() {
        dataStore?.edit { preferences ->
            preferences.remove(stringPreferencesKey(ACCESS_TOKEN_KEY))
            preferences.remove(stringPreferencesKey(USER_ID_KEY))
            preferences.remove(stringPreferencesKey(REFRESH_TOKEN_KEY))
        }
    }

    suspend fun isLoggedIn(): Boolean {
        return !getAccessToken().isNullOrBlank()
    }
}

