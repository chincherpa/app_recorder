package com.example.apprecorder.speech

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import java.util.Locale

/**
 * Thin wrapper around Android's [SpeechRecognizer] API.
 *
 * IMPORTANT: All methods ([start], [destroy]) — and the constructor itself — must be called
 * on the **main thread**, since [SpeechRecognizer] is not thread-safe.
 * The [NoteViewModel] ensures this via [kotlinx.coroutines.Dispatchers.Main].
 *
 * @param onPartial  Called with the current partial recognition result (live update).
 * @param onResult   Called once with the final recognised text when the user stops speaking.
 * @param onError    Called with a [SpeechRecognizer] error code on failure.
 */
class SpeechRecognizerManager(
    context: Context,
    private val onPartial: (String) -> Unit,
    private val onResult: (String) -> Unit,
    private val onError: (Int) -> Unit
) {
    private val recognizer: SpeechRecognizer =
        SpeechRecognizer.createSpeechRecognizer(context)

    init {
        recognizer.setRecognitionListener(object : RecognitionListener {

            override fun onReadyForSpeech(params: Bundle?) = Unit
            override fun onBeginningOfSpeech() = Unit
            override fun onRmsChanged(rmsdB: Float) = Unit
            override fun onBufferReceived(buffer: ByteArray?) = Unit
            override fun onEndOfSpeech() = Unit
            override fun onEvent(eventType: Int, params: Bundle?) = Unit

            override fun onPartialResults(partialResults: Bundle?) {
                val text = partialResults
                    ?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    ?.firstOrNull()
                    ?: return
                onPartial(text)
            }

            override fun onResults(results: Bundle?) {
                val text = results
                    ?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    ?.firstOrNull()
                    ?: return
                onResult(text)
            }

            override fun onError(error: Int) {
                this@SpeechRecognizerManager.onError(error)
            }
        })
    }

    /** Starts listening. Must be called on the main thread. */
    fun start() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault().toLanguageTag())

        }
        recognizer.startListening(intent)
    }

    /**
     * Stops listening and submits captured audio for recognition.
     * This triggers [onResult] with the best match so far.
     * Must be called on the main thread.
     */
    fun stop() {
        recognizer.stopListening()
    }

    /** Releases resources. Must be called on the main thread (done in ViewModel.onCleared). */
    fun destroy() {
        recognizer.destroy()
    }
}
