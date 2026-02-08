// Smart LLM Router - Wählt automatisch das beste Modell
// Standard für normale User, optional eigene API-Keys für Power-User

const { GoogleGenerativeAI } = require('@google/generative-ai');
const { Anthropic } = require('@anthropic-ai/sdk');
const OpenAI = require('openai');

class SmartLLMRouter {
  constructor() {
    // Default APIs (unsere Keys)
    this.gemini = new GoogleGenerativeAI(process.env.GEMINI_API_KEY);
    this.anthropic = new Anthropic({ apiKey: process.env.CLAUDE_API_KEY });
    this.openai = new OpenAI({ apiKey: process.env.OPENAI_API_KEY });
    
    // Model configurations
    this.models = {
      // Schnelle Antworten
      geminiFlash: {
        name: 'gemini-2.0-flash',
        provider: 'google',
        speed: 'fast',
        cost: 'low',
        strengths: ['quick', 'factual', 'short']
      },
      // Komplexes Reasoning
      claudeSonnet: {
        name: 'claude-3-5-sonnet-20241022',
        provider: 'anthropic',
        speed: 'medium',
        cost: 'medium',
        strengths: ['reasoning', 'analysis', 'code', 'long']
      },
      // Kreatives Schreiben
      gpt4: {
        name: 'gpt-4o',
        provider: 'openai',
        speed: 'medium',
        cost: 'medium',
        strengths: ['creative', 'conversation', 'general']
      },
      // Power-User Modelle (nur mit eigenem API-Key)
      opus: {
        name: 'claude-3-opus-20240229',
        provider: 'anthropic',
        speed: 'slow',
        cost: 'high',
        strengths: ['complex', 'research', 'analysis'],
        requiresCustomKey: true
      },
      codex: {
        name: 'codex-latest',
        provider: 'openai',
        speed: 'medium',
        cost: 'high',
        strengths: ['coding', 'technical'],
        requiresCustomKey: true
      },
      moonshot: {
        name: 'kimi-k2.5',
        provider: 'moonshot',
        speed: 'fast',
        cost: 'low',
        strengths: ['chinese', 'multilingual', 'long-context'],
        requiresCustomKey: true
      },
      deepseek: {
        name: 'deepseek-chat',
        provider: 'deepseek',
        speed: 'fast',
        cost: 'low',
        strengths: ['reasoning', 'coding', 'chinese'],
        requiresCustomKey: true
      }
    };
  }

  // Analysiert Query und wählt bestes Modell
  async selectModel(query, userConfig = {}) {
    const lowerQuery = query.toLowerCase();
    
    // Power-User hat eigenen Key für spezifisches Modell?
    if (userConfig.preferredModel && userConfig.apiKeys) {
      const preferred = this.models[userConfig.preferredModel];
      if (preferred && userConfig.apiKeys[preferred.provider]) {
        return {
          model: preferred,
          apiKey: userConfig.apiKeys[preferred.provider],
          source: 'user-config'
        };
      }
    }
    
    // Automatische Auswahl basierend auf Query-Typ
    let selectedModel;
    
    // Code/Technische Fragen
    if (this.isCodeQuery(lowerQuery)) {
      selectedModel = this.models.claudeSonnet; // Bestes Preis-Leistung
    }
    // Kurze Fakten-Fragen
    else if (this.isFactualQuery(lowerQuery)) {
      selectedModel = this.models.geminiFlash; // Schnell & günstig
    }
    // Lange/komplexe Anfragen
    else if (query.length > 200 || this.isComplexQuery(lowerQuery)) {
      selectedModel = this.models.claudeSonnet;
    }
    // Standard: GPT-4 für beste allgemeine Qualität
    else {
      selectedModel = this.models.gpt4;
    }
    
    return {
      model: selectedModel,
      apiKey: null, // Use our default keys
      source: 'auto-selected'
    };
  }

  isCodeQuery(query) {
    const codeKeywords = [
      'code', 'function', 'bug', 'error', 'debug', 'programming',
      'python', 'javascript', 'java', 'kotlin', 'swift', 'api',
      'script', 'syntax', 'compile', 'runtime', 'exception'
    ];
    return codeKeywords.some(kw => query.includes(kw));
  }

  isFactualQuery(query) {
    const factualPatterns = [
      'was ist', 'wie viel', 'wann', 'wo', 'wer', 'wie groß',
      'temperatur', 'zeit', 'datum', 'wetter', 'definition',
      'what is', 'how much', 'when', 'where', 'who'
    ];
    return factualPatterns.some(p => query.includes(p));
  }

  isComplexQuery(query) {
    const complexKeywords = [
      'analysiere', 'vergleiche', 'erkläre detailliert', 'research',
      'schreibe', 'generiere', 'erstelle', 'konzeption',
      'analyze', 'compare', 'explain in detail', 'write', 'create'
    ];
    return complexKeywords.some(kw => query.includes(kw));
  }

  // Führt Query aus mit gewähltem Modell
  async executeQuery(query, userConfig = {}, history = []) {
    const selection = await this.selectModel(query, userConfig);
    const { model, apiKey, source } = selection;
    
    console.log(`[SmartRouter] Using ${model.name} (${source})`);
    
    let response;
    const startTime = Date.now();
    
    try {
      switch (model.provider) {
        case 'google':
          response = await this.callGemini(query, model.name, history, apiKey);
          break;
        case 'anthropic':
          response = await this.callClaude(query, model.name, history, apiKey);
          break;
        case 'openai':
          response = await this.callOpenAI(query, model.name, history, apiKey);
          break;
        case 'moonshot':
          response = await this.callMoonshot(query, model.name, history, apiKey);
          break;
        case 'deepseek':
          response = await this.callDeepSeek(query, model.name, history, apiKey);
          break;
        default:
          throw new Error(`Unknown provider: ${model.provider}`);
      }
      
      const duration = Date.now() - startTime;
      
      return {
        content: response,
        model: model.name,
        provider: model.provider,
        source: source,
        duration: duration,
        cached: false
      };
      
    } catch (error) {
      console.error(`[SmartRouter] Error with ${model.name}:`, error.message);
      
      // Fallback zu Gemini Flash (schnell & zuverlässig)
      if (model.name !== 'gemini-2.0-flash') {
        console.log('[SmartRouter] Falling back to Gemini Flash');
        return this.executeQuery(query, {}, history);
      }
      
      throw error;
    }
  }

  async callGemini(query, modelName, history, customApiKey) {
    const genAI = customApiKey 
      ? new GoogleGenerativeAI(customApiKey)
      : this.gemini;
      
    const model = genAI.getGenerativeModel({ model: modelName });
    
    const chat = model.startChat({
      history: history.map(h => ({
        role: h.role === 'user' ? 'user' : 'model',
        parts: [{ text: h.content }]
      }))
    });
    
    const result = await chat.sendMessage(query);
    return result.response.text();
  }

  async callClaude(query, modelName, history, customApiKey) {
    const client = customApiKey
      ? new Anthropic({ apiKey: customApiKey })
      : this.anthropic;
      
    const messages = history.map(h => ({
      role: h.role,
      content: h.content
    }));
    messages.push({ role: 'user', content: query });
    
    const response = await client.messages.create({
      model: modelName,
      max_tokens: 4096,
      messages: messages
    });
    
    return response.content[0].text;
  }

  async callOpenAI(query, modelName, history, customApiKey) {
    const client = customApiKey
      ? new OpenAI({ apiKey: customApiKey })
      : this.openai;
      
    const messages = history.map(h => ({
      role: h.role,
      content: h.content
    }));
    messages.push({ role: 'user', content: query });
    
    const response = await client.chat.completions.create({
      model: modelName,
      messages: messages
    });
    
    return response.choices[0].message.content;
  }

  async callMoonshot(query, modelName, history, apiKey) {
    // Moonshot API implementation
    const response = await fetch('https://api.moonshot.cn/v1/chat/completions', {
      method: 'POST',
      headers: {
        'Authorization': `Bearer ${apiKey}`,
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({
        model: modelName,
        messages: [
          ...history.map(h => ({ role: h.role, content: h.content })),
          { role: 'user', content: query }
        ]
      })
    });
    
    const data = await response.json();
    return data.choices[0].message.content;
  }

  async callDeepSeek(query, modelName, history, apiKey) {
    const response = await fetch('https://api.deepseek.com/v1/chat/completions', {
      method: 'POST',
      headers: {
        'Authorization': `Bearer ${apiKey}`,
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({
        model: modelName,
        messages: [
          ...history.map(h => ({ role: h.role, content: h.content })),
          { role: 'user', content: query }
        ]
      })
    });
    
    const data = await response.json();
    return data.choices[0].message.content;
  }

  // Verfügbare Power-User Modelle
  getAvailablePowerModels() {
    return Object.entries(this.models)
      .filter(([_, model]) => model.requiresCustomKey)
      .map(([key, model]) => ({
        id: key,
        name: model.name,
        provider: model.provider,
        strengths: model.strengths,
        requiresKey: true
      }));
  }
}

module.exports = SmartLLMRouter;
