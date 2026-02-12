//
//  IntentHandler.swift
//  OpenClawThin
//
//  SiriKit Intent Handler
//

import Foundation
import Intents

class AskOpenClawIntentHandler: NSObject, AskOpenClawIntentHandling {
    
    func handle(intent: AskOpenClawIntent, completion: @escaping (AskOpenClawIntentResponse) -> Void) {
        guard let query = intent.query else {
            completion(AskOpenClawIntentResponse(code: .failure, userActivity: nil))
            return
        }
        
        // Store the query for the app to process
        let userActivity = NSUserActivity(activityType: "com.openclaw.siri-query")
        userActivity.userInfo = ["query": query]
        
        let response = AskOpenClawIntentResponse(code: .continueInApp, userActivity: userActivity)
        completion(response)
    }
    
    func resolveQuery(for intent: AskOpenClawIntent, with completion: @escaping (INStringResolutionResult) -> Void) {
        if let query = intent.query, !query.isEmpty {
            completion(.success(with: query))
        } else {
            completion(.needsValue())
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
