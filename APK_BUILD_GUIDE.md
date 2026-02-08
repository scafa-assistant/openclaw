# APK Build Guide für OpenClaw

**Ziel:** APK erstellen für Installation auf echtes Android Handy

---

## Option 1: Lokaler Build (Android Studio)

### Voraussetzungen:
- Android Studio installiert
- Projekt geöffnet (`~/openclaw-backup/openclaw-android`)

### Build Schritte:

#### 1. Release Variant wählen:
```
Build → Select Build Variant → release
```

#### 2. Keystore erstellen (einmalig):
```
Build → Generate Signed Bundle/APK → APK
Create new...
Key store path: ~/openclaw-release.keystore
Password: [wähle sicheres Passwort]
Key alias: openclaw
Validity: 25 years
```

#### 3. APK bauen:
```
Build → Generate Signed Bundle/APK → APK
→ Wähle Keystore
→ Release
→ Build
```

#### 4. APK finden:
```
app/release/app-release.apk
```

---

## Option 2: GitHub Actions (Cloud Build)

### Vorteile:
- Kein Android Studio nötig
- Automatisch bei jedem Push
- Downloadbare APK

### Setup:

#### 1. GitHub Repo erstellen
#### 2. Secrets hinzufügen:
```
Settings → Secrets → New repository secret
- KEYSTORE_BASE64: [Base64 encoded keystore]
- KEYSTORE_PASSWORD: [Passwort]
- KEY_ALIAS: openclaw
- KEY_PASSWORD: [Passwort]
```

#### 3. Workflow läuft automatisch:
```
.github/workflows/android.yml
```

#### 4. APK herunterladen:
```
Actions → [Workflow Run] → Artifacts → app-release.apk
```

---

## Option 3: Schnell-APK (Debug)

### Für sofortiges Testen:
```
Build → Build Bundle(s) / APK(s) → Build APK(s)
```

**Ausgabe:** `app/build/outputs/apk/debug/app-debug.apk`

**Nachteil:** Debug-APK ist nicht optimiert

---

## APK auf Handy installieren

### Methode 1: USB
```
1. USB Debugging aktivieren:
   Handy → Einstellungen → Entwickleroptionen → USB-Debugging

2. Per ADB installieren:
   adb install app-release.apk
```

### Methode 2: Download
```
1. APK auf Google Drive hochladen
2. Auf Handy herunterladen
3. "Unbekannte Quellen" erlauben
4. Installieren
```

### Methode 3: QR Code
```
1. APK auf temporären Server (ngrok)
2. QR Code generieren
3. Handy scannt → Download → Install
```

---

## Backend für Handy freigeben

### Problem:
Handy kann nicht auf `localhost:3000` zugreifen

### Lösungen:

#### A) Gleiches WiFi + ngrok (EINFACH):
```bash
# Auf PC:
npx ngrok http 3000

# Gibt URL: https://abc123.ngrok.io
# Diese URL in App eintragen
```

#### B) Lokale IP nutzen:
```kotlin
// In ApiClient.kt
// Statt 10.0.2.2 (nur Emulator):
const val BASE_URL = "http://192.168.1.100:3000/"  // Deine PC IP
```

**PC IP herausfinden:**
```powershell
ipconfig
# Suche nach: IPv4 Address
```

#### C) Hotspot:
```
1. PC macht Hotspot
2. Handy verbindet mit PC-Hotspot
3. Dann geht localhost:3000
```

---

## Checkliste vor APK-Test

- [ ] Backend läuft auf PC
- [ ] PC und Handy im gleichen Netzwerk
- [ ] API_URL in App auf PC-IP gesetzt
- [ ] Firewall: Port 3000 erlaubt
- [ ] APK gebaut und auf Handy

---

## Test-Plan auf echtem Handy

### 1. Installation:
```
APK installieren → Öffnen → Berechtigungen erlauben
```

### 2. Onboarding:
```
3 Screens durchklicken → Gast Mode
```

### 3. Voice Test:
```
"Hey Google, OpenClaw" → App öffnet
Oder: Home-Button halten → OpenClaw wählen
```

### 4. Chat Test:
```
"Wie spät ist es?" → Antwort?
"Erkläre Quantenphysik" → Antwort?
```

### 5. Backend-Verbindung:
```
Im Chat: Siehst du "Verbunden" Status?
```

---

## Troubleshooting

### "App nicht installiert"
→ Unbekannte Quellen erlauben
→ Oder: Debug-APK statt Release nutzen

### "Keine Verbindung zum Server"
→ PC IP checken
→ Firewall Port 3000 öffnen
→ Gleiches WiFi?

### "Mikrofon funktioniert nicht"
→ Berechtigung erteilt?
→ In App-Einstellungen checken

---

## Fertig?

Wenn alles läuft:
→ Play Store Upload Guide!

**Probleme? Melde dich!** 🚀
