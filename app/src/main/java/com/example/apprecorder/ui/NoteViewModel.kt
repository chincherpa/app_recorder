package com.example.apprecorder.ui

import android.app.Application
import android.speech.SpeechRecognizer
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.apprecorder.data.remote.NoteRepository
import com.example.apprecorder.speech.SpeechRecognizerManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel for the HomeScreen.
 *
 * Uses [AndroidViewModel] to hold [Application] context — required by [SpeechRecognizerManager]
 * because [SpeechRecognizer] needs a Context but must not hold an Activity reference.
 *
 * Threading rules:
 * - [SpeechRecognizerManager] must be created and used on the **main thread**.
 *   Property initializers run on the thread that constructs the ViewModel, which is always
 *   the main thread when using `viewModel()` from Compose.
 * - [startRecording] explicitly dispatches to [Dispatchers.Main] to satisfy SpeechRecognizer.
 * - Network calls ([loadNotes], [onFinalResult]) dispatch to [Dispatchers.IO].
 */
class NoteViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = NoteRepository()

    private val _uiState = MutableStateFlow(NoteUiState())
    val uiState: StateFlow<NoteUiState> = _uiState.asStateFlow()

    // Created on the main thread (ViewModel construction always happens there via viewModel()).
    private val speechManager = SpeechRecognizerManager(
        context = application,
        onPartial = { text ->
            _uiState.update { it.copy(partialTranscription = text) }
        },
        onResult = { text ->
            onFinalResult(text)
        },
        onError = { errorCode ->
            _uiState.update {
                it.copy(
                    isRecording = false,
                    partialTranscription = "",
                    // ERROR_NO_MATCH (7) just means silence — not a real error.
                    error = if (errorCode == SpeechRecognizer.ERROR_NO_MATCH) null
                            else "Spracherkennungsfehler (Code: $errorCode)"
                )
            }
        }
    )

    init {
        loadNotes()
    }

    /**
     * Starts a new recording session.
     * Runs on the main thread so [SpeechRecognizer.startListening] is thread-safe.
     */
    fun startRecording() {
        viewModelScope.launch(Dispatchers.Main) {
            _uiState.update { it.copy(isRecording = true, partialTranscription = "", error = null) }
            speechManager.start()
        }
    }

    /**
     * Stops the active recording session.
     * [SpeechRecognizer.stopListening] finalises the current audio and fires [onResult].
     */
    fun stopRecording() {
        viewModelScope.launch(Dispatchers.Main) {
            speechManager.stop()
        }
    }

    /** Fetches all notes from Supabase and updates the UI state. */
    fun loadNotes() {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val notes = repository.fetchNotes()
                _uiState.update { it.copy(notes = notes, isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = "Ladefehler: ${e.message}") }
            }
        }
    }

    /**
     * Called by [SpeechRecognizerManager] when the final transcription is ready.
     * Saves to Supabase, then refreshes the notes list.
     */
    private fun onFinalResult(text: String) {
        if (text.isBlank()) {
            _uiState.update { it.copy(isRecording = false, partialTranscription = "") }
            return
        }
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update { it.copy(isRecording = false, partialTranscription = "") }
            try {
                repository.saveNote(text)
                loadNotes()
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Fehler beim Speichern: ${e.message}") }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        // SpeechRecognizer must be destroyed on the main thread.
        viewModelScope.launch(Dispatchers.Main) {
            speechManager.destroy()
        }
    }
}
