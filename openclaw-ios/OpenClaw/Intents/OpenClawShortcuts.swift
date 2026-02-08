import AppIntents

struct OpenClawShortcuts: AppShortcutsProvider {

    static var appShortcuts: [AppShortcut] {
        AppShortcut(
            intent: AskOpenClawIntent(),
            phrases: [
                "Frag \(.applicationName) \(\.$question)",
                "\(.applicationName) \(\.$question)",
                "Frage an \(.applicationName) \(\.$question)",
                "Hey \(.applicationName) \(\.$question)",
            ],
            shortTitle: "Frag OpenClaw",
            systemImageName: "bubble.left.fill"
        )
    }
}
