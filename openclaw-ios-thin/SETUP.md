# OpenClawThin Setup

## Schnellstart

### 1. Projekt öffnen
```bash
open OpenClawThin.xcodeproj
```

### 2. Konfiguration anpassen

In Xcode:
- Bundle Identifier ändern (z.B. `deinname.openclaw`)
- Signing & Capabilities > Team auswählen

### 3. Siri aktivieren

- Target > Capabilities > Siri hinzufügen
- Target > Info > Privacy - Siri Usage Description (bereits in Info.plist)

### 4. Build & Run

Auf iPhone oder Simulator ausführen

## Features

✅ SwiftUI Interface
✅ SiriKit Integration
✅ Spracheingabe (Mikrofon)
✅ HTML-Content Anzeige
✅ Keychain Speicherung
✅ Session Management

## App Struktur

```
OpenClawThin/
├── OpenClawThinApp.swift      # App Entry Point
├── ContentView.swift          # Haupt-Navigation
├── Models/
│   └── Models.swift           # Datenmodelle
├── Services/
│   ├── APIService.swift       # HTTP Kommunikation
│   ├── KeychainService.swift  # Sichere Speicherung
│   └── SpeechRecognitionService.swift  # Spracherkennung
├── Views/
│   ├── WelcomeView.swift      # Begrüßung
│   ├── SetupView.swift        # Konfiguration
│   ├── MainView.swift         # Chat
│   ├── SettingsView.swift     # Einstellungen
│   └── HTMLContentView.swift  # WebView
├── Intents/
│   ├── AskOpenClawIntent.swift    # Siri Intent
│   └── IntentHandler.swift    # Intent Handler
├── Assets.xcassets/           # Icons & Assets
└── Info.plist                 # App Configuration
```

## Server-URL Format

- `http://192.168.1.100:18789`
- `https://openclaw.example.com`
- `http://localhost:18789` (Simulator)

## Bekannte Probleme

1. **Siri funktioniert nicht**: Stelle sicher dass Siri in den Geräteeinstellungen aktiviert ist
2. **Keine Verbindung**: Prüfe Firewall und Netzwerk-Einstellungen
3. **Mikrofon nicht verfügbar**: Berechtigungen in Einstellungen > OpenClawThin prüfen
