package de.openclaw.assistant.service

import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionService
import android.util.Log

class OpenClawRecognitionService : RecognitionService() {

    companion object {
        private const val TAG = "OpenClawRecognition"
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "RecognitionService created")
    }

    override fun onStartListening(
        recognizerIntent: Intent?,
        listener: Callback?
    ) {
        Log.d(TAG, "Start listening")
        // Implementation für Custom STT (optional)
        // Aktuell nutzen wir Android SpeechRecognizer direkt
    }

    override fun onCancel(listener: Callback?) {
        Log.d(TAG, "Cancel listening")
    }

    override fun onStopListening(listener: Callback?) {
        Log.d(TAG, "Stop listening")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "RecognitionService destroyed")
    }
}
