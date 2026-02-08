import SwiftUI

@main
struct OpenClawApp: App {
    var body: some Scene {
        WindowGroup {
            ContentView()
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

struct ContentView: View {
    @StateObject private var authService = AuthService()

    var body: some View {
        Group {
            if authService.isAuthenticated {
                ChatView()
            } else {
                OnboardingView()
            }
        }
        .environmentObject(authService)
    }
}

struct OnboardingView: View {
    @EnvironmentObject var authService: AuthService

    var body: some View {
        VStack(spacing: 20) {
            Image(systemName: "bubble.left.fill")
                .font(.system(size: 80))
                .foregroundColor(.orange)

            Text("OpenClaw")
                .font(.largeTitle)
                .bold()

            Text("Dein KI-Assistent für iPhone")
                .font(.subheadline)
                .foregroundColor(.secondary)

            Spacer()

            // Sign in with Apple Button would go here
            Button("Los geht's") {
                // For demo, skip auth
                authService.isAuthenticated = true
            }
            .buttonStyle(.borderedProminent)
            .tint(.orange)
            .padding()
        }
        .padding()
    }
}
