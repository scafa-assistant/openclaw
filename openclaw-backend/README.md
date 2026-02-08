# OpenClaw Backend API

## Tech Stack
- **Runtime:** Node.js 18+
- **Framework:** Express.js
- **Database:** MongoDB
- **Auth:** JWT
- **Logging:** Winston

## Setup

### 1. Install dependencies
```bash
npm install
```

### 2. Environment variables
```bash
cp .env.example .env
# Edit .env with your values
```

### 3. Start MongoDB
```bash
# Using Docker
docker run -d -p 27017:27017 --name openclaw-mongo mongo:latest

# Or use MongoDB Atlas (cloud)
```

### 4. Start server
```bash
# Development
npm run dev

# Production
npm start
```

## API Endpoints

### Auth
- `POST /api/v1/auth/register` - Register new user
- `POST /api/v1/auth/login` - Login user
- `POST /api/v1/auth/google` - Google Sign-In
- `POST /api/v1/auth/apple` - Apple Sign-In
- `POST /api/v1/auth/refresh` - Refresh access token
- `GET /api/v1/auth/me` - Get current user

### Chat
- `POST /api/v1/chat/message` - Send message
- `GET /api/v1/chat/history` - Get chat history

### User
- `GET /api/v1/user/profile` - Get user profile
- `GET /api/v1/user/settings` - Get user settings
- `PUT /api/v1/user/settings` - Update user settings
- `POST /api/v1/user/subscription/verify` - Verify subscription

### Health
- `GET /health` - Health check

## Environment Variables

```env
PORT=3000
NODE_ENV=development
MONGODB_URI=mongodb://localhost:27017/openclaw
JWT_SECRET=your-secret-key
JWT_REFRESH_SECRET=your-refresh-secret
CORS_ORIGIN=*

# LLM API Keys (add as needed)
GEMINI_API_KEY=
ANTHROPIC_API_KEY=
OPENAI_API_KEY=
```

## Deployment

### Docker
```bash
docker build -t openclaw-backend .
docker run -p 3000:3000 openclaw-backend
```

### Railway/Render/Heroku
1. Connect GitHub repo
2. Set environment variables
3. Deploy

## Credits

Erstellt von: ORACLE (JARVIS SWARM)
Datum: 2026-02-08
