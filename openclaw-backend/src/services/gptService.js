const axios = require('axios');

class GPTService {
  constructor() {
    this.apiKey = process.env.OPENAI_API_KEY;
    this.baseUrl = 'https://api.openai.com/v1';
    this.model = 'gpt-4o-mini';
  }

  async generateResponse(prompt, conversationHistory = []) {
    if (!this.apiKey) {
      console.warn('OPENAI_API_KEY not set, returning mock response');
      return this.getMockResponse(prompt);
    }

    try {
      const messages = this.buildMessages(prompt, conversationHistory);
      
      const response = await axios.post(
        `${this.baseUrl}/chat/completions`,
        {
          model: this.model,
          messages,
          temperature: 0.7,
          max_tokens: 2048,
          top_p: 0.9
        },
        {
          headers: {
            'Authorization': `Bearer ${this.apiKey}`,
            'Content-Type': 'application/json'
          },
          timeout: 30000
        }
      );

      return {
        content: response.data.choices[0].message.content,
        tokens: response.data.usage?.total_tokens || 0,
        model: this.model,
        provider: 'openai'
      };
    } catch (error) {
      console.error('OpenAI API error:', error.message);
      return this.getMockResponse(prompt);
    }
  }

  buildMessages(prompt, history) {
    const messages = [
      {
        role: 'system',
        content: 'Du bist OpenClaw, ein hilfreicher AI-Assistent. Antworte präzise und hilfreich auf Deutsch.'
      }
    ];
    
    for (const msg of history.slice(-10)) {
      messages.push({
        role: msg.role,
        content: msg.content
      });
    }

    messages.push({
      role: 'user',
      content: prompt
    });

    return messages;
  }

  getMockResponse(prompt) {
    return {
      content: `[DEMO - GPT] Antwort auf: "${prompt.substring(0, 50)}..."\n\nBitte OPENAI_API_KEY setzen für echte GPT-Antworten.`,
      tokens: 35,
      model: this.model,
      provider: 'openai',
      isMock: true
    };
  }
}

module.exports = new GPTService();
