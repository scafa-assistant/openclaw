# OpenClaw Thin Client - iOS

## Projektübersicht

Dies ist eine komplette iOS Thin-Client App für OpenClaw mit folgenden Features:

### ✅ Implementierte Features

- **SwiftUI Interface**: Moderne, native iOS UI
- **SiriKit Integration**: "Hey Siri, frag OpenClaw..."
- **Spracheingabe**: Mikrofon-Button mit Speech Recognition
- **HTML-Anzeige**: WKWebView für formatierte Antworten
- **Keychain-Speicherung**: Sichere API-Key Speicherung
- **Session-Management**: Automatische Session-IDs

### 📱 Screens

1. **WelcomeView**: Logo, Willkommen, Einrichten-Button
2. **SetupView**: Server-URL, API-Key, Verbindung testen
3. **MainView**: Chat-Interface mit ScrollView
4. **SettingsView**: Einstellungen ändern, Logout

### 🗣️ Siri Integration

```
"Hey Siri, frag OpenClaw was ist auf meiner Einkaufsliste"
"Hey Siri, frag OpenClaw wie das Wetter wird"
"Frag OpenClaw..."
```

### 📁 Dateistruktur

```
OpenClawThin.xcodeproj/          # Xcode Projekt
OpenClawThin/
├── OpenClawThinApp.swift        # App Entry Point
├── ContentView.swift            # Haupt-Navigation
├── Models/
│   └── Models.swift             # ChatMessage, API Response
├── Services/
│   ├── APIService.swift         # URLSession HTTP Client
│   ├── KeychainService.swift    # Sichere Credentials
│   └── SpeechRecognitionService.swift  # Siri/Speech
├── Views/
│   ├── WelcomeView.swift        # Begrüßung
│   ├── SetupView.swift          # Konfiguration
│   ├── MainView.swift           # Chat Interface
│   ├── SettingsView.swift       # Einstellungen
│   └── HTMLContentView.swift    # WKWebView Wrapper
├── Intents/
│   ├── AskOpenClawIntent.swift  # AppIntent Definition
│   └── IntentHandler.swift      # Intent Handler
├── Utils/
│   └── Extensions.swift         # Swift Extensions
├── Assets.xcassets/             # Icons & Assets
└── Info.plist                   # App Configuration
```

### 🚀 Quick Start

1. **Projekt öffnen**:
   ```bash
   cd ~/openclaw-backup/openclaw-ios-thin
   open OpenClawThin.xcodeproj
   ```

2. **Xcode einrichten**:
   - Bundle Identifier anpassen (z.B. `deinname.openclaw`)
   - Signing Team auswählen
   - Deployment Target: iOS 16.0+

3. **Siri aktivieren**:
   - Target > Signing & Capabilities
   - + Capability > Siri

4. **Build & Run** auf iPhone oder Simulator

### 🔧 Konfiguration

Die App speichert folgende Daten sicher im iOS Keychain:
- **Server-URL**: Die OpenClaw Gateway URL
- **API-Key**: Der Authentifizierungsschlüssel

### 🔌 API Endpoints

```
GET  /api/health     - Verbindung testen
POST /api/chat       - Nachricht senden
```

Request:
```json
{
  "message": "Hallo OpenClaw",
  "session_id": "optional-session-id"
}
```

Response:
```json
{
  "response": "Hallo! Wie kann ich helfen?",
  "html_response": "<p>Hallo! Wie kann ich helfen?</p>",
  "session_id": "session-id"
}
```

### 📝 Lizenz

MIT License - Open Source
