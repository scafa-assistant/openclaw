const axios = require('axios');

class GeminiService {
  constructor() {
    this.apiKey = process.env.GEMINI_API_KEY;
    this.baseUrl = 'https://generativelanguage.googleapis.com/v1beta';
    this.model = 'gemini-2.5-flash';
  }

  async generateResponse(prompt, conversationHistory = []) {
    if (!this.apiKey) {
      console.warn('GEMINI_API_KEY not set, returning mock response');
      return this.getMockResponse(prompt);
    }

    try {
      const url = `${this.baseUrl}/models/${this.model}:generateContent?key=${this.apiKey}`;
      
      // Build conversation context
      const contents = this.buildContents(prompt, conversationHistory);
      
      const response = await axios.post(url, {
        contents,
        generationConfig: {
          temperature: 0.7,
          maxOutputTokens: 2048,
          topP: 0.9,
          topK: 40
        },
        safetySettings: [
          {
            category: 'HARM_CATEGORY_HARASSMENT',
            threshold: 'BLOCK_MEDIUM_AND_ABOVE'
          },
          {
            category: 'HARM_CATEGORY_HATE_SPEECH',
            threshold: 'BLOCK_MEDIUM_AND_ABOVE'
          }
        ]
      }, {
        timeout: 30000
      });

      const text = response.data.candidates?.[0]?.content?.parts?.[0]?.text || '';
      const usage = response.data.usageMetadata;

      return {
        content: text,
        tokens: usage?.totalTokenCount || 0,
        model: this.model,
        provider: 'gemini'
      };
    } catch (error) {
      console.error('Gemini API error:', error.message);
      if (error.response) {
        console.error('Response:', error.response.data);
      }
      return this.getMockResponse(prompt);
    }
  }

  buildContents(prompt, history) {
    const contents = [];
    
    // Add system instruction
    contents.push({
      role: 'user',
      parts: [{ text: 'Du bist OpenClaw, ein hilfreicher AI-Assistent. Antworte präzise und hilfreich auf Deutsch.' }]
    });
    contents.push({
      role: 'model',
      parts: [{ text: 'Verstanden. Ich bin OpenClaw, bereit zu helfen.' }]
    });

    // Add conversation history
    for (const msg of history.slice(-10)) { // Keep last 10 messages
      contents.push({
        role: msg.role === 'user' ? 'user' : 'model',
        parts: [{ text: msg.content }]
      });
    }

    // Add current prompt
    contents.push({
      role: 'user',
      parts: [{ text: prompt }]
    });

    return contents;
  }

  getMockResponse(prompt) {
    return {
      content: `[DEMO] Das ist eine Demo-Antwort auf: "${prompt.substring(0, 50)}..."\n\nIn der Produktionsversion wird hier Gemini antworten. Bitte GEMINI_API_KEY in den Environment Variables setzen.`,
      tokens: 50,
      model: this.model,
      provider: 'gemini',
      isMock: true
    };
  }
}

module.exports = new GeminiService();
