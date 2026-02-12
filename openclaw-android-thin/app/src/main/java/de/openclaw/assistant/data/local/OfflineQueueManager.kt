package de.openclaw.assistant.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private val Context.offlineQueueDataStore by preferencesDataStore(name = "offline_queue")

/**
 * Offline Queue Manager
 * Speichert Commands wenn offline, sendet später
 * CEO-Standard: 100% Zuverlässigkeit
 */
class OfflineQueueManager(private val context: Context) {
    
    private val json = Json { ignoreUnknownKeys = true }
    private val queueKey = stringPreferencesKey("command_queue")
    
    @Serializable
    data class QueuedCommand(
        val id: String,
        val text: String,
        val timestamp: Long,
        val retryCount: Int = 0
    )
    
    suspend fun enqueue(command: String) {
        val newCommand = QueuedCommand(
            id = System.currentTimeMillis().toString(),
            text = command,
            timestamp = System.currentTimeMillis()
        )
        
        context.offlineQueueDataStore.edit { prefs ->
            val currentQueue = getQueueFromPrefs(prefs[queueKey])
            val updatedQueue = currentQueue + newCommand
            prefs[queueKey] = json.encodeToString(updatedQueue)
        }
    }
    
    fun getQueue(): Flow<List<QueuedCommand>> {
        return context.offlineQueueDataStore.data.map { prefs ->
            getQueueFromPrefs(prefs[queueKey])
        }
    }
    
    suspend fun remove(commandId: String) {
        context.offlineQueueDataStore.edit { prefs ->
            val currentQueue = getQueueFromPrefs(prefs[queueKey])
            val updatedQueue = currentQueue.filter { it.id != commandId }
            prefs[queueKey] = json.encodeToString(updatedQueue)
        }
    }
    
    suspend fun clear() {
        context.offlineQueueDataStore.edit { prefs ->
            prefs.remove(queueKey)
        }
    }
    
    private fun getQueueFromPrefs(jsonString: String?): List<QueuedCommand> {
        return try {
            jsonString?.let { json.decodeFromString(it) } ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }
}