package com.task.hotelhop.domain.repo

import com.task.hotelhop.domain.entity.User
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    suspend fun loginWithEmail(email: String, password: String): User
    suspend fun signUpWithEmail(firstName: String, lastName: String, email: String, password: String, gender: String): User
    suspend fun signInWithGoogle(idToken: String): User
    suspend fun logout()
    suspend fun getCurrentUser(): User?
    fun isDarkMode(): Flow<Boolean?>
    fun getLanguage(): Flow<String>
    fun hasSeenOnboarding(): Flow<Boolean>
    suspend fun toggleTheme(isDark: Boolean)
    suspend fun changeLanguage(languageCode: String)
    suspend fun completeOnboarding()

    fun isUserLoggedIn(): Flow<Boolean>
}