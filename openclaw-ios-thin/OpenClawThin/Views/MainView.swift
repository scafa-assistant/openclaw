import SwiftUI
import Speech

/// Main chat interface
struct MainView: View {
    @StateObject private var viewModel = MainViewModel()
    @State private var messageText = ""
    @State private var showingSettings = false
    
    var body: some View {
        VStack(spacing: 0) {
            // Status bar
            HStack {
                Circle()
                    .fill(viewModel.isConnected ? Color.green : Color.red)
                    .frame(width: 8, height: 8)
                Text(viewModel.isConnected ? "Verbunden" : "Nicht verbunden")
                    .font(.caption)
                Spacer()
                Button(action: { showingSettings = true }) {
                    Image(systemName: "gear")
                }
            }
            .padding()
            .background(Color(.systemGray6))
            
            // Chat messages
            ScrollViewReader { proxy in
                ScrollView {
                    LazyVStack(spacing: 12) {
                        ForEach(viewModel.messages) { message in
                            MessageBubble(message: message)
                                .id(message.id)
                        }
                    }
                    .padding()
                }
                .onChange(of: viewModel.messages.count) { _ in
                    if let last = viewModel.messages.last {
                        proxy.scrollTo(last.id, anchor: .bottom)
                    }
                }
            }
            
            // Input area
            VStack(spacing: 8) {
                if let html = viewModel.currentWebsiteHTML {
                    WebsitePreviewButton(html: html) {
                        viewModel.showingWebsite = true
                    }
                }
                
                HStack(spacing: 12) {
                    Button(action: { viewModel.startSpeechRecognition() }) {
                        Image(systemName: "mic.fill")
                            .font(.title3)
                            .foregroundColor(.white)
                            .frame(width: 44, height: 44)
                            .background(Color.blue)
                            .clipShape(Circle())
                    }
                    
                    TextField("Befehl eingeben...", text: $messageText)
                        .textFieldStyle(RoundedBorderTextFieldStyle())
                        .disabled(viewModel.isLoading)
                    
                    Button(action: sendMessage) {
                        Image(systemName: "arrow.up.circle.fill")
                            .font(.title2)
                    }
                    .disabled(messageText.isEmpty || viewModel.isLoading)
                }
                .padding(.horizontal)
            }
            .padding(.vertical, 8)
            .background(Color(.systemBackground))
        }
        .sheet(isPresented: $showingSettings) {
            SettingsView()
        }
        .sheet(isPresented: $viewModel.showingWebsite) {
            if let html = viewModel.currentWebsiteHTML {
                WebsiteView(htmlContent: html)
            }
        }
        .alert("Fehler", isPresented: $viewModel.showError) {
            Button("OK", role: .cancel) {}
        } message: {
            Text(viewModel.errorMessage)
        }
    }
    
    private func sendMessage() {
        let text = messageText.trimmingCharacters(in: .whitespacesAndNewlines)
        guard !text.isEmpty else { return }
        
        messageText = ""
        viewModel.sendMessage(text)
    }
}

// MARK: - View Model

@MainActor
class MainViewModel: ObservableObject {
    @Published var messages: [ChatMessage] = []
    @Published var isConnected = false
    @Published var isLoading = false
    @Published var showError = false
    @Published var errorMessage = ""
    @Published var showingWebsite = false
    @Published var currentWebsiteHTML: String?
    
    private let speechRecognizer = SFSpeechRecognizer(locale: Locale(identifier: "de-DE"))
    
    init() {
        testConnection()
    }
    
    func testConnection() {
        Task {
            do {
                _ = try await OpenClawService.shared.getStatus()
                isConnected = true
            } catch {
                isConnected = false
            }
        }
    }
    
    func sendMessage(_ text: String) {
        let userMessage = ChatMessage(content: text, isUser: true)
        messages.append(userMessage)
        
        isLoading = true
        
        Task {
            do {
                let response = try await OpenClawService.shared.sendCommand(text)
                
                let assistantMessage = ChatMessage(
                    content: response.result.response,
                    isUser: false,
                    type: response.result.type
                )
                messages.append(assistantMessage)
                
                // Handle website
                if response.result.type == "website", let html = response.result.html {
                    currentWebsiteHTML = html
                }
                
                isLoading = false
                
            } catch {
                errorMessage = error.localizedDescription
                showError = true
                isLoading = false
            }
        }
    }
    
    func startSpeechRecognition() {
        // Request authorization
        SFSpeechRecognizer.requestAuthorization { status in
            // Handle speech recognition
        }
    }
}

// MARK: - Models

struct ChatMessage: Identifiable {
    let id = UUID()
    let content: String
    let isUser: Bool
    let type: String?
    let timestamp = Date()
    
    init(content: String, isUser: Bool, type: String? = nil) {
        self.content = content
        self.isUser = isUser
        self.type = type
    }
}

// MARK: - Components

struct MessageBubble: View {
    let message: ChatMessage
    
    var body: some View {
        HStack {
            if message.isUser { Spacer() }
            
            VStack(alignment: .leading, spacing: 4) {
                Text(message.content)
                    .padding(12)
                    .background(message.isUser ? Color.blue : Color(.systemGray5))
                    .foregroundColor(message.isUser ? .white : .primary)
                    .cornerRadius(16)
                
                if let type = message.type {
                    Text(type.capitalized)
                        .font(.caption2)
                        .foregroundColor(.secondary)
                }
            }
            
            if !message.isUser { Spacer() }
        }
    }
}

struct WebsitePreviewButton: View {
    let html: String
    let action: () -> Void
    
    var body: some View {
        Button(action: action) {
            HStack {
                Image(systemName: "globe")
                Text("Webseite anzeigen")
                Spacer()
                Image(systemName: "chevron.right")
            }
            .padding()
            .background(Color.green.opacity(0.1))
            .cornerRadius(8)
            .foregroundColor(.green)
        }
        .padding(.horizontal)
    }
}
