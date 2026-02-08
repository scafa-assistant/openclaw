const geminiService = require('./geminiService');
const claudeService = require('./claudeService');
const gptService = require('./gptService');

// LLM Service - routes messages to different providers
class LLMService {
  constructor() {
    this.providers = {
      'gemini-2.5-flash': geminiService,
      'gemini-pro': geminiService,
      'claude-sonnet': claudeService,
      'claude-opus': claudeService,
      'gpt-4o': gptService,
      'gpt-4o-mini': gptService
    };
  }

  async sendMessage(message, model = 'gemini-2.5-flash', conversationHistory = []) {
    const service = this.providers[model] || geminiService;
    
    try {
      const result = await service.generateResponse(message, conversationHistory);
      return {
        content: result.content,
        tokens: result.tokens,
        model: result.model || model,
        provider: result.provider,
        isMock: result.isMock || false
      };
    } catch (error) {
      console.error('LLM Service error:', error);
      return {
        content: 'Entschuldigung, ich konnte gerade nicht antworten. Bitte versuche es erneut.',
        tokens: 0,
        model: model,
        provider: 'error',
        isMock: true
      };
    }
  }

  getAvailableModels() {
    return [
      { id: 'gemini-2.5-flash', name: 'Gemini 2.5 Flash', provider: 'google', tier: 'FREE' },
      { id: 'gemini-pro', name: 'Gemini Pro', provider: 'google', tier: 'PREMIUM' },
      { id: 'claude-sonnet', name: 'Claude 3.5 Sonnet', provider: 'anthropic', tier: 'PREMIUM' },
      { id: 'claude-opus', name: 'Claude 3 Opus', provider: 'anthropic', tier: 'PREMIUM' },
      { id: 'gpt-4o-mini', name: 'GPT-4o Mini', provider: 'openai', tier: 'FREE' },
      { id: 'gpt-4o', name: 'GPT-4o', provider: 'openai', tier: 'PREMIUM' }
    ];
  }
}

module.exports = new LLMService();
