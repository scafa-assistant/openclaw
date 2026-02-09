package de.openclaw.assistant.router

import android.content.Context
import de.openclaw.assistant.intent.IntentClassifier
import de.openclaw.assistant.data.api.OpenClawApi
import de.openclaw.assistant.data.local.SettingsDataStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Hybrid Router - Intelligentes Routing zwischen:
 * - Local System APIs (Wecker, Timer, Kamera, etc.)
 * - On-Device Voice (Siri/Google für einfache Sachen)
 * - External AI APIs (Claude, GPT für komplexe Sachen)
 * - SWARM Agents (Orakel, Morpheus, Neo)
 */
class HybridRouter(
    private val context: Context,
    private val api: OpenClawApi,
    private val settings: SettingsDataStore
) {
    private val intentClassifier = IntentClassifier()
    
    /**
     * Routing-Ergebnisse
     */
    sealed class RoutingResult {
        data class LocalAction(val description: String) : RoutingResult()
        data class AIResponse(val response: String, val model: String) : RoutingResult()
        data class SwarmDelegated(val taskId: String, val agent: String) : RoutingResult()
        data class VoiceOnly(val prompt: String) : RoutingResult()
        data class Error(val message: String) : RoutingResult()
    }
    
    /**
     * Haupt-Routing-Funktion
     * Nimmt User-Input und routed ihn intelligent
     */
    suspend fun processInput(input: String): RoutingResult = withContext(Dispatchers.IO) {
        
        // Schritt 1: Intent klassifizieren
        val classifiedIntent = intentClassifier.classify(input)
        
        // Schritt 2: Lokal ausführen oder extern routen
        val executionResult = intentClassifier.execute(context, classifiedIntent)
        
        return@withContext when (executionResult) {
            is IntentClassifier.ExecutionResult.Success -> {
                // Lokale Aktion erfolgreich
                RoutingResult.LocalAction(executionResult.message)
            }
            
            is IntentClassifier.ExecutionResult.Error -> {
                // Lokale Aktion fehlgeschlagen → Fallback zu AI
                callExternalAI(input, IntentClassifier.Complexity.SIMPLE)
            }
            
            is IntentClassifier.ExecutionResult.ExternalAPI -> {
                // Muss an externe AI-API
                callExternalAI(executionResult.query, executionResult.complexity)
            }
            
            is IntentClassifier.ExecutionResult.Swarm -> {
                // Delegiere an SWARM Agent
                delegateToSwarm(executionResult.task, executionResult.agent)
            }
        }
    }
    
    /**
     * Ruft externe AI-API basierend auf Komplexität auf
     */
    private suspend fun callExternalAI(query: String, complexity: IntentClassifier.Complexity): RoutingResult {
        
        // Prüfe ob Power-User Keys vorhanden
        val userTier = settings.userTier.toString()
        
        return when (complexity) {
            IntentClassifier.Complexity.SIMPLE -> {
                // Einfache Anfragen: Nutze kostenloses Gemini oder System Voice
                if (userTier == "FREE") {
                    // Free Tier: System Voice oder Gemini Flash
                    RoutingResult.VoiceOnly(query)
                } else {
                    // Premium: Schnelles Modell
                    callFastModel(query)
                }
            }
            
            IntentClassifier.Complexity.MEDIUM -> {
                // Mittlere Komplexität: Claude Haiku oder GPT-3.5
                callBalancedModel(query)
            }
            
            IntentClassifier.Complexity.COMPLEX -> {
                // Hohe Komplexität: GPT-4 oder Claude Opus
                callPremiumModel(query)
            }
        }
    }
    
    /**
     * Kostenlose/Schnelle Modelle
     */
    private suspend fun callFastModel(query: String): RoutingResult {
        return try {
            // TODO: Implement Gemini Flash oder lokale Antwort
            RoutingResult.AIResponse(
                "Ich habe verstanden: '$query'. (Demo-Antwort - Backend noch nicht verbunden)",
                "gemini-flash"
            )
        } catch (e: Exception) {
            RoutingResult.Error("API-Fehler: ${e.message}")
        }
    }
    
    /**
     * Ausgewogene Modelle
     */
    private suspend fun callBalancedModel(query: String): RoutingResult {
        return try {
            // TODO: Implement Claude Haiku oder GPT-3.5
            RoutingResult.AIResponse(
                "Hier ist meine Antwort auf '$query': (Demo - Backend nicht verbunden)",
                "claude-haiku"
            )
        } catch (e: Exception) {
            RoutingResult.Error("API-Fehler: ${e.message}")
        }
    }
    
    /**
     * Premium Modelle
     */
    private suspend fun callPremiumModel(query: String): RoutingResult {
        return try {
            // TODO: Implement GPT-4 oder Claude Opus
            RoutingResult.AIResponse(
                "Detaillierte Analyse für '$query': (Demo - Backend nicht verbunden)",
                "gpt-4"
            )
        } catch (e: Exception) {
            RoutingResult.Error("API-Fehler: ${e.message}")
        }
    }
    
    /**
     * Delegiert an SWARM Agent
     */
    private suspend fun delegateToSwarm(task: String, agent: String): RoutingResult {
        // TODO: Implement SWARM communication
        return RoutingResult.SwarmDelegated(
            taskId = "swarm-${System.currentTimeMillis()}",
            agent = agent
        )
    }
    
    /**
     * Übersicht woher die Antwort kam (für UI)
     */
    fun getSourceDescription(result: RoutingResult): String {
        return when (result) {
            is RoutingResult.LocalAction -> "📱 System"
            is RoutingResult.AIResponse -> "🤖 ${result.model}"
            is RoutingResult.SwarmDelegated -> "🐝 SWARM:${result.agent}"
            is RoutingResult.VoiceOnly -> "🎤 Voice"
            is RoutingResult.Error -> "❌ Fehler"
        }
    }
    
    /**
     * Kostenschätzung anzeigen
     */
    fun getCostEstimate(result: RoutingResult): String {
        return when (result) {
            is RoutingResult.LocalAction -> "Kostenlos"
            is RoutingResult.VoiceOnly -> "Kostenlos"
            is RoutingResult.AIResponse -> when {
                result.model.contains("flash") -> "$0.0001"
                result.model.contains("haiku") -> "$0.002"
                result.model.contains("gpt-4") -> "$0.03"
                else -> "$0.01"
            }
            is RoutingResult.SwarmDelegated -> "SWARM"
            is RoutingResult.Error -> "-"
        }
    }
}
