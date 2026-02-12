package de.openclaw.thin.api

import retrofit2.Call
import retrofit2.http.*

interface OpenClawApi {
    @POST("api/mobile/register")
    fun register(@Body request: RegisterRequest): Call<RegisterResponse>
    
    @POST("api/mobile/auth")
    fun authenticate(@Body request: AuthRequest): Call<AuthResponse>
    
    @POST("api/mobile/command")
    fun sendCommand(
        @Header("x-api-key") apiKey: String,
        @Body request: CommandRequest
    ): Call<CommandResponse>
    
    @GET("api/mobile/status")
    fun getStatus(@Header("x-api-key") apiKey: String): Call<StatusResponse>
    
    @GET("api/mobile/history")
    fun getHistory(@Header("x-api-key") apiKey: String): Call<HistoryResponse>
}

data class RegisterRequest(val userId: String? = null)
data class RegisterResponse(val apiKey: String, val userId: String)

data class AuthRequest(val apiKey: String)
data class AuthResponse(val valid: Boolean, val userId: String?)

data class CommandRequest(val command: String, val context: Map<String, String>? = null)
data class CommandResponse(
    val success: Boolean,
    val response: String,
    val type: String? = "text",
    val url: String? = null,
    val html: String? = null
)

data class StatusResponse(val status: String, val version: String?)
data class HistoryResponse(val commands: List<HistoryItem>)
data class HistoryItem(
    val command: String,
    val response: String,
    val timestamp: Long
)
