import Foundation
import Combine

// MARK: - UserDefaults Wrapper
@propertyWrapper
struct UserDefault<T: Codable> {
    let key: String
    let defaultValue: T
    
    var wrappedValue: T {
        get {
            guard let data = UserDefaults.standard.object(forKey: key) as? Data else {
                return defaultValue
            }
            return (try? JSONDecoder().decode(T.self, from: data)) ?? defaultValue
        }
        set {
            if let encoded = try? JSONEncoder().encode(newValue) {
                UserDefaults.standard.set(encoded, forKey: key)
            }
        }
    }
}

// MARK: - Settings Manager
class SettingsManager: ObservableObject {
    static let shared = SettingsManager()
    
    @UserDefault(key: "auth_token", defaultValue: nil)
    var authToken: String?
    
    @UserDefault(key: "user_email", defaultValue: nil)
    var userEmail: String?
    
    @UserDefault(key: "user_tier", defaultValue: "FREE")
    var userTier: String
    
    @UserDefault(key: "preferred_model", defaultValue: "gemini-2.5-flash")
    var preferredModel: String
    
    @UserDefault(key: "language", defaultValue: "de-DE")
    var language: String
    
    @UserDefault(key: "tts_enabled", defaultValue: true)
    var ttsEnabled: Bool
    
    @UserDefault(key: "offline_mode", defaultValue: false)
    var offlineMode: Bool
    
    // Published properties for UI updates
    @Published var isTTSEnabled: Bool = true
    @Published var currentLanguage: String = "de-DE"
    
    private init() {
        self.isTTSEnabled = ttsEnabled
        self.currentLanguage = language
    }
    
    func saveAuth(token: String, email: String) {
        authToken = token
        userEmail = email
    }
    
    func clearAuth() {
        authToken = nil
        userEmail = nil
    }
    
    func updateSettings(model: String? = nil, language: String? = nil, tts: Bool? = nil) {
        if let model = model {
            preferredModel = model
        }
        if let language = language {
            self.language = language
            currentLanguage = language
        }
        if let tts = tts {
            ttsEnabled = tts
            isTTSEnabled = tts
        }
    }
    
    var isAuthenticated: Bool {
        authToken != nil
    }
}

// MARK: - Local Chat Storage
class LocalChatStorage: ObservableObject {
    static let shared = LocalChatStorage()
    
    @UserDefault(key: "chat_messages", defaultValue: [])
    var messages: [ChatMessage]
    
    @UserDefault(key: "pending_messages", defaultValue: [])
    var pendingMessages: [PendingMessage]
    
    func addMessage(_ message: ChatMessage) {
        messages.append(message)
        // Keep only last 100 messages
        if messages.count > 100 {
            messages = Array(messages.suffix(100))
        }
    }
    
    func clearMessages() {
        messages.removeAll()
    }
    
    func queuePendingMessage(_ content: String) {
        let pending = PendingMessage(id: UUID(), content: content, timestamp: Date())
        pendingMessages.append(pending)
    }
    
    func removePendingMessage(id: UUID) {
        pendingMessages.removeAll { $0.id == id }
    }
    
    func clearPendingMessages() {
        pendingMessages.removeAll()
    }
}

// MARK: - Pending Message Model
struct PendingMessage: Codable, Identifiable {
    let id: UUID
    let content: String
    let timestamp: Date
}
