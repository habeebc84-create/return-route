package com.example.auth

import android.app.Activity
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
import com.google.firebase.auth.ActionCodeSettings
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.android.gms.tasks.Task
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.concurrent.TimeUnit
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
    private val authState = appContext.getSharedPreferences("return_route_auth", Context.MODE_PRIVATE)

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

    suspend fun signInWithEmailPassword(email: String, password: String): AuthenticatedAccount {
        val auth = firebaseAuthOrThrow()
        val user = auth
            .signInWithEmailAndPassword(email.trim(), password)
            .awaitResult()
            .user ?: throw IllegalStateException("Email sign-in completed without a user account.")
        if (!user.isEmailVerified) {
            auth.signOut()
            throw IllegalStateException("Verify your email address using the link we sent before signing in.")
        }
        return user.toAuthenticatedAccount()
    }

    suspend fun createEmailPasswordAccount(email: String, password: String) {
        val auth = firebaseAuthOrThrow()
        val user = auth
            .createUserWithEmailAndPassword(email.trim(), password)
            .awaitResult()
            .user ?: throw IllegalStateException("Account creation completed without a user account.")
        try {
            user.sendEmailVerification().awaitResult()
        } finally {
            auth.signOut()
        }
    }

    suspend fun sendPasswordResetEmail(email: String) {
        firebaseAuthOrThrow().sendPasswordResetEmail(email.trim()).awaitResult()
    }

    fun sendPhoneVerification(
        activity: Activity,
        phoneNumber: String,
        onCodeSent: (verificationId: String) -> Unit,
        onAutoVerified: (PhoneAuthCredential) -> Unit,
        onError: (Exception) -> Unit
    ) {
        val auth = firebaseAuthOrThrow()
        val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                onAutoVerified(credential)
            }

            override fun onVerificationFailed(error: com.google.firebase.FirebaseException) {
                onError(error)
            }

            override fun onCodeSent(
                verificationId: String,
                token: PhoneAuthProvider.ForceResendingToken
            ) {
                onCodeSent(verificationId)
            }
        }
        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phoneNumber.trim())
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(activity)
            .setCallbacks(callbacks)
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    suspend fun signInWithPhoneCode(verificationId: String, code: String): AuthenticatedAccount {
        val credential = PhoneAuthProvider.getCredential(verificationId, code.trim())
        return signInWithPhoneCredential(credential)
    }

    suspend fun signInWithPhoneCredential(credential: PhoneAuthCredential): AuthenticatedAccount {
        val user = firebaseAuthOrThrow().signInWithCredential(credential).awaitResult().user
            ?: throw IllegalStateException("Phone verification completed without a user account.")
        return user.toAuthenticatedAccount()
    }

    suspend fun sendEmailSignInLink(email: String) {
        val auth = firebaseAuthOrThrow()
        val projectId = FirebaseApp.getInstance().options.projectId
            ?: throw IllegalStateException("The Firebase project ID is missing from google-services.json.")
        val settings = ActionCodeSettings.newBuilder()
            .setUrl("https://$projectId.firebaseapp.com/finishSignIn")
            .setHandleCodeInApp(true)
            .setAndroidPackageName(appContext.packageName, true, "1")
            .build()
        auth.sendSignInLinkToEmail(email.trim(), settings).awaitResult()
        authState.edit().putString(KEY_PENDING_EMAIL, email.trim()).apply()
    }

    fun pendingEmailForLink(): String? = authState.getString(KEY_PENDING_EMAIL, null)

    suspend fun signInWithEmailLink(email: String, link: String): AuthenticatedAccount {
        val auth = firebaseAuthOrThrow()
        if (!auth.isSignInWithEmailLink(link)) {
            throw IllegalArgumentException("This link is not a valid ReturnRoute email sign-in link.")
        }
        val user = auth.signInWithEmailLink(email.trim(), link).awaitResult().user
            ?: throw IllegalStateException("Email-link sign-in completed without a user account.")
        authState.edit().remove(KEY_PENDING_EMAIL).apply()
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
                ?: phoneNumber?.takeIf { it.isNotBlank() }
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

    private companion object {
        const val KEY_PENDING_EMAIL = "pending_email_link_email"
    }
}
