package de.openclaw.assistant.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import de.openclaw.assistant.data.model.AvailableModels
import de.openclaw.assistant.data.model.UserConfig

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PowerUserSettingsScreen(
    onBack: () -> Unit,
    onSave: (UserConfig) -> Unit,
    currentConfig: UserConfig = UserConfig()
) {
    var config by remember { mutableStateOf(currentConfig) }
    var selectedModel by remember { mutableStateOf(config.preferredModel ?: "auto") }
    var showApiKeySection by remember { mutableStateOf(false) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Erweiterte Einstellungen") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Zurück")
                    }
                },
                actions = {
                    TextButton(onClick = { onSave(config) }) {
                        Text("Speichern")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Info Card
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        "Standardmäßig wählt OpenClaw automatisch das beste Modell. Für fortgeschrittene Features kannst du eigene API-Keys hinzufügen.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
            
            // Modell-Auswahl
            Text(
                "KI-Modell",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
            )
            
            // Default Models (Auto, Fast, Smart)
            AvailableModels.DEFAULT_MODELS.forEach { model ->
                ModelSelectionCard(
                    title = model.name,
                    description = model.description,
                    selected = selectedModel == model.id,
                    onClick = { 
                        selectedModel = model.id
                        config = config.copy(preferredModel = null) // Auto
                        showApiKeySection = false
                    }
                )
            }
            
            Divider(modifier = Modifier.padding(vertical = 8.dp))
            
            // Power Models Section
            Text(
                "Power-User Modelle (eigene API-Keys)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
            )
            
            Text(
                "Für komplexere Aufgaben mit eigenen API-Keys:",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
            
            AvailableModels.POWER_MODELS.forEach { model ->
                val hasKey = when (model.provider) {
                    "anthropic" -> config.apiKeys?.anthropic != null
                    "openai" -> config.apiKeys?.openai != null
                    "google" -> config.apiKeys?.google != null
                    "moonshot" -> config.apiKeys?.moonshot != null
                    "deepseek" -> config.apiKeys?.deepseek != null
                    "mistral" -> config.apiKeys?.mistral != null
                    else -> false
                }
                
                PowerModelCard(
                    model = model,
                    hasKey = hasKey,
                    selected = selectedModel == model.id && hasKey,
                    onClick = {
                        if (hasKey) {
                            selectedModel = model.id
                            config = config.copy(preferredModel = model.id)
                        } else {
                            showApiKeySection = true
                        }
                    }
                )
            }
            
            // API Key Section (expandable)
            if (showApiKeySection) {
                ApiKeyInputSection(
                    currentKeys = config.apiKeys,
                    onKeysUpdated = { newKeys ->
                        config = config.copy(apiKeys = newKeys)
                    }
                )
            }
            
            // Voice Settings
            Divider(modifier = Modifier.padding(vertical = 8.dp))
            
            Text(
                "Spracheingabe",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Spracheingabe aktivieren")
                Switch(
                    checked = config.voiceEnabled,
                    onCheckedChange = { 
                        config = config.copy(voiceEnabled = it)
                    }
                )
            }
            
            // Dark Mode
            Divider(modifier = Modifier.padding(vertical = 8.dp))
            
            Text(
                "Erscheinungsbild",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
            )
            
            var darkModeOption by remember { 
                mutableStateOf(
                    when (config.darkMode) {
                        null -> 0 // System
                        true -> 1 // Dunkel
                        false -> 2 // Hell
                    }
                )
            }
            
            Column {
                DarkModeOption(
                    title = "System-Standard",
                    selected = darkModeOption == 0,
                    onClick = { 
                        darkModeOption = 0
                        config = config.copy(darkMode = null)
                    }
                )
                DarkModeOption(
                    title = "Dunkel",
                    selected = darkModeOption == 1,
                    onClick = { 
                        darkModeOption = 1
                        config = config.copy(darkMode = true)
                    }
                )
                DarkModeOption(
                    title = "Hell",
                    selected = darkModeOption == 2,
                    onClick = { 
                        darkModeOption = 2
                        config = config.copy(darkMode = false)
                    }
                )
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun ModelSelectionCard(
    title: String,
    description: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (selected) 
                MaterialTheme.colorScheme.primaryContainer
            else 
                MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Medium
                )
                Text(
                    description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
            if (selected) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun PowerModelCard(
    model: de.openclaw.assistant.data.model.PowerModel,
    hasKey: Boolean,
    selected: Boolean,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = when {
                selected -> MaterialTheme.colorScheme.primaryContainer
                !hasKey -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                else -> MaterialTheme.colorScheme.surface
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    model.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Medium
                )
                Text(
                    model.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
                if (!hasKey) {
                    Text(
                        "API-Key erforderlich →",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            when {
                selected -> Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                hasKey -> Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "Key vorhanden",
                    tint = MaterialTheme.colorScheme.tertiary
                )
                else -> Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Key hinzufügen",
                    tint = MaterialTheme.colorScheme.outline
                )
            }
        }
    }
}

@Composable
private fun ApiKeyInputSection(
    currentKeys: de.openclaw.assistant.data.model.ApiKeys?,
    onKeysUpdated: (de.openclaw.assistant.data.model.ApiKeys) -> Unit
) {
    var keys by remember { mutableStateOf(currentKeys ?: de.openclaw.assistant.data.model.ApiKeys()) }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                "API-Keys hinzufügen",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
            )
            
            Text(
                "Deine Keys werden verschlüsselt gespeichert und nur für deine Anfragen verwendet.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
            
            ApiKeyInput(
                label = "OpenAI API Key",
                value = keys.openai ?: "",
                onValueChange = { 
                    keys = keys.copy(openai = it.takeIf { it.isNotBlank() })
                    onKeysUpdated(keys)
                }
            )
            
            ApiKeyInput(
                label = "Anthropic API Key",
                value = keys.anthropic ?: "",
                onValueChange = { 
                    keys = keys.copy(anthropic = it.takeIf { it.isNotBlank() })
                    onKeysUpdated(keys)
                }
            )
            
            ApiKeyInput(
                label = "Google AI Studio Key",
                value = keys.google ?: "",
                onValueChange = { 
                    keys = keys.copy(google = it.takeIf { it.isNotBlank() })
                    onKeysUpdated(keys)
                }
            )
            
            ApiKeyInput(
                label = "Moonshot API Key",
                value = keys.moonshot ?: "",
                onValueChange = { 
                    keys = keys.copy(moonshot = it.takeIf { it.isNotBlank() })
                    onKeysUpdated(keys)
                }
            )
            
            ApiKeyInput(
                label = "DeepSeek API Key",
                value = keys.deepseek ?: "",
                onValueChange = { 
                    keys = keys.copy(deepseek = it.takeIf { it.isNotBlank() })
                    onKeysUpdated(keys)
                }
            )
            
            ApiKeyInput(
                label = "Mistral API Key",
                value = keys.mistral ?: "",
                onValueChange = { 
                    keys = keys.copy(mistral = it.takeIf { it.isNotBlank() })
                    onKeysUpdated(keys)
                }
            )
        }
    }
}

@Composable
private fun ApiKeyInput(
    label: String,
    value: String,
    onValueChange: (String) -> Unit
) {
    var visible by remember { mutableStateOf(false) }
    
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        singleLine = true,
        visualTransformation = if (visible) VisualTransformation.None else PasswordVisualTransformation(),
        trailingIcon = {
            IconButton(onClick = { visible = !visible }) {
                Icon(
                    imageVector = if (visible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                    contentDescription = if (visible) "Verbergen" else "Anzeigen"
                )
            }
        },
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun DarkModeOption(
    title: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title)
        RadioButton(
            selected = selected,
            onClick = onClick
        )
    }
}