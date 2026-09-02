package com.task.hotelhop.data.datasource.user

import com.task.hotelhop.data.util.safeCall
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.UserProfileChangeRequest
import com.task.hotelhop.data.mapper.mapFirebaseUserToDomain
import com.task.hotelhop.domain.entity.User
import com.task.hotelhop.domain.exception.AppException
import kotlinx.coroutines.tasks.await


class RemoteUserDataSourceImpl(
    private val firebaseAuth: FirebaseAuth
) : RemoteUserDataSource {

    override suspend fun loginWithEmail(email: String, password: String): Result<User> {
        return safeCall {
            try {
                val result = firebaseAuth.signInWithEmailAndPassword(email, password).await()
                mapFirebaseUserToDomain(result.user)
            } catch (e: FirebaseAuthInvalidCredentialsException) {
                throw AppException.InvalidCredentialsException()
            }
        }
    }

    override suspend fun signUpWithEmail(email: String, password: String, displayName: String): Result<User> {
        return safeCall {
            try {
                val result = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
                val firebaseUser = result.user ?: throw AppException.UnknownException()
                firebaseUser.updateProfile(
                    UserProfileChangeRequest.Builder()
                        .setDisplayName(displayName)
                        .build()
                ).await()
                firebaseUser.reload().await()
                mapFirebaseUserToDomain(firebaseAuth.currentUser, fallbackName = displayName)
            } catch (e: FirebaseAuthUserCollisionException) {
                throw AppException.UserAlreadyExistsException()
            }
        }
    }

    override suspend fun signInWithGoogle(idToken: String): Result<User> {
        return safeCall {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val result = firebaseAuth.signInWithCredential(credential).await()
            mapFirebaseUserToDomain(result.user)
        }
    }

    override suspend fun logout(): Result<Unit> {
        return safeCall { firebaseAuth.signOut() }
    }

    override suspend fun getCurrentUser(): User? {
        return firebaseAuth.currentUser?.let { mapFirebaseUserToDomain(it) }
    }


}