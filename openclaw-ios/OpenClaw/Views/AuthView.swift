import SwiftUI

class AuthViewModel: ObservableObject {
    @Published var isAuthenticated = false
    @Published var isGuest = false
    @Published var isLoading = false
    @Published var errorMessage: String?
    @Published var userEmail: String?
    
    func continueAsGuest() {
        isGuest = true
        isAuthenticated = true
        userEmail = "guest@openclaw.local"
        
        // Save to UserDefaults
        UserDefaults.standard.set(true, forKey: "is_guest")
        UserDefaults.standard.set(userEmail, forKey: "user_email")
    }
    
    func login(email: String, password: String) {
        isLoading = true
        errorMessage = nil
        
        // Mock login - replace with real API
        DispatchQueue.main.asyncAfter(deadline: .now() + 1) {
            self.isLoading = false
            self.isAuthenticated = true
            self.isGuest = false
            self.userEmail = email
            
            UserDefaults.standard.set(email, forKey: "user_email")
            UserDefaults.standard.set(false, forKey: "is_guest")
        }
    }
    
    func register(email: String, password: String) {
        isLoading = true
        errorMessage = nil
        
        DispatchQueue.main.asyncAfter(deadline: .now() + 1) {
            self.isLoading = false
            self.isAuthenticated = true
            self.isGuest = false
            self.userEmail = email
            
            UserDefaults.standard.set(email, forKey: "user_email")
            UserDefaults.standard.set(false, forKey: "is_guest")
        }
    }
    
    func logout() {
        isAuthenticated = false
        isGuest = false
        userEmail = nil
        UserDefaults.standard.removeObject(forKey: "user_email")
        UserDefaults.standard.removeObject(forKey: "is_guest")
    }
}

struct AuthView: View {
    @StateObject private var viewModel = AuthViewModel()
    @State private var email = ""
    @State private var password = ""
    @State private var isLogin = true
    
    var onAuthSuccess: () -> Void
    
    var body: some View {
        VStack(spacing: 24) {
            // Header
            VStack(spacing: 12) {
                Image(systemName: "bubble.left.fill")
                    .font(.system(size: 80))
                    .foregroundColor(.accentColor)
                
                Text("OpenClaw")
                    .font(.largeTitle)
                    .fontWeight(.bold)
                
                Text(isLogin ? "Willkommen zurück" : "Account erstellen")
                    .font(.headline)
                    .foregroundColor(.secondary)
            }
            
            // Error
            if let error = viewModel.errorMessage {
                Text(error)
                    .foregroundColor(.red)
                    .font(.caption)
            }
            
            // Form
            VStack(spacing: 16) {
                TextField("E-Mail", text: $email)
                    .textFieldStyle(RoundedBorderTextFieldStyle())
                    .textInputAutocapitalization(.never)
                    .keyboardType(.emailAddress)
                
                SecureField("Passwort", text: $password)
                    .textFieldStyle(RoundedBorderTextFieldStyle())
            }
            
            // Action Button
            Button(action: {
                if isLogin {
                    viewModel.login(email: email, password: password)
                } else {
                    viewModel.register(email: email, password: password)
                }
            }) {
                if viewModel.isLoading {
                    ProgressView()
                        .progressViewStyle(CircularProgressViewStyle(tint: .white))
                } else {
                    Text(isLogin ? "Anmelden" : "Registrieren")
                        .fontWeight(.semibold)
                }
            }
            .buttonStyle(.borderedProminent)
            .tint(.accentColor)
            .disabled(viewModel.isLoading || email.isEmpty || password.count < 6)
            
            // Toggle
            Button(action: { isLogin.toggle() }) {
                Text(isLogin ? "Noch kein Account? Registrieren" : "Bereits Account? Anmelden")
                    .font(.subheadline)
                    .foregroundColor(.accentColor)
            }
            
            Divider()
                .padding(.vertical, 8)
            
            // Guest Mode (PROMINENT)
            VStack(spacing: 8) {
                Button(action: {
                    viewModel.continueAsGuest()
                    onAuthSuccess()
                }) {
                    VStack(spacing: 4) {
                        Text("Als Gast fortfahren")
                            .font(.headline)
                        Text("Keine Registrierung nötig")
                            .font(.caption)
                            .foregroundColor(.secondary)
                    }
                    .frame(maxWidth: .infinity)
                    .padding()
                    .background(
                        RoundedRectangle(cornerRadius: 12)
                            .stroke(Color.accentColor, lineWidth: 2)
                    )
                }
                .foregroundColor(.accentColor)
                
                Text("Deine Daten werden lokal gespeichert. Für Cloud-Sync erstelle einen Account.")
                    .font(.caption2)
                    .foregroundColor(.secondary)
                    .multilineTextAlignment(.center)
            }
        }
        .padding(32)
        .onChange(of: viewModel.isAuthenticated) { authenticated in
            if authenticated {
                onAuthSuccess()
            }
        }
    }
}