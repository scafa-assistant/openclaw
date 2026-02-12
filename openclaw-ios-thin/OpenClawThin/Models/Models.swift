//
//  Models.swift
//  OpenClawThin
//
//  Datenmodelle für die App
//

import Foundation

// MARK: - Chat Message
struct ChatMessage: Identifiable, Equatable {
    let id = UUID()
    let content: String
    let isUser: Bool
    let timestamp: Date
    var htmlContent: String?
    
    static func == (lhs: ChatMessage, rhs: ChatMessage) -> Bool {
        lhs.id == rhs.id
    }
}

// MARK: - API Request/Response
struct ChatRequest: Codable {
    let message: String
    let session_id: String?
    let context: [String: String]?
}

struct ChatResponse: Codable {
    let response: String
    let html_response: String?
    let session_id: String?
    let actions: [Action]?
}

struct Action: Codable {
    let type: String
    let payload: [String: String]?
}

// MARK: - Connection Test Response
struct ConnectionTestResponse: Codable {
    let status: String
    let version: String?
    let message: String?
}

// MARK: - Siri Intent Query
struct SiriQuery {
    let text: String
    let timestamp: Date
}
