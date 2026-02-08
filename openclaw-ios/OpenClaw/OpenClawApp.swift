import SwiftUI

@main
struct OpenClawApp: App {
    @AppStorage("has_completed_onboarding") private var hasCompletedOnboarding = false
    @AppStorage("is_authenticated") private var isAuthenticated = false
    @State private var startVoiceImmediately = false
    
    var body: some Scene {
        WindowGroup {
            Group {
                if !hasCompletedOnboarding {
                    OnboardingView(isCompleted: $hasCompletedOnboarding)
                } else if !isAuthenticated {
                    AuthView {
                        isAuthenticated = true
                    }
                } else {
                    MainContentView(startVoiceImmediately: $startVoiceImmediately)
                }
            }
            .onOpenURL { url in
                handleDeepLink(url)
            }
            .onContinueUserActivity("INSpeakableString") { userActivity in
                // Wenn Siri die App öffnet
                if let phrase = userActivity.suggestedInvocationPhrase {
                    print("Opened via Siri with phrase: \(phrase)")
                    startVoiceImmediately = true
                }
            }
        }
    }

    private func handleDeepLink(_ url: URL) {
        // openclaw://ask → start voice input immediately
        if url.host == "ask" {
            startVoiceImmediately = true
            NotificationCenter.default.post(name: .startVoiceInput, object: nil)
        }
    }
}

extension Notification.Name {
    static let startVoiceInput = Notification.Name("startVoiceInput")
}

struct MainContentView: View {
    @StateObject private var chatViewModel = ChatViewModel()
    @Binding var startVoiceImmediately: Bool

    var body: some View {
        ChatView(startVoiceImmediately: $startVoiceImmediately)
            .environmentObject(chatViewModel)
    }
}