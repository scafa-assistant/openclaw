# OpenClaw iOS Assistant

## Projekt-Struktur

```
openclaw-ios/
└── OpenClaw/
    ├── OpenClawApp.swift              # App Entry Point
    ├── Models/
    │   └── ChatMessage.swift          # Data Models
    ├── Services/
    │   ├── APIService.swift           # Backend HTTP Client
    │   ├── VoiceService.swift         # STT + TTS
    │   └── AuthService.swift          # Apple Sign-In
    ├── ViewModels/
    │   └── ChatViewModel.swift
    ├── Views/
    │   └── ChatView.swift             # SwiftUI Chat UI
    └── Intents/
        ├── AskOpenClawIntent.swift    # "Hey Siri, frag OpenClaw"
        └── OpenClawShortcuts.swift    # AppShortcutsProvider
```

## Features

- ✅ Siri Integration (AppIntents)
- ✅ Speech-to-Text (iOS Speech Framework)
- ✅ Text-to-Speech (AVSpeechSynthesizer)
- ✅ URLSession API Client
- ✅ SwiftUI Chat Interface
- ✅ Apple Sign-In

## Siri Bridge

User kann sagen:
- "Hey Siri, frag OpenClaw was ist Quantenphysik"
- "Hey Siri, frag OpenClaw wie spät ist es"

## Deep Links

- `openclaw://ask` → Startet Voice Input

## Build

In Xcode:
1. Projekt öffnen
2. Team & Bundle ID konfigurieren
3. Build & Run

## Deployment

1. Apple Developer Account ($99/Jahr)
2. App Store Connect
3. TestFlight Beta
4. App Store Release

## Backend

Benötigt OpenClaw Backend API unter:
- Debug: `http://localhost:3000/api/v1`
- Release: `https://api.openclaw.de/api/v1`

## Credits

Erstellt von: ORACLE (JARVIS SWARM)
Basierend auf: Agent NEO Build Instructions
Datum: 2026-02-08
