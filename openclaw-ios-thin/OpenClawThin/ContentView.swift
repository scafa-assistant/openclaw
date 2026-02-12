//
//  ContentView.swift
//  OpenClawThin
//
//  Haupt-View mit Navigation
//

import SwiftUI

struct ContentView: View {
    @StateObject private var appState = AppState()
    
    var body: some View {
        Group {
            if appState.isConfigured {
                MainView()
                    .environmentObject(appState)
            } else {
                WelcomeView()
                    .environmentObject(appState)
            }
        }
        .onAppear {
            appState.checkConfiguration()
        }
        .onReceive(NotificationCenter.default.publisher(for: .siriIntentReceived)) { notification in
            if let query = notification.userInfo?["query"] as? String {
                appState.siriQuery = query
                appState.isConfigured = true
            }
        }
    }
}

// MARK: - App State
class AppState: ObservableObject {
    @Published var isConfigured: Bool = false
    @Published var siriQuery: String?
    
    private let keychain = KeychainService()
    
    func checkConfiguration() {
        let hasURL = keychain.getServerURL() != nil
        let hasAPIKey = keychain.getAPIKey() != nil
        isConfigured = hasURL && hasAPIKey
    }
    
    func logout() {
        keychain.clearCredentials()
        isConfigured = false
        siriQuery = nil
    }
}
