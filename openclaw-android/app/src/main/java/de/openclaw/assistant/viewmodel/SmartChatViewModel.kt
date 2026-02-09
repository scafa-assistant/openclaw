package de.openclaw.assistant.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import de.openclaw.assistant.data.api.OpenClawApi
import de.openclaw.assistant.data.local.SettingsDataStore
import de.openclaw.assistant.router.HybridRouter
import de.openclaw.assistant.intent.IntentClassifier
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * Chat ViewModel mit Hybrid Router
 * Intelligentes Routing zwischen lokal, extern und SWARM
 */
class SmartChatViewModel(
    private val router: HybridRouter,
    private val settings: SettingsDataStore
) : ViewModel() {

    private val _messages = MutableStateFlow<List<SmartMessage>>(emptyList())
    val messages: StateFlow<List<SmartMessage>> = _messages

    private val _isProcessing = MutableStateFlow(false)
    val isProcessing: StateFlow<Boolean> = _isProcessing

    private val _currentSource = MutableStateFlow<String>("")
    val currentSource: StateFlow<String> = _currentSource

    /**
     * Nachricht mit Hybrid-Verarbeitung senden
     */
    fun sendMessage(text: String) {
        viewModelScope.launch {
            addMessage(SmartMessage.User(text))
            _isProcessing.value = true
            
            try {
                val result = router.processInput(text)
                _currentSource.value = router.getSourceDescription(result)
                
                when (result) {
                    is HybridRouter.RoutingResult.LocalAction -> {
                        addMessage(SmartMessage.Assistant(
                            content = result.description,
                            source = "System",
                            cost = "Kostenlos"
                        ))
                    }
                    is HybridRouter.RoutingResult.AIResponse -> {
                        addMessage(SmartMessage.Assistant(
                            content = result.response,
                            source = result.model,
                            cost = router.getCostEstimate(result)
                        ))
                    }
                    is HybridRouter.RoutingResult.VoiceOnly -> {
                        addMessage(SmartMessage.Assistant(
                            content = "Voice-Antwort: " + result.prompt,
                            source = "Voice",
                            cost = "Kostenlos"
                        ))
                    }
                    is HybridRouter.RoutingResult.SwarmDelegated -> {
                        addMessage(SmartMessage.Assistant(
                            content = "Task an " + result.agent.uppercase() + " delegiert",
                            source = "SWARM",
                            cost = "SWARM"
                        ))
                    }
                    is HybridRouter.RoutingResult.Error -> {
                        addMessage(SmartMessage.Error(result.message))
                    }
                }
            } catch (e: Exception) {
                addMessage(SmartMessage.Error("Fehler: " + e.message))
            } finally {
                _isProcessing.value = false
            }
        }
    }

    fun startVoiceInput() {
        addMessage(SmartMessage.System("Spracheingabe aktiviert..."))
    }

    fun executeLocalCommand(command: String) {
        viewModelScope.launch {
            val intent = IntentClassifier().classify(command)
            val result = IntentClassifier().execute(android.app.Application(), intent)
            
            when (result) {
                is IntentClassifier.ExecutionResult.Success -> {
                    addMessage(SmartMessage.System(result.message))
                }
                is IntentClassifier.ExecutionResult.Error -> {
                    addMessage(SmartMessage.Error(result.message))
                }
                else -> {
                    sendMessage(command)
                }
            }
        }
    }

    private fun addMessage(message: SmartMessage) {
        _messages.value = _messages.value + message
    }

    fun clearChat() {
        _messages.value = emptyList()
    }

    sealed class SmartMessage {
        abstract val content: String
        abstract val timestamp: Long

        data class User(
            override val content: String,
            override val timestamp: Long = java.lang.System.currentTimeMillis()
        ) : SmartMessage()

        data class Assistant(
            override val content: String,
            val source: String,
            val cost: String,
            override val timestamp: Long = java.lang.System.currentTimeMillis()
        ) : SmartMessage()

        data class System(
            override val content: String,
            override val timestamp: Long = java.lang.System.currentTimeMillis()
        ) : SmartMessage()

        data class Error(
            override val content: String,
            override val timestamp: Long = java.lang.System.currentTimeMillis()
        ) : SmartMessage()
    }

    class Factory(
        private val context: Context,
        private val api: OpenClawApi,
        private val settings: SettingsDataStore
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            val router = HybridRouter(context, api, settings)
            return SmartChatViewModel(router, settings) as T
        }
    }
}
