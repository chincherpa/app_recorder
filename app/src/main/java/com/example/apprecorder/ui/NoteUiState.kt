package com.example.apprecorder.ui

import com.example.apprecorder.data.model.Note

/**
 * Immutable snapshot of the HomeScreen UI state.
 * Collected via [kotlinx.coroutines.flow.StateFlow] in the composable.
 */
data class NoteUiState(
    /** All saved notes, ordered newest-first. */
    val notes: List<Note> = emptyList(),

    /** True while the SpeechRecognizer is actively listening. */
    val isRecording: Boolean = false,

    /** Live partial transcription shown below the record button. Empty when not recording. */
    val partialTranscription: String = "",

    /** True while notes are being fetched from Supabase. */
    val isLoading: Boolean = false,

    /** Non-null when an error occurred (network, speech, etc.). */
    val error: String? = null
)
