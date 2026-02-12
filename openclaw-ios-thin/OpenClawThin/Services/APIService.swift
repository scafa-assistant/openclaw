//
//  APIService.swift
//  OpenClawThin
//
//  HTTP Kommunikation mit OpenClaw Gateway
//

import Foundation
import Combine

enum APIError: Error, LocalizedError {
    case invalidURL
    case invalidResponse
n    case serverError(Int)
    case decodingError
    case networkError(Error)
    case notConfigured

    var errorDescription: String? {
        switch self {
        case .invalidURL:
            return "Ungültige Server-URL"
        case .invalidResponse:
            return "Ungültige Server-Antwort"
        case .serverError(let code):
            return "Server-Fehler: \(code)"
        case .decodingError:
            return "Fehler beim Verarbeiten der Antwort"
        case .networkError(let error):
            return "Netzwerk-Fehler: \(error.localizedDescription)"
        case .notConfigured:
            return "App nicht konfiguriert"
        }
    }
}

class APIService {
    static let shared = APIService()
    
    private let keychain = KeychainService()
    private var sessionId: String?
    
    private var baseURL: String? {
        keychain.getServerURL()
    }
    
    private var apiKey: String? {
        keychain.getAPIKey()
    }
    
    private var isConfigured: Bool {
        baseURL != nil && apiKey != nil
    }
    
    // MARK: - Test Connection
    func testConnection(serverURL: String, apiKey: String) async throws -> ConnectionTestResponse {
        guard let url = URL(string: "\(serverURL)/api/health") else {
            throw APIError.invalidURL
        }
        
        var request = URLRequest(url: url)
        request.httpMethod = "GET"
        request.setValue(apiKey, forHTTPHeaderField: "X-API-Key")
        request.setValue("application/json", forHTTPHeaderField: "Accept")
        request.timeoutInterval = 10
        
        do {
            let (data, response) = try await URLSession.shared.data(for: request)
            
            guard let httpResponse = response as? HTTPURLResponse else {
                throw APIError.invalidResponse
            }
            
            guard httpResponse.statusCode == 200 else {
                throw APIError.serverError(httpResponse.statusCode)
            }
            
            // Try to decode as ConnectionTestResponse
            if let testResponse = try? JSONDecoder().decode(ConnectionTestResponse.self, from: data) {
                return testResponse
            }
            
            // Fallback for simple health check
            return ConnectionTestResponse(
                status: "ok",
                version: "unknown",
                message: "Verbindung erfolgreich"
            )
        } catch let error as APIError {
            throw error
        } catch {
            throw APIError.networkError(error)
        }
    }
    
    // MARK: - Send Message
    func sendMessage(_ message: String) async throws -> ChatResponse {
        guard isConfigured else {
            throw APIError.notConfigured
        }
        
        guard let baseURL = baseURL,
              let url = URL(string: "\(baseURL)/api/chat") else {
            throw APIError.invalidURL
        }
        
        let requestBody = ChatRequest(
            message: message,
            session_id: sessionId,
            context: nil
        )
        
        var request = URLRequest(url: url)
        request.httpMethod = "POST"
        request.setValue("application/json", forHTTPHeaderField: "Content-Type")
        request.setValue(apiKey, forHTTPHeaderField: "X-API-Key")
        request.httpBody = try JSONEncoder().encode(requestBody)
        request.timeoutInterval = 60
        
        do {
            let (data, response) = try await URLSession.shared.data(for: request)
            
            guard let httpResponse = response as? HTTPURLResponse else {
                throw APIError.invalidResponse
            }
            
            guard httpResponse.statusCode == 200 else {
                throw APIError.serverError(httpResponse.statusCode)
            }
            
            let chatResponse = try JSONDecoder().decode(ChatResponse.self, from: data)
            
            // Update session ID
            if let newSessionId = chatResponse.session_id {
                self.sessionId = newSessionId
            }
            
            return chatResponse
        } catch let error as APIError {
            throw error
        } catch {
            throw APIError.networkError(error)
        }
    }
    
    // MARK: - Reset Session
    func resetSession() {
        sessionId = nil
    }
}
