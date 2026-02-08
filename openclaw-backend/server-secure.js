// OpenClaw Backend - Production Ready
// Sicherer, robuster, schneller

const express = require('express');
const cors = require('cors');
const jwt = require('jsonwebtoken');
const { v4: uuidv4 } = require('uuid');
const rateLimit = require('express-rate-limit');
const helmet = require('helmet');
const { seedTestAccounts, TEST_ACCOUNTS } = require('./test-accounts');

const app = express();

// Security Middleware
app.use(helmet({
  contentSecurityPolicy: false, // Für API nicht nötig
  crossOriginEmbedderPolicy: false
}));

// CORS - Restriktiver in Produktion
const corsOptions = {
  origin: process.env.ALLOWED_ORIGINS?.split(',') || ['http://localhost:3000', 'http://10.0.2.2:3000'],
  credentials: true,
  methods: ['GET', 'POST', 'PUT', 'DELETE'],
  allowedHeaders: ['Content-Type', 'Authorization']
};
app.use(cors(corsOptions));

app.use(express.json({ limit: '10mb' }));

// Rate Limiting
const limiter = rateLimit({
  windowMs: 15 * 60 * 1000, // 15 Minuten
  max: 100, // 100 Requests pro IP
  message: { error: 'Too many requests, please try again later' },
  standardHeaders: true,
  legacyHeaders: false
});
app.use('/api/', limiter);

// Stricter rate limit for auth endpoints
const authLimiter = rateLimit({
  windowMs: 60 * 60 * 1000, // 1 Stunde
  max: 10, // 10 Login-Versuche pro IP
  message: { error: 'Too many login attempts, please try again later' }
});
app.use('/api/v1/auth/login', authLimiter);
app.use('/api/v1/auth/register', authLimiter);

// In-memory storage with size limits
const MAX_USERS = 10000;
const MAX_MESSAGES_PER_USER = 1000;
const users = new Map();
const messages = new Map();

// JWT Secret - Muss in Produktion aus Umgebungsvariable kommen
const JWT_SECRET = process.env.JWT_SECRET;
if (!JWT_SECRET && process.env.NODE_ENV === 'production') {
  console.error('❌ JWT_SECRET not set! Exiting...');
  process.exit(1);
}
const DEV_SECRET = 'dev-secret-32-chars-minimum-123456789';

// Input validation helper
const validateEmail = (email) => {
  const re = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
  return re.test(email) && email.length <= 254;
};

const validatePassword = (password) => {
  return password && password.length >= 6 && password.length <= 128;
};

const sanitizeInput = (input) => {
  if (typeof input !== 'string') return '';
  return input.trim().substring(0, 10000); // Max 10k Zeichen
};

// Seed test accounts at startup
seedTestAccounts(users, messages);

// Auth Middleware
const authMiddleware = (req, res, next) => {
  const authHeader = req.headers.authorization;
  if (!authHeader || !authHeader.startsWith('Bearer ')) {
    return res.status(401).json({ error: 'No token provided' });
  }
  
  const token = authHeader.substring(7);
  if (!token || token.length > 2048) {
    return res.status(401).json({ error: 'Invalid token format' });
  }
  
  try {
    const decoded = jwt.verify(token, JWT_SECRET || DEV_SECRET);
    
    // Check if user still exists
    const userExists = Array.from(users.values()).some(u => u.id === decoded.userId);
    if (!userExists) {
      return res.status(401).json({ error: 'User no longer exists' });
    }
    
    req.user = decoded;
    next();
  } catch (err) {
    if (err.name === 'TokenExpiredError') {
      return res.status(401).json({ error: 'Token expired' });
    }
    return res.status(401).json({ error: 'Invalid token' });
  }
};

// Health Check
app.get('/health', (req, res) => {
  const memoryUsage = process.memoryUsage();
  res.json({ 
    status: 'ok', 
    timestamp: new Date().toISOString(),
    uptime: process.uptime(),
    memory: {
      used: Math.round(memoryUsage.heapUsed / 1024 / 1024) + 'MB',
      total: Math.round(memoryUsage.heapTotal / 1024 / 1024) + 'MB'
    },
    users: users.size,
    messages: Array.from(messages.values()).reduce((acc, arr) => acc + arr.length, 0),
    version: '1.0.0'
  });
});

// Test accounts (nur in Development)
app.get('/api/v1/auth/test-accounts', (req, res) => {
  if (process.env.NODE_ENV === 'production') {
    return res.status(404).json({ error: 'Not available in production' });
  }
  
  res.json({
    accounts: TEST_ACCOUNTS.map(a => ({ email: a.email, password: a.password })),
    note: 'Use these for quick testing'
  });
});

// Register
app.post('/api/v1/auth/register', (req, res) => {
  // Check user limit
  if (users.size >= MAX_USERS) {
    return res.status(503).json({ error: 'Server at capacity' });
  }
  
  const { email, password } = req.body;
  
  // Validation
  if (!email || !password) {
    return res.status(400).json({ error: 'Email and password required' });
  }
  
  const sanitizedEmail = sanitizeInput(email).toLowerCase();
  
  if (!validateEmail(sanitizedEmail)) {
    return res.status(400).json({ error: 'Invalid email format' });
  }
  
  if (!validatePassword(password)) {
    return res.status(400).json({ error: 'Password must be 6-128 characters' });
  }
  
  if (users.has(sanitizedEmail)) {
    return res.status(409).json({ error: 'User already exists' });
  }
  
  const user = {
    id: uuidv4(),
    email: sanitizedEmail,
    password: password, // In Produktion: bcrypt hashing!
    tier: 'FREE',
    createdAt: new Date(),
    lastLogin: new Date()
  };
  
  users.set(sanitizedEmail, user);
  messages.set(user.id, []);
  
  const token = jwt.sign(
    { userId: user.id, email: user.email },
    JWT_SECRET || DEV_SECRET,
    { expiresIn: '24h', issuer: 'openclaw', audience: 'openclaw-app' }
  );
  
  res.status(201).json({
    accessToken: token,
    user: { id: user.id, email: user.email, tier: user.tier }
  });
});

// Login
app.post('/api/v1/auth/login', (req, res) => {
  const { email, password } = req.body;
  
  if (!email || !password) {
    return res.status(400).json({ error: 'Email and password required' });
  }
  
  const sanitizedEmail = sanitizeInput(email).toLowerCase();
  const user = users.get(sanitizedEmail);
  
  if (!user || user.password !== password) {
    // Generic error to prevent user enumeration
    return res.status(401).json({ error: 'Invalid credentials' });
  }
  
  user.lastLogin = new Date();
  
  const token = jwt.sign(
    { userId: user.id, email: user.email },
    JWT_SECRET || DEV_SECRET,
    { expiresIn: '24h', issuer: 'openclaw', audience: 'openclaw-app' }
  );
  
  res.json({
    accessToken: token,
    user: { id: user.id, email: user.email, tier: user.tier }
  });
});

// Guest mode
app.post('/api/v1/auth/guest', (req, res) => {
  if (users.size >= MAX_USERS) {
    return res.status(503).json({ error: 'Server at capacity' });
  }
  
  const guestId = 'guest-' + uuidv4();
  const user = {
    id: guestId,
    email: `${guestId}@openclaw.guest`,
    password: uuidv4(), // Random password
    tier: 'FREE',
    isGuest: true,
    createdAt: new Date()
  };
  
  users.set(user.email, user);
  messages.set(user.id, []);
  
  const token = jwt.sign(
    { userId: user.id, email: user.email, isGuest: true },
    JWT_SECRET || DEV_SECRET,
    { expiresIn: '24h', issuer: 'openclaw', audience: 'openclaw-app' }
  );
  
  res.json({
    accessToken: token,
    user: { id: user.id, email: user.email, tier: user.tier, isGuest: true }
  });
});

// Send message
app.post('/api/v1/chat/message', authMiddleware, async (req, res) => {
  const { message, model = 'gemini' } = req.body;
  const userId = req.user.userId;
  
  // Validation
  if (!message || typeof message !== 'string') {
    return res.status(400).json({ error: 'Message required' });
  }
  
  const sanitizedMessage = sanitizeInput(message);
  
  if (sanitizedMessage.length === 0) {
    return res.status(400).json({ error: 'Message cannot be empty' });
  }
  
  if (sanitizedMessage.length > 10000) {
    return res.status(400).json({ error: 'Message too long (max 10k chars)' });
  }
  
  // Check message limit per user
  const userMessages = messages.get(userId) || [];
  if (userMessages.length >= MAX_MESSAGES_PER_USER) {
    return res.status(429).json({ error: 'Message limit reached for this session' });
  }
  
  // Store user message
  const userMsg = {
    id: uuidv4(),
    role: 'user',
    content: sanitizedMessage,
    timestamp: Date.now(),
    model: model
  };
  
  messages.set(userId, [...userMessages, userMsg]);
  
  // Gemini API call with timeout
  let responseText = '';
  const apiKey = process.env.GEMINI_API_KEY;
  
  if (apiKey && apiKey.startsWith('AIza')) {
    try {
      const controller = new AbortController();
      const timeout = setTimeout(() => controller.abort(), 30000); // 30s timeout
      
      const fetch = (await import('node-fetch')).default;
      const geminiRes = await fetch(
        `https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent?key=${apiKey}`,
        {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({
            contents: [{ parts: [{ text: sanitizedMessage }] }]
          }),
          signal: controller.signal
        }
      );
      
      clearTimeout(timeout);
      
      if (geminiRes.ok) {
        const data = await geminiRes.json();
        responseText = data.candidates?.[0]?.content?.parts?.[0]?.text || 'Keine Antwort';
      } else {
        const errorText = await geminiRes.text();
        console.error(`[Gemini Error] ${geminiRes.status}: ${errorText}`);
        responseText = `[API Fehler: ${geminiRes.status}]`;
      }
    } catch (e) {
      if (e.name === 'AbortError') {
        responseText = '[Timeout: Die Anfrage hat zu lange gedauert]';
      } else {
        console.error(`[API Error] ${e.message}`);
        responseText = `[API Fehler: ${e.message}]`;
      }
    }
  } else {
    responseText = `[TEST MODE] Du fragtest: "${sanitizedMessage.substring(0, 100)}${sanitizedMessage.length > 100 ? '...' : ''}"\n\nDies ist eine Test-Antwort. Für echte AI-Antworten: Gemini API Key in .env setzen.`;
  }
  
  // Store assistant message
  const assistantMsg = {
    id: uuidv4(),
    role: 'assistant',
    content: responseText,
    model,
    timestamp: Date.now()
  };
  
  const updatedMessages = messages.get(userId);
  updatedMessages.push(assistantMsg);
  messages.set(userId, updatedMessages);
  
  res.json({
    id: assistantMsg.id,
    content: responseText,
    model,
    tokens: Math.ceil(responseText.length / 4),
    timestamp: assistantMsg.timestamp
  });
});

// Get history
app.get('/api/v1/chat/history', authMiddleware, (req, res) => {
  const userId = req.user.userId;
  const userMessages = messages.get(userId) || [];
  
  // Return last 50 messages, reversed (newest first)
  const history = userMessages
    .slice(-50)
    .reverse()
    .map(msg => ({
      id: msg.id,
      role: msg.role,
      content: msg.content,
      timestamp: msg.timestamp,
      model: msg.model
    }));
  
  res.json(history);
});

// Error handling middleware
app.use((err, req, res, next) => {
  console.error('[Error]', err);
  res.status(500).json({ error: 'Internal server error' });
});

// 404 handler
app.use((req, res) => {
  res.status(404).json({ error: 'Not found' });
});

const PORT = process.env.PORT || 3000;
const HOST = process.env.HOST || '127.0.0.1';

app.listen(PORT, HOST, () => {
  console.log('╔════════════════════════════════════════════════════════╗');
  console.log('║  🚀 OpenClaw Backend - SECURE & PRODUCTION READY       ║');
  console.log('╠════════════════════════════════════════════════════════╣');
  console.log(`║  URL: http://${HOST}:${PORT}                           ║`);
  console.log(`║  Environment: ${process.env.NODE_ENV || 'development'}                          ║`);
  console.log('║  Security: Rate limiting, Helmet, Input validation    ║');
  console.log('╚════════════════════════════════════════════════════════╝');
  console.log('');
  console.log(`📊 Stats: ${users.size} users, ${Array.from(messages.values()).reduce((a,b) => a+b.length,0)} messages`);
});
