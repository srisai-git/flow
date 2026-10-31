package com.simats.focusflow.data.session

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

// Application-scoped DataStore delegate
private val Context.sessionDataStore: DataStore<Preferences> by preferencesDataStore(name = "focusflow_session_prefs")

/**
 * Immutable data holder representing the active user session.
 */
data class UserSession(
    val isLoggedIn: Boolean = false,
    val userId: Int? = null,
    val userName: String? = null,
    val userEmail: String? = null,
    val userAvatar: String? = null,
    val userAvatarUri: String? = null,
    val userBio: String? = null,
    val isOnboardingCompleted: Boolean = false
)

/**
 * Singleton repository managing authentication and session persistence using Jetpack DataStore Preferences.
 */
class SessionManager private constructor(
    private val dataStore: DataStore<Preferences>
) {

    companion object {
        val KEY_IS_LOGGED_IN = booleanPreferencesKey("key_is_logged_in")
        val KEY_USER_ID = intPreferencesKey("key_user_id")
        val KEY_USER_NAME = stringPreferencesKey("key_user_name")
        val KEY_USER_EMAIL = stringPreferencesKey("key_user_email")
        val KEY_USER_AVATAR = stringPreferencesKey("key_user_avatar")
        val KEY_USER_AVATAR_URI = stringPreferencesKey("user_avatar_uri")
        val KEY_USER_BIO = stringPreferencesKey("key_user_bio")
        val KEY_ONBOARDING_COMPLETED = booleanPreferencesKey("key_onboarding_completed")
        val KEY_THEME_MODE = stringPreferencesKey("key_theme_mode")

        @Volatile
        private var INSTANCE: SessionManager? = null

        fun getInstance(context: Context): SessionManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: SessionManager(context.applicationContext.sessionDataStore).also {
                    INSTANCE = it
                }
            }
        }
    }

    /**
     * Emits whether the user is actively logged in.
     */
    val isLoggedInFlow: Flow<Boolean> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[KEY_IS_LOGGED_IN] ?: false
        }

    /**
     * Emits the authenticated user's numeric ID or null.
     */
    val userIdFlow: Flow<Int?> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[KEY_USER_ID]
        }

    /**
     * Emits the cached user avatar uri/url directly.
     */
    val userAvatarFlow: Flow<String?> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[KEY_USER_AVATAR_URI] ?: preferences[KEY_USER_AVATAR]
        }

    /**
     * Emits the entire composite UserSession.
     */
    val userSessionFlow: Flow<UserSession> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            val avatar = preferences[KEY_USER_AVATAR_URI] ?: preferences[KEY_USER_AVATAR]
            UserSession(
                isLoggedIn = preferences[KEY_IS_LOGGED_IN] ?: false,
                userId = preferences[KEY_USER_ID],
                userName = preferences[KEY_USER_NAME],
                userEmail = preferences[KEY_USER_EMAIL],
                userAvatar = avatar,
                userAvatarUri = avatar,
                userBio = preferences[KEY_USER_BIO],
                isOnboardingCompleted = preferences[KEY_ONBOARDING_COMPLETED] ?: false
            )
        }

    /**
     * Saves user credentials and activates logged-in status.
     */
    suspend fun saveSession(
        userId: Int,
        name: String,
        email: String,
        avatar: String = "",
        bio: String = "",
        isOnboardingCompleted: Boolean = false
    ) {
        dataStore.edit { preferences ->
            preferences[KEY_IS_LOGGED_IN] = true
            preferences[KEY_USER_ID] = userId
            preferences[KEY_USER_NAME] = name
            preferences[KEY_USER_EMAIL] = email
            if (avatar.isNotBlank()) {
                preferences[KEY_USER_AVATAR] = avatar
                preferences[KEY_USER_AVATAR_URI] = avatar
            }
            if (bio.isNotBlank()) {
                preferences[KEY_USER_BIO] = bio
            }
            preferences[KEY_ONBOARDING_COMPLETED] = isOnboardingCompleted
        }
    }

    /**
     * Updates the user profile name, email, bio, and avatar across the active session.
     */
    suspend fun updateProfile(name: String, email: String, bio: String = "", avatar: String = "") {
        dataStore.edit { preferences ->
            preferences[KEY_USER_NAME] = name
            preferences[KEY_USER_EMAIL] = email
            if (bio.isNotBlank()) preferences[KEY_USER_BIO] = bio
            if (avatar.isNotBlank()) {
                preferences[KEY_USER_AVATAR] = avatar
                preferences[KEY_USER_AVATAR_URI] = avatar
            }
        }
    }

    /**
     * Saves the avatar URI/URL directly to local DataStore.
     */
    suspend fun saveAvatarUri(uri: String) {
        dataStore.edit { preferences ->
            preferences[KEY_USER_AVATAR] = uri
            preferences[KEY_USER_AVATAR_URI] = uri
        }
    }

    /**
     * Updates the onboarding flag once the user completes the "Make It Yours" customization.
     */
    suspend fun setOnboardingCompleted(completed: Boolean = true) {
        dataStore.edit { preferences ->
            preferences[KEY_ONBOARDING_COMPLETED] = completed
        }
    }

    /**
     * Emits the selected theme mode ("dark" or "light"). Defaults to "light".
     */
    val themeModeFlow: Flow<String> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[KEY_THEME_MODE] ?: "light"
        }

    /**
     * Updates the persistent theme mode ("dark" or "light").
     */
    suspend fun setThemeMode(mode: String) {
        dataStore.edit { preferences ->
            preferences[KEY_THEME_MODE] = mode
        }
    }

    /**
     * Clears all session keys upon logout and resets theme to clean light default.
     */
    suspend fun clearSession() {
        dataStore.edit { preferences ->
            preferences.remove(KEY_IS_LOGGED_IN)
            preferences.remove(KEY_USER_ID)
            preferences.remove(KEY_USER_NAME)
            preferences.remove(KEY_USER_EMAIL)
            preferences.remove(KEY_USER_AVATAR)
            preferences.remove(KEY_USER_AVATAR_URI)
            preferences.remove(KEY_USER_BIO)
            preferences.remove(KEY_ONBOARDING_COMPLETED)
            preferences[KEY_THEME_MODE] = "light"
        }
    }
}
