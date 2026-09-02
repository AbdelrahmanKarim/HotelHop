package com.task.hotelhop.data.datasource.user

import kotlinx.coroutines.flow.Flow

interface LocalUserDataSource {
    fun isDarkMode(): Flow<Boolean?>
    fun getLanguage(): Flow<String>
    fun hasSeenOnboarding(): Flow<Boolean>
    suspend fun toggleTheme(isDark: Boolean)
    suspend fun changeLanguage(languageCode: String)
    suspend fun completeOnboarding()
    fun isUserLoggedIn(): Flow<Boolean>
    suspend fun setUserLoggedIn(isLoggedIn: Boolean)
}