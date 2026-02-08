# OpenClaw Backend Deployment

**Ziel:** Backend live schalten damit Apps funktionieren

---

## Option 1: Cloudflare Tunnel (Schnellste Lösung)

### Vorteile:
- Keine Firewall-Config nötig
- Kein Port-Forwarding
- Kostenlos
- SSL inklusive

### Schritte:
```bash
# 1. Cloudflare Tunnel installieren
winget install cloudflare.cloudflared

# 2. Tunnel erstellen
cloudflared tunnel login
cloudflared tunnel create openclaw-backend

# 3. Config erstellen (config.yml)
# 4. Tunnel starten
cloudflared tunnel run openclaw-backend
```

### Config (`~/.cloudflared/config.yml`):
```yaml
tunnel: <TUNNEL-ID>
credentials-file: ~/.cloudflared/<TUNNEL-ID>.json

ingress:
  - hostname: api.openclaw.ungehoert.musik
    service: http://localhost:3000
  - service: http_status:404
```

---

## Option 2: Railway/Render (Einfach)

### Railway:
```bash
# 1. Railway CLI installieren
npm install -g @railway/cli

# 2. Login
railway login

# 3. Projekt erstellen
railway init

# 4. Deploy
railway up
```

### Environment Variables:
```
GEMINI_API_KEY=xxx
CLAUDE_API_KEY=xxx
OPENAI_API_KEY=xxx
JWT_SECRET=xxx
MONGODB_URI=xxx
```

---

## Option 3: Hetzner Server (Langfristig)

### Problem:
- SSH Permission denied (Key-Problem)
- Firewall Port 3000 blockiert

### Lösung:
- SSH Key neu deployen
- Oder: Cloud-Init Script nutzen
- Oder: Hetzner Console (Web)

---

## Empfohlene Reihenfolge:

1. **Sofort:** Cloudflare Tunnel (5 Min)
   - Backend sofort erreichbar
   - Tests können beginnen
   
2. **Parallel:** Railway als Backup
   - Cloud-Hosted
   - Keine lokale Firewall-Probleme
   
3. **Langfristig:** Hetzner
   - Volle Kontrolle
   - Kostengünstiger

---

## Domain Config:

### DNS Eintrag:
```
api.openclaw.ungehoert.musik  CNAME  <TUNNEL-ID>.cfargotunnel.com
```

---

## Test nach Deploy:

```bash
# Health Check
curl https://api.openclaw.ungehoert.musik/health

# Auth Test
curl -X POST https://api.openclaw.ungehoert.musik/api/v1/auth/test-accounts

# Chat Test
curl -X POST https://api.openclaw.ungehoert.musik/api/v1/chat/message \
  -H "Authorization: Bearer <TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{"message":"Hallo OpenClaw"}'
```

---

**Next:** Cloudflare Tunnel setup starten?
