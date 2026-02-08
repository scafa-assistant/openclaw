import SwiftUI

struct ChatView: View {
    @StateObject private var viewModel = ChatViewModel()
    @State private var messageText = ""
    @State private var isRecording = false
    @Binding var startVoiceImmediately: Bool
    
    @StateObject private var voiceService = HybridVoiceService()
    
    var body: some View {
        VStack(spacing: 0) {
            // Header
            HStack {
                Text("OpenClaw")
                    .font(.headline)
                Spacer()
                // Voice indicator
                if voiceService.voiceState == .listening {
                    HStack(spacing: 4) {
                        Circle()
                            .fill(Color.red)
                            .frame(width: 8, height: 8)
                        Text("Hört zu...")
                            .font(.caption)
                            .foregroundColor(.secondary)
                    }
                }
            }
            .padding()
            
            // Chat messages list
            ScrollView {
                LazyVStack(spacing: 12) {
                    ForEach(viewModel.messages) { message in
                        MessageRow(message: message)
                    }
                }
                .padding()
            }
            
            // Voice Button (groß, zentral)
            VStack(spacing: 8) {
                Button(action: {
                    toggleVoiceInput()
                }) {
                    ZStack {
                        Circle()
                            .fill(voiceButtonColor)
                            .frame(width: 80, height: 80)
                        
                        Image(systemName: voiceIcon)
                            .font(.system(size: 35))
                            .foregroundColor(.white)
                    }
                    .scaleEffect(voiceService.voiceState == .listening ? 1.1 : 1.0)
                    .animation(.easeInOut(duration: 0.2), value: voiceService.voiceState)
                }
                
                Text(voiceButtonText)
                    .font(.caption)
                    .foregroundColor(.secondary)
            }
            .padding(.vertical, 20)
            
            // Text input (optional)
            HStack {
                TextField("Oder tippe hier...", text: $messageText)
                    .textFieldStyle(RoundedBorderTextFieldStyle())
                
                Button(action: {
                    viewModel.sendMessage(messageText)
                    messageText = ""
                }) {
                    Image(systemName: "arrow.up.circle.fill")
                        .font(.title2)
                        .foregroundColor(.accentColor)
                }
            }
            .padding()
        }
        .onAppear {
            // Wenn über Siri geöffnet, sofort Mikrofon aktivieren
            if startVoiceImmediately {
                DispatchQueue.main.asyncAfter(deadline: .now() + 0.5) {
                    startVoiceInput()
                    startVoiceImmediately = false
                }
            }
        }
    }
    
    private func toggleVoiceInput() {
        if voiceService.voiceState == .listening {
            voiceService.stop()
        } else {
            startVoiceInput()
        }
    }
    
    private func startVoiceInput() {
        voiceService.startVoiceInput { result in
            handleVoiceResult(result)
        }
    }
    
    private func handleVoiceResult(_ result: HybridVoiceService.HybridResult) {
        if let content = result.content {
            viewModel.sendMessage(content)
        }
    }
    
    private var voiceIcon: String {
        switch voiceService.voiceState {
        case .idle: return "mic.fill"
        case .listening: return "waveform"
        case .processing: return "brain.head.fill"
        case .speaking: return "speaker.wave.2.fill"
        case .error: return "exclamationmark.triangle.fill"
        }
    }
    
    private var voiceButtonColor: Color {
        switch voiceService.voiceState {
        case .idle: return .accentColor
        case .listening: return .red
        case .processing: return .orange
        case .speaking: return .green
        case .error: return .red
        }
    }
    
    private var voiceButtonText: String {
        switch voiceService.voiceState {
        case .idle: return "Tippe zum Sprechen"
        case .listening: return "Hört zu..."
        case .processing: return "Denke..."
        case .speaking: return "Sprech..."
        case .error: return "Fehler"
        }
    }
}

struct MessageRow: View {
    let message: ChatMessage
    
    var body: some View {
        HStack {
            if message.isUser {
                Spacer()
            }
            
            VStack(alignment: .leading, spacing: 4) {
                Text(message.content)
                    .padding(12)
                    .background(message.isUser ? Color.accentColor : Color.gray.opacity(0.2))
                    .foregroundColor(message.isUser ? .white : .primary)
                    .cornerRadius(16)
                
                if let cost = message.cost {
                    Text(cost)
                        .font(.caption2)
                        .foregroundColor(.secondary)
                }
            }
            
            if !message.isUser {
                Spacer()
            }
        }
    }
}

struct ChatMessage: Identifiable {
    let id = UUID()
    let content: String
    let isUser: Bool
    let cost: String?
}

class ChatViewModel: ObservableObject {
    @Published var messages: [ChatMessage] = []
    
    func sendMessage(_ text: String) {
        messages.append(ChatMessage(content: text, isUser: true, cost: nil))
        
        // TODO: Send to backend
        DispatchQueue.main.asyncAfter(deadline: .now() + 1) {
            self.messages.append(ChatMessage(
                content: "Das ist eine Test-Antwort von OpenClaw.",
                isUser: false,
                cost: "Kostenlos"
            ))
        }
    }
}
