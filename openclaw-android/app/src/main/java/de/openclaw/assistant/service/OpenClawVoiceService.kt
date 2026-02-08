package de.openclaw.assistant.service

import android.service.voice.VoiceInteractionService
import android.os.Bundle
import android.util.Log

class OpenClawVoiceService : VoiceInteractionService() {

    companion object {
        private const val TAG = "OpenClawVoiceService"
    }

    override fun onReady() {
        super.onReady()
        Log.d(TAG, "OpenClaw VoiceService ready")
        // Hier später: Hotword Detection initialisieren
    }

    override fun onShutdown() {
        super.onShutdown()
        Log.d(TAG, "OpenClaw VoiceService shutdown")
    }
}
