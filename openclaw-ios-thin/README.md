# OpenClawThin

iOS Thin-Client App für OpenClaw Gateway

## Architektur

- **Thin-Client**: Nur Konfiguration + Weiterleitung an OpenClaw Gateway
- **SiriKit Integration**: "Hey Siri, frag OpenClaw..."
- **Sichere Speicherung**: API-Key im iOS Keychain

## Screens

1. **WelcomeView**: Logo, Willkommen, Einrichten-Button
2. **SetupView**: Server-URL, API-Key, Verbindung testen
3. **MainView**: Chat-Interface mit Spracheingabe
4. **SettingsView**: Einstellungen ändern, Logout

## Siri Integration

### Verwendung

Sage einfach: 
- "Hey Siri, frag OpenClaw was ist auf meiner Einkaufsliste"
- "Hey Siri, frag OpenClaw wie spät es ist"
- "Frag OpenClaw...

### Einrichtung

1. App öffnen und einrichten
2. Einstellungen > Siri & Suchen
3. OpenClawThin aktivieren
4. Shortcuts erstellen

## API Endpoints

Die App kommuniziert mit dem OpenClaw Gateway über:

- `GET /api/health` - Verbindung testen
- `POST /api/chat` - Nachricht senden

## Sicherheit

- API-Key wird im iOS Keychain gespeichert
- HTTPS wird empfohlen (HTTP für lokale Entwicklung erlaubt)
- Session-ID wird nur im Memory gehalten

## Entwicklung

### Voraussetzungen

- Xcode 15.0+
- iOS 16.0+
- Swift 5.7+

### Build

1. Projekt in Xcode öffnen
2. Bundle Identifier anpassen
3. Signing Team auswählen
4. Build & Run

## Lizenz

MIT License
