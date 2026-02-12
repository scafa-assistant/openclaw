import Foundation
import Intents

/// Siri Intent Handler for OpenClaw
class AskOpenClawIntentHandler: NSObject, AskOpenClawIntentHandling {
    
    func handle(intent: AskOpenClawIntent, completion: @escaping (AskOpenClawIntentResponse) -> Void) {
        guard let command = intent.command else {
            completion(AskOpenClawIntentResponse(code: .failure, userActivity: nil))
            return
        }
        
        // Check if configured
        guard OpenClawService.shared.isConfigured else {
            let response = AskOpenClawIntentResponse(code: .failure, userActivity: nil)
            response.result = "Bitte zuerst die OpenClaw App einrichten"
            completion(response)
            return
        }
        
        // Send command to server
        Task {
            do {
                let result = try await OpenClawService.shared.sendCommand(command)
                
                let response = AskOpenClawIntentResponse(code: .success, userActivity: nil)
                response.result = result.result.response
                completion(response)
                
            } catch {
                let response = AskOpenClawIntentResponse(code: .failure, userActivity: nil)
                response.result = "Fehler: \(error.localizedDescription)"
                completion(response)
            }
        }
    }
    
    func resolveCommand(for intent: AskOpenClawIntent, with completion: @escaping (INStringResolutionResult) -> Void) {
        if let command = intent.command, !command.isEmpty {
            completion(INStringResolutionResult.success(with: command))
        } else {
            completion(INStringResolutionResult.needsValue())
        }
    }
}

// MARK: - Intent Handler Provider

class IntentHandler: INExtension {
    override func handler(for intent: INIntent) -> Any {
        if intent is AskOpenClawIntent {
            return AskOpenClawIntentHandler()
        }
        return self
    }
}
