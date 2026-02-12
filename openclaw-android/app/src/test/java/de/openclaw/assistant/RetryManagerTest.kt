package de.openclaw.assistant

import de.openclaw.assistant.util.RetryManager
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Test
import java.io.IOException

@ExperimentalCoroutinesApi
class RetryManagerTest {

    @Test
    fun `successful operation returns immediately`() = runTest {
        val retryManager = RetryManager(maxRetries = 3)
        var callCount = 0
        
        val result = retryManager.executeWithRetry {
            callCount++
            "success"
        }
        
        Assert.assertEquals(1, callCount)
        Assert.assertEquals("success", result)
    }

    @Test
    fun `retries on failure then succeeds`() = runTest {
        val retryManager = RetryManager(maxRetries = 3, initialDelayMs = 10)
        var callCount = 0
        
        val result = retryManager.executeWithRetry {
            callCount++
            if (callCount < 3) throw IOException("Network error")
            "success"
        }
        
        Assert.assertEquals(3, callCount)
        Assert.assertEquals("success", result)
    }

    @Test
    fun `fails after max retries`() = runTest {
        val retryManager = RetryManager(maxRetries = 3, initialDelayMs = 10)
        var callCount = 0
        
        try {
            retryManager.executeWithRetry {
                callCount++
                throw IOException("Network error")
            }
            Assert.fail("Should have thrown exception")
        } catch (e: IOException) {
            Assert.assertEquals("Network error", e.message)
            Assert.assertEquals(3, callCount)
        }
    }

    @Test
    fun `calls onRetry callback`() = runTest {
        val retryManager = RetryManager(maxRetries = 3, initialDelayMs = 10)
        var callbackCount = 0
        
        try {
            retryManager.executeWithRetry(
                operation = {
                    throw IOException("Error")
                },
                onRetry = { attempt, _ ->
                    callbackCount++
                }
            )
        } catch (_: IOException) {
            // Expected
        }
        
        Assert.assertEquals(2, callbackCount) // Called for attempts 1 and 2
    }
}
