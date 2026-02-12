import SwiftUI

/// Settings view
struct SettingsView: View {
    @Environment(\.dismiss) var dismiss
    @State private var showingLogoutAlert = false
    
    var body: some View {
        NavigationView {
            Form {
                Section("Verbindung") {
                    Button("Verbindung testen") {
                        testConnection()
                    }
                    
                    Button("Server URL ändern") {
                        // Show edit dialog
                    }
                }
                
                Section("Account") {
                    Button("Neuen API Key generieren") {
                        generateNewKey()
                    }
                    
                    Button("Verlauf anzeigen") {
                        // Navigate to history
                    }
                }
                
                Section {
                    Button("Abmelden", role: .destructive) {
                        showingLogoutAlert = true
                    }
                }
            }
            .navigationTitle("Einstellungen")
            .navigationBarItems(trailing: Button("Fertig") {
                dismiss()
            })
            .alert("Abmelden?", isPresented: $showingLogoutAlert) {
                Button("Abbrechen", role: .cancel) {}
                Button("Abmelden", role: .destructive) {
                    logout()
                }
            } message: {
                Text("Alle gespeicherten Daten werden gelöscht.")
            }
        }
    }
    
    private func testConnection() {
        Task {
            do {
                _ = try await OpenClawService.shared.authenticate()
                // Show success
            } catch {
                // Show error
            }
        }
    }
    
    private func generateNewKey() {
        Task {
            do {
                let response = try await OpenClawService.shared.register()
                OpenClawService.shared.configure(
                    serverURL: UserDefaults.standard.string(forKey: "server_url") ?? "",
                    apiKey: response.data.apiKey,
                    userId: response.data.userId
                )
            } catch {
                // Handle error
            }
        }
    }
    
    private func logout() {
        OpenClawService.shared.clearConfiguration()
        dismiss()
    }
}
