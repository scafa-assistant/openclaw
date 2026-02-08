package de.openclaw.assistant.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Send
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import de.openclaw.assistant.viewmodel.ChatViewModel
import de.openclaw.assistant.voice.SpeechToTextManager

@Composable
fun ChatScreen(
    autoListen: Boolean = false,
    initialCommand: String? = null,
    vm: ChatViewModel = viewModel()
) {
    val messages by vm.messages.collectAsState()
    val isLoading by vm.isLoading.collectAsState()
    val sttState by vm.stt.state.collectAsState()
    val partialText by vm.stt.text.collectAsState()
    var textInput by remember { mutableStateOf("") }

    val isListening = sttState is
        SpeechToTextManager.STTState.Listening

    // Auto-Listen wenn vom Assistant gestartet
    LaunchedEffect(autoListen) {
        if (autoListen) vm.startListening()
    }

    // Handle initial command from widget
    LaunchedEffect(initialCommand) {
        initialCommand?.let {
            vm.sendMessage(it)
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("OpenClaw") }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Chat Messages
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(16.dp),
                reverseLayout = true
            ) {
                items(messages.reversed()) { msg ->
                    ChatBubble(msg)
                    Spacer(Modifier.height(8.dp))
                }
            }

            // Partial STT Text
            if (isListening && partialText.isNotEmpty()) {
                Text(
                    text = partialText,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(16.dp)
                )
            }

            // Loading
            if (isLoading) {
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Input Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Voice Button
                IconButton(
                    onClick = {
                        if (isListening) vm.stopListening()
                        else vm.startListening()
                    }
                ) {
                    Icon(
                        if (isListening) Icons.Default.Stop
                        else Icons.Default.Mic,
                        contentDescription = "Voice"
                    )
                }

                // Text Input
                OutlinedTextField(
                    value = textInput,
                    onValueChange = { textInput = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Nachricht...") },
                    singleLine = true
                )

                // Send Button
                IconButton(
                    onClick = {
                        vm.sendMessage(textInput)
                        textInput = ""
                    },
                    enabled = textInput.isNotBlank()
                ) {
                    Icon(Icons.Default.Send, "Senden")
                }
            }
        }
    }
}

@Composable
private fun ChatBubble(
    msg: de.openclaw.assistant.data.model.ChatMessage
) {
    val isUser = msg.role == "user"
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement =
            if (isUser) Arrangement.End
            else Arrangement.Start
    ) {
        Surface(
            shape = MaterialTheme.shapes.medium,
            color = if (isUser)
                MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.widthIn(max = 280.dp)
        ) {
            Text(
                text = msg.content,
                modifier = Modifier.padding(12.dp),
                color = if (isUser)
                    MaterialTheme.colorScheme.onPrimary
                else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
