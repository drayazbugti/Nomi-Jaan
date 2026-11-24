package com.example.data

import android.content.Context
import com.example.R
import com.example.model.CloudRecord
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class CloudBackendRepository(
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth = Firebase.auth
) {
    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    private fun requireUserId(): String {
        return auth.currentUser?.uid
            ?: throw IllegalStateException("User must be signed in with Google before accessing Firestore.")
    }

    fun observeRecords(userId: String): Flow<List<CloudRecord>> = flow {
        val uid = requireUserId()
        check(uid == userId) { "Signed-in user changed; refusing cross-user read." }
        val path = "users/$uid/cloud_records"
        emitAll(
            db.collection("users")
                .document(uid)
                .collection("cloud_records")
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(
                        CloudRecord::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    ).sortedByDescending { it.createdAt ?: Timestamp(0, 0) }
                }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    val exceptionToLog = firestoreError ?: (error as? Exception)
                    if (exceptionToLog != null && auth.currentUser?.uid == uid) {
                        handleFirestoreError(exceptionToLog, OperationType.LIST, path)
                    }
                    throw error
                }
        )
    }

    suspend fun saveRecord(
        title: String,
        category: String,
        payload: String
    ): Result<Unit> {
        val cleanTitle = title.trim()
        val cleanCategory = category.trim().ifEmpty { "General" }
        val cleanPayload = payload.trim()

        if (cleanTitle.isEmpty() || cleanTitle.length > 120) {
            return Result.failure(IllegalArgumentException("Title must be 1 to 120 characters."))
        }
        if (cleanCategory.length > 60) {
            return Result.failure(IllegalArgumentException("Category must be under 60 characters."))
        }
        if (cleanPayload.isEmpty() || cleanPayload.length > 4000) {
            return Result.failure(IllegalArgumentException("Payload must be 1 to 4000 characters."))
        }

        var docPath = "users/${auth.currentUser?.uid ?: "unauthenticated"}/cloud_records"
        return try {
            val uid = requireUserId()
            val docRef = db.collection("users")
                .document(uid)
                .collection("cloud_records")
                .document()
            docPath = docRef.path

            val writeMap = mapOf(
                "userId" to uid,
                "title" to cleanTitle,
                "category" to cleanCategory,
                "payload" to cleanPayload,
                "appTag" to "Bugti Editz",
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            docRef.set(writeMap).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docPath)
            Result.failure(e)
        }
    }

    suspend fun updateRecord(
        recordId: String,
        title: String,
        category: String,
        payload: String
    ): Result<Unit> {
        val cleanTitle = title.trim()
        val cleanCategory = category.trim().ifEmpty { "General" }
        val cleanPayload = payload.trim()
        if (recordId.isBlank()) {
            return Result.failure(IllegalArgumentException("Record ID cannot be blank."))
        }
        if (cleanTitle.isEmpty() || cleanTitle.length > 120 ||
            cleanCategory.length > 60 ||
            cleanPayload.isEmpty() || cleanPayload.length > 4000
        ) {
            return Result.failure(IllegalArgumentException("Record fields exceed allowed bounds."))
        }

        var docPath = "users/${auth.currentUser?.uid ?: "unauthenticated"}/cloud_records/$recordId"
        return try {
            val uid = requireUserId()
            val docRef = db.collection("users")
                .document(uid)
                .collection("cloud_records")
                .document(recordId)
            docPath = docRef.path

            docRef.update(
                mapOf(
                    "title" to cleanTitle,
                    "category" to cleanCategory,
                    "payload" to cleanPayload,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docPath)
            Result.failure(e)
        }
    }

    suspend fun deleteRecord(recordId: String): Result<Unit> {
        if (recordId.isBlank()) {
            return Result.failure(IllegalArgumentException("Record ID cannot be blank."))
        }
        var docPath = "users/${auth.currentUser?.uid ?: "unauthenticated"}/cloud_records/$recordId"
        return try {
            val uid = requireUserId()
            val docRef = db.collection("users")
                .document(uid)
                .collection("cloud_records")
                .document(recordId)
            docPath = docRef.path
            docRef.delete().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docPath)
            Result.failure(e)
        }
    }
}
