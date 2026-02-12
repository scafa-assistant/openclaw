# iOS BUILD GUIDE - OpenClaw Assistant
**Für: Gigi (René Scarfarti)**  
**Erstellt:** CEO-Modus | JARVIS SWARM v3.2

---

## 🎯 Übersicht

Voraussetzung: Du brauchst einen Mac mit Xcode für den iOS Build.

## 📋 Pre-Build Checkliste

- [ ] Mac mit macOS 14+ (Sonoma)
- [ ] Xcode 15+ installiert
- [ ] Apple Developer Account ($99/Jahr) - für App Store
- [ ] ODER Apple ID (kostenlos) - für Ad-Hoc Installation auf eigenem Gerät

---

## 🚀 Build-Optionen

### Option A: Ad-Hoc (Kostenlos, nur deine Geräte)
Für interne Tests ohne Developer Account.

### Option B: App Store (Empfohlen, $99/Jahr)
Für öffentliche Verteilung.

---

## 🛠️ Build Schritte

### 1. Projekt öffnen
```bash
cd ~/openclaw-backup/openclaw-ios
open OpenClaw.xcodeproj
```

### 2. Signing & Capabilities konfigurieren
- In Xcode: **OpenClaw** Target → **Signing & Capabilities**
- **Team**: Deine Apple ID auswählen
- **Bundle Identifier**: `de.openclaw.assistant` (ändern falls Konflikt)
- **Automatically manage signing**: ✅ Aktivieren

### 3. SiriKit Entitlements
WICHTIG: SiriKit braucht spezielle Provisioning:
- Capabilities → **+ Capability** → **Siri** hinzufügen
- **Intents Extension** Target → Auch Signing konfigurieren

### 4. Build Varianten

#### Debug Build (Simulator)
```bash
# Im Terminal:
xcodebuild -project OpenClaw.xcodeproj \
  -scheme OpenClaw \
  -destination 'platform=iOS Simulator,name=iPhone 15' \
  build
```

#### Release Build (Gerät)
```bash
# Clean build
xcodebuild clean -project OpenClaw.xcodeproj -scheme OpenClaw

# Archive erstellen
xcodebuild archive \
  -project OpenClaw.xcodeproj \
  -scheme OpenClaw \
  -destination 'generic/platform=iOS' \
  -archivePath build/OpenClaw.xcarchive
```

### 5. IPA Export (Ad-Hoc)

**ExportOptions.plist** erstellen (bereits im Ordner):
```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE plist PUBLIC "-//Apple//DTD PLIST 1.0//EN" "http://www.apple.com/DTDs/PropertyList-1.0.dtd">
<plist version="1.0">
<dict>
    <key>method</key>
    <string>ad-hoc</string>
    <key>teamID</key>
    <string>DEINE_TEAM_ID</string>
    <key>provisioningProfiles</key>
    <dict>
        <key>de.openclaw.assistant</key>
        <string>OpenClaw AdHoc Profile</string>
    </dict>
</dict>
</plist>
```

**Export Command:**
```bash
xcodebuild -exportArchive \
  -archivePath build/OpenClaw.xcarchive \
  -exportOptionsPlist ExportOptions.plist \
  -exportPath build/IPA
```

**Ergebnis:** `build/IPA/OpenClaw.ipa` (~15-20MB)

---

## 📱 Installation auf Gerät

### Ohne App Store (Ad-Hoc):
1. **Apple Configurator 2** (kostenlos aus Mac App Store)
2. iPhone per USB anschließen
3. IPA Datei auf Gerät ziehen

### Mit TestFlight (Empfohlen):
1. App Store Connect → My Apps → New App
2. Build hochladen (Xcode → Product → Archive → Distribute)
3. Internal Testing hinzufügen
4. Einladung auf TestFlight per Email

---

## 🔧 App Store Upload (Final)

```bash
# App Store Connect API Key einrichten (einmalig)
xcrun altool --store-password-in-keychain-item "AC_API_KEY" \
  -u "user@example.com" -p "app-specific-password"

# Upload
xcrun altool --upload-app \
  --type ios \
  --file build/IPA/OpenClaw.ipa \
  --apiKey YOUR_API_KEY \
  --apiIssuer YOUR_ISSUER_ID
```

---

## ⚠️ Bekannte Issues & Lösungen

### Issue 1: SiriKit Provisioning
**Fehler:** "Siri entitlement not allowed"
**Fix:** 
- developer.apple.com → Certificates, Identifiers & Profiles
- Neue App ID erstellen mit Siri Capability
- Provisioning Profile neu generieren

### Issue 2: Widget Extension
**Fehler:** Widget lässt sich nicht bauen
**Fix:** Auch Widget-Target unter Signing & Capabilities konfigurieren

### Issue 3: Bundle ID Konflikt
**Fehler:** "Bundle identifier already taken"
**Fix:** Ändere zu `de.ungehoert.openclaw` oder eindeutige Variante

---

## 📊 Projekt-Struktur (Wichtige Files)

```
openclaw-ios/
├── OpenClaw/
│   ├── OpenClawApp.swift          # Main App Entry
│   ├── Info.plist                 # App Konfiguration
│   ├── Intents/
│   │   ├── OpenClawIntents.swift  # SiriKit Intents
│   │   └── SiriKitIntegration.swift
│   ├── Services/
│   │   ├── APIService.swift       # Backend API Calls
│   │   ├── VoiceService.swift     # TTS/STT
│   │   └── HybridVoiceService.swift # Siri/Google Integration
│   └── Views/
│       ├── ChatView.swift         # Haupt-UI
│       └── OnboardingView.swift   # Erste Einrichtung
└── OpenClaw.xcodeproj/            # Xcode Projekt
```

---

## 🎯 CEO Entscheidung

**Empfohlene Reihenfolge:**
1. **Ad-Hoc Build** auf deinem iPhone testen (kostenlos)
2. **TestFlight** Beta mit Freunden (Developer Account)
3. **App Store** Release (nach Android Launch)

**Zeitschätzung:**
- Erster Build: 30 Min (inkl. Xcode Download)
- Ad-Hoc IPA: 5 Min
- App Store Upload: 15 Min
- Review Wartezeit: 1-2 Tage

---

**Status:** ✅ Build-Guide erstellt | 🔄 Warte auf Mac für Build

*JARVIS SWARM v3.2 – CEO Execution Mode*
