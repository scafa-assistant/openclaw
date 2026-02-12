package de.openclaw.assistant.voice

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class SpeechToTextManager(private val context: Context) {

    private var recognizer: SpeechRecognizer? = null

    private val _state = MutableStateFlow<STTState>(STTState.Idle)
    val state: StateFlow<STTState> = _state

    private val _text = MutableStateFlow("")
    val text: StateFlow<String> = _text

    sealed class STTState {
        object Idle : STTState()
        object Listening : STTState()
        object Processing : STTState()
        data class Result(val text: String) : STTState()
        data class Error(val message: String) : STTState()
    }

    fun startListening(language: String = "de-DE") {
        recognizer?.destroy()
        recognizer = SpeechRecognizer.createSpeechRecognizer(context)

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        intent.putExtra(
            RecognizerIntent.EXTRA_LANGUAGE_MODEL,
            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
        )
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, language)
        intent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
        intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)

        recognizer?.setRecognitionListener(
            object : RecognitionListener {
                override fun onReadyForSpeech(params: Bundle?) {
                    _state.value = STTState.Listening
                }
                override fun onBeginningOfSpeech() {}
                override fun onRmsChanged(rmsdB: Float) {}
                override fun onBufferReceived(buffer: ByteArray?) {}
                override fun onEndOfSpeech() {
                    _state.value = STTState.Processing
                }
                override fun onError(error: Int) {
                    _state.value = STTState.Error("STT Error: $error")
                }
                override fun onResults(results: Bundle?) {
                    val matches = results?.getStringArrayList(
                        SpeechRecognizer.RESULTS_RECOGNITION
                    )
                    val result = matches?.firstOrNull() ?: ""
                    _text.value = result
                    _state.value = STTState.Result(result)
                }
                override fun onPartialResults(partial: Bundle?) {
                    val matches = partial?.getStringArrayList(
                        SpeechRecognizer.RESULTS_RECOGNITION
                    )
                    _text.value = matches?.firstOrNull() ?: ""
                }
                override fun onEvent(type: Int, params: Bundle?) {}
            }
        )
        recognizer?.startListening(intent)
    }

    fun stopListening() {
        recognizer?.stopListening()
        _state.value = STTState.Idle
    }

    fun destroy() {
        recognizer?.destroy()
        recognizer = null
    }
}
