# 🚀 OpenClaw Assistant Platform

**Eine vollständige AI-Assistant-Plattform für Android, iOS & Web.**

> Built by ORACLE (JARVIS SWARM) — 2026-02-08

---

## 📱 Plattform-Übersicht

| Plattform | Status | Features | Lines of Code |
|-----------|--------|----------|---------------|
| **Android** | ✅ Production-Ready | Voice, Widget, Room DB, Tests | ~3000 |
| **iOS** | ✅ Production-Ready | Siri, WidgetKit, SwiftData, Tests | ~2500 |
| **Backend** | ✅ Production-Ready | Node.js, MongoDB, JWT, Multi-LLM | ~2000 |

---

## ✨ Features

### Core Features (Alle Plattformen)
- 🎤 **Voice-First Interface** — Spracheingabe als primäre Interaktion
- 🤖 **Multi-LLM Support** — Gemini, Claude, GPT (wählbar)
- 🔒 **Sichere Auth** — JWT + OAuth (Google, Apple)
- 💾 **Offline-First** — Lokale Speicherung, Sync wenn online
- 📱 **Widgets** — Schnellzugriff vom Home Screen
- 🎯 **Onboarding** — Guided Setup für neue Nutzer

### Android-Spezifisch
- Ersetzt Google Assistant (VoiceInteractionService)
- App Widget mit Quick Actions
- Room Database für Chat-Verlauf
- Material Design 3

### iOS-Spezifisch
- Siri Shortcuts Integration
- WidgetKit (small/medium/large)
- SwiftData/UserDefaults
- SwiftUI mit nativer Performance

### Backend
- Node.js + Express
- MongoDB mit Mongoose
- JWT Authentication
- Gemini/Claude/GPT APIs
- Docker + GitHub Actions

---

## 🏗️ Architektur

```
┌─────────────────┐     ┌─────────────────┐     ┌─────────────────┐
│   Android App   │     │     iOS App     │     │  Web Dashboard  │
│   (Kotlin)      │     │    (Swift)      │     │   (Future)      │
└────────┬────────┘     └────────┬────────┘     └─────────────────┘
         │                       │
         └───────────┬───────────┘
                     │ HTTPS
                     ▼
         ┌─────────────────────────┐
         │   OpenClaw Backend API  │
         │   (Node.js/Express)     │
         └───────────┬─────────────┘
                     │
         ┌───────────┼───────────┐
         ▼           ▼           ▼
    ┌────────┐  ┌────────┐  ┌────────┐
    │ MongoDB│  │ Gemini │  │ Claude │  ...
    │        │  │  API   │  │  API   │
    └────────┘  └────────┘  └────────┘
```

---

## 🚀 Quick Start

### Backend
```bash
cd openclaw-backend
npm install
cp .env.example .env
# Add your API keys to .env
npm run dev
```

### Android
```bash
cd openclaw-android
./gradlew assembleDebug
# Or open in Android Studio
```

### iOS
```bash
cd openclaw-ios
open OpenClaw.xcodeproj
# Build and run in Xcode
```

---

## 📁 Projektstruktur

```
projects/
├── openclaw-android/          # Android App (Kotlin)
│   ├── app/src/main/java/     # Source Code
│   │   ├── data/              # API, Database, Models
│   │   ├── service/           # Voice Services
│   │   ├── ui/screens/        # Composable Screens
│   │   ├── util/              # Error Handling, Retry
│   │   ├── viewmodel/         # MVVM ViewModels
│   │   └── voice/             # STT/TTS Managers
│   └── app/src/test/          # Unit Tests
│
├── openclaw-ios/              # iOS App (Swift)
│   ├── OpenClaw/
│   │   ├── Intents/           # Siri Shortcuts
│   │   ├── Models/            # Data Models
│   │   ├── Services/          # API, Storage, Voice
│   │   ├── ViewModels/        # MVVM
│   │   ├── Views/             # SwiftUI Views
│   │   └── OpenClawApp.swift  # Entry Point
│   ├── OpenClawWidget/        # Widget Extension
│   └── OpenClawTests/         # Unit Tests
│
├── openclaw-backend/          # Backend API (Node.js)
│   ├── src/routes/            # API Routes
│   ├── src/models/            # MongoDB Models
│   ├── src/middleware/        # Auth, Validation
│   ├── src/services/          # LLM Services
│   └── Dockerfile             # Container Config
│
├── .github/workflows/         # CI/CD
│   ├── android.yml            # Android Build
│   ├── deploy-backend.yml     # Railway Deploy
│   └── deploy-backend-render.yml
│
├── PRIVACY_POLICY.md          # GDPR Compliant
├── STORE_LISTING.md           # App Store Text
└── SCREENSHOTS_GUIDE.md       # Screenshot Specs
```

---

## 🔧 Technologie-Stack

### Android
- **Language:** Kotlin
- **UI:** Jetpack Compose
- **DI:** Hilt (prepared)
- **DB:** Room
- **Network:** Retrofit + OkHttp
- **Storage:** DataStore
- **Testing:** JUnit, MockK, Robolectric

### iOS
- **Language:** Swift
- **UI:** SwiftUI
- **Storage:** UserDefaults, SwiftData
- **Network:** URLSession
- **Voice:** AVFoundation, Speech
- **Testing:** XCTest

### Backend
- **Runtime:** Node.js 18+
- **Framework:** Express.js
- **Database:** MongoDB
- **Auth:** JWT
- **LLMs:** Gemini, Claude, GPT APIs
- **Deployment:** Docker, Railway/Render

---

## 🔐 Environment Variables

### Backend (.env)
```env
PORT=3000
NODE_ENV=production
MONGODB_URI=mongodb+srv://...
JWT_SECRET=your-secret-key
JWT_REFRESH_SECRET=your-refresh-secret

# LLM API Keys
GEMINI_API_KEY=
ANTHROPIC_API_KEY=
OPENAI_API_KEY=
```

---

## 🧪 Testing

### Android
```bash
./gradlew test              # Unit Tests
./gradlew connectedCheck    # Instrumented Tests
```

### iOS
```bash
cmd+U in Xcode  # Run Tests
```

### Backend
```bash
npm test
```

---

## 📦 Deployment

### Backend → Railway
```bash
railway login
railway link
railway up
```

### Android → Play Store
1. `./gradlew assembleRelease`
2. Sign APK
3. Upload to Google Play Console

### iOS → App Store
1. Archive in Xcode
2. Upload to App Store Connect
3. Submit for Review

---

## 📝 API Endpoints

| Endpoint | Method | Auth | Description |
|----------|--------|------|-------------|
| `/api/v1/auth/register` | POST | No | User Registration |
| `/api/v1/auth/login` | POST | No | User Login |
| `/api/v1/auth/me` | GET | Yes | Get Current User |
| `/api/v1/chat/message` | POST | Yes | Send Message |
| `/api/v1/chat/history` | GET | Yes | Get History |
| `/api/v1/user/settings` | GET/PUT | Yes | User Settings |
| `/health` | GET | No | Health Check |

---

## 📊 Git History

```
90d4292 🔧 Integration: Manifest, Gradle, App Entry Points
1038e89 🎯 Widgets, Onboarding & LLM Integration
6b9cd02 🎨 Phase 3: Store Assets & Documentation
785b97d ✨ Phase 2: Error Handling, State Persistence & Tests
6d2fd30 🚀 Initial commit: OpenClaw Assistant Platform
```

**Total:** 91 files, ~8000 lines of code

---

## 👥 Credits

- **Architecture & Lead:** ORACLE (JARVIS SWARM)
- **Platform:** OpenClaw
- **Purpose:** AI Assistant für ungehört. Musik-Label

---

## 📄 License

Proprietary — All rights reserved.

---

*Last Updated: 2026-02-08*
