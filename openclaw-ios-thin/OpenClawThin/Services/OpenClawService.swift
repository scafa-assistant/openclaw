import Foundation

/// Service for OpenClaw API communication
class OpenClawService {
    static let shared = OpenClawService()
    
    private var baseURL: String {
        get { UserDefaults.standard.string(forKey: "server_url") ?? "http://localhost:3001/" }
        set { UserDefaults.standard.set(newValue, forKey: "server_url") }
    }
    
    private var apiKey: String? {
        get { KeychainHelper.shared.get(key: "api_key") }
        set { 
            if let value = newValue {
                KeychainHelper.shared.save(key: "api_key", value: value)
            } else {
                KeychainHelper.shared.delete(key: "api_key")
            }
        }
    }
    
    private var userId: String? {
        get { UserDefaults.standard.string(forKey: "user_id") }
        set { UserDefaults.standard.set(newValue, forKey: "user_id") }
    }
    
    var isConfigured: Bool {
        apiKey != nil && !baseURL.isEmpty
    }
    
    // MARK: - Configuration
    
    func configure(serverURL: String, apiKey: String, userId: String) {
        self.baseURL = serverURL
        self.apiKey = apiKey
        self.userId = userId
    }
    
    func clearConfiguration() {
        self.apiKey = nil
        self.userId = nil
    }
    
    // MARK: - API Calls
    
    func register() async throws -> RegisterResponse {
        let url = try makeURL(endpoint: "api/mobile/register")
        var request = URLRequest(url: url)
        request.httpMethod = "POST"
        request.setValue("application/json", forHTTPHeaderField: "Content-Type")
        
        let body = ["name": "iOS User", "description": "iOS Thin Client"]
        request.httpBody = try JSONSerialization.data(withJSONObject: body)
        
        let (data, response) = try await URLSession.shared.data(for: request)
        try checkResponse(response, data: data)
        
        return try JSONDecoder().decode(RegisterResponse.self, from: data)
    }
    
    func authenticate() async throws -> AuthResponse {
        guard let key = apiKey else {
            throw NetworkError.unauthorized
        }
        
        let url = try makeURL(endpoint: "api/mobile/auth")
        var request = URLRequest(url: url)
        request.httpMethod = "POST"
        request.setValue(key, forHTTPHeaderField: "x-api-key")
        
        let (data, response) = try await URLSession.shared.data(for: request)
        try checkResponse(response, data: data)
        
        return try JSONDecoder().decode(AuthResponse.self, from: data)
    }
    
    func sendCommand(_ command: String) async throws -> CommandResponse {
        guard let key = apiKey else {
            throw NetworkError.unauthorized
        }
        
        let url = try makeURL(endpoint: "api/mobile/command")
        var request = URLRequest(url: url)
        request.httpMethod = "POST"
        request.setValue("application/json", forHTTPHeaderField: "Content-Type")
        request.setValue(key, forHTTPHeaderField: "x-api-key")
        
        let body = CommandRequest(command: command)
        request.httpBody = try JSONEncoder().encode(body)
        
        let (data, response) = try await URLSession.shared.data(for: request)
        try checkResponse(response, data: data)
        
        return try JSONDecoder().decode(CommandResponse.self, from: data)
    }
    
    func getStatus() async throws -> StatusResponse {
        let url = try makeURL(endpoint: "api/mobile/status")
        let (data, response) = try await URLSession.shared.data(from: url)
        try checkResponse(response, data: data)
        return try JSONDecoder().decode(StatusResponse.self, from: data)
    }
    
    // MARK: - Helpers
    
    private func makeURL(endpoint: String) throws -> URL {
        let fullURL = baseURL + endpoint
        guard let url = URL(string: fullURL) else {
            throw NetworkError.invalidURL
        }
        return url
    }
    
    private func checkResponse(_ response: URLResponse, data: Data) throws {
        guard let httpResponse = response as? HTTPURLResponse else {
            throw NetworkError.noData
        }
        
        switch httpResponse.statusCode {
        case 200...299:
            return
        case 401:
            throw NetworkError.unauthorized
        case 429:
            throw NetworkError.rateLimited
        case 400...499:
            throw NetworkError.serverError(httpResponse.statusCode)
        case 500...599:
            throw NetworkError.serverError(httpResponse.statusCode)
        default:
            throw NetworkError.serverError(httpResponse.statusCode)
        }
    }
}

// MARK: - Keychain Helper

class KeychainHelper {
    static let shared = KeychainHelper()
    
    func save(key: String, value: String) {
        let data = Data(value.utf8)
        let query: [String: Any] = [
            kSecClass as String: kSecClassGenericPassword,
            kSecAttrAccount as String: key,
            kSecValueData as String: data
        ]
        
        SecItemDelete(query as CFDictionary)
        SecItemAdd(query as CFDictionary, nil)
    }
    
    func get(key: String) -> String? {
        let query: [String: Any] = [
            kSecClass as String: kSecClassGenericPassword,
            kSecAttrAccount as String: key,
            kSecReturnData as String: true,
            kSecMatchLimit as String: kSecMatchLimitOne
        ]
        
        var result: AnyObject?
        SecItemCopyMatching(query as CFDictionary, &result)
        
        guard let data = result as? Data else { return nil }
        return String(data: data, encoding: .utf8)
    }
    
    func delete(key: String) {
        let query: [String: Any] = [
            kSecClass as String: kSecClassGenericPassword,
            kSecAttrAccount as String: key
        ]
        SecItemDelete(query as CFDictionary)
    }
}
