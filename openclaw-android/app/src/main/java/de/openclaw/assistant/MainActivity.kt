package de.openclaw.assistant

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import de.openclaw.assistant.ui.screens.ChatScreen
import de.openclaw.assistant.ui.theme.OpenClawTheme

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
        val fromAssistant = intent.getBooleanExtra(
            "FROM_ASSISTANT", false
        )
        val autoListen = intent.getBooleanExtra(
            "AUTO_LISTEN", false
        )

        setContent {
            OpenClawTheme {
                ChatScreen(
                    autoListen = autoListen
                )
            }
        }
    }
}
