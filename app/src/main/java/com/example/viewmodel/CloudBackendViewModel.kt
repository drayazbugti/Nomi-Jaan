package com.example.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.CloudBackendRepository
import com.example.model.CloudRecord
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface CloudRecordsUiState {
    data object Loading : CloudRecordsUiState
    data class Success(val records: List<CloudRecord>) : CloudRecordsUiState
    data class Error(val message: String) : CloudRecordsUiState
}

class CloudBackendViewModel(
    private val repository: CloudBackendRepository,
    private val currentUserId: String
) : ViewModel() {

    val recordsState: StateFlow<CloudRecordsUiState> = repository.observeRecords(currentUserId)
        .map<List<CloudRecord>, CloudRecordsUiState> { CloudRecordsUiState.Success(it) }
        .catch { error ->
            val firestoreError = generateSequence(error) { it.cause }
                .filterIsInstance<FirebaseFirestoreException>()
                .firstOrNull()
            if (firestoreError == null && error is CancellationException) throw error
            val rootError = firestoreError ?: error
            Log.w(TAG, "Error observing cloud records", rootError)
            emit(CloudRecordsUiState.Error(rootError.localizedMessage ?: "Failed to sync cloud records"))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = CloudRecordsUiState.Loading
        )

    private val _actionStatus = MutableStateFlow<String?>(null)
    val actionStatus: StateFlow<String?> = _actionStatus.asStateFlow()

    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving.asStateFlow()

    fun saveRecord(title: String, category: String, payload: String, onSaved: () -> Unit = {}) {
        viewModelScope.launch {
            _isSaving.value = true
            _actionStatus.value = "Saving record to Cloud Firestore..."
            val result = repository.saveRecord(title, category, payload)
            _isSaving.value = false
            result.fold(
                onSuccess = {
                    _actionStatus.value = "Saved to online Cloud Firestore!"
                    onSaved()
                },
                onFailure = { err ->
                    _actionStatus.value = err.localizedMessage ?: "Failed to save record"
                }
            )
        }
    }

    fun deleteRecord(recordId: String) {
        viewModelScope.launch {
            val result = repository.deleteRecord(recordId)
            result.fold(
                onSuccess = { _actionStatus.value = "Record deleted from cloud." },
                onFailure = { err -> _actionStatus.value = err.localizedMessage ?: "Failed to delete record" }
            )
        }
    }

    fun uploadDrafts(
        drafts: List<com.example.model.SavedCreation>,
        onCompleted: (uploadedIds: Set<String>, message: String) -> Unit
    ) {
        if (drafts.isEmpty()) return
        viewModelScope.launch {
            _isSaving.value = true
            val uploaded = mutableSetOf<String>()
            var lastError: String? = null
            drafts.forEach { draft ->
                val payload = "Mode: ${draft.modeLabel} | Resolution: ${draft.resolutionLabel} | Type: ${if (draft.isVideo) "Video" else "Photo"}"
                val result = repository.saveRecord(
                    title = draft.title,
                    category = "Studio Draft",
                    payload = payload
                )
                result.fold(
                    onSuccess = { uploaded.add(draft.id) },
                    onFailure = { err -> lastError = err.localizedMessage }
                )
            }
            _isSaving.value = false
            val msg = if (uploaded.isNotEmpty()) {
                _actionStatus.value = "Uploaded ${uploaded.size} draft(s) to Cloud Firestore!"
                "Uploaded ${uploaded.size} draft(s) to Cloud"
            } else {
                val errMsg = lastError ?: "Cloud upload failed"
                _actionStatus.value = errMsg
                errMsg
            }
            onCompleted(uploaded, msg)
        }
    }

    fun reportSyncNotice(count: Int) {
        _actionStatus.value = "Synced $count record(s) from online database."
    }

    private companion object {
        const val TAG = "CloudBackendVM"
    }
}
