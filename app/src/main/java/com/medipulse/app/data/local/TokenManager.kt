package com.medipulse.app.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

val Context.dataStore by preferencesDataStore(name = "medipulse_prefs")

@Singleton
class TokenManager @Inject constructor(@ApplicationContext private val context: Context) {
    companion object {
        val ACCESS_TOKEN_KEY = stringPreferencesKey("access_token")
        val REFRESH_TOKEN_KEY = stringPreferencesKey("refresh_token")
        val USER_ROLE_KEY = stringPreferencesKey("user_role")
        val USER_NAME_KEY = stringPreferencesKey("user_name")
        val USER_STATUS_KEY = stringPreferencesKey("user_status")
    }

    val accessToken: Flow<String?> = context.dataStore.data.map { it[ACCESS_TOKEN_KEY] }
    val userRole: Flow<String?> = context.dataStore.data.map { it[USER_ROLE_KEY] }
    val userName: Flow<String?> = context.dataStore.data.map { it[USER_NAME_KEY] }
    val userStatus: Flow<String?> = context.dataStore.data.map { it[USER_STATUS_KEY] }

    suspend fun saveTokens(access: String, refresh: String, role: String, name: String, status: String) {
        context.dataStore.edit { prefs ->
            prefs[ACCESS_TOKEN_KEY] = access
            prefs[REFRESH_TOKEN_KEY] = refresh
            prefs[USER_ROLE_KEY] = role
            prefs[USER_NAME_KEY] = name
            prefs[USER_STATUS_KEY] = status
        }
    }

    suspend fun clearSession() {
        context.dataStore.edit { prefs ->
            prefs.clear()
        }
    }
}
