// Hybrid AI Service - Nutzt Built-in Intelligence (kostenlos) + Externe APIs (bei Bedarf)
// iOS: Siri Intelligence, Android: Google Assistant/Gemini
// Externe APIs nur für komplexe/power-user Anfragen

class HybridAIService {
  constructor() {
    // Externe APIs (nur für komplexe Anfragen)
    this.smartRouter = require('./smartLLMRouter');
  }

  // Haupt-Entry Point
  async processQuery(query, context = {}, userConfig = {}) {
    console.log(`[HybridAI] Query: "${query.substring(0, 50)}..."`);
    
    // 1. Check: Kann Built-in AI das beantworten?
    const builtInCapable = this.canBuiltInHandle(query);
    
    if (builtInCapable && !userConfig.forceExternalAI) {
      console.log('[HybridAI] → Routing to Built-in Intelligence (FREE)');
      return {
        source: 'builtin',
        content: null, // App nutzt Siri/Google direkt
        action: 'delegate_to_assistant',
        assistantPrompt: this.buildAssistantPrompt(query, context)
      };
    }
    
    // 2. Externe AI für komplexe Anfragen
    console.log('[HybridAI] → Routing to External AI (PAID)');
    const response = await this.smartRouter.executeQuery(query, userConfig, context.history);
    
    return {
      source: 'external',
      ...response
    };
  }

  // Entscheidet ob Built-in AI ausreicht
  canBuiltInHandle(query) {
    const lower = query.toLowerCase();
    
    // Siri/Google können das gut:
    const builtInStrengths = [
      // Zeit & Datum
      { pattern: /^(wie spät|wieviel uhr|uhrzeit|zeit)/, type: 'time' },
      { pattern: /^(welcher tag|datum|wann ist|wann war)/, type: 'date' },
      { pattern: /^(timer|wecker|erinnerung|termin)/, type: 'reminder' },
      
      // Wetter
      { pattern: /^(wetter|temperatur|regnet|schneit|sonne)/, type: 'weather' },
      
      // Navigation & Orte
      { pattern: /^(wo ist|navigiere|route|weg|entfernung)/, type: 'navigation' },
      
      // Grundlagen Fakten
      { pattern: /^(wer ist|was ist|wo liegt|wie groß|wie alt)/, type: 'fact' },
      
      // Einfache Berechnungen
      { pattern: /^(rechne|wie viel ist|berechne|\d+\s*[\+\-\*\/]\s*\d+)/, type: 'math' },
      
      // Geräte-Steuerung
      { pattern: /^(licht|lampe|musik|spotify|lautstärke|helligkeit)/, type: 'device' },
      
      // Messaging
      { pattern: /^(schreibe|sende|nachricht|whatsapp|sms|mail)/, type: 'message' },
      
      // Suche
      { pattern: /^(suche|google|finde|zeige mir)/, type: 'search' }
    ];
    
    for (const strength of builtInStrengths) {
      if (strength.pattern.test(lower)) {
        console.log(`[HybridAI] Detected: ${strength.type} → Built-in optimal`);
        return true;
      }
    }
    
    // Zu lang oder komplex für Built-in
    if (query.length > 100) {
      console.log('[HybridAI] Query too long for built-in');
      return false;
    }
    
    // Standard: Externe AI (bessere Qualität)
    return false;
  }

  // Baut Prompt für Siri/Google
  buildAssistantPrompt(query, context) {
    // Für iOS Siri Shortcut
    if (context.platform === 'ios') {
      return {
        intent: 'custom',
        phrase: query,
        responseExpected: true
      };
    }
    
    // Für Android
    if (context.platform === 'android') {
      return {
        action: 'android.intent.action.VOICE_COMMAND',
        query: query
      };
    }
    
    return { query };
  }

  // Preis-Schätzung für User
  estimateCost(query, complexity = 'auto') {
    const costs = {
      builtin: { cost: 0, label: 'Kostenlos (Siri/Google)' },
      geminiFlash: { cost: 0.0001, label: 'Sehr günstig (Gemini)' },
      claudeSonnet: { cost: 0.003, label: 'Standard (Claude)' },
      gpt4: { cost: 0.005, label: 'Premium (GPT-4)' },
      opus: { cost: 0.015, label: 'Maximum (Opus)' }
    };
    
    if (complexity === 'auto') {
      complexity = this.canBuiltInHandle(query) ? 'builtin' : 'claudeSonnet';
    }
    
    return costs[complexity] || costs.claudeSonnet;
  }
}

module.exports = HybridAIService;
