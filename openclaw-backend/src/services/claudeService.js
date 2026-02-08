const axios = require('axios');

class ClaudeService {
  constructor() {
    this.apiKey = process.env.ANTHROPIC_API_KEY;
    this.baseUrl = 'https://api.anthropic.com/v1';
    this.model = 'claude-3-5-sonnet-20241022';
  }

  async generateResponse(prompt, conversationHistory = []) {
    if (!this.apiKey) {
      console.warn('ANTHROPIC_API_KEY not set, returning mock response');
      return this.getMockResponse(prompt);
    }

    try {
      const messages = this.buildMessages(prompt, conversationHistory);
      
      const response = await axios.post(
        `${this.baseUrl}/messages`,
        {
          model: this.model,
          max_tokens: 2048,
          messages,
          system: 'Du bist OpenClaw, ein hilfreicher AI-Assistent. Antworte präzise und hilfreich auf Deutsch.'
        },
        {
          headers: {
            'x-api-key': this.apiKey,
            'anthropic-version': '2023-06-01',
            'Content-Type': 'application/json'
          },
          timeout: 30000
        }
      );

      return {
        content: response.data.content[0].text,
        tokens: response.data.usage?.input_tokens + response.data.usage?.output_tokens || 0,
        model: this.model,
        provider: 'claude'
      };
    } catch (error) {
      console.error('Claude API error:', error.message);
      return this.getMockResponse(prompt);
    }
  }

  buildMessages(prompt, history) {
    const messages = [];
    
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
      content: `[DEMO - Claude] Antwort auf: "${prompt.substring(0, 50)}..."\n\nBitte ANTHROPIC_API_KEY setzen für echte Claude-Antworten.`,
      tokens: 40,
      model: this.model,
      provider: 'claude',
      isMock: true
    };
  }
}

module.exports = new ClaudeService();
