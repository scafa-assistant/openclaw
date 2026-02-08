import Foundation

actor APIService {
    static let shared = APIService()

    #if DEBUG
    private let baseURL = "http://localhost:3000/api/v1"
    #else
    private let baseURL = "https://api.openclaw.de/api/v1"
    #endif

    private var accessToken: String?

    func setToken(_ token: String?) { accessToken = token }

    // MARK: - Chat

    func sendMessage(_ text: String) async throws -> ChatResponse {
        let request = ChatRequest(message: text)
        return try await post(path: "chat/message", body: request)
    }

    func getChatHistory(limit: Int = 50) async throws -> [ChatMessage] {
        return try await get(path: "chat/history?limit=\(limit)")
    }

    // MARK: - Auth

    func login(email: String, password: String) async throws -> AuthResponse {
        let request = AuthRequest(email: email, password: password)
        let response: AuthResponse = try await post(path: "auth/login", body: request)
        self.accessToken = response.accessToken
        return response
    }

    func appleSignIn(identityToken: String) async throws -> AuthResponse {
        let body = ["identityToken": identityToken]
        let response: AuthResponse = try await post(path: "auth/apple", body: body)
        self.accessToken = response.accessToken
        return response
    }

    // MARK: - Generic HTTP

    private func get<T: Decodable>(path: String) async throws -> T {
        let url = URL(string: "\(baseURL)/\(path)")!
        var request = URLRequest(url: url)
        request.httpMethod = "GET"
        addAuth(&request)
        let (data, _) = try await URLSession.shared.data(for: request)
        return try JSONDecoder().decode(T.self, from: data)
    }

    private func post<T: Decodable, B: Encodable>(path: String, body: B) async throws -> T {
        let url = URL(string: "\(baseURL)/\(path)")!
        var request = URLRequest(url: url)
        request.httpMethod = "POST"
        request.setValue("application/json", forHTTPHeaderField: "Content-Type")
        request.httpBody = try JSONEncoder().encode(body)
        addAuth(&request)
        let (data, _) = try await URLSession.shared.data(for: request)
        return try JSONDecoder().decode(T.self, from: data)
    }

    private func addAuth(_ request: inout URLRequest) {
        if let token = accessToken {
            request.setValue("Bearer \(token)", forHTTPHeaderField: "Authorization")
        }
    }
}
