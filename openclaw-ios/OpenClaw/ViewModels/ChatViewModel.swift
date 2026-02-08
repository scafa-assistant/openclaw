import Foundation
import SwiftUI

@MainActor
class ChatViewModel: ObservableObject {

    @Published var messages: [ChatMessage] = []
    @Published var isLoading = false
    @Published var inputText = ""

    let voiceService = VoiceService()

    init() {
        // When STT delivers result -> send automatically
        voiceService.onResult = { [weak self] text in
            Task { @MainActor in
                self?.sendMessage(text)
            }
        }
    }

    func sendMessage(_ text: String) {
        guard !text.isEmpty else { return }

        // Add user message
        let userMsg = ChatMessage(role: "user", content: text)
        messages.append(userMsg)
        isLoading = true
        inputText = ""

        Task {
            do {
                let response = try await APIService.shared.sendMessage(text)

                let assistantMsg = ChatMessage(
                    id: response.id,
                    role: "assistant",
                    content: response.content,
                    model: response.model
                )
                messages.append(assistantMsg)

                // Speak answer
                voiceService.speak(response.content)

            } catch {
                let errMsg = ChatMessage(
                    role: "assistant",
                    content: "Fehler: \(error.localizedDescription)"
                )
                messages.append(errMsg)
            }
            isLoading = false
        }
    }

    func startListening() {
        Task {
            let granted = await voiceService.requestPermissions()
            if granted {
                voiceService.startListening()
            }
        }
    }

    func stopListening() {
        voiceService.stopListening()
    }
}
