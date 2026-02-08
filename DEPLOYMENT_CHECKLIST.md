## 🚀 Deployment Checklist

### Step 1: Accounts erstellen (Manuell erforderlich)

#### 1.1 GitHub Account
- [ ] Account erstellen: https://github.com/signup
- [ ] Repository erstellen: `openclaw-assistant`
- [ ] Personal Access Token generieren (Settings → Developer settings → PAT)
- [ ] Token in `~/.openclaw/.env` speichern: `GITHUB_TOKEN=ghp_...`

#### 1.2 Railway Account
- [ ] Account erstellen: https://railway.app
- [ ] Mit GitHub verbinden
- [ ] Warten auf Email-Verifikation

#### 1.3 MongoDB Atlas
- [ ] Account erstellen: https://www.mongodb.com/cloud/atlas/register
- [ ] Free Tier Cluster erstellen (M0)
- [ ] Database User erstellen
- [ ] Network Access: Allow from anywhere (0.0.0.0/0)
- [ ] Connection String kopieren

#### 1.4 Gemini API Key
- [ ] Google AI Studio: https://aistudio.google.com/app/apikey
- [ ] API Key generieren
- [ ] Quota prüfen (Free: 60 requests/min)

#### 1.5 Domain (Optional)
- [ ] Domain kaufen: https://www.namecheap.com oder https://www.cloudflare.com
- [ ] DNS A-Record auf Railway IP setzen

---

### Step 2: Backend deployen

```bash
# 1. Environment Variablen setzen
cp projects/openclaw-backend/.env.example projects/openclaw-backend/.env
# Edit .env with your values

# 2. Railway CLI installieren
npm install -g @railway/cli

# 3. Login & Project erstellen
railway login
railway init --name openclaw-backend

# 4. Environment Variablen in Railway setzen
railway variables set PORT=3000
railway variables set NODE_ENV=production
railway variables set MONGODB_URI="your-mongodb-uri"
railway variables set JWT_SECRET="$(openssl rand -hex 32)"
railway variables set JWT_REFRESH_SECRET="$(openssl rand -hex 32)"
railway variables set GEMINI_API_KEY="your-key"

# 5. Deploy
railway up

# 6. Domain hinzufügen (optional)
railway domain
```

---

### Step 3: GitHub Push

```bash
# 1. Remote hinzufügen
cd projects
git remote add origin https://github.com/YOUR_USERNAME/openclaw-assistant.git

# 2. Push
git branch -M main
git push -u origin main

# 3. GitHub Actions prüfen
# Gehe zu: https://github.com/YOUR_USERNAME/openclaw-assistant/actions
```

---

### Step 4: Android Build

```bash
# 1. Repository klonen (oder lokal)
git clone https://github.com/YOUR_USERNAME/openclaw-assistant.git
cd openclaw-assistant/openclaw-android

# 2. Build
./gradlew assembleRelease

# 3. APK finden
# app/build/outputs/apk/release/app-release.apk
```

---

### Step 5: iOS Build (Mac + Xcode erforderlich)

```bash
# 1. Repository klonen
git clone https://github.com/YOUR_USERNAME/openclaw-assistant.git
cd openclaw-assistant/openclaw-ios

# 2. In Xcode öffnen
open OpenClaw.xcodeproj

# 3. Signing & Capabilities konfigurieren
# - Apple ID hinzufügen
# - Team auswählen
# - Bundle ID: de.openclaw.assistant

# 4. Build
# Product → Archive
```

---

## ⚠️ HÄUFIGE FEHLER

### MongoDB Connection Failed
- **Lösung:** Network Access auf 0.0.0.0/0 setzen
- **Oder:** Railway IP whitelisten (Railway hat keine static IPs)

### JWT Secret zu kurz
- **Lösung:** Mindestens 32 Zeichen, besser: `openssl rand -hex 64`

### Gemini API Rate Limit
- **Lösung:** Free Tier = 60 req/min. Für mehr: Upgrade oder mehrere Keys rotieren

### CORS Fehler
- **Lösung:** `CORS_ORIGIN` auf Frontend-Domain setzen, nicht `*`

---

## 📱 APP STORE SUBMISSION

### Google Play Store
1. Play Console kaufen: https://play.google.com/console ($25)
2. App erstellen
3. Privacy Policy URL: `https://yourdomain.com/privacy`
4. Content Rating ausfüllen
5. APK upload

### Apple App Store
1. Apple Dev Account: https://developer.apple.com ($99/Jahr)
2. App Store Connect: https://appstoreconnect.apple.com
3. App registrieren
4. TestFlight Beta
5. Submission

---

## 🔧 POST-DEPLOYMENT

### Monitoring
- Railway Dashboard: Logs, Metrics
- MongoDB Atlas: Performance Insights
- Uptime Monitoring: https://upptime.js.org (optional)

### Backups
- MongoDB Atlas: Automated backups (M0 = 7 Tage)
- Manual exports: `mongodump` für wichtige Daten

### Updates
```bash
# Backend update
git commit -am "Update"
git push
# Railway deployed automatisch

# Mobile Apps
# Neue Version builden → Store Update
```

---

*Letzte Aktualisierung: 2026-02-08*
