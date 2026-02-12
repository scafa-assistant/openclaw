import SwiftUI

/// Setup view for first-time configuration
struct SetupView: View {
    @State private var serverURL = "http://localhost:3001/"
    @State private var apiKey = ""
    @State private var isLoading = false
    @State private var errorMessage: String?
    @State private var generatedKey: String?
    
    var body: some View {
        NavigationView {
            Form {
                Section(header: Text("Server Konfiguration")) {
                    TextField("Server URL", text: $serverURL)
                        .keyboardType(.URL)
                        .autocapitalization(.none)
                    
                    Button("Verbindung testen") {
                        testConnection()
                    }
                }
                
                Section(header: Text("API Key")) {
                    if let key = generatedKey {
                        Text(key)
                            .font(.caption)
                            .foregroundColor(.green)
                    } else {
                        TextField("API Key", text: $apiKey)
                            .autocapitalization(.none)
                    }
                    
                    Button("Neuen API Key generieren") {
                        generateKey()
                    }
                    .disabled(isLoading)
                }
                
                if let error = errorMessage {
                    Section {
                        Text(error)
                            .foregroundColor(.red)
                    }
                }
                
                Section {
                    Button("Speichern & Fortfahren") {
                        saveConfiguration()
                    }
                    .disabled(!canSave || isLoading)
                }
            }
            .navigationTitle("OpenClaw Setup")
            .overlay {
                if isLoading {
                    ProgressView()
                        .scaleEffect(1.5)
                        .frame(maxWidth: .infinity, maxHeight: .infinity)
                        .background(Color.black.opacity(0.2))
                }
            }
        }
    }
    
    private var canSave: Bool {
        !serverURL.isEmpty && (!apiKey.isEmpty || generatedKey != nil)
    }
    
    private func testConnection() {
        isLoading = true
        errorMessage = nil
        
        Task {
            OpenClawService.shared.configure(serverURL: serverURL, apiKey: "", userId: "")
            
            do {
                let status = try await OpenClawService.shared.getStatus()
                await MainActor.run {
                    errorMessage = "Verbunden! Version: \(status.version)"
                    isLoading = false
                }
            } catch {
                await MainActor.run {
                    errorMessage = "Fehler: \(error.localizedDescription)"
                    isLoading = false
                }
            }
        }
    }
    
    private func generateKey() {
        isLoading = true
        errorMessage = nil
        
        Task {
            OpenClawService.shared.configure(serverURL: serverURL, apiKey: "", userId: "")
            
            do {
                let response = try await OpenClawService.shared.register()
                await MainActor.run {
                    generatedKey = response.data.apiKey
                    apiKey = response.data.apiKey
                    isLoading = false
                }
            } catch {
                await MainActor.run {
                    errorMessage = "Fehler: \(error.localizedDescription)"
                    isLoading = false
                }
            }
        }
    }
    
    private func saveConfiguration() {
        let finalKey = generatedKey ?? apiKey
        
        isLoading = true
        errorMessage = nil
        
        Task {
            OpenClawService.shared.configure(serverURL: serverURL, apiKey: finalKey, userId: "")
            
            do {
                let auth = try await OpenClawService.shared.authenticate()
                OpenClawService.shared.configure(
                    serverURL: serverURL,
                    apiKey: finalKey,
                    userId: auth.data.userId
                )
                await MainActor.run {
                    isLoading = false
                }
            } catch {
                await MainActor.run {
                    errorMessage = "Authentifizierung fehlgeschlagen"
                    isLoading = false
                }
            }
        }
    }
}
