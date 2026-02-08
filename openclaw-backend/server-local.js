// Simple local backend without MongoDB - using in-memory storage
const express = require('express');
const cors = require('cors');
const jwt = require('jsonwebtoken');
const { v4: uuidv4 } = require('uuid');

const app = express();
app.use(cors());
app.use(express.json());

// In-memory storage
const users = new Map();
const messages = new Map();

// JWT Secret
const JWT_SECRET = process.env.JWT_SECRET || 'dev-secret-32-chars-minimum';

// Middleware
const authMiddleware = (req, res, next) => {
  const token = req.headers.authorization?.replace('Bearer ', '');
  if (!token) return res.status(401).json({ error: 'No token' });
  try {
    req.user = jwt.verify(token, JWT_SECRET);
    next();
  } catch {
    res.status(401).json({ error: 'Invalid token' });
  }
};

// Health
app.get('/health', (req, res) => {
  res.json({ 
    status: 'ok', 
    timestamp: new Date().toISOString(),
    mode: 'local-test',
    users: users.size,
    messages: Array.from(messages.values()).reduce((acc, arr) => acc + arr.length, 0)
  });
});

// Register
app.post('/api/v1/auth/register', (req, res) => {
  const { email, password } = req.body;
  if (users.has(email)) {
    return res.status(400).json({ error: 'User exists' });
  }
  
  const user = {
    id: uuidv4(),
    email,
    password, // In real: hash this!
    tier: 'FREE',
    createdAt: new Date()
  };
  users.set(email, user);
  messages.set(user.id, []);
  
  const token = jwt.sign({ userId: user.id, email }, JWT_SECRET, { expiresIn: '24h' });
  
  res.json({
    accessToken: token,
    refreshToken: token, // Simplified
    user: { id: user.id, email: user.email, tier: user.tier }
  });
});

// Login
app.post('/api/v1/auth/login', (req, res) => {
  const { email, password } = req.body;
  const user = users.get(email);
  
  if (!user || user.password !== password) {
    return res.status(400).json({ error: 'Invalid credentials' });
  }
  
  const token = jwt.sign({ userId: user.id, email }, JWT_SECRET, { expiresIn: '24h' });
  
  res.json({
    accessToken: token,
    refreshToken: token,
    user: { id: user.id, email: user.email, tier: user.tier }
  });
});

// Send message
app.post('/api/v1/chat/message', authMiddleware, async (req, res) => {
  const { message, model = 'gemini' } = req.body;
  const userId = req.user.userId;
  
  // Store user message
  const userMsg = {
    id: uuidv4(),
    role: 'user',
    content: message,
    timestamp: Date.now()
  };
  
  if (!messages.has(userId)) messages.set(userId, []);
  messages.get(userId).push(userMsg);
  
  // Gemini API call
  let responseText = '';
  const apiKey = process.env.GEMINI_API_KEY;
  
  if (apiKey && apiKey.startsWith('AIza')) {
    try {
      const fetch = (await import('node-fetch')).default;
      const geminiRes = await fetch(
        `https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent?key=${apiKey}`,
        {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({
            contents: [{ parts: [{ text: message }] }]
          })
        }
      );
      
      if (geminiRes.ok) {
        const data = await geminiRes.json();
        responseText = data.candidates?.[0]?.content?.parts?.[0]?.text || 'Keine Antwort';
      } else {
        responseText = `[Gemini API Fehler: ${geminiRes.status}]`;
      }
    } catch (e) {
      responseText = `[API Fehler: ${e.message}]`;
    }
  } else {
    responseText = `[TEST MODE] Du fragtest: "${message}"\n\nDies ist eine Test-Antwort. Für echte AI-Antworten: Gemini API Key in .env setzen.`;
  }
  
  // Store assistant message
  const assistantMsg = {
    id: uuidv4(),
    role: 'assistant',
    content: responseText,
    model,
    timestamp: Date.now()
  };
  messages.get(userId).push(assistantMsg);
  
  res.json({
    id: assistantMsg.id,
    content: responseText,
    model,
    tokens: Math.ceil(responseText.length / 4)
  });
});

// Get history
app.get('/api/v1/chat/history', authMiddleware, (req, res) => {
  const userId = req.user.userId;
  const userMessages = messages.get(userId) || [];
  res.json(userMessages.slice(-50).reverse());
});

const PORT = 3000;
app.listen(PORT, '127.0.0.1', () => {
  console.log('╔════════════════════════════════════════════════════════╗');
  console.log('║  🚀 OpenClaw Backend - LOCAL TEST SERVER              ║');
  console.log('╠════════════════════════════════════════════════════════╣');
  console.log(`║  URL: http://127.0.0.1:${PORT}                        ║`);
  console.log('║  Health: http://127.0.0.1:3000/health                 ║');
  console.log('║  API: http://127.0.0.1:3000/api/v1/chat/message       ║');
  console.log('╚════════════════════════════════════════════════════════╝');
  console.log('');
  console.log('📋 TEST COMMANDS:');
  console.log('  curl http://127.0.0.1:3000/health');
  console.log('  curl -X POST http://127.0.0.1:3000/api/v1/auth/register -H "Content-Type: application/json" -d \'{"email":"test@test.de","password":"test123"}\'');
});
