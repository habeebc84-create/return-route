package com.example.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.functions.FirebaseFunctions
import com.google.android.gms.tasks.Task
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException


data class RiderAccessRequest(
    val uid: String,
    val displayName: String,
    val email: String,
    val note: String,
    val status: String,
    val createdAt: Long
)

/** Firebase-backed workflow. Custom claims are only changed by trusted callable functions. */
class RiderAccessRepository {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private val functions = FirebaseFunctions.getInstance("asia-south1")

    suspend fun requestRiderAccess(note: String) {
        val user = auth.currentUser ?: error("Sign in before requesting Rider access.")
        functions.getHttpsCallable("requestRiderAccess").call(
            mapOf("note" to note.trim().take(500))
        ).awaitResult()
    }

    fun listenPendingRequests(
        onUpdate: (List<RiderAccessRequest>) -> Unit,
        onError: (Exception) -> Unit
    ): ListenerRegistration {
        return firestore.collection("riderAccessRequests")
            .whereEqualTo("status", "pending")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    onError(error)
                    return@addSnapshotListener
                }
                val requests = snapshot?.documents.orEmpty().map { doc ->
                    RiderAccessRequest(
                        uid = doc.getString("uid") ?: doc.id,
                        displayName = doc.getString("displayName") ?: "ReturnRoute user",
                        email = doc.getString("email").orEmpty(),
                        note = doc.getString("note").orEmpty(),
                        status = doc.getString("status") ?: "pending",
                        createdAt = doc.getTimestamp("createdAt")?.toDate()?.time
                            ?: doc.getLong("createdAt")
                            ?: 0L
                    )
                }.sortedByDescending { it.createdAt }
                onUpdate(requests)
            }
    }

    fun listenUserRequestStatus(uid: String, onUpdate: (String?) -> Unit): ListenerRegistration {
        return firestore.collection("riderAccessRequests").document(uid)
            .addSnapshotListener { snapshot, _ ->
                onUpdate(if (snapshot?.exists() == true) snapshot.getString("status") else null)
            }
    }

    suspend fun reviewRequest(uid: String, approved: Boolean, note: String = "") {
        check(auth.currentUser != null) { "Sign in as an administrator before reviewing requests." }
        functions.getHttpsCallable("reviewRiderAccess").call(
            mapOf("uid" to uid, "approved" to approved, "note" to note.trim().take(500))
        ).awaitResult()
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
