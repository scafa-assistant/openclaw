# GitHub Actions Setup für OpenClaw

**Automatische APK Builds bei jedem Push!**

---

## 🚀 Schnellstart

### 1. Repository auf GitHub erstellen
```bash
# Auf GitHub.com:
# New Repository → "openclaw" → Create
```

### 2. Code pushen
```bash
cd ~/openclaw-backup
git remote add origin https://github.com/GIGI-USERNAME/openclaw.git
git branch -M main
git push -u origin main
```

### 3. GitHub Actions läuft automatisch!
- Gehe zu: `github.com/GIGI-USERNAME/openclaw/actions`
- Jeder Push startet automatisch einen Build
- APK wird als Artifact hochgeladen

---

## 📱 APK Download

### Nach erfolgreichem Build:
1. GitHub Repository öffnen
2. **Actions** Tab klicken
3. Neuesten Workflow auswählen
4. **Artifacts** Section
5. `openclaw-debug-apk` herunterladen
6. Auf Android Handy installieren

---

## 🔐 Release Build (Signiert)

Für signierte Release-APKs:

### 1. Keystore erstellen (lokal)
```bash
cd openclaw-android
keytool -genkey -v -keystore release.keystore -alias openclaw -keyalg RSA -keysize 2048 -validity 10000
```

### 2. Base64 encodieren
```bash
base64 -i release.keystore -o keystore.base64
```

### 3. GitHub Secrets hinzufügen
```
GitHub → Settings → Secrets → New repository secret

- KEYSTORE_BASE64: [Inhalt von keystore.base64]
- KEYSTORE_PASSWORD: [Dein Passwort]
- KEY_ALIAS: openclaw
- KEY_PASSWORD: [Dein Passwort]
```

### 4. Release Build starten
```
Actions → Android CI/CD → Run workflow → build_type: release
```

---

## ⚙️ Workflow Features

### Automatisch bei:
- ✅ Jedem Push auf main/master
- ✅ Jedem Pull Request
- ✅ Manuellem Start (workflow_dispatch)

### Erstellt:
- ✅ Debug APK (schnell, für Tests)
- ✅ Release APK (optimiert, signiert)
- ✅ Test Reports
- ✅ Build Logs

---

## 🔧 Troubleshooting

### "Build failed"
→ Actions Tab → Failed Build → Logs ansehen

### "No artifacts"
→ Build noch nicht fertig? Warte 5-10 Minuten

### "App not installed"
→ Unbekannte Quellen erlauben (Android Einstellungen)

---

## 📊 Build-Status

[![Android CI/CD](https://github.com/GIGI-USERNAME/openclaw/actions/workflows/android.yml/badge.svg)](https://github.com/GIGI-USERNAME/openclaw/actions)

**Badge in README einfügen nach erstem Build!**

---

**Jetzt bei jedem Push: Automatisch APK bauen!** 🚀
