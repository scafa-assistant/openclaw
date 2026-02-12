import Foundation

// MARK: - Request Models

struct CommandRequest: Codable {
    let command: String
    let context: CommandContext
    let model: String
    
    init(command: String, source: String = "ios", device: String = "iphone") {
        self.command = command
        self.context = CommandContext(source: source, device: device)
        self.model = "gemini-2.5-flash"
    }
}

struct CommandContext: Codable {
    let source: String
    let device: String
    let timestamp: String
    
    init(source: String, device: String) {
        self.source = source
        self.device = device
        self.timestamp = ISO8601DateFormatter().string(from: Date())
    }
}

// MARK: - Response Models

struct CommandResponse: Codable {
    let success: Bool
    let commandId: String
    let result: CommandResult
    let processingTime: Int
    let userId: String
}

struct CommandResult: Codable {
    let response: String
    let model: String
    let tokens: Int
    let type: String?
    let outputPath: String?
    let html: String?
    let url: String?
}

struct StatusResponse: Codable {
    let status: String
    let timestamp: String
    let version: String
    let features: Features
}

struct Features: Codable {
    let commands: Bool
    let websocket: Bool
    let cache: CacheStats
    let activeUsers: Int
}

struct CacheStats: Codable {
    let hits: Int
    let misses: Int
    let size: Int
}

struct RegisterResponse: Codable {
    let success: Bool
    let data: RegisterData
}

struct RegisterData: Codable {
    let apiKey: String
    let userId: String
    let createdAt: String
}

struct AuthResponse: Codable {
    let success: Bool
    let data: AuthData
}

struct AuthData: Codable {
    let userId: String
    let metadata: Metadata
    let rateLimit: RateLimit
}

struct Metadata: Codable {
    let name: String
    let description: String
}

struct RateLimit: Codable {
    let remaining: Int
    let limit: Int
}

// MARK: - Error Models

struct APIError: Codable, Error {
    let error: String
    let message: String
    let code: String?
}

enum NetworkError: Error {
    case invalidURL
    case noData
    case decodingError
    case serverError(Int)
    case unauthorized
    case rateLimited
}
