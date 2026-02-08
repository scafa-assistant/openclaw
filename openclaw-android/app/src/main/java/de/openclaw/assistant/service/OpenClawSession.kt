package de.openclaw.assistant.service

import android.app.VoiceInteractor
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.service.voice.VoiceInteractionSession
import android.util.Log
import de.openclaw.assistant.MainActivity

class OpenClawSession(
    private val context: Context
) : VoiceInteractionSession(context) {

    companion object {
        private const val TAG = "OpenClawSession"
    }

    override fun onShow(
        args: Bundle?,
        showFlags: Int
    ) {
        super.onShow(args, showFlags)
        Log.d(TAG, "Session shown - starting OpenClaw")

        // Option A: Activity starten (Fullscreen)
        val intent = Intent(context, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        intent.putExtra("FROM_ASSISTANT", true)
        intent.putExtra("AUTO_LISTEN", true)
        context.startActivity(intent)

        // Session beenden (Activity übernimmt)
        finish()
    }

    override fun onHandleAssist(
        state: AssistState
    ) {
        super.onHandleAssist(state)
        Log.d(TAG, "Assist data received")
    }
}
