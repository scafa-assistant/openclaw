# Android Emulator Setup für OpenClaw Testing

**Ziel:** OpenClaw auf PC testen ohne echtes Handy

---

## Schritt 1: Android Studio Installieren

### Download:
```
https://developer.android.com/studio
```

### Installation:
1. Installer herunterladen
2. "Standard" Installation wählen
3. Warten (ca. 10-15 Minuten)

---

## Schritt 2: Emulator Erstellen

### In Android Studio:
1. **Tools → Device Manager**
2. **Create Device**
3. Wähle:
   - Phone: **Pixel 7** (oder aktuelles Modell)
   - System Image: **Android 14 (API 34)**
   - Download falls nötig

### Einstellungen:
```
RAM: 4GB (oder mehr)
Internal Storage: 4GB
VM heap: 512MB
Graphics: Hardware - GLES 2.0
```

---

## Schritt 3: OpenClaw Projekt Öffnen

### In Android Studio:
1. **File → Open**
2. Navigiere zu: `C:\Users\r.scafarti\openclaw-backup\openclaw-android`
3. Warte auf Gradle Sync (erster Start dauert lange!)

### Falls Gradle Sync fehlschlägt:
```
File → Invalidate Caches → Invalidate and Restart
```

---

## Schritt 4: Backend URL Anpassen

### Datei: `app/src/main/java/.../data/api/ApiClient.kt`

Ändere die BASE_URL:
```kotlin
// Alt (für späteren Deploy):
// const val BASE_URL = "https://api.openclaw.ungehoert.musik/"

// Neu (für lokales Testing):
const val BASE_URL = "http://10.0.2.2:3000/"
```

**Wichtig:** `10.0.2.2` ist die Android Emulator IP für localhost!

---

## Schritt 5: App Starten

### 1. Backend starten (Terminal):
```powershell
cd ~/openclaw-backup/openclaw-backend
node server-local.js
```

### 2. Emulator starten:
- In Android Studio: **Device Manager → Play Button**

### 3. App deployen:
- **Run → Run 'app'** (Shift+F10)
- Warte auf Build (erster Mal dauert 5-10 Min)

---

## Schritt 6: Testen!

### Im Emulator:
1. App öffnet sich
2. Onboarding durchklicken
3. Guest Mode wählen
4. Chat öffnet sich
5. **Voice Button testen!**

### Test-Befehle:
```
"Wie spät ist es?"
"Erkläre mir Quantenphysik"
"Schreibe eine E-Mail"
```

---

## Troubleshooting

### Problem: "Unable to locate ADB"
```
File → Settings → Build → SDK Tools → Android SDK Platform-Tools installieren
```

### Problem: Emulator startet nicht
```
BIOS → Virtualisierung aktivieren (Intel VT-x / AMD-V)
```

### Problem: Backend nicht erreichbar
```
# Prüfe ob Backend läuft:
curl http://127.0.0.1:3000/health

# Firewall Check:
# Windows Defender → App zulassen → Node.js
```

### Problem: Mikrofon im Emulator
```
Emulator → Extended Controls → Microphone → Virtual microphone uses host audio input
```

---

## Nächster Schritt

Wenn das läuft:
→ APK Build Guide für echtes Handy!

**Fertig? Dann melde dich und ich mache den APK Build Guide!**
