package de.openclaw.assistant.util

import kotlinx.coroutines.*
import kotlin.math.pow

/**
 * Retry Manager mit Exponential Backoff
 */
class RetryManager(
    private val maxRetries: Int = 3,
    private val initialDelayMs: Long = 1000,
    private val maxDelayMs: Long = 10000,
    private val backoffMultiplier: Double = 2.0
) {
    suspend fun <T> executeWithRetry(
        operation: suspend () -> T,
        onRetry: ((attempt: Int, error: Throwable) -> Unit)? = null
    ): T {
        var lastException: Throwable? = null
        
        for (attempt in 0 until maxRetries) {
            try {
                return operation()
            } catch (e: Exception) {
                lastException = e
                
                if (attempt < maxRetries - 1) {
                    val delayMs = calculateDelay(attempt)
                    onRetry?.invoke(attempt + 1, e)
                    delay(delayMs)
                }
            }
        }
        
        throw lastException ?: Exception("Unknown error after $maxRetries retries")
    }
    
    private fun calculateDelay(attempt: Int): Long {
        val exponentialDelay = initialDelayMs * backoffMultiplier.pow(attempt.toDouble())
        return minOf(exponentialDelay.toLong(), maxDelayMs)
    }
}

// Network monitor interface
interface NetworkMonitor {
    fun isOnline(): Boolean
}
