package de.openclaw.assistant.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

/**
 * Error Fallback UI
 * Zeigt Fehler an wenn etwas schiefgeht
 * CEO-Standard: Zero-Crash Policy
 */
@Composable
fun ErrorFallback(error: Exception) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            text = "Fehler: ${error.message}",
            modifier = Modifier.padding(16.dp),
            color = MaterialTheme.colorScheme.error
        )
    }
}

@Preview
@Composable
private fun ErrorFallbackPreview() {
    ErrorFallback(error = Exception("Test Error"))
}