// MARK: - SiriKit Integration (für tiefe Integration)
// Diese Datei ermöglicht es, Siri komplett an OpenClaw zu binden

import Intents
import IntentsUI

/**
 * INInteraction Handler
 * Fängt Siri-Anfragen ab und leitet sie an OpenClaw weiter
 */
class OpenClawSiriHandler: NSObject, INSendMessageIntentHandling {
    
    // Wenn User sagt: "Schreibe eine Nachricht"
    func handle(intent: INSendMessageIntent, completion: @escaping (INSendMessageIntentResponse) -> Void) {
        guard let content = intent.content else {
            completion(INSendMessageIntentResponse(code: .failure, userActivity: nil))
            return
        }
        
        // Leite an OpenClaw weiter
        Task {
            do {
                let response = try await OpenClawAPIService.shared.processQuery(
                    query: "Optimiere diese Nachricht: \(content)",
                    context: [],
                    userId: getCurrentUserId()
                )
                
                // Antwort über Siri ausgeben
                let activity = NSUserActivity(activityType: "OpenClawMessageSent")
                activity.userInfo = ["response": response.text]
                
                completion(INSendMessageIntentResponse(code: .success, userActivity: activity))
            } catch {
                completion(INSendMessageIntentResponse(code: .failure, userActivity: nil))
            }
        }
    }
    
    func resolveRecipients(for intent: INSendMessageIntent, with completion: @escaping ([INSendMessageRecipientResolutionResult]) -> Void) {
        // Implementation für Kontaktvorschläge
        completion([])
    }
    
    func resolveContent(for intent: INSendMessageIntent, with completion: @escaping (INStringResolutionResult) -> Void) {
        if let content = intent.content {
            completion(.success(with: content))
        } else {
            completion(.needsValue())
        }
    }
}

/**
 * Custom Intent für komplexe Anfragen
 */
class OpenClawCustomIntentHandler: NSObject {
    
    func handleCustomQuery(_ query: String) async throws -> String {
        let response = try await OpenClawAPIService.shared.processQuery(
            query: query,
            context: [],
            userId: getCurrentUserId()
        )
        return response.text
    }
}

/**
 * Intent Extension Info
 * Muss in Info.plist registriert werden:
 *
 * NSExtension > NSExtensionAttributes > IntentsSupported:
 * - INSendMessageIntent
 * - INSearchForPhotosIntent
 * - INCreateNoteIntent
 * - etc.
 */

// MARK: - Shortcut Phrases (Deutsch)
/*
 * Folgende Phrasen aktivieren OpenClaw über Siri:
 *
 * Allgemein:
 * - "Hey Siri, frag OpenClaw [Anfrage]"
 * - "Hey Siri, OpenClaw [Anfrage]"
 * - "Hey Siri, kannst du [Anfrage]?"
 *
 * Nachrichten:
 * - "Hey Siri, schreibe eine intelligente Nachricht an [Kontakt]"
 * - "Hey Siri, optimiere diese Nachricht"
 *
 * Recherche:
 * - "Hey Siri, recherchiere [Thema] mit OpenClaw"
 * - "Hey Siri, was weiß OpenClaw über [Thema]?"
 *
 * Code:
 * - "Hey Siri, schreibe Code für [Aufgabe]"
 * - "Hey Siri, OpenClaw, programmiere [Funktion]"
 *
 * Konversation:
 * - "Hey Siri, erkläre mir [Thema]"
 * - "Hey Siri, hilf mir bei [Problem]"
 */
