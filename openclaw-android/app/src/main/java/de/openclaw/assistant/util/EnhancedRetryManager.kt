package de.openclaw.assistant.util

import kotlinx.coroutines.*
import retrofit2.Response
import java.io.IOException
import java.net.SocketTimeoutException

/**
 * Verbesserte Retry-Logik mit Exponential Backoff
 */
class RetryManager(
    private val maxRetries: Int = 3,
    private val baseDelayMs: Long = 1000,
    private val maxDelayMs: Long = 10000
) {
    suspend fun <T> execute(
        block: suspend () -> Response<T>,
        onRetry: ((attempt: Int, error: Throwable) -> Unit)? = null
    ): Result<T> {
        var lastException: Throwable? = null
        
        for (attempt in 0 until maxRetries) {
            try {
                val response = block()
                
                if (response.isSuccessful) {
                    val body = response.body()
                    return if (body != null) {
                        Result.success(body)
                    } else {
                        Result.failure(IOException("Empty response body"))
                    }
                } else {
                    // Bei 4xx Errors nicht retryen (Client Error)
                    if (response.code() in 400..499) {
                        return Result.failure(
                            ApiException(
                                response.code(),
                                response.errorBody()?.string() ?: "Client error"
                            )
                        )
                    }
                    // 5xx Server Errors - retry
                    throw IOException("Server error: ${response.code()}")
                }
            } catch (e: Exception) {
                lastException = e
                
                // Nicht retryen bei bestimmten Exceptions
                if (e is ApiException && e.code in 400..499) {
                    return Result.failure(e)
                }
                
                if (attempt < maxRetries - 1) {
                    val delay = calculateDelay(attempt)
                    onRetry?.invoke(attempt + 1, e)
                    delay(delay)
                }
            }
        }
        
        return Result.failure(lastException ?: IOException("Unknown error"))
    }
    
    private fun calculateDelay(attempt: Int): Long {
        // Exponential backoff mit Jitter
        val exponentialDelay = baseDelayMs * (1 shl attempt)
        val jitter = (0..1000).random()
        return minOf(exponentialDelay + jitter, maxDelayMs)
    }
}

class ApiException(val code: Int, message: String) : IOException(message)

/**
 * Input Validation Utilities
 */
object InputValidator {
    fun validateEmail(email: String): Boolean {
        return email.matches(Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"))
    }
    
    fun validatePassword(password: String): Boolean {
        return password.length in 6..128
    }
    
    fun sanitizeInput(input: String): String {
        return input.trim().take(10000) // Max 10k Zeichen
    }
    
    fun isValidApiKey(key: String, provider: String): Boolean {
        return when (provider) {
            "openai" -> key.startsWith("sk-") && key.length > 40
            "anthropic" -> key.startsWith("sk-ant-") && key.length > 40
            "google" -> key.startsWith("AIza") && key.length > 35
            else -> key.length > 20
        }
    }
}

/**
 * Performance Monitor für API Calls
 */
class PerformanceMonitor {
    private var totalRequests = 0
    private var failedRequests = 0
    private var totalResponseTime = 0L
    
    fun recordRequest(durationMs: Long, success: Boolean) {
        totalRequests++
        if (!success) failedRequests++
        totalResponseTime += durationMs
    }
    
    fun getStats(): Stats {
        return Stats(
            totalRequests = totalRequests,
            failedRequests = failedRequests,
            successRate = if (totalRequests > 0) 
                ((totalRequests - failedRequests) * 100 / totalRequests) 
            else 100,
            avgResponseTime = if (totalRequests > 0) 
                totalResponseTime / totalRequests 
            else 0
        )
    }
    
    data class Stats(
        val totalRequests: Int,
        val failedRequests: Int,
        val successRate: Int,
        val avgResponseTime: Long
    )
}

/**
 * Cache für API-Antworten
 */
class ResponseCache(private val maxSize: Int = 50) {
    private val cache = LinkedHashMap<String, CacheEntry>(maxSize, 0.75f, true)
    private val defaultTtlMs = 5 * 60 * 1000L // 5 Minuten
    
    data class CacheEntry(
        val data: Any,
        val timestamp: Long,
        val ttlMs: Long
    )
    
    @Synchronized
    fun get(key: String): Any? {
        val entry = cache[key] ?: return null
        
        if (System.currentTimeMillis() - entry.timestamp > entry.ttlMs) {
            cache.remove(key)
            return null
        }
        
        return entry.data
    }
    
    @Synchronized
    fun put(key: String, data: Any, ttlMs: Long = defaultTtlMs) {
        if (cache.size >= maxSize) {
            // Remove oldest entry
            cache.remove(cache.keys.first())
        }
        
        cache[key] = CacheEntry(data, System.currentTimeMillis(), ttlMs)
    }
    
    @Synchronized
    fun clear() {
        cache.clear()
    }
    
    @Synchronized
    fun size(): Int = cache.size
}
