package com.task.hotelhop.data.datasource.user

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.task.hotelhop.domain.entity.User
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class LocalUserDataSourceImpl(
    private val dataStore: DataStore<Preferences>
) : LocalUserDataSource {


    override fun isDarkMode(): Flow<Boolean?> = dataStore.data.map { it[THEME_KEY] }

    override fun getLanguage(): Flow<String> = dataStore.data.map { it[LANGUAGE_KEY] ?: "en" }

    override fun hasSeenOnboarding(): Flow<Boolean> = dataStore.data.map { it[ONBOARDING_KEY] ?: false }

    override suspend fun toggleTheme(isDark: Boolean?) {
        dataStore.edit { prefs ->
            if (isDark == null) {
                prefs.remove(THEME_KEY)
            } else {
                prefs[THEME_KEY] = isDark
            }
        }
    }

    override suspend fun changeLanguage(languageCode: String) {
        dataStore.edit { it[LANGUAGE_KEY] = languageCode }
    }

    override suspend fun completeOnboarding() {
        dataStore.edit { it[ONBOARDING_KEY] = true }
    }

    override fun isUserLoggedIn(): Flow<Boolean> = dataStore.data.map { it[IS_LOGGED_IN_KEY] ?: false }

    override fun isGuest(): Flow<Boolean> = dataStore.data.map { it[IS_GUEST_KEY] ?: false }

    override suspend fun setUserLoggedIn(isLoggedIn: Boolean) {
        dataStore.edit {
            it[IS_LOGGED_IN_KEY] = isLoggedIn
            if (isLoggedIn) it[IS_GUEST_KEY] = false
            if (!isLoggedIn) it[IS_GUEST_KEY] = false
        }
        if (!isLoggedIn) clearUser()
    }

    override suspend fun enterGuestMode() {
        dataStore.edit {
            it[IS_GUEST_KEY] = true
            it[IS_LOGGED_IN_KEY] = false
        }
        clearUser()
    }

    override suspend fun saveUser(user: User) {
        dataStore.edit {
            it[USER_ID_KEY] = user.id
            it[USER_FIRST_NAME_KEY] = user.firstName
            it[USER_LAST_NAME_KEY] = user.lastName
            it[USER_EMAIL_KEY] = user.email
            it[USER_GENDER_KEY] = user.gender
        }
    }

    override suspend fun getCachedUser(): User? {
        val prefs = dataStore.data.first()
        val id = prefs[USER_ID_KEY] ?: return null
        val email = prefs[USER_EMAIL_KEY].orEmpty()
        if (id.isBlank() && email.isBlank()) return null
        return User(
            id = id,
            firstName = prefs[USER_FIRST_NAME_KEY].orEmpty(),
            lastName = prefs[USER_LAST_NAME_KEY].orEmpty(),
            email = email,
            gender = prefs[USER_GENDER_KEY].orEmpty()
        )
    }

    override suspend fun clearUser() {
        dataStore.edit {
            it.remove(USER_ID_KEY)
            it.remove(USER_FIRST_NAME_KEY)
            it.remove(USER_LAST_NAME_KEY)
            it.remove(USER_EMAIL_KEY)
            it.remove(USER_GENDER_KEY)
        }
    }

    companion object {
        val THEME_KEY = booleanPreferencesKey("is_dark_mode")
        val LANGUAGE_KEY = stringPreferencesKey("language_code")
        val ONBOARDING_KEY = booleanPreferencesKey("has_seen_onboarding")
        val IS_LOGGED_IN_KEY = booleanPreferencesKey("is_logged_in")
        val IS_GUEST_KEY = booleanPreferencesKey("is_guest")
        val USER_ID_KEY = stringPreferencesKey("cached_user_id")
        val USER_FIRST_NAME_KEY = stringPreferencesKey("cached_user_first_name")
        val USER_LAST_NAME_KEY = stringPreferencesKey("cached_user_last_name")
        val USER_EMAIL_KEY = stringPreferencesKey("cached_user_email")
        val USER_GENDER_KEY = stringPreferencesKey("cached_user_gender")
    }
}