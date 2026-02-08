package de.openclaw.assistant.data.model

data class ChatMessage(
    val id: String,
    val role: String,      // "user" | "assistant"
    val content: String,
    val model: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)

data class ChatRequest(
    val message: String,
    val model: String = "gemini-2.5-flash",
    val stream: Boolean = false
)

data class ChatResponse(
    val id: String,
    val content: String,
    val model: String,
    val tokens: Int
)

data class AuthRequest(
    val email: String,
    val password: String
)

data class AuthResponse(
    val accessToken: String,
    val refreshToken: String,
    val user: UserProfile
)

data class UserProfile(
    val id: String,
    val email: String,
    val displayName: String,
    val tier: String    // "FREE" | "PREMIUM"
)
