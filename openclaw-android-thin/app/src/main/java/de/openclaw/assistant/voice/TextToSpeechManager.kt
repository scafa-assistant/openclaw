package de.openclaw.assistant.voice

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.Locale

class TextToSpeechManager(
    private val context: Context
) : TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = null
    private var isReady = false

    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking

    init { 
        tts = TextToSpeech(context, this) 
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            tts?.language = Locale.GERMAN
            tts?.setSpeechRate(1.0f)
            tts?.setPitch(1.0f)
            isReady = true

            tts?.setOnUtteranceProgressListener(
                object : UtteranceProgressListener() {
                    override fun onStart(id: String?) {
                        _isSpeaking.value = true
                    }
                    override fun onDone(id: String?) {
                        _isSpeaking.value = false
                    }
                    override fun onError(id: String?) {
                        _isSpeaking.value = false
                    }
                }
            )
        }
    }

    fun speak(text: String) {
        if (!isReady) return
        tts?.speak(
            text,
            TextToSpeech.QUEUE_FLUSH,
            null,
            "openclaw_utterance_${System.currentTimeMillis()}"
        )
    }

    fun stop() { 
        tts?.stop() 
    }
    
    fun destroy() { 
        tts?.shutdown() 
    }
}
