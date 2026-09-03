package com.task.hotelhop.testutil

import com.task.hotelhop.domain.entity.User
import com.task.hotelhop.domain.exception.AppException
import com.task.hotelhop.domain.repo.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class FakeUserRepository : UserRepository {
    var enterGuestCalls = 0
    var lastLoginEmail: String? = null

    private val loggedIn = MutableStateFlow(false)
    private val guest = MutableStateFlow(false)

    override suspend fun loginWithEmail(email: String, password: String): User {
        lastLoginEmail = email
        loggedIn.value = true
        guest.value = false
        return User("u1", "Test", "User", email, "other")
    }

    override suspend fun signUpWithEmail(
        firstName: String,
        lastName: String,
        email: String,
        password: String,
        gender: String
    ): User = throw AppException.UnknownException()

    override suspend fun signInWithGoogle(idToken: String): User = throw AppException.UnknownException()

    override suspend fun logout() {
        loggedIn.value = false
        guest.value = false
    }

    override suspend fun getCurrentUser(): User? = null

    override fun isDarkMode(): Flow<Boolean?> = MutableStateFlow(null)

    override fun getLanguage(): Flow<String> = MutableStateFlow("en")

    override fun hasSeenOnboarding(): Flow<Boolean> = MutableStateFlow(true)

    override suspend fun toggleTheme(isDark: Boolean?) = Unit

    override suspend fun changeLanguage(languageCode: String) = Unit

    override suspend fun completeOnboarding() = Unit

    override fun isUserLoggedIn(): Flow<Boolean> = loggedIn.asStateFlow()

    override fun isGuest(): Flow<Boolean> = guest.asStateFlow()

    override suspend fun enterGuestMode() {
        enterGuestCalls += 1
        guest.value = true
        loggedIn.value = false
    }
}
