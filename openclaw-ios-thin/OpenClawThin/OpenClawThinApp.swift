import SwiftUI

/// Main app entry point
@main
struct OpenClawThinApp: App {
    @UIApplicationDelegateAdaptor(AppDelegate.self) var appDelegate
    
    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}

/// App Delegate for handling deeplinks
class AppDelegate: NSObject, UIApplicationDelegate {
    func application(_ app: UIApplication, open url: URL, options: [UIApplication.OpenURLOptionsKey : Any] = [:]) -> Bool {
        // Handle Siri shortcuts / deeplinks
        if url.scheme == "openclaw" {
            NotificationCenter.default.post(name: .openclawDeeplink, object: url)
            return true
        }
        return false
    }
}

extension Notification.Name {
    static let openclawDeeplink = Notification.Name("openclawDeeplink")
}
