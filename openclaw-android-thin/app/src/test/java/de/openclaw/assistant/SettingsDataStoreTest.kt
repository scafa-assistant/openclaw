package de.openclaw.assistant

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import de.openclaw.assistant.data.local.SettingsDataStore
import de.openclaw.assistant.util.ErrorHandler
import de.openclaw.assistant.util.AppError
import io.mockk.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.*
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

@ExperimentalCoroutinesApi
@RunWith(RobolectricTestRunner::class)
class SettingsDataStoreTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var settingsDataStore: SettingsDataStore
    private val context = RuntimeEnvironment.getApplication()

    @Before
    fun setup() {
        settingsDataStore = SettingsDataStore(context)
    }

    @Test
    fun `save and retrieve auth token`() = runTest {
        val token = "test_token_123"
        settingsDataStore.saveAuthToken(token)
        
        val retrieved = settingsDataStore.authToken.first()
        Assert.assertEquals(token, retrieved)
    }

    @Test
    fun `clear auth token`() = runTest {
        settingsDataStore.saveAuthToken("test_token")
        settingsDataStore.clearAuthToken()
        
        val retrieved = settingsDataStore.authToken.first()
        Assert.assertNull(retrieved)
    }

    @Test
    fun `save and retrieve user data`() = runTest {
        settingsDataStore.saveUserData("test@example.com", "PREMIUM")
        
        val email = settingsDataStore.userEmail.first()
        val tier = settingsDataStore.userTier.first()
        
        Assert.assertEquals("test@example.com", email)
        Assert.assertEquals("PREMIUM", tier)
    }

    @Test
    fun `save and retrieve settings`() = runTest {
        settingsDataStore.saveSettings("claude-sonnet", "en-US", false)
        
        val model = settingsDataStore.preferredModel.first()
        val language = settingsDataStore.language.first()
        val tts = settingsDataStore.ttsEnabled.first()
        
        Assert.assertEquals("claude-sonnet", model)
        Assert.assertEquals("en-US", language)
        Assert.assertEquals(false, tts)
    }

    @Test
    fun `default values are correct`() = runTest {
        val model = settingsDataStore.preferredModel.first()
        val language = settingsDataStore.language.first()
        val tts = settingsDataStore.ttsEnabled.first()
        
        Assert.assertEquals("gemini-2.5-flash", model)
        Assert.assertEquals("de-DE", language)
        Assert.assertEquals(true, tts)
    }
}

@ExperimentalCoroutinesApi
@RunWith(RobolectricTestRunner::class)
class ErrorHandlerTest {

    @Test
    fun `handle socket timeout exception`() {
        val exception = java.net.SocketTimeoutException("Timeout")
        val error = ErrorHandler.handle(exception)
        
        Assert.assertTrue(error is AppError.Network.Timeout)
    }

    @Test
    fun `handle unknown host exception`() {
        val exception = java.net.UnknownHostException("No connection")
        val error = ErrorHandler.handle(exception)
        
        Assert.assertTrue(error is AppError.Network.NoConnection)
    }

    @Test
    fun `user friendly message for timeout`() {
        val message = ErrorHandler.getUserFriendlyMessage(AppError.Network.Timeout)
        Assert.assertTrue(message.contains("Zeitüberschreitung"))
    }

    @Test
    fun `user friendly message for no connection`() {
        val message = ErrorHandler.getUserFriendlyMessage(AppError.Network.NoConnection)
        Assert.assertTrue(message.contains("Internetverbindung"))
    }
}
