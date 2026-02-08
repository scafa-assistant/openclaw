import XCTest
@testable import OpenClaw

class LocalStorageServiceTests: XCTestCase {
    
    override func setUp() {
        super.setUp()
        // Clear UserDefaults before each test
        UserDefaults.standard.removePersistentDomain(forName: Bundle.main.bundleIdentifier!)
    }
    
    func testSaveAndRetrieveAuth() {
        let settings = SettingsManager.shared
        settings.saveAuth(token: "test_token", email: "test@example.com")
        
        XCTAssertEqual(settings.authToken, "test_token")
        XCTAssertEqual(settings.userEmail, "test@example.com")
        XCTAssertTrue(settings.isAuthenticated)
    }
    
    func testClearAuth() {
        let settings = SettingsManager.shared
        settings.saveAuth(token: "test_token", email: "test@example.com")
        settings.clearAuth()
        
        XCTAssertNil(settings.authToken)
        XCTAssertNil(settings.userEmail)
        XCTAssertFalse(settings.isAuthenticated)
    }
    
    func testDefaultSettings() {
        let settings = SettingsManager.shared
        
        XCTAssertEqual(settings.preferredModel, "gemini-2.5-flash")
        XCTAssertEqual(settings.language, "de-DE")
        XCTAssertTrue(settings.ttsEnabled)
        XCTAssertEqual(settings.userTier, "FREE")
    }
    
    func testUpdateSettings() {
        let settings = SettingsManager.shared
        settings.updateSettings(model: "claude-sonnet", language: "en-US", tts: false)
        
        XCTAssertEqual(settings.preferredModel, "claude-sonnet")
        XCTAssertEqual(settings.language, "en-US")
        XCTAssertFalse(settings.ttsEnabled)
    }
}

class ChatMessageTests: XCTestCase {
    
    func testChatMessageCreation() {
        let message = ChatMessage(
            id: "123",
            role: .user,
            content: "Hello",
            timestamp: Date()
        )
        
        XCTAssertEqual(message.id, "123")
        XCTAssertEqual(message.role, .user)
        XCTAssertEqual(message.content, "Hello")
    }
    
    func testChatMessageRoleEnum() {
        let userMessage = ChatMessage(role: .user, content: "Hi")
        let assistantMessage = ChatMessage(role: .assistant, content: "Hello")
        
        XCTAssertEqual(userMessage.role, .user)
        XCTAssertEqual(assistantMessage.role, .assistant)
    }
}

class LocalChatStorageTests: XCTestCase {
    
    override func setUp() {
        super.setUp()
        LocalChatStorage.shared.clearMessages()
        LocalChatStorage.shared.clearPendingMessages()
    }
    
    func testAddAndRetrieveMessages() {
        let storage = LocalChatStorage.shared
        let message = ChatMessage(id: "1", role: .user, content: "Test", timestamp: Date())
        
        storage.addMessage(message)
        
        XCTAssertEqual(storage.messages.count, 1)
        XCTAssertEqual(storage.messages.first?.content, "Test")
    }
    
    func testMessageLimit() {
        let storage = LocalChatStorage.shared
        
        // Add 110 messages
        for i in 0..<110 {
            let message = ChatMessage(id: "\(i)", role: .user, content: "Message \(i)", timestamp: Date())
            storage.addMessage(message)
        }
        
        // Should keep only last 100
        XCTAssertEqual(storage.messages.count, 100)
    }
    
    func testPendingMessages() {
        let storage = LocalChatStorage.shared
        
        storage.queuePendingMessage("Pending message")
        
        XCTAssertEqual(storage.pendingMessages.count, 1)
        XCTAssertEqual(storage.pendingMessages.first?.content, "Pending message")
        
        if let id = storage.pendingMessages.first?.id {
            storage.removePendingMessage(id: id)
        }
        
        XCTAssertTrue(storage.pendingMessages.isEmpty)
    }
}
