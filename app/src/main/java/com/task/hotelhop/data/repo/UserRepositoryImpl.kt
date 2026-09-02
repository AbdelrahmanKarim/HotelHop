package com.task.hotelhop.data.repo


import com.task.hotelhop.data.datasource.user.LocalUserDataSource
import com.task.hotelhop.data.datasource.user.RemoteUserDataSource
import com.task.hotelhop.domain.entity.User
import com.task.hotelhop.domain.repo.UserRepository
import kotlinx.coroutines.flow.Flow

class UserRepositoryImpl(
    private val remoteUser: RemoteUserDataSource,
    private val localUser: LocalUserDataSource
) : UserRepository {

    override suspend fun loginWithEmail(email: String, password: String): User {
        val user = remoteUser.loginWithEmail(email, password).getOrThrow()
        localUser.setUserLoggedIn(true)
        localUser.saveUser(user)
        return user
    }

    override suspend fun signUpWithEmail(firstName: String, lastName: String, email: String, password: String, gender: String): User {
        val displayName = "$firstName $lastName".trim()
        val remote = remoteUser.signUpWithEmail(email, password, displayName).getOrThrow()
        val user = remote.copy(firstName = firstName, lastName = lastName, gender = gender)
        localUser.setUserLoggedIn(true)
        localUser.saveUser(user)
        return user
    }

    override suspend fun signInWithGoogle(idToken: String): User {
        val user = remoteUser.signInWithGoogle(idToken).getOrThrow()
        localUser.setUserLoggedIn(true)
        localUser.saveUser(user)
        return user
    }

    override suspend fun logout() {
        remoteUser.logout().getOrThrow()
        localUser.setUserLoggedIn(false)
        localUser.clearUser()
    }

    override fun isUserLoggedIn(): Flow<Boolean> = localUser.isUserLoggedIn()

    override suspend fun getCurrentUser(): User? {
        val cached = localUser.getCachedUser()
        val remote = runCatching { remoteUser.getCurrentUser() }.getOrNull()
        return when {
            cached != null && cached.firstName.isNotBlank() -> cached
            remote != null -> {
                localUser.saveUser(remote)
                remote
            }
            else -> cached
        }
    }

    override fun isDarkMode(): Flow<Boolean?> = localUser.isDarkMode()

    override fun getLanguage(): Flow<String> = localUser.getLanguage()

    override fun hasSeenOnboarding(): Flow<Boolean> = localUser.hasSeenOnboarding()

    override suspend fun toggleTheme(isDark: Boolean?) {
        localUser.toggleTheme(isDark)
    }

    override suspend fun changeLanguage(languageCode: String) {
        localUser.changeLanguage(languageCode)
    }

    override suspend fun completeOnboarding() {
        localUser.completeOnboarding()
    }
}