import SwiftUI

struct QuickTestView: View {
    @StateObject private var viewModel = AuthViewModel()
    var onTestComplete: () -> Void
    
    let testAccounts = [
        ("demo1@openclaw.test", "demo123"),
        ("demo2@openclaw.test", "demo123"),
        ("gast@openclaw.test", "gast123")
    ]
    
    var body: some View {
        VStack(spacing: 24) {
            Image(systemName: "bolt.fill")
                .font(.system(size: 60))
                .foregroundColor(.accentColor)
            
            Text("Schnell-Test")
                .font(.title)
                .fontWeight(.bold)
            
            Text("Wähle einen Test-Account")
                .foregroundColor(.secondary)
            
            VStack(spacing: 12) {
                ForEach(testAccounts, id: \.0) { email, password in
                    Button(action: {
                        viewModel.login(email: email, password: password)
                        onTestComplete()
                    }) {
                        VStack(spacing: 4) {
                            Text(email)
                                .font(.subheadline)
                            Text("Passwort: \(password)")
                                .font(.caption)
                                .foregroundColor(.secondary)
                        }
                        .frame(maxWidth: .infinity)
                        .padding()
                        .background(Color.gray.opacity(0.1))
                        .cornerRadius(8)
                    }
                    .foregroundColor(.primary)
                }
            }
            
            Divider()
                .padding(.vertical)
            
            Button(action: {
                viewModel.continueAsGuest()
                onTestComplete()
            }) {
                Text("Als Gast fortfahren")
                    .fontWeight(.semibold)
                    .frame(maxWidth: .infinity)
                    .padding()
                    .background(Color.accentColor)
                    .foregroundColor(.white)
                    .cornerRadius(12)
            }
            
            Button(action: onTestComplete) {
                Text("Eigener Account")
                    .foregroundColor(.secondary)
            }
        }
        .padding(24)
    }
}