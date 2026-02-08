import SwiftUI

// Power-User Konfiguration für erweiterte Features
// Normale User brauchen das nicht - Default funktioniert out-of-the-box

struct UserConfig: Codable {
    var preferredModel: String? = nil // nil = Auto-Router
    var apiKeys: APIKeys? = nil
    var voiceEnabled: Bool = true
    var voiceLanguage: String = "de-DE"
    var darkMode: Bool? = nil // nil = System
    var fontSize: FontSize = .medium
    
    enum FontSize: String, Codable {
        case small, medium, large
    }
}

struct APIKeys: Codable {
    var openai: String?
    var anthropic: String?
    var google: String?
    var moonshot: String?
    var deepseek: String?
    var mistral: String?
}

// Verfügbare Modelle
struct PowerModel: Identifiable {
    let id: String
    let name: String
    let provider: String
    let description: String
    let requiresKey: Bool
}

struct DefaultModel: Identifiable {
    let id: String
    let name: String
    let description: String
}

extension PowerModel {
    static let all = [
        PowerModel(id: "opus", name: "Claude 3 Opus", provider: "anthropic", 
                   description: "Bestes Reasoning & Forschung", requiresKey: true),
        PowerModel(id: "codex", name: "OpenAI Codex", provider: "openai",
                   description: "Programmierung & Code", requiresKey: true),
        PowerModel(id: "moonshot", name: "Moonshot Kimi", provider: "moonshot",
                   description: "Langer Kontext & Mehrsprachig", requiresKey: true),
        PowerModel(id: "deepseek", name: "DeepSeek Chat", provider: "deepseek",
                   description: "Reasoning & Coding", requiresKey: true),
        PowerModel(id: "mistral", name: "Mistral Large", provider: "mistral",
                   description: "Europäische AI", requiresKey: true)
    ]
}

extension DefaultModel {
    static let all = [
        DefaultModel(id: "auto", name: "Automatisch", 
                     description: "OpenClaw wählt das beste Modell"),
        DefaultModel(id: "fast", name: "Schnell",
                     description: "Schnelle Antworten"),
        DefaultModel(id: "smart", name: "Intelligent",
                     description: "Beste Qualität")
    ]
}
