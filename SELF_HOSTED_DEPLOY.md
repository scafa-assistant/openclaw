# OpenClaw Self-Hosted Deployment

## Option 1: Lokale Entwicklung (Sofort)

```bash
# 1. Repository vorbereiten
cd projects/openclaw-backend

# 2. .env erstellen mit vorhandenen Keys
cat > .env << 'EOF'
PORT=3000
NODE_ENV=development
MONGODB_URI=mongodb://localhost:27017/openclaw
JWT_SECRET=dev-secret-$(openssl rand -hex 16)
JWT_REFRESH_SECRET=dev-refresh-$(openssl rand -hex 16)
CORS_ORIGIN=*
GEMINI_API_KEY=AIzaSyBSG9fkuY4Kj-B3nG3Y6vHk0i8MFRsEZg8
EOF

# 3. MongoDB starten (Docker)
docker run -d -p 27017:27017 --name openclaw-mongo mongo:latest

# 4. Backend starten
npm install
npm run dev

# 5. API Test
curl http://localhost:3000/health
```

**API läuft lokal:** `http://localhost:3000/api/v1`

---

## Option 2: Docker Compose (Self-Hosted)

```bash
# 1. Auf Server (Hetzner) oder lokal
cd projects

# 2. .env erstellen
cat > .env << 'EOF'
JWT_SECRET=$(openssl rand -hex 32)
JWT_REFRESH_SECRET=$(openssl rand -hex 32)
CORS_ORIGIN=https://api.ungehoert.musik
GEMINI_API_KEY=AIzaSyBSG9fkuY4Kj-B3nG3Y6vHk0i8MFRsEZg8
EOF

# 3. Starten
docker-compose -f docker-compose.prod.yml up -d

# 4. Logs prüfen
docker-compose -f docker-compose.prod.yml logs -f backend
```

---

## Option 3: Hetzner Server (Wenn SSH klappt)

```bash
# Auf dem Server:
mkdir -p /opt/openclaw
cd /opt/openclaw

# Code kopieren (via Git oder SCP)
git clone https://github.com/USER/openclaw-assistant.git .

# Docker Compose starten
docker-compose -f docker-compose.prod.yml up -d

# SSL mit Let's Encrypt (Certbot)
docker run -it --rm \
  -v certbot-data:/etc/letsencrypt \
  -v /var/www/certbot:/var/www/certbot \
  certbot/certbot certonly --webroot -w /var/www/certbot \
  -d api.ungehoert.musik --agree-tos -m admin@ungehoert.musik
```

---

## Option 4: Cloudflare Tunnel (Keine Server-Konfig nötig)

```bash
# 1. Cloudflared installieren
# https://developers.cloudflare.com/cloudflare-one/connections/connect-networks/downloads/

# 2. Tunnel erstellen
cloudflared tunnel create openclaw-api

# 3. Config erstellen
cloudflared tunnel route dns openclaw-api api.ungehoert.musik

# 4. Tunnel starten
cloudflared tunnel run openclaw-api
```

---

## Domain Konfiguration

### DNS Records für ungehoert.musik:

```
Type: A
Name: api
Value: SERVER_IP (oder Cloudflare Tunnel)
TTL: Auto
```

Oder für Subdomain:
```
Type: CNAME
Name: openclaw
Value: cname.vercel-dns.com (für Vercel)
# oder
Value: railway.app (für Railway)
```

---

## Verfügbare API Keys (aus ~/.openclaw/.env)

| Service | Key | Verwendung |
|---------|-----|------------|
| Google/Gemini | `AIzaSyBSG9fkuY4Kj-B3nG3Y6vHk0i8MFRsEZg8` | ✅ LLM Backend |
| Moonshot | `sk-sTLEthYosheyZv1gSP5XdwZL7bu4NVJK7FhLGycnbmN0Kooz` | ✅ Alternative LLM |

---

## Schnell-Test

```bash
# Health Check
curl http://localhost:3000/health

# Auth Test
curl -X POST http://localhost:3000/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{"email":"test@example.com","password":"test123"}'

# Chat Test
curl -X POST http://localhost:3000/api/v1/chat/message \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -d '{"message":"Hallo OpenClaw!"}'
```

---

## Mobile Apps verbinden

### Android
```kotlin
// In ApiClient.kt ändern:
private const val BASE_URL = "http://DEINE_SERVER_IP:3000/api/v1/"
// oder für Production:
private const val BASE_URL = "https://api.ungehoert.musik/api/v1/"
```

### iOS
```swift
// In APIService.swift ändern:
private let baseURL = "https://api.ungehoert.musik/api/v1"
```

---

**Wähle Option 1 für sofortigen Test, Option 2/3 für Production.**
