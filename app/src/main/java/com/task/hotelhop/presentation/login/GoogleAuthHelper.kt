package com.task.hotelhop.presentation.login

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.task.hotelhop.BuildConfig
import java.security.MessageDigest
import java.util.UUID

object GoogleAuthHelper {

    suspend fun requestIdToken(activityContext: Context): String {
        val serverClientId = BuildConfig.GOOGLE_WEB_CLIENT_ID
        require(serverClientId.isNotBlank()) { "missing_web_client" }

        val credentialManager = CredentialManager.create(activityContext)
        val nonce = sha256(UUID.randomUUID().toString())

        val result = runCatching {
            credentialManager.getCredential(
                context = activityContext,
                request = GetCredentialRequest.Builder()
                    .addCredentialOption(
                        GetSignInWithGoogleOption.Builder(serverClientId)
                            .setNonce(nonce)
                            .build()
                    )
                    .build()
            )
        }.recoverCatching {
            credentialManager.getCredential(
                context = activityContext,
                request = GetCredentialRequest.Builder()
                    .addCredentialOption(
                        GetGoogleIdOption.Builder()
                            .setFilterByAuthorizedAccounts(false)
                            .setServerClientId(serverClientId)
                            .setAutoSelectEnabled(false)
                            .setNonce(nonce)
                            .build()
                    )
                    .build()
            )
        }.getOrThrow()

        return extractIdToken(result)
    }

    private fun extractIdToken(result: GetCredentialResponse): String {
        val credential = result.credential
        if (credential is CustomCredential &&
            credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
        ) {
            return GoogleIdTokenCredential.createFrom(credential.data).idToken
        }
        error("unsupported_credential")
    }

    private fun sha256(value: String): String {
        val digest = MessageDigest.getInstance("SHA-256").digest(value.toByteArray())
        return digest.joinToString("") { "%02x".format(it) }
    }
}
