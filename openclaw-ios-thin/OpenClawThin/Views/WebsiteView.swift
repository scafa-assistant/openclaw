import SwiftUI
import WebKit

/// WebView for displaying generated websites
struct WebsiteView: View {
    let htmlContent: String
    @Environment(\.dismiss) var dismiss
    
    var body: some View {
        NavigationView {
            WebView(htmlString: htmlContent)
                .navigationTitle("Webseite")
                .navigationBarItems(
                    leading: Button("Schließen") { dismiss() },
                    trailing: HStack {
                        Button(action: shareWebsite) {
                            Image(systemName: "square.and.arrow.up")
                        }
                        Button(action: saveWebsite) {
                            Image(systemName: "arrow.down.circle")
                        }
                    }
                )
        }
    }
    
    private func shareWebsite() {
        // Share sheet
    }
    
    private func saveWebsite() {
        // Save to documents
        let filename = "website_\(Int(Date().timeIntervalSince1970)).html"
        let url = FileManager.default.urls(for: .documentDirectory, in: .userDomainMask)[0]
            .appendingPathComponent(filename)
        
        try? htmlContent.write(to: url, atomically: true, encoding: .utf8)
    }
}

struct WebView: UIViewRepresentable {
    let htmlString: String
    
    func makeUIView(context: Context) -> WKWebView {
        let webView = WKWebView()
        return webView
    }
    
    func updateUIView(_ uiView: WKWebView, context: Context) {
        uiView.loadHTMLString(htmlString, baseURL: nil)
    }
}
