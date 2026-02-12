import SwiftUI

/// Main content view - routes between setup and main
struct ContentView: View {
    @StateObject private var viewModel = ContentViewModel()
    
    var body: some View {
        Group {
            if viewModel.isConfigured {
                MainView()
            } else {
                SetupView()
            }
        }
        .onAppear {
            viewModel.checkConfiguration()
        }
        .onReceive(NotificationCenter.default.publisher(for: .openclawDeeplink)) { notification in
            if let url = notification.object as? URL {
                viewModel.handleDeeplink(url)
            }
        }
    }
}

@MainActor
class ContentViewModel: ObservableObject {
    @Published var isConfigured = false
    @Published var pendingCommand: String?
    
    func checkConfiguration() {
        isConfigured = OpenClawService.shared.isConfigured
    }
    
    func handleDeeplink(_ url: URL) {
        // Parse openclaw://command?text=...
        guard url.host == "command",
              let components = URLComponents(url: url, resolvingAgainstBaseURL: true),
              let text = components.queryItems?.first(where: { $0.name == "text" })?.value else {
            return
        }
        
        pendingCommand = text
    }
}
