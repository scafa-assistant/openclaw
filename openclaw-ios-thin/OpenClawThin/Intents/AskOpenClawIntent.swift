//
//  AskOpenClawIntent.swift
//  OpenClawThin
//
//  SiriKit Intent Definition
//

import Foundation
import AppIntents

// MARK: - Siri Intent
struct AskOpenClawIntent: AppIntent {
    static var title: LocalizedStringResource = "OpenClaw fragen"
    static var description = IntentDescription("Stelle eine Frage an deinen OpenClaw Assistant")
    
    @Parameter(title: "Frage", description: "Was möchtest du wissen?")
    var query: String
    
    static var parameterSummary: some ParameterSummary {
        Summary("Frage OpenClaw: \($query)")
    }
    
    func perform() async throws -> some IntentResult & ReturnsValue<String> {
        // The actual API call is handled by the app
        // This intent just passes the query to the app
        return .result(value: query)
    }
}

// MARK: - Intent Provider
struct OpenClawShortcuts: AppShortcutsProvider {
    static var appShortcuts: [AppShortcut] {
        AppShortcut(
            intent: AskOpenClawIntent(),
            phrases: [
                "Frage \(.applicationName) \($query)",
                "Frag \(.applicationName) \($query)",
                "\(.applicationName) \($query)",
                "Hey \(.applicationName) \($query)"
            ],
            shortTitle: "OpenClaw fragen",
            systemImageName: "waveform.circle.fill"
        )
    }
}
