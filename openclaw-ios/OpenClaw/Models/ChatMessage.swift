import Foundation

struct ChatMessage: Identifiable, Codable {
    let id: String
    let role: String          // "user" | "assistant"
    let content: String
    let model: String?
    let timestamp: Date

    init(id: String = UUID().uuidString,
         role: String, content: String,
         model: String? = nil,
         timestamp: Date = Date()) {
        self.id = id
        self.role = role
        self.content = content
        self.model = model
        self.timestamp = timestamp
    }
}

struct ChatRequest: Codable {
    let message: String
    let model: String
    let stream: Bool

    init(message: String,
         model: String = "gemini-2.5-flash",
         stream: Bool = false) {
        self.message = message
        self.model = model
        self.stream = stream
    }
}

struct ChatResponse: Codable {
    let id: String
    let content: String
    let model: String
    let tokens: Int
}

struct AuthRequest: Codable {
    let email: String
    let password: String
}

struct AuthResponse: Codable {
    let accessToken: String
    let refreshToken: String
    let user: UserProfile
}

struct UserProfile: Codable {
    let id: String
    let email: String
    let displayName: String
    let tier: String  // "FREE" | "PREMIUM"
}
