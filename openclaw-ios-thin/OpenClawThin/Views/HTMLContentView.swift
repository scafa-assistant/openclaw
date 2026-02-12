//
//  HTMLContentView.swift
//  OpenClawThin
//
//  WKWebView für HTML-Antworten
//

import SwiftUI
import WebKit

struct HTMLContentView: View {
    let html: String
    @Environment(\.dismiss) private var dismiss
    
    var body: some View {
        NavigationView {
            WebView(html: html)
                .navigationTitle("Details")
                .navigationBarTitleDisplayMode(.inline)
                .toolbar {
                    ToolbarItem(placement: .navigationBarTrailing) {
                        Button("Fertig") {
                            dismiss()
                        }
                    }
                }
        }
    }
}

// MARK: - WebView UIViewRepresentable
struct WebView: UIViewRepresentable {
    let html: String
    
    func makeUIView(context: Context) -> WKWebView {
        let webView = WKWebView()
        webView.backgroundColor = .systemBackground
        return webView
    }
    
    func updateUIView(_ webView: WKWebView, context: Context) {
        // Wrap HTML in a nice template
        let styledHTML = """
        <!DOCTYPE html>
        <html>
        <head>
            <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
            <style>
                body {
                    font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, Helvetica, Arial, sans-serif;
                    font-size: 16px;
                    line-height: 1.6;
                    color: #333;
                    padding: 20px;
                    margin: 0;
                    background-color: #f5f5f5;
                }
                .container {
                    background-color: white;
                    border-radius: 12px;
                    padding: 20px;
                    box-shadow: 0 2px 8px rgba(0,0,0,0.1);
                }
                h1, h2, h3 {
                    color: #1a1a1a;
                    margin-top: 0;
                }
                a {
                    color: #007AFF;
                    text-decoration: none;
                }
                table {
                    width: 100%;
                    border-collapse: collapse;
                    margin: 16px 0;
                }
                th, td {
                    text-align: left;
                    padding: 12px;
                    border-bottom: 1px solid #eee;
                }
                th {
                    background-color: #f8f8f8;
                    font-weight: 600;
                }
                code {
                    background-color: #f4f4f4;
                    padding: 2px 6px;
                    border-radius: 4px;
                    font-family: "SF Mono", Monaco, monospace;
                    font-size: 14px;
                }
                pre {
                    background-color: #f4f4f4;
                    padding: 16px;
                    border-radius: 8px;
                    overflow-x: auto;
                }
                pre code {
                    background-color: transparent;
                    padding: 0;
                }
            </style>
        </head>
        <body>
            <div class="container">
                \(html)
            </div>
        </body>
        </html>
        """
        
        webView.loadHTMLString(styledHTML, baseURL: nil)
    }
}

#Preview {
    HTMLContentView(html: "<h1>Test</h1><p>Dies ist ein Test.</p>")
}
