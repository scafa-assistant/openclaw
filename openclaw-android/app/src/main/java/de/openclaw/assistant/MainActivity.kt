package de.openclaw.assistant

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.core.content.ContextCompat
import androidx.datastore.preferences.preferencesDataStore
import de.openclaw.assistant.ui.screens.AuthScreen
import de.openclaw.assistant.ui.screens.ChatScreen
import de.openclaw.assistant.ui.screens.OnboardingScreen
import de.openclaw.assistant.ui.theme.OpenClawTheme
import de.openclaw.assistant.viewmodel.AuthViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import androidx.datastore.preferences.core.booleanPreferencesKey

val Context.dataStore by preferencesDataStore(name = "settings")

class MainActivity : ComponentActivity() {

    private val requestPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) loadApp()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

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

    private fun loadApp() {
        val fromAssistant = intent.getBooleanExtra("FROM_ASSISTANT", false)
        val autoListen = intent.getBooleanExtra("AUTO_LISTEN", false)
        val quickCommand = intent.getStringExtra("quick_command")
        val startVoice = intent.getBooleanExtra("start_voice", false)

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
                                showAuth = true
                            }
                        )
                    }
                    !isAuthenticated && showAuth -> {
                        AuthScreen(
                            onAuthSuccess = {
                                isAuthenticated = true
                                showAuth = false
                            }
                        )
                    }
                    isAuthenticated || !showAuth -> {
                        ChatScreen(
                            autoListen = autoListen || startVoice || fromAssistant,
                            initialCommand = quickCommand
                        )
                    }
                }
            }
        }
    }
}