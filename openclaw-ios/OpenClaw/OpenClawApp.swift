import SwiftUI

@main
struct OpenClawApp: App {
    @AppStorage("has_completed_onboarding") private var hasCompletedOnboarding = false
    @AppStorage("is_authenticated") private var isAuthenticated = false
    
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
                    MainContentView()
                }
            }
            .onOpenURL { url in
                handleDeepLink(url)
            }
        }
    }

    private func handleDeepLink(_ url: URL) {
        // openclaw://ask → start voice input immediately
        if url.host == "ask" {
            NotificationCenter.default.post(name: .startVoiceInput, object: nil)
        }
    }
}

extension Notification.Name {
    static let startVoiceInput = Notification.Name("startVoiceInput")
}

struct MainContentView: View {
    @StateObject private var chatViewModel = ChatViewModel()

    var body: some View {
        ChatView()
            .environmentObject(chatViewModel)
    }
}