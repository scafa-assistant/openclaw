package de.openclaw.assistant.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.openclaw.assistant.data.api.OpenClawApi
import de.openclaw.assistant.data.local.SettingsDataStore
import de.openclaw.assistant.data.model.AuthRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class AuthViewModel(
    private val api: OpenClawApi,
    private val settingsDataStore: SettingsDataStore
) : ViewModel() {

    private val _isAuthenticated = MutableStateFlow(false)
    val isAuthenticated: StateFlow<Boolean> = _isAuthenticated

    private val _isGuest = MutableStateFlow(false)
    val isGuest: StateFlow<Boolean> = _isGuest

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    init {
        viewModelScope.launch {
            // Check if we have a token
            val token = settingsDataStore.authToken.first()
            _isAuthenticated.value = token != null && token.isNotEmpty()
        }
    }

    fun continueAsGuest() {
        viewModelScope.launch {
            _isGuest.value = true
            _isAuthenticated.value = true
            // Save guest mode in settings
            settingsDataStore.saveAuthToken("guest-token")
            settingsDataStore.saveUserData("guest@openclaw.local", "FREE")
        }
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            try {
                val request = AuthRequest(email, password)
                val response = api.login(request)
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null) {
                        settingsDataStore.saveAuthToken(body.accessToken)
                        settingsDataStore.saveUserData(
                            email = body.user.email,
                            tier = body.user.tier
                        )
                        _isAuthenticated.value = true
                        _isGuest.value = false
                    } else {
                        _error.value = "Empty response"
                    }
                } else {
                    _error.value = "Login failed: ${response.code()}"
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Login failed"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun register(email: String, password: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            try {
                val request = AuthRequest(email, password)
                val response = api.register(request)
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null) {
                        settingsDataStore.saveAuthToken(body.accessToken)
                        settingsDataStore.saveUserData(
                            email = body.user.email,
                            tier = body.user.tier
                        )
                        _isAuthenticated.value = true
                        _isGuest.value = false
                    } else {
                        _error.value = "Empty response"
                    }
                } else {
                    _error.value = "Registration failed: ${response.code()}"
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Registration failed"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            settingsDataStore.clearAuthToken()
            settingsDataStore.clearAll()
            _isAuthenticated.value = false
            _isGuest.value = false
        }
    }
}
