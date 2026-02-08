const axios = require('axios');

// LLM Service - routes messages to different providers
class LLMService {
  constructor() {
    this.providers = {
      'gemini-2.5-flash': this.callGemini.bind(this),
      'claude-sonnet': this.callClaude.bind(this),
      'gpt-4o': this.callGPT4.bind(this)
    };
  }

  async sendMessage(message, model = 'gemini-2.5-flash') {
    const provider = this.providers[model] || this.providers['gemini-2.5-flash'];
    return await provider(message);
  }

  async callGemini(message) {
    // TODO: Implement actual Gemini API call
    // For now, return mock response
    return {
      content: `Das ist eine Demo-Antwort auf: "${message}"\n\nIn der Produktionsversion wird hier die echte Gemini API antworten.`,
      tokens: 50
    };
  }

  async callClaude(message) {
    // TODO: Implement actual Claude API call
    return {
      content: `Claude Demo-Antwort auf: "${message}"`,
      tokens: 30
    };
  }

  async callGPT4(message) {
    // TODO: Implement actual GPT-4 API call
    return {
      content: `GPT-4 Demo-Antwort auf: "${message}"`,
      tokens: 40
    };
  }
}

module.exports = new LLMService();
