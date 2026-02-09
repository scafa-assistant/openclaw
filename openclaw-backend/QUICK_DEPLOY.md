# OpenClaw Backend - Quick Deploy

## Option 1: Railway (Empfohlen - 2 Minuten)

```bash
cd ~/openclaw-backup/openclaw-backend

# Install Railway CLI
npm install -g @railway/cli

# Login
railway login

# Init project
railway init --name openclaw-backend

# Set environment variables
railway variables set GEMINI_API_KEY="your-key"
railway variables set CLAUDE_API_KEY="your-key"
railway variables set OPENAI_API_KEY="your-key"
railway variables set JWT_SECRET="$(openssl rand -base64 32)"
railway variables set PORT="3000"

# Deploy
railway up

# Get domain
railway domain
```

**Fertig!** Backend ist live unter `https://openclaw-backend.up.railway.app`

---

## Option 2: Cloudflare Tunnel (Lokal, aber öffentlich)

```bash
# Install cloudflared
winget install cloudflare.cloudflared

# Login
cloudflared tunnel login

# Create tunnel
cloudflared tunnel create openclaw-backend

# Create config
mkdir -p ~/.cloudflared
cat > ~/.cloudflared/config.yml << 'EOF'
tunnel: YOUR-TUNNEL-ID
credentials-file: ~/.cloudflared/YOUR-TUNNEL-ID.json

ingress:
  - hostname: api.openclaw.local
    service: http://localhost:3000
  - service: http_status:404
EOF

# Start tunnel (in new terminal)
cloudflared tunnel run openclaw-backend
```

---

## Environment Variables (Alle Optionen)

| Variable | Beschreibung | Required |
|----------|-------------|----------|
| `GEMINI_API_KEY` | Google Gemini API | Ja |
| `CLAUDE_API_KEY` | Anthropic Claude API | Ja |
| `OPENAI_API_KEY` | OpenAI GPT API | Optional |
| `JWT_SECRET` | Zufälliger String für Tokens | Ja |
| `PORT` | Server Port (default: 3000) | Nein |
| `MONGODB_URI` | MongoDB Connection | Nein (In-Memory Fallback) |
| `REDIS_URL` | Redis für Caching | Nein (Memory Fallback) |

---

## Test Commands

```bash
# Health Check
curl https://YOUR-URL/health

# Auth
curl -X POST https://YOUR-URL/api/v1/auth/guest

# Chat
curl -X POST https://YOUR-URL/api/v1/chat/message \
  -H "Content-Type: application/json" \
  -d '{"message":"Hallo"}'
```
