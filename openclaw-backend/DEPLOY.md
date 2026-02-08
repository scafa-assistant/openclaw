# Quick Deploy Guide

## Option 1: Railway (Empfohlen)
1. Account erstellen: https://railway.app
2. Neues Projekt → Deploy from GitHub repo
3. MongoDB Atlas URL als Environment Variable setzen
4. JWT_SECRET generieren und setzen
5. Deploy → Backend läuft unter `https://openclaw-backend.up.railway.app`

## Option 2: Render
1. Account erstellen: https://render.com
2. New Web Service → Connect GitHub repo
3. Root Directory: `projects/openclaw-backend`
4. Build Command: `npm install`
5. Start Command: `node server.js`
6. Environment Variables setzen (siehe .env.example)
7. Deploy

## Option 3: Heroku
```bash
heroku create openclaw-backend
heroku addons:create mongolab:sandbox
heroku config:set JWT_SECRET=your-secret
heroku config:set NODE_ENV=production
git push heroku main
```

## Option 4: Self-Hosted (Docker)
```bash
cd projects/openclaw-backend
docker build -t openclaw-backend .
docker run -p 3000:3000 -e MONGODB_URI=your-mongo-uri -e JWT_SECRET=your-secret openclaw-backend
```

## Erforderliche Environment Variables
```
PORT=3000
NODE_ENV=production
MONGODB_URI=mongodb+srv://user:pass@cluster.mongodb.net/openclaw
JWT_SECRET=random-generated-secret-min-32-chars
JWT_REFRESH_SECRET=another-random-secret
CORS_ORIGIN=*
```

## MongoDB Atlas Setup (Kostenlos)
1. https://www.mongodb.com/cloud/atlas/register
2. Free Tier wählen
3. Cluster erstellen
4. Database Access → User erstellen
5. Network Access → Allow from anywhere (0.0.0.0/0)
6. Connect → Connection String kopieren

## Nach dem Deploy
- Health Check: `GET https://your-domain/health`
- API Base: `https://your-domain/api/v1`

**Status:** Bereit für Deployment ✅
