package de.openclaw.assistant.service

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.Locale

/**
 * Hybrid Voice Service
 * Nutzt Google Assistant für einfache Anfragen (kostenlos)
 * OpenClaw Backend nur für komplexe Anfragen
 */
class HybridVoiceService(
    private val activity: Activity,
    private val onQueryResult: (HybridResult) -> Unit
) {
    private val _voiceState = MutableStateFlow(VoiceState.IDLE)
    val voiceState: StateFlow<VoiceState> = _voiceState

    private var textToSpeech: TextToSpeech? = null
    private var speechRecognizer: SpeechRecognizer? = null

    enum class VoiceState {
        IDLE, LISTENING, PROCESSING, SPEAKING, ERROR
    }

    enum class ResultSource {
        BUILTIN_GOOGLE, // Kostenlos - Google Assistant
        EXTERNAL_AI     // Bezahlt - OpenClaw Backend
    }

    data class HybridResult(
        val source: ResultSource,
        val content: String?,
        val action: AssistantAction?,
        val estimatedCost: String
    )

    data class AssistantAction(
        val type: String,
        val data: Map<String, Any>
    )

    init {
        initTextToSpeech()
    }

    private fun initTextToSpeech() {
        textToSpeech = TextToSpeech(activity) { status ->
            if (status == TextToSpeech.SUCCESS) {
                textToSpeech?.language = Locale.GERMAN
            }
        }
    }

    /**
     * Startet Spracheingabe
     * Entscheidet automatisch: Google Assistant oder OpenClaw
     */
    fun startVoiceInput() {
        _voiceState.value = VoiceState.LISTENING

        // Option 1: Google Assistant direkt (für einfache Anfragen)
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.GERMAN)
            putExtra(RecognizerIntent.EXTRA_PROMPT, "Sprich zu OpenClaw...")
        }

        try {
            activity.startActivityForResult(intent, REQUEST_VOICE_INPUT)
        } catch (e: ActivityNotFoundException) {
            // Fallback zu lokalem SpeechRecognizer
            startLocalSpeechRecognition()
        }
    }

    /**
     * Verarbeitet die Spracheingabe
     * Entscheidet: Google Assistant oder externe AI
     */
    fun processVoiceInput(transcript: String) {
        _voiceState.value = VoiceState.PROCESSING

        // Check: Ist das eine einfache Anfrage für Google Assistant?
        when {
            isBuiltInQuery(transcript) -> {
                // Leite an Google Assistant weiter (KOSTENLOS)
                Log.d(TAG, "Routing to Google Assistant (FREE): $transcript")
                delegateToGoogleAssistant(transcript)
            }
            else -> {
                // Sende an OpenClaw Backend (mit Kosten)
                Log.d(TAG, "Routing to OpenClaw Backend: $transcript")
                onQueryResult(HybridResult(
                    source = ResultSource.EXTERNAL_AI,
                    content = null, // Wird vom Backend gefüllt
                    action = null,
                    estimatedCost = "~$0.003"
                ))
            }
        }
    }

    /**
     * Prüft ob Google Assistant die Anfrage beantworten kann
     */
    private fun isBuiltInQuery(query: String): Boolean {
        val lower = query.lowercase()

        val builtInPatterns = listOf(
            Regex("^(wie spät|wieviel uhr|uhrzeit)"),
            Regex("^(welcher tag|datum|wann ist heute)"),
            Regex("^(wetter|temperatur|regnet es)"),
            Regex("^(timer|wecker|erinner mich|termin)"),
            Regex("^(navigiere|weg nach|route zu)"),
            Regex("^(wer ist|was ist|wo liegt|wie groß)"),
            Regex("^(rechne|wie viel ist [0-9])"),
            Regex("^(licht|lampe|musik|lautstärke)")
        )

        return builtInPatterns.any { it.containsMatchIn(lower) }
    }

    /**
     * Delegiert an Google Assistant (kostenlos)
     */
    private fun delegateToGoogleAssistant(query: String) {
        val intent = Intent(Intent.ACTION_VOICE_COMMAND).apply {
            putExtra(Intent.EXTRA_TEXT, query)
        }

        try {
            activity.startActivity(intent)
            onQueryResult(HybridResult(
                source = ResultSource.BUILTIN_GOOGLE,
                content = null,
                action = AssistantAction("delegate_to_google", mapOf("query" to query)),
                estimatedCost = "Kostenlos"
            ))
        } catch (e: ActivityNotFoundException) {
            // Fallback: Zeige Google Suche
            val searchIntent = Intent(Intent.ACTION_WEB_SEARCH).apply {
                putExtra(SearchManager.QUERY, query)
            }
            activity.startActivity(searchIntent)
        }

        _voiceState.value = VoiceState.IDLE
    }

    /**
     * Lokale Spracherkennung (Fallback)
     */
    private fun startLocalSpeechRecognition() {
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(activity)
        // ... Implementation
    }

    /**
     * Text-to-Speech für Antworten
     */
    fun speak(text: String) {
        _voiceState.value = VoiceState.SPEAKING
        textToSpeech?.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
    }

    fun stop() {
        textToSpeech?.stop()
        speechRecognizer?.destroy()
        _voiceState.value = VoiceState.IDLE
    }

    companion object {
        private const val TAG = "HybridVoiceService"
        const val REQUEST_VOICE_INPUT = 1001
    }
}
