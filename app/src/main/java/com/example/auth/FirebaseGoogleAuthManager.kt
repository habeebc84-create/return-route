package com.example.auth

import android.content.Context
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.ClearCredentialException
import com.example.data.model.UserRole
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.android.gms.tasks.Task
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

data class AuthenticatedAccount(
    val uid: String,
    val displayName: String,
    val email: String,
    val emailVerified: Boolean,
    val role: UserRole
)

class FirebaseGoogleAuthManager(context: Context) {
    private val appContext = context.applicationContext
    private val credentialManager = CredentialManager.create(appContext)

    suspend fun signIn(activityContext: Context): AuthenticatedAccount {
        val auth = firebaseAuthOrThrow()
        val webClientId = appContext.resources.getIdentifier(
            "default_web_client_id",
            "string",
            appContext.packageName
        ).takeIf { it != 0 }?.let(appContext::getString)
            ?: throw IllegalStateException(
                "Firebase Google sign-in is not configured. Add this Android app's google-services.json and enable Google sign-in in Firebase Console."
            )

        val googleOption = GetGoogleIdOption.Builder()
            .setServerClientId(webClientId)
            .setFilterByAuthorizedAccounts(false)
            .setAutoSelectEnabled(false)
            .build()
        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleOption)
            .build()

        val response = credentialManager.getCredential(activityContext, request)
        val credential = response.credential as? CustomCredential
            ?: throw IllegalStateException("Google did not return a supported sign-in credential.")
        if (credential.type != GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
            throw IllegalStateException("Google did not return an ID token credential.")
        }

        val googleToken = GoogleIdTokenCredential.createFrom(credential.data).idToken
        val firebaseCredential = GoogleAuthProvider.getCredential(googleToken, null)
        val user = auth.signInWithCredential(firebaseCredential).awaitResult().user
            ?: throw IllegalStateException("Firebase sign-in completed without a user account.")
        return user.toAuthenticatedAccount()
    }

    suspend fun restoreSignedInAccount(): AuthenticatedAccount? {
        val app = FirebaseApp.getApps(appContext).firstOrNull()
            ?: FirebaseApp.initializeApp(appContext)
            ?: return null
        val user = FirebaseAuth.getInstance(app).currentUser ?: return null
        return user.toAuthenticatedAccount()
    }

    suspend fun signOut() {
        firebaseAppOrNull()?.let { FirebaseAuth.getInstance(it).signOut() }
        try {
            credentialManager.clearCredentialState(ClearCredentialStateRequest())
        } catch (_: ClearCredentialException) {
            // Firebase is already signed out; a Credential Manager cleanup failure is non-fatal.
        }
    }

    private fun firebaseAuthOrThrow(): FirebaseAuth {
        val app = firebaseAppOrNull()
            ?: throw IllegalStateException(
                "Firebase is not configured for this app. Add google-services.json for application ID ${appContext.packageName}."
            )
        return FirebaseAuth.getInstance(app)
    }

    private fun firebaseAppOrNull(): FirebaseApp? =
        FirebaseApp.getApps(appContext).firstOrNull() ?: FirebaseApp.initializeApp(appContext)

    private suspend fun FirebaseUser.toAuthenticatedAccount(): AuthenticatedAccount {
        val claims = getIdToken(false).awaitResult().claims
        val role = when {
            claims["admin"] == true -> UserRole.ADMIN
            else -> when ((claims["role"] as? String)?.lowercase()) {
                "admin", "administrator" -> UserRole.ADMIN
                "rider", "driver" -> UserRole.DRIVER
                "delivery_provider" -> UserRole.DELIVERY_PROVIDER
                "parcel_sender" -> UserRole.PARCEL_SENDER
                else -> UserRole.PASSENGER
            }
        }
        return AuthenticatedAccount(
            uid = uid,
            displayName = displayName?.takeIf { it.isNotBlank() }
                ?: email?.substringBefore('@')?.takeIf { it.isNotBlank() }
                ?: "Passenger",
            email = email.orEmpty(),
            emailVerified = isEmailVerified,
            role = role
        )
    }

    private suspend fun <T> Task<T>.awaitResult(): T = suspendCancellableCoroutine { continuation ->
        addOnSuccessListener { result ->
            if (continuation.isActive) continuation.resume(result)
        }
        addOnFailureListener { error ->
            if (continuation.isActive) continuation.resumeWithException(error)
        }
        addOnCanceledListener {
            if (continuation.isActive) continuation.cancel()
        }
    }
}
