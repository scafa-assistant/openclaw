import AppIntents
import SwiftUI

/**
 * OpenClaw als Siri Backend
 * Siri bleibt das Interface - OpenClaw ist die Intelligenz dahinter
 */

// MARK: - Haupt Intent: Alle Anfragen an OpenClaw
@available(iOS 16.0, *)
struct OpenClawSmartQueryIntent: AppIntent {
    static var title: LocalizedStringResource = "Mit OpenClaw sprechen"
    static var description = IntentDescription("Stelle eine Frage oder gib einen Befehl - OpenClaw hilft dir.")
    
    // Parameter: Die Anfrage des Users
    @Parameter(title: "Anfrage", requestValueDialog: "Was kann ich für dich tun?")
    var query: String
    
    // Optional: Kontext aus vorheriger Konversation
    @Parameter(title: "Kontext", default: [])
    var context: [String]
    
    static var parameterSummary: some ParameterSummary {
        Summary("\($query)")
    }
    
    @MainActor
    func perform() async throws -> some IntentResult & ReturnsValue<String> {
        // 1. Sende an OpenClaw Backend
        let response = try await OpenClawAPIService.shared.processQuery(
            query: query,
            context: context,
            userId: getCurrentUserId()
        )
        
        // 2. Antwort kommt zurück - Siri spricht sie aus
        return .result(
            value: response.text,
            dialog: IntentDialog(stringLiteral: response.text)
        )
    }
}

// MARK: - Spezialisierte Intents für häufige Aktionen

@available(iOS 16.0, *)
struct OpenClawSendMessageIntent: AppIntent {
    static var title: LocalizedStringResource = "Nachricht senden"
    static var description = IntentDescription("Sende eine Nachricht über OpenClaw")
    
    @Parameter(title: "An", requestValueDialog: "An wen soll ich schreiben?")
    var recipient: String
    
    @Parameter(title: "Nachricht", requestValueDialog: "Was soll ich schreiben?")
    var message: String
    
    static var parameterSummary: some ParameterSummary {
        Summary("Schreibe \($recipient): \($message)")
    }
    
    @MainActor
    func perform() async throws -> some IntentResult & ReturnsValue<String> {
        let prompt = "Schreibe eine Nachricht an \(recipient): \(message)"
        let response = try await OpenClawAPIService.shared.processQuery(
            query: prompt,
            context: [],
            userId: getCurrentUserId()
        )
        
        return .result(
            value: response.text,
            dialog: IntentDialog(stringLiteral: "Ich habe folgendes geschrieben: \(response.text). Soll ich senden?")
        )
    }
}

@available(iOS 16.0, *)
struct OpenClawResearchIntent: AppIntent {
    static var title: LocalizedStringResource = "Recherchieren"
    static var description = IntentDescription("OpenClaw recherchiert ein Thema für dich")
    
    @Parameter(title: "Thema", requestValueDialog: "Über welches Thema soll ich recherchieren?")
    var topic: String
    
    static var parameterSummary: some ParameterSummary {
        Summary("Recherchiere \($topic)")
    }
    
    @MainActor
    func perform() async throws -> some IntentResult & ReturnsValue<String> {
        let prompt = "Recherchiere ausführlich: \(topic). Fasse die wichtigsten Punkte zusammen."
        let response = try await OpenClawAPIService.shared.processQuery(
            query: prompt,
            context: [],
            userId: getCurrentUserId()
        )
        
        return .result(
            value: response.text,
            dialog: IntentDialog(stringLiteral: response.text)
        )
    }
}

@available(iOS 16.0, *)
struct OpenClawCodeIntent: AppIntent {
    static var title: LocalizedStringResource = "Code schreiben"
    static var description = IntentDescription("OpenClaw schreibt Code für dich")
    
    @Parameter(title: "Aufgabe", requestValueDialog: "Was soll ich programmieren?")
    var task: String
    
    @Parameter(title: "Sprache", default: "Python")
    var language: String
    
    static var parameterSummary: some ParameterSummary {
        Summary("Schreibe \($language) Code: \($task)")
    }
    
    @MainActor
    func perform() async throws -> some IntentResult & ReturnsValue<String> {
        let prompt = "Schreibe \(language) Code für: \(task). Kommentiere den Code."
        let response = try await OpenClawAPIService.shared.processQuery(
            query: prompt,
            context: [],
            userId: getCurrentUserId()
        )
        
        return .result(
            value: response.text,
            dialog: IntentDialog(stringLiteral: "Hier ist der Code: \(response.text)")
        )
    }
}

// MARK: - API Service
@available(iOS 16.0, *)
class OpenClawAPIService {
    static let shared = OpenClawAPIService()
    
    private let baseURL = "https://api.openclaw.ungehoert.musik/v1"
    
    func processQuery(query: String, context: [String], userId: String) async throws -> OpenClawResponse {
        let url = URL(string: "\(baseURL)/chat")!
        
        var request = URLRequest(url: url)
        request.httpMethod = "POST"
        request.setValue("application/json", forHTTPHeaderField: "Content-Type")
        request.setValue("Bearer \(getAuthToken())", forHTTPHeaderField: "Authorization")
        
        let body = [
            "query": query,
            "context": context,
            "userId": userId,
            "platform": "ios_siri"
        ] as [String : Any]
        
        request.httpBody = try JSONSerialization.data(withJSONObject: body)
        
        let (data, _) = try await URLSession.shared.data(for: request)
        let response = try JSONDecoder().decode(OpenClawResponse.self, from: data)
        
        return response
    }
    
    private func getAuthToken() -> String {
        // Aus Keychain laden
        return UserDefaults.standard.string(forKey: "openclaw_token") ?? ""
    }
}

struct OpenClawResponse: Codable {
    let text: String
    let source: String // "siri_builtin" oder "openclaw_ai"
    let cost: String // "Kostenlos" oder "~$0.003"
}

// MARK: - Hilfsfunktionen
@available(iOS 16.0, *)
func getCurrentUserId() -> String {
    return UserDefaults.standard.string(forKey: "openclaw_user_id") ?? "anonymous"
}

// MARK: - Siri Integration
@available(iOS 16.0, *)
struct OpenClawShortcuts: AppShortcutsProvider {
    static var appShortcuts: [AppShortcut] {
        return [
            AppShortcut(
                intent: OpenClawSmartQueryIntent(),
                phrases: [
                    "Frag OpenClaw \($query)",
                    "OpenClaw \($query)",
                    "Hey OpenClaw \($query)",
                    "\($query) mit OpenClaw",
                    "Kannst du \($query)?"
                ],
                shortTitle: "OpenClaw fragen",
                systemImageName: "bubble.left.fill"
            )
        ]
    }
}
