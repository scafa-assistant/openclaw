import SwiftUI

@main
struct OpenClawApp: App {
    @AppStorage("has_completed_onboarding") private var hasCompletedOnboarding = false
    
    var body: some Scene {
        WindowGroup {
            Group {
                if hasCompletedOnboarding {
                    MainContentView()
                } else {
                    OnboardingView(isCompleted: $hasCompletedOnboarding)
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
    @StateObject private var authService = AuthService()
    @StateObject private var chatViewModel = ChatViewModel()

    var body: some View {
        Group {
            if authService.isAuthenticated {
                ChatView()
                    .environmentObject(chatViewModel)
            } else {
                LoginView()
            }
        }
        .environmentObject(authService)
    }
}

struct LoginView: View {
    @EnvironmentObject var authService: AuthService

    var body: some View {
        VStack(spacing: 20) {
            Image(systemName: "bubble.left.fill")
                .font(.system(size: 80))
                .foregroundColor(.accentColor)

            Text("OpenClaw")
                .font(.largeTitle)
                .bold()

            Text("Melde dich an, um deinen AI-Assistenten zu nutzen")
                .font(.subheadline)
                .foregroundColor(.secondary)
                .multilineTextAlignment(.center)
                .padding(.horizontal)

            Spacer()

            // Guest Mode Button
            Button("Als Gast fortfahren") {
                authService.signInAsGuest()
            }
            .buttonStyle(.bordered)
            .tint(.accentColor)

            // Sign in with Apple Button would go here
            Button("Mit Apple anmelden") {
                authService.signInWithApple()
            }
            .buttonStyle(.borderedProminent)
            .tint(.accentColor)
            .padding()
        }
        .padding()
    }
}
