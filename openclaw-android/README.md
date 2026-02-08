# OpenClaw Android Assistant

## Projekt-Struktur

```
openclaw-android/
├── .github/workflows/android.yml    # GitHub Actions CI/CD
├── build.gradle.kts                 # Project level build
├── app/build.gradle.kts            # App level build
├── app/src/main/
│   ├── AndroidManifest.xml         # VoiceInteractionService config
│   ├── java/de/openclaw/assistant/
│   │   ├── MainActivity.kt
│   │   ├── OpenClawApplication.kt
│   │   ├── service/
│   │   │   ├── OpenClawVoiceService.kt      # VoiceInteractionService
│   │   │   ├── OpenClawSessionService.kt
│   │   │   ├── OpenClawSession.kt
│   │   │   └── OpenClawRecognitionService.kt
│   │   ├── voice/
│   │   │   ├── SpeechToTextManager.kt       # Android SpeechRecognizer
│   │   │   └── TextToSpeechManager.kt       # Android TTS
│   │   ├── data/
│   │   │   ├── model/ChatMessage.kt
│   │   │   └── api/
│   │   │       ├── OpenClawApi.kt
│   │   │       └── ApiClient.kt
│   │   ├── viewmodel/
│   │   │   ├── ChatViewModel.kt
│   │   │   └── AuthViewModel.kt
│   │   └── ui/
│   │       ├── screens/ChatScreen.kt
│   │       └── theme/
│   │           ├── Color.kt
│   │           ├── Theme.kt
│   │           └── Type.kt
│   └── res/
│       ├── xml/voice_interaction_service.xml
│       └── values/
│           ├── strings.xml
│           ├── colors.xml
│           └── themes.xml
└── gradle/wrapper/
    ├── gradle-wrapper.properties
    └── gradle-wrapper.jar
```

## Features

- ✅ VoiceInteractionService (ersetzt Google Assistant)
- ✅ Speech-to-Text (Android SpeechRecognizer)
- ✅ Text-to-Speech (Android TTS)
- ✅ Retrofit API Client
- ✅ Jetpack Compose UI
- ✅ Chat Interface
- ✅ Auth (Login/Register)

## Build

Lokal:
```bash
./gradlew assembleDebug
```

Via GitHub Actions:
- Push zu `main` oder `develop` Branch
- APK wird automatisch gebaut und als Artifact hochgeladen

## Deployment

1. GitHub Repository erstellen
2. Code pushen
3. GitHub Actions baut APK
4. APK herunterladen und testen
5. Google Play Store Release

## Backend

Benötigt OpenClaw Backend API unter:
- Debug: `http://10.0.2.2:3000/api/v1`
- Release: `https://api.openclaw.de/api/v1`

## Credits

Erstellt von: ORACLE (JARVIS SWARM)
Basierend auf: Agent MORPHEUS Build Instructions
Datum: 2026-02-08
