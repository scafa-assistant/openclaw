import AuthenticationServices
import SwiftUI

class AuthService: ObservableObject {
    @Published var isAuthenticated = false
    @Published var user: UserProfile?
    @Published var isLoading = false
    @Published var error: String?

    func handleAppleSignIn(_ result: Result<ASAuthorization, Error>) {
        switch result {
        case .success(let auth):
            guard let credential = auth.credential as? ASAuthorizationAppleIDCredential,
                  let tokenData = credential.identityToken,
                  let token = String(data: tokenData, encoding: .utf8)
            else { return }

            Task {
                do {
                    let response = try await APIService.shared.appleSignIn(identityToken: token)
                    await MainActor.run {
                        self.user = response.user
                        self.isAuthenticated = true
                    }
                } catch {
                    self.error = "Auth Error: \(error.localizedDescription)"
                }
            }

        case .failure(let error):
            self.error = "Apple Sign-In Error: \(error.localizedDescription)"
        }
    }

    func logout() {
        APIService.shared.setToken(nil)
        user = nil
        isAuthenticated = false
    }
}
