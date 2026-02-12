package de.openclaw.assistant.intent

import android.content.Context
import android.content.Intent
import android.provider.AlarmClock
import android.provider.CalendarContract
import android.provider.ContactsContract
import android.provider.MediaStore
import android.provider.Settings
import androidx.core.content.ContextCompat.startActivity

/**
 * Intent Classifier - On-Device ML für Routing-Entscheidungen
 * Klassifiziert User-Intents und routet sie intelligent
 */
class IntentClassifier {

    /**
     * Intent-Kategorien für Routing
     */
    sealed class ClassifiedIntent {
        // Lokale System-APIs (kein Netzwerk nötig)
        data class LocalAlarm(val message: String, val hour: Int, val minute: Int) : ClassifiedIntent()
        data class LocalTimer(val seconds: Int) : ClassifiedIntent()
        data class LocalCalendar(val title: String, val startTime: Long) : ClassifiedIntent()
        data class LocalContact(val name: String) : ClassifiedIntent()
        data class LocalSettings(val setting: String) : ClassifiedIntent()
        data class LocalCamera(val mode: String) : ClassifiedIntent()
        
        // Externe AI-APIs (komplexe Anfragen)
        data class ExternalAI(val query: String, val complexity: Complexity) : ClassifiedIntent()
        
        // SWARM-Aufgaben (Remote Agents)
        data class SwarmTask(val task: String, val agent: String) : ClassifiedIntent()
        
        // Fallback
        data class Unknown(val rawInput: String) : ClassifiedIntent()
    }
    
    enum class Complexity {
        SIMPLE,      // → Handy-Assistant (Siri/Google)
        MEDIUM,      // → Gemini Flash / Claude Haiku
        COMPLEX      // → GPT-4 / Claude Opus
    }
    
    /**
     * Klassifiziert User-Input und entscheidet Routing
     */
    fun classify(input: String): ClassifiedIntent {
        val lowerInput = input.lowercase()
        
        // === LOKALE SYSTEM-APIS ===
        
        // Alarm / Wecker
        if (containsAny(lowerInput, listOf("wecker", "alarm", "weck mich", "weckt mich"))) {
            val time = extractTime(lowerInput)
            if (time != null) {
                return ClassifiedIntent.LocalAlarm(input, time.first, time.second)
            }
        }
        
        // Timer
        if (containsAny(lowerInput, listOf("timer", "stoppuhr", "countdown", "in 5 minuten"))) {
            val minutes = extractMinutes(lowerInput)
            if (minutes != null) {
                return ClassifiedIntent.LocalTimer(minutes * 60)
            }
        }
        
        // Kalender / Termin
        if (containsAny(lowerInput, listOf("termin", "kalender", "meeting", "besprechung"))) {
            return ClassifiedIntent.LocalCalendar(input, System.currentTimeMillis() + 3600000) // +1h
        }
        
        // Kontakte
        if (containsAny(lowerInput, listOf("ruf", "anrufen", "schreib", "whatsapp", "sms an"))) {
            val name = extractName(input)
            if (name != null) {
                return ClassifiedIntent.LocalContact(name)
            }
        }
        
        // Einstellungen
        if (containsAny(lowerInput, listOf("einstellungen", "wlan", "bluetooth", "helligkeit", "lautstärke"))) {
            return ClassifiedIntent.LocalSettings(extractSetting(lowerInput))
        }
        
        // Kamera
        if (containsAny(lowerInput, listOf("foto", "kamera", "bild", "selfie"))) {
            val mode = if (containsAny(lowerInput, listOf("selfie", "front"))) "front" else "back"
            return ClassifiedIntent.LocalCamera(mode)
        }
        
        // === SWARM-TASKS ===
        
        if (containsAny(lowerInput, listOf("orakel", "morpheus", "neo", "swarm", "agent"))) {
            val agent = when {
                lowerInput.contains("orakel") -> "oracle"
                lowerInput.contains("morpheus") -> "morpheus"
                lowerInput.contains("neo") -> "neo"
                else -> "oracle"
            }
            return ClassifiedIntent.SwarmTask(input, agent)
        }
        
        // === EXTERNE AI-APIS ===
        
        val complexity = assessComplexity(lowerInput)
        return ClassifiedIntent.ExternalAI(input, complexity)
    }
    
    /**
     * Führt klassifizierte Intents aus
     */
    fun execute(context: Context, intent: ClassifiedIntent): ExecutionResult {
        return when (intent) {
            is ClassifiedIntent.LocalAlarm -> setAlarm(context, intent)
            is ClassifiedIntent.LocalTimer -> setTimer(context, intent)
            is ClassifiedIntent.LocalCalendar -> addCalendarEvent(context, intent)
            is ClassifiedIntent.LocalContact -> openContacts(context, intent)
            is ClassifiedIntent.LocalSettings -> openSettings(context, intent)
            is ClassifiedIntent.LocalCamera -> openCamera(context, intent)
            is ClassifiedIntent.ExternalAI -> ExecutionResult.ExternalAPI(intent.query, intent.complexity)
            is ClassifiedIntent.SwarmTask -> ExecutionResult.Swarm(intent.task, intent.agent)
            is ClassifiedIntent.Unknown -> ExecutionResult.ExternalAPI(intent.rawInput, Complexity.MEDIUM)
        }
    }
    
    // === HELPER METHODS ===
    
    private fun containsAny(input: String, keywords: List<String>): Boolean {
        return keywords.any { input.contains(it) }
    }
    
    private fun extractTime(input: String): Pair<Int, Int>? {
        // Simple Regex für "um 15:30" oder "um 9 Uhr"
        val regex1 = Regex("um (\\d{1,2}):([0-5]\\d)")
        val regex2 = Regex("um (\\d{1,2}) uhr")
        
        regex1.find(input)?.let {
            return Pair(it.groupValues[1].toInt(), it.groupValues[2].toInt())
        }
        regex2.find(input)?.let {
            return Pair(it.groupValues[1].toInt(), 0)
        }
        return null
    }
    
    private fun extractMinutes(input: String): Int? {
        val regex = Regex("(\\d+) minuten?")
        regex.find(input)?.let {
            return it.groupValues[1].toInt()
        }
        return null
    }
    
    private fun extractName(input: String): String? {
        // Extrahiert Namen nach "ruf" oder "anrufen"
        val regex = Regex("(?:ruf|anrufen|schreib|whatsapp)\\s+(\\w+)", RegexOption.IGNORE_CASE)
        return regex.find(input)?.groupValues?.get(1)
    }
    
    private fun extractSetting(input: String): String {
        return when {
            input.contains("wlan") -> Settings.ACTION_WIFI_SETTINGS
            input.contains("bluetooth") -> Settings.ACTION_BLUETOOTH_SETTINGS
            input.contains("helligkeit") -> Settings.ACTION_DISPLAY_SETTINGS
            else -> Settings.ACTION_SETTINGS
        }
    }
    
    private fun assessComplexity(input: String): Complexity {
        val wordCount = input.split(" ").size
        
        // Komplexitätskriterien
        val complexIndicators = listOf(
            "erklär", "analysier", "vergleich", "schreib", "code", "programmier",
            "übersetz", "zusammenfass", "recherchier", "berechne", "formel"
        )
        
        val simpleIndicators = listOf(
            "wie spät", "wetter", "datum", "uhrzeit", "hallo", "hi", "ja", "nein"
        )
        
        return when {
            complexIndicators.any { input.contains(it) } -> Complexity.COMPLEX
            simpleIndicators.any { input.contains(it) } -> Complexity.SIMPLE
            wordCount > 15 -> Complexity.COMPLEX
            wordCount > 8 -> Complexity.MEDIUM
            else -> Complexity.SIMPLE
        }
    }
    
    // === LOCAL EXECUTION METHODS ===
    
    private fun setAlarm(context: Context, alarm: ClassifiedIntent.LocalAlarm): ExecutionResult {
        val intent = Intent(AlarmClock.ACTION_SET_ALARM).apply {
            putExtra(AlarmClock.EXTRA_HOUR, alarm.hour)
            putExtra(AlarmClock.EXTRA_MINUTES, alarm.minute)
            putExtra(AlarmClock.EXTRA_MESSAGE, alarm.message)
        }
        return try {
            startActivity(context, intent, null)
            ExecutionResult.Success("Wecker gestellt für ${alarm.hour}:${alarm.minute}")
        } catch (e: Exception) {
            ExecutionResult.Error("Konnte Wecker nicht stellen: ${e.message}")
        }
    }
    
    private fun setTimer(context: Context, timer: ClassifiedIntent.LocalTimer): ExecutionResult {
        val intent = Intent(AlarmClock.ACTION_SET_TIMER).apply {
            putExtra(AlarmClock.EXTRA_LENGTH, timer.seconds)
        }
        return try {
            startActivity(context, intent, null)
            ExecutionResult.Success("Timer für ${timer.seconds / 60} Minuten gestellt")
        } catch (e: Exception) {
            ExecutionResult.Error("Konnte Timer nicht stellen: ${e.message}")
        }
    }
    
    private fun addCalendarEvent(context: Context, event: ClassifiedIntent.LocalCalendar): ExecutionResult {
        val intent = Intent(Intent.ACTION_INSERT).apply {
            data = CalendarContract.Events.CONTENT_URI
            putExtra(CalendarContract.Events.TITLE, event.title)
            putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, event.startTime)
        }
        return try {
            startActivity(context, intent, null)
            ExecutionResult.Success("Kalender-Event erstellt")
        } catch (e: Exception) {
            ExecutionResult.Error("Konnte Termin nicht erstellen: ${e.message}")
        }
    }
    
    private fun openContacts(context: Context, contact: ClassifiedIntent.LocalContact): ExecutionResult {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = ContactsContract.Contacts.CONTENT_URI
        }
        return try {
            startActivity(context, intent, null)
            ExecutionResult.Success("Öffne Kontakte für ${contact.name}")
        } catch (e: Exception) {
            ExecutionResult.Error("Konnte Kontakte nicht öffnen: ${e.message}")
        }
    }
    
    private fun openSettings(context: Context, setting: ClassifiedIntent.LocalSettings): ExecutionResult {
        val intent = Intent(setting.setting)
        return try {
            startActivity(context, intent, null)
            ExecutionResult.Success("Öffne Einstellungen")
        } catch (e: Exception) {
            ExecutionResult.Error("Konnte Einstellungen nicht öffnen: ${e.message}")
        }
    }
    
    private fun openCamera(context: Context, camera: ClassifiedIntent.LocalCamera): ExecutionResult {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        return try {
            startActivity(context, intent, null)
            ExecutionResult.Success("Öffne Kamera")
        } catch (e: Exception) {
            ExecutionResult.Error("Konnte Kamera nicht öffnen: ${e.message}")
        }
    }
    
    // === RESULT TYPES ===
    
    sealed class ExecutionResult {
        data class Success(val message: String) : ExecutionResult()
        data class Error(val message: String) : ExecutionResult()
        data class ExternalAPI(val query: String, val complexity: Complexity) : ExecutionResult()
        data class Swarm(val task: String, val agent: String) : ExecutionResult()
    }
}
