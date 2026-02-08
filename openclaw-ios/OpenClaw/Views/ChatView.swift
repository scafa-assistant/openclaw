import SwiftUI

struct ChatView: View {
    @StateObject private var vm = ChatViewModel()
    @FocusState private var isTextFieldFocused: Bool

    var body: some View {
        NavigationStack {
            VStack(spacing: 0) {
                // Chat Messages
                ScrollViewReader { proxy in
                    ScrollView {
                        LazyVStack(spacing: 12) {
                            ForEach(vm.messages) { msg in
                                ChatBubbleView(message: msg)
                                    .id(msg.id)
                            }
                        }
                        .padding()
                    }
                    .onChange(of: vm.messages.count) {
                        if let last = vm.messages.last {
                            withAnimation {
                                proxy.scrollTo(last.id, anchor: .bottom)
                            }
                        }
                    }
                }

                // Partial Transcript
                if vm.voiceService.isListening {
                    Text(vm.voiceService.transcript)
                        .font(.callout)
                        .foregroundColor(.secondary)
                        .padding(.horizontal)
                        .frame(maxWidth: .infinity, alignment: .leading)
                }

                // Loading
                if vm.isLoading {
                    ProgressView()
                        .padding(.vertical, 4)
                }

                Divider()

                // Input Bar
                HStack(spacing: 12) {
                    // Voice Button
                    Button {
                        if vm.voiceService.isListening {
                            vm.stopListening()
                        } else {
                            vm.startListening()
                        }
                    } label: {
                        Image(systemName: vm.voiceService.isListening ? "stop.circle.fill" : "mic.circle.fill")
                            .font(.title)
                            .foregroundColor(vm.voiceService.isListening ? .red : .orange)
                    }

                    // Text Field
                    TextField("Nachricht...", text: $vm.inputText)
                        .textFieldStyle(.roundedBorder)
                        .focused($isTextFieldFocused)
                        .onSubmit {
                            vm.sendMessage(vm.inputText)
                        }

                    // Send Button
                    Button {
                        vm.sendMessage(vm.inputText)
                    } label: {
                        Image(systemName: "arrow.up.circle.fill")
                            .font(.title)
                            .foregroundColor(.orange)
                    }
                    .disabled(vm.inputText.isEmpty)
                }
                .padding()
            }
            .navigationTitle("OpenClaw")
            .navigationBarTitleDisplayMode(.inline)
        }
    }
}

struct ChatBubbleView: View {
    let message: ChatMessage

    private var isUser: Bool { message.role == "user" }

    var body: some View {
        HStack {
            if isUser { Spacer() }

            VStack(alignment: isUser ? .trailing : .leading, spacing: 4) {
                Text(message.content)
                    .padding(12)
                    .background(isUser ? Color.orange : Color(.systemGray6))
                    .foregroundColor(isUser ? .white : .primary)
                    .cornerRadius(16)

                if let model = message.model {
                    Text("via \(model)")
                        .font(.caption2)
                        .foregroundColor(.secondary)
                }
            }
            .frame(maxWidth: 280, alignment: isUser ? .trailing : .leading)

            if !isUser { Spacer() }
        }
    }
}
