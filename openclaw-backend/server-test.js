// Quick test server without MongoDB
const express = require('express');
const app = express();

app.use(express.json());

// Health endpoint
app.get('/health', (req, res) => {
  res.json({ status: 'ok', timestamp: new Date().toISOString(), mode: 'test' });
});

// Mock auth
app.post('/api/v1/auth/register', (req, res) => {
  res.json({ 
    accessToken: 'test-token',
    refreshToken: 'test-refresh',
    user: { email: req.body.email, tier: 'FREE' }
  });
});

// Mock chat with Gemini API integration
app.post('/api/v1/chat/message', async (req, res) => {
  const message = req.body.message;
  
  // Try to use Gemini API
  const apiKey = process.env.GEMINI_API_KEY;
  let responseText;
  
  if (apiKey && apiKey !== 'YOUR_API_KEY') {
    try {
      const fetch = (await import('node-fetch')).default;
      const geminiResponse = await fetch(
        `https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent?key=${apiKey}`,
        {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({
            contents: [{
              parts: [{ text: message }]
            }]
          })
        }
      );
      
      if (geminiResponse.ok) {
        const data = await geminiResponse.json();
        responseText = data.candidates?.[0]?.content?.parts?.[0]?.text || 'Keine Antwort';
      } else {
        responseText = `[Gemini API Error: ${geminiResponse.status}]`;
      }
    } catch (e) {
      responseText = `[API Error: ${e.message}]`;
    }
  } else {
    responseText = `[TEST MODE] Echo: ${message}`;
  }
  
  res.json({
    id: 'msg-' + Date.now(),
    content: responseText,
    model: apiKey ? 'gemini-2.0-flash' : 'test',
    tokens: 10
  });
});

const PORT = 3000;
app.listen(PORT, '0.0.0.0', () => {
  console.log(`✅ OpenClaw Test Server running on port ${PORT}`);
  console.log(`📡 Health: http://localhost:${PORT}/health`);
  console.log(`💬 API: http://localhost:${PORT}/api/v1/chat/message`);
  console.log(`🌐 Network: http://0.0.0.0:${PORT}/health`);
});
