package de.openclaw.assistant.data.model

import kotlinx.serialization.Serializable

/**
 * User-Konfiguration für Power-User Features
 * Normale User brauchen das nicht - Default funktioniert out-of-the-box
 */
@Serializable
data class UserConfig(
    // Standard: null = Auto-Router entscheidet
    val preferredModel: String? = null,
    
    // Eigene API-Keys (verschlüsselt gespeichert)
    val apiKeys: ApiKeys? = null,
    
    // Voice Einstellungen
    val voiceEnabled: Boolean = true,
    val voiceLanguage: String = "de-DE",
    
    // UI Einstellungen
    val darkMode: Boolean? = null, // null = System-Default
    val fontSize: FontSize = FontSize.MEDIUM
) {
    enum class FontSize {
        SMALL, MEDIUM, LARGE
    }
}

@Serializable
data class ApiKeys(
    val openai: String? = null,
    val anthropic: String? = null,
    val google: String? = null,
    val moonshot: String? = null,
    val deepseek: String? = null,
    val mistral: String? = null
)

/**
 * Verfügbare Power-User Modelle
 * Diese erfordern eigene API-Keys
 */
object AvailableModels {
    val POWER_MODELS = listOf(
        PowerModel(
            id = "opus",
            name = "Claude 3 Opus",
            provider = "anthropic",
            description = "Bestes Reasoning & Forschung",
            requiresKey = true
        ),
        PowerModel(
            id = "codex",
            name = "OpenAI Codex",
            provider = "openai",
            description = "Programmierung & Code",
            requiresKey = true
        ),
        PowerModel(
            id = "moonshot",
            name = "Moonshot Kimi",
            provider = "moonshot",
            description = "Langer Kontext & Mehrsprachig",
            requiresKey = true
        ),
        PowerModel(
            id = "deepseek",
            name = "DeepSeek Chat",
            provider = "deepseek",
            description = "Reasoning & Coding",
            requiresKey = true
        ),
        PowerModel(
            id = "mistral",
            name = "Mistral Large",
            provider = "mistral",
            description = "Europäische AI",
            requiresKey = true
        )
    )
    
    // Default Models (kein eigener Key nötig)
    val DEFAULT_MODELS = listOf(
        DefaultModel(
            id = "auto",
            name = "Automatisch",
            description = "OpenClaw wählt das beste Modell"
        ),
        DefaultModel(
            id = "fast",
            name = "Schnell",
            description = "Schnelle Antworten (Gemini Flash)"
        ),
        DefaultModel(
            id = "smart",
            name = "Intelligent",
            description = "Beste Qualität (Claude/GPT-4)"
        )
    )
}

@Serializable
data class PowerModel(
    val id: String,
    val name: String,
    val provider: String,
    val description: String,
    val requiresKey: Boolean
)

@Serializable
data class DefaultModel(
    val id: String,
    val name: String,
    val description: String
)
