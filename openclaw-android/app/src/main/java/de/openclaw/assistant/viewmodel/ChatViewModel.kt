package de.openclaw.assistant.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import de.openclaw.assistant.data.api.ApiClient
import de.openclaw.assistant.data.model.*
import de.openclaw.assistant.voice.SpeechToTextManager
import de.openclaw.assistant.voice.TextToSpeechManager
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ChatViewModel(app: Application) : AndroidViewModel(app) {

    val stt = SpeechToTextManager(app)
    val tts = TextToSpeechManager(app)

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    fun startListening() {
        stt.startListening("de-DE")
    }

    fun stopListening() {
        stt.stopListening()
    }

    init {
        // Wenn STT ein Ergebnis liefert → automatisch senden
        viewModelScope.launch {
            stt.state.collect { state ->
                if (state is SpeechToTextManager.STTState.Result) {
                    sendMessage(state.text)
                }
            }
        }
    }

    fun sendMessage(text: String) {
        if (text.isBlank()) return

        // User-Nachricht hinzufügen
        val userMsg = ChatMessage(
            id = "user_${System.currentTimeMillis()}",
            role = "user",
            content = text
        )
        _messages.value = _messages.value + userMsg
        _isLoading.value = true

        viewModelScope.launch {
            try {
                val response = ApiClient.api.sendMessage(
                    ChatRequest(message = text)
                )
                if (response.isSuccessful) {
                    val body = response.body()!!
                    val assistantMsg = ChatMessage(
                        id = body.id,
                        role = "assistant",
                        content = body.content,
                        model = body.model
                    )
                    _messages.value = _messages.value + assistantMsg

                    // Antwort vorlesen
                    tts.speak(body.content)
                } else {
                    handleError("API Error: ${response.code()}")
                }
            } catch (e: Exception) {
                handleError("Network Error: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun handleError(msg: String) {
        val errMsg = ChatMessage(
            id = "err_${System.currentTimeMillis()}",
            role = "assistant",
            content = "Fehler: $msg"
        )
        _messages.value = _messages.value + errMsg
    }

    override fun onCleared() {
        stt.destroy()
        tts.destroy()
    }
}
