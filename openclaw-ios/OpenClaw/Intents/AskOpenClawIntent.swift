import AppIntents
import Foundation

struct AskOpenClawIntent: AppIntent {

    static var title: LocalizedStringResource = "Frag OpenClaw"
    static var description = IntentDescription("Stelle OpenClaw eine Frage")

    @Parameter(title: "Frage")
    var question: String

    static var parameterSummary: some ParameterSummary {
        Summary("Frag OpenClaw \(\.$question)")
    }

    func perform() async throws -> some IntentResult & ProvidesDialog & ShowsSnippetView {
        // API call
        let response = try await APIService.shared.sendMessage(question)

        // Answer as dialog (Siri reads it)
        return .result(dialog: "\(response.content)") {
            // Optional: Custom SwiftUI View in Siri
            OpenClawResponseView(question: question, answer: response.content, model: response.model)
        }
    }
}

// SwiftUI View shown in Siri
import SwiftUI
struct OpenClawResponseView: View {
    let question: String
    let answer: String
    let model: String

    var body: some View {
        VStack(alignment: .leading, spacing: 8) {
            Text(question)
                .font(.caption)
                .foregroundColor(.secondary)
            Text(answer)
                .font(.body)
            Text("via \(model)")
                .font(.caption2)
                .foregroundColor(.orange)
        }
        .padding()
    }
}
