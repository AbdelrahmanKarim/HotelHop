package com.task.hotelhop.data.datasource.user

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class LocalUserDataSourceImpl(
    private val dataStore: DataStore<Preferences>
) : LocalUserDataSource {


    override fun isDarkMode(): Flow<Boolean?> = dataStore.data.map { it[THEME_KEY] }

    override fun getLanguage(): Flow<String> = dataStore.data.map { it[LANGUAGE_KEY] ?: "en" }

    override fun hasSeenOnboarding(): Flow<Boolean> = dataStore.data.map { it[ONBOARDING_KEY] ?: false }

    override suspend fun toggleTheme(isDark: Boolean) {
        dataStore.edit { it[THEME_KEY] = isDark }
    }

    override suspend fun changeLanguage(languageCode: String) {
        dataStore.edit { it[LANGUAGE_KEY] = languageCode }
    }

    override suspend fun completeOnboarding() {
        dataStore.edit { it[ONBOARDING_KEY] = true }
    }

    override fun isUserLoggedIn(): Flow<Boolean> = dataStore.data.map { it[IS_LOGGED_IN_KEY] ?: false }

    override suspend fun setUserLoggedIn(isLoggedIn: Boolean) {
        dataStore.edit { it[IS_LOGGED_IN_KEY] = isLoggedIn }
    }

    companion object {
        val THEME_KEY = booleanPreferencesKey("is_dark_mode")
        val LANGUAGE_KEY = stringPreferencesKey("language_code")
        val ONBOARDING_KEY = booleanPreferencesKey("has_seen_onboarding")
        val IS_LOGGED_IN_KEY = booleanPreferencesKey("is_logged_in")
    }
}