package de.openclaw.assistant.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import de.openclaw.assistant.viewmodel.AuthViewModel

@Composable
fun QuickTestScreen(
    onTestComplete: () -> Unit,
    viewModel: AuthViewModel = viewModel()
) {
    var isLoading by remember { mutableStateOf(false) }
    val testAccounts = listOf(
        "demo1@openclaw.test" to "demo123",
        "demo2@openclaw.test" to "demo123",
        "gast@openclaw.test" to "gast123"
    )
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.SmartToy,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = "Schnell-Test",
            style = MaterialTheme.typography.headlineMedium
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "Wähle einen Test-Account oder erstelle deinen eigenen",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Test Accounts
        Text(
            text = "Test Accounts",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        testAccounts.forEach { (email, password) ->
            OutlinedButton(
                onClick = {
                    isLoading = true
                    viewModel.login(email, password)
                    onTestComplete()
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading
            ) {
                Column(modifier = Modifier.padding(vertical = 4.dp)) {
                    Text(email)
                    Text(
                        "Passwort: $password",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // OR divider
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            HorizontalDivider(modifier = Modifier.weight(1f))
            Text(
                "  ODER  ",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
            HorizontalDivider(modifier = Modifier.weight(1f))
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Guest Mode
        Button(
            onClick = {
                viewModel.continueAsGuest()
                onTestComplete()
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading
        ) {
            Text("Als Gast fortfahren")
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        TextButton(
            onClick = onTestComplete
        ) {
            Text("Eigener Account erstellen →")
        }
    }
}