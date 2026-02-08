package de.openclaw.assistant.util

import kotlinx.coroutines.*
import kotlin.math.pow

class RetryManager(
    private val maxRetries: Int = 3,
    private val initialDelayMs: Long = 1000,
    private val maxDelayMs: Long = 10000,
    private val backoffMultiplier: Double = 2.0
) {
    suspend fun <T> executeWithRetry(
        operation: suspend () -> T,
        onRetry: ((attempt: Int, error: Throwable) -> Unit)? = null
    ): Result<T> {
        var lastException: Throwable? = null
        
        for (attempt in 0 until maxRetries) {
            try {
                val result = operation()
                return Result.success(result)
            } catch (e: Exception) {
                lastException = e
                
                if (attempt < maxRetries - 1) {
                    val delayMs = calculateDelay(attempt)
                    onRetry?.invoke(attempt + 1, e)
                    delay(delayMs)
                }
            }
        }
        
        return Result.failure(lastException ?: Exception("Unknown error"))
    }
    
    private fun calculateDelay(attempt: Int): Long {
        val exponentialDelay = initialDelayMs * backoffMultiplier.pow(attempt.toDouble())
        return minOf(exponentialDelay.toLong(), maxDelayMs)
    }
}

// Network-aware retry
class NetworkRetryManager(
    private val networkMonitor: NetworkMonitor
) {
    suspend fun <T> executeWhenOnline(
        operation: suspend () -> T,
        onOffline: () -> Unit = {},
        onSuccess: (T) -> Unit = {},
        onFailure: (Throwable) -> Unit = {}
    ) {
        if (!networkMonitor.isOnline()) {
            onOffline()
            // Queue for later
            OfflineQueueManager.addToQueue { executeWhenOnline(operation, onOffline, onSuccess, onFailure) }
            return
        }
        
        val retryManager = RetryManager(maxRetries = 3)
        val result = retryManager.executeWithRetry(operation)
        
        result.onSuccess { onSuccess(it) }
        result.onFailure { onFailure(it) }
    }
}

// Offline queue for messages
object OfflineQueueManager {
    private val pendingOperations = mutableListOf<suspend () -> Unit>()
    private var isProcessing = false
    
    fun addToQueue(operation: suspend () -> Unit) {
        pendingOperations.add(operation)
    }
    
    suspend fun processQueue(networkMonitor: NetworkMonitor) {
        if (isProcessing || !networkMonitor.isOnline()) return
        
        isProcessing = true
        
        while (pendingOperations.isNotEmpty() && networkMonitor.isOnline()) {
            val operation = pendingOperations.removeFirstOrNull() ?: break
            try {
                operation()
            } catch (e: Exception) {
                // Re-add to queue if failed
                pendingOperations.add(0, operation)
                break
            }
        }
        
        isProcessing = false
    }
    
    fun clearQueue() {
        pendingOperations.clear()
    }
}

// Network monitor interface
interface NetworkMonitor {
    fun isOnline(): Boolean
}
