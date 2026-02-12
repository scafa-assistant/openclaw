package de.openclaw.assistant

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.core.content.ContextCompat
import androidx.datastore.preferences.preferencesDataStore
import de.openclaw.assistant.ui.screens.AuthScreen
import de.openclaw.assistant.ui.screens.OnboardingScreen
import de.openclaw.assistant.ui.screens.SmartChatScreen
import de.openclaw.assistant.ui.theme.OpenClawTheme
import de.openclaw.assistant.viewmodel.AuthViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import androidx.datastore.preferences.core.booleanPreferencesKey

val Context.dataStore by preferencesDataStore(name = "settings")

class MainActivity : ComponentActivity() {

    companion object {
        private const val TAG = "MainActivity"
        const val EXTRA_COMMAND = "command_text"
        const val EXTRA_FROM_ASSISTANT = "FROM_ASSISTANT"
        const val EXTRA_AUTO_LISTEN = "AUTO_LISTEN"
    }

    private val requestPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) loadApp()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Deeplink/Intent-Verarbeitung
        handleIntent(intent)

        // Mikrofon-Permission prüfen
        if (ContextCompat.checkSelfPermission(
            this, Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED) {
            loadApp()
        } else {
            requestPermission.launch(
                Manifest.permission.RECORD_AUDIO
            )
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    /**
     * Verarbeitet Intents und Deeplinks von Google Assistant
     */
    private fun handleIntent(intent: Intent) {
        val action = intent.action
        val data = intent.data

        Log.d(TAG, "Handling intent: action=$action, data=$data")

        when (action) {
            Intent.ACTION_VIEW -> handleDeepLink(data)
            Intent.ACTION_ASSIST -> handleAssistIntent(intent)
        }
    }

    /**
     * Verarbeitet Deeplinks im Format openclaw://command?text={command}
     */
    private fun handleDeepLink(data: Uri?) {
        if (data == null) return

        if (data.scheme == "openclaw") {
            when (data.host) {
                "command" -> {
                    val command = data.getQueryParameter("text")
                    command?.let {
                        Log.d(TAG, "Command from deeplink: $it")
                        intent.putExtra(EXTRA_COMMAND, it)
                        intent.putExtra(EXTRA_FROM_ASSISTANT, true)
                        intent.putExtra(EXTRA_AUTO_LISTEN, true)
                    }
                }
                "main" -> {
                    Log.d(TAG, "Opening main screen")
                }
            }
        }
    }

    /**
     * Verarbeitet direkte Assistant-Anfragen
     */
    private fun handleAssistIntent(intent: Intent) {
        // EXTRA_ASSIST_QUERY ist nicht in Android SDK vorhanden
        // Assistant-Queries werden über AppActionsActivity verarbeitet
        Log.d(TAG, "Assist intent received")
    }

    private fun loadApp() {
        // Intent-Extras extrahieren
        val fromAssistant = intent.getBooleanExtra(EXTRA_FROM_ASSISTANT, false)
        val autoListen = intent.getBooleanExtra(EXTRA_AUTO_LISTEN, false)
        val command = intent.getStringExtra(EXTRA_COMMAND)
        val quickCommand = intent.getStringExtra("quick_command")
        val startVoice = intent.getBooleanExtra("start_voice", false)

        Log.d(TAG, "Loading app: fromAssistant=$fromAssistant, autoListen=$autoListen, command=$command")

        // Command verarbeiten falls vorhanden
        command?.let {
            // TODO: Command an ViewModel weitergeben
            // chatViewModel.processCommand(it)
        }

        // Check if onboarding completed
        val onboardingCompleted = runBlocking {
            dataStore.data.first()[booleanPreferencesKey("onboarding_completed")] ?: false
        }

        setContent {
            OpenClawTheme {
                var showOnboarding by remember { mutableStateOf(!onboardingCompleted) }
                var showAuth by remember { mutableStateOf(false) }
                var isAuthenticated by remember { mutableStateOf(false) }

                when {
                    showOnboarding -> {
                        OnboardingScreen(
                            onComplete = {
                                showOnboarding = false
                                isAuthenticated = true // Direkt als Gast einloggen
                            }
                        )
                    }
                    !isAuthenticated -> {
                        AuthScreen(
                            onAuthSuccess = {
                                isAuthenticated = true
                            }
                        )
                    }
                    else -> {
                        SmartChatScreen(
                            autoListen = autoListen || startVoice || fromAssistant,
                            initialCommand = command ?: quickCommand
                        )
                    }
                }
            }
        }
    }
}