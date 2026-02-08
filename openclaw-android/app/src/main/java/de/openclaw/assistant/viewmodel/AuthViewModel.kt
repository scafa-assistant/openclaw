package de.openclaw.assistant.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import de.openclaw.assistant.data.api.ApiClient
import de.openclaw.assistant.data.model.AuthRequest
import de.openclaw.assistant.data.model.AuthResponse
import de.openclaw.assistant.data.model.UserProfile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AuthViewModel(app: Application) : AndroidViewModel(app) {

    private val _isAuthenticated = MutableStateFlow(false)
    val isAuthenticated: StateFlow<Boolean> = _isAuthenticated

    private val _user = MutableStateFlow<UserProfile?>(null)
    val user: StateFlow<UserProfile?> = _user

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    fun login(email: String, password: String) {
        _isLoading.value = true
        _error.value = null

        viewModelScope.launch {
            try {
                val response = ApiClient.api.login(
                    AuthRequest(email, password)
                )
                if (response.isSuccessful) {
                    val authResponse = response.body()!!
                    ApiClient.setToken(authResponse.accessToken)
                    _user.value = authResponse.user
                    _isAuthenticated.value = true
                } else {
                    _error.value = "Login fehlgeschlagen: ${response.code()}"
                }
            } catch (e: Exception) {
                _error.value = "Netzwerkfehler: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun register(email: String, password: String) {
        _isLoading.value = true
        _error.value = null

        viewModelScope.launch {
            try {
                val response = ApiClient.api.register(
                    AuthRequest(email, password)
                )
                if (response.isSuccessful) {
                    val authResponse = response.body()!!
                    ApiClient.setToken(authResponse.accessToken)
                    _user.value = authResponse.user
                    _isAuthenticated.value = true
                } else {
                    _error.value = "Registrierung fehlgeschlagen: ${response.code()}"
                }
            } catch (e: Exception) {
                _error.value = "Netzwerkfehler: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun logout() {
        ApiClient.setToken(null)
        _user.value = null
        _isAuthenticated.value = false
    }
}
