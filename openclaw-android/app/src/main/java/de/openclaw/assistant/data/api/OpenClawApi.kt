package de.openclaw.assistant.data.api

import de.openclaw.assistant.data.model.*
import retrofit2.Response
import retrofit2.http.*

interface OpenClawApi {

    // Auth
    @POST("auth/register")
    suspend fun register(
        @Body request: AuthRequest
    ): Response<AuthResponse>

    @POST("auth/login")
    suspend fun login(
        @Body request: AuthRequest
    ): Response<AuthResponse>

    @POST("auth/google")
    suspend fun googleSignIn(
        @Body body: Map<String, String>
    ): Response<AuthResponse>

    @POST("auth/refresh")
    suspend fun refreshToken(
        @Body body: Map<String, String>
    ): Response<AuthResponse>

    // Chat
    @POST("chat/message")
    suspend fun sendMessage(
        @Body request: ChatRequest
    ): Response<ChatResponse>

    @GET("chat/history")
    suspend fun getChatHistory(
        @Query("limit") limit: Int = 50
    ): Response<List<ChatMessage>>

    // User
    @GET("user/profile")
    suspend fun getProfile(): Response<UserProfile>

    @GET("user/settings")
    suspend fun getSettings(): Response<Map<String, Any>>

    @PUT("user/settings")
    suspend fun updateSettings(
        @Body settings: Map<String, Any>
    ): Response<Map<String, Any>>
}
