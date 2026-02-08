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
        
        Assert.assertTrue(result.isSuccess)
        Assert.assertEquals(1, callCount)
        Assert.assertEquals("success", result.getOrNull())
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
        
        Assert.assertTrue(result.isSuccess)
        Assert.assertEquals(3, callCount)
    }

    @Test
    fun `fails after max retries`() = runTest {
        val retryManager = RetryManager(maxRetries = 3, initialDelayMs = 10)
        var callCount = 0
        
        val result = retryManager.executeWithRetry {
            callCount++
            throw IOException("Network error")
        }
        
        Assert.assertTrue(result.isFailure)
        Assert.assertEquals(3, callCount)
    }

    @Test
    fun `calls onRetry callback`() = runTest {
        val retryManager = RetryManager(maxRetries = 3, initialDelayMs = 10)
        var callbackCount = 0
        
        retryManager.executeWithRetry(
            operation = {
                throw IOException("Error")
            },
            onRetry = { attempt, _ ->
                callbackCount++
            }
        )
        
        Assert.assertEquals(2, callbackCount) // Called for attempts 1 and 2
    }
}
