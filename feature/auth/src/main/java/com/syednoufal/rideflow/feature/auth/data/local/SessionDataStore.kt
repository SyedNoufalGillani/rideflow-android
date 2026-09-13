package com.syednoufal.rideflow.feature.auth.data.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.syednoufal.rideflow.core.domain.model.AuthSession
import com.syednoufal.rideflow.core.domain.model.User
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * Persists the rider's [AuthSession] to DataStore Preferences (as individual
 * scalar fields, deliberately avoiding a JSON blob so the domain [User]
 * model never needs a serialization dependency) so the session survives
 * process death.
 */
class SessionDataStore @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) {

    fun observeSession(): Flow<AuthSession?> = dataStore.data.map(::toAuthSessionOrNull)

    suspend fun getSession(): AuthSession? = toAuthSessionOrNull(dataStore.data.first())

    suspend fun saveSession(session: AuthSession) {
        dataStore.edit { preferences ->
            preferences[ACCESS_TOKEN_KEY] = session.accessToken
            preferences[REFRESH_TOKEN_KEY] = session.refreshToken
            preferences[USER_ID_KEY] = session.user.id
            preferences[USER_FULL_NAME_KEY] = session.user.fullName
            preferences[USER_PHONE_KEY] = session.user.phoneNumber
            session.user.email?.let { preferences[USER_EMAIL_KEY] = it }
            session.user.profilePhotoUrl?.let { preferences[USER_PHOTO_KEY] = it }
            preferences[USER_RATING_KEY] = session.user.rating
            preferences[USER_MEMBER_SINCE_KEY] = session.user.memberSinceEpochMillis
        }
    }

    suspend fun clearSession() {
        dataStore.edit { it.clear() }
    }

    private fun toAuthSessionOrNull(preferences: Preferences): AuthSession? {
        val accessToken = preferences[ACCESS_TOKEN_KEY] ?: return null
        val refreshToken = preferences[REFRESH_TOKEN_KEY] ?: return null
        val userId = preferences[USER_ID_KEY] ?: return null
        return AuthSession(
            accessToken = accessToken,
            refreshToken = refreshToken,
            user = User(
                id = userId,
                fullName = preferences[USER_FULL_NAME_KEY].orEmpty(),
                phoneNumber = preferences[USER_PHONE_KEY].orEmpty(),
                email = preferences[USER_EMAIL_KEY],
                profilePhotoUrl = preferences[USER_PHOTO_KEY],
                rating = preferences[USER_RATING_KEY] ?: DEFAULT_RATING,
                memberSinceEpochMillis = preferences[USER_MEMBER_SINCE_KEY] ?: 0L,
            ),
        )
    }

    private companion object {
        val ACCESS_TOKEN_KEY = stringPreferencesKey("access_token")
        val REFRESH_TOKEN_KEY = stringPreferencesKey("refresh_token")
        val USER_ID_KEY = stringPreferencesKey("user_id")
        val USER_FULL_NAME_KEY = stringPreferencesKey("user_full_name")
        val USER_PHONE_KEY = stringPreferencesKey("user_phone")
        val USER_EMAIL_KEY = stringPreferencesKey("user_email")
        val USER_PHOTO_KEY = stringPreferencesKey("user_photo")
        val USER_RATING_KEY = floatPreferencesKey("user_rating")
        val USER_MEMBER_SINCE_KEY = longPreferencesKey("user_member_since")
        const val DEFAULT_RATING = 5.0f
    }
}
