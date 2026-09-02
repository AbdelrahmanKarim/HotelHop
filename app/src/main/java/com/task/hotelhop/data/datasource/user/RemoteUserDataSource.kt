package com.task.hotelhop.data.datasource.user

import com.task.hotelhop.domain.entity.User

interface RemoteUserDataSource {
    suspend fun loginWithEmail(email: String, password: String): Result<User>
    suspend fun signUpWithEmail(email: String, password: String, displayName: String): Result<User>
    suspend fun signInWithGoogle(idToken: String): Result<User>
    suspend fun logout(): Result<Unit>
    suspend fun getCurrentUser(): User?
}