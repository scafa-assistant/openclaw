package de.openclaw.assistant.data.api

import retrofit2.Response
import retrofit2.http.GET

/**
 * Health Check API
 * Prüft ob Backend erreichbar ist
 * CEO-Standard: Always know system status
 */
interface HealthCheckApi {
    
    @GET("/health")
    suspend fun checkHealth(): Response<HealthResponse>
}

data class HealthResponse(
    val status: String,
    val timestamp: String,
    val version: String
)

class BackendHealthChecker(private val api: HealthCheckApi) {
    
    suspend fun isHealthy(): Boolean {
        return try {
            val response = api.checkHealth()
            response.isSuccessful && response.body()?.status == "ok"
        } catch (e: Exception) {
            false
        }
    }
}