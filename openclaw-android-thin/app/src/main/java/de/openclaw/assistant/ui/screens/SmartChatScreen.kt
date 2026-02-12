package de.openclaw.assistant.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import de.openclaw.assistant.data.api.ApiClient
import de.openclaw.assistant.data.local.SettingsDataStore
import de.openclaw.assistant.viewmodel.SmartChatViewModel
import androidx.compose.ui.platform.LocalContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SmartChatScreen(
    autoListen: Boolean = false,
    initialCommand: String? = null,
    viewModel: SmartChatViewModel = viewModel(
        factory = SmartChatViewModel.Factory(
            context = LocalContext.current,
            api = ApiClient.api,
            settings = SettingsDataStore(LocalContext.current)
        )
    )
) {
    val messages by viewModel.messages.collectAsState()
    val isProcessing by viewModel.isProcessing.collectAsState()
    val currentSource by viewModel.currentSource.collectAsState()
    var textInput by remember { mutableStateOf("") }

    // Auto-Listen beim Start (wenn vom Assistant gekommen)
    LaunchedEffect(autoListen) {
        if (autoListen) {
            viewModel.startVoiceInput()
        }
    }
    
    // Initial Command verarbeiten
    LaunchedEffect(initialCommand) {
        initialCommand?.let {
            viewModel.sendCommand(it)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("OpenClaw Hybrid") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                actions = {
                    // Zeigt aktuelle Quelle an (System, AI, SWARM)
                    if (currentSource.isNotEmpty()) {
                        Text(
                            text = currentSource,
                            modifier = Modifier.padding(end = 16.dp),
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Chat-Verlauf
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp),
                reverseLayout = true
            ) {
                items(messages.reversed()) { message ->
                    SmartMessageBubble(message)
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            // Eingabebereich
            Surface(
                tonalElevation = 3.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Voice Button
                    IconButton(
                        onClick = { viewModel.startVoiceInput() },
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Mic,
                            contentDescription = "Spracheingabe"
                        )
                    }

                    // Textfeld
                    OutlinedTextField(
                        value = textInput,
                        onValueChange = { textInput = it },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("Schreib etwas oder nutze Voice...") },
                        enabled = !isProcessing,
                        singleLine = true
                    )

                    // Senden Button
                    IconButton(
                        onClick = {
                            if (textInput.isNotBlank()) {
                                viewModel.sendMessage(textInput)
                                textInput = ""
                            }
                        },
                        enabled = textInput.isNotBlank() && !isProcessing
                    ) {
                        if (isProcessing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Send,
                                contentDescription = "Senden"
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SmartMessageBubble(message: SmartChatViewModel.SmartMessage) {
    when (message) {
        is SmartChatViewModel.SmartMessage.User -> {
            // User-Nachricht (rechts)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.primary,
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier.padding(start = 64.dp)
                ) {
                    Text(
                        text = message.content,
                        modifier = Modifier.padding(12.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        }

        is SmartChatViewModel.SmartMessage.Assistant -> {
            // Assistant-Nachricht (links) mit Source-Info
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.Start
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier.padding(end = 64.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = message.content,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        
                        // Source & Cost Info
                        Row(
                            modifier = Modifier.padding(top = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = message.source,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                            )
                            Text(
                                text = "•",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.5f)
                            )
                            Text(
                                text = message.cost,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            }
        }

        is SmartChatViewModel.SmartMessage.System -> {
            // System-Nachricht (zentriert)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = message.content,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    modifier = Modifier.padding(8.dp)
                )
            }
        }

        is SmartChatViewModel.SmartMessage.Error -> {
            // Fehler-Nachricht
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.errorContainer,
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        text = message.content,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.padding(8.dp),
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }
        }
    }
}
