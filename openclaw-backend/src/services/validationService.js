// Input Validation Service
const validator = require('validator');

class ValidationService {
  // Validiere E-Mail
  static validateEmail(email) {
    if (!email || typeof email !== 'string') {
      return { valid: false, error: 'E-Mail erforderlich' };
    }
    
    if (!validator.isEmail(email)) {
      return { valid: false, error: 'Ungültige E-Mail-Adresse' };
    }
    
    if (email.length > 254) {
      return { valid: false, error: 'E-Mail zu lang' };
    }
    
    return { valid: true };
  }

  // Validiere Passwort
  static validatePassword(password) {
    if (!password || typeof password !== 'string') {
      return { valid: false, error: 'Passwort erforderlich' };
    }
    
    if (password.length < 6) {
      return { valid: false, error: 'Passwort mindestens 6 Zeichen' };
    }
    
    if (password.length > 128) {
      return { valid: false, error: 'Passwort zu lang' };
    }
    
    // Keine Kontrollzeichen erlaubt
    if (/[\x00-\x1F\x7F-\x9F]/.test(password)) {
      return { valid: false, error: 'Ungültige Zeichen im Passwort' };
    }
    
    return { valid: true };
  }

  // Validiere Chat Nachricht
  static validateMessage(message) {
    if (!message || typeof message !== 'string') {
      return { valid: false, error: 'Nachricht erforderlich' };
    }
    
    if (message.trim().length === 0) {
      return { valid: false, error: 'Nachricht darf nicht leer sein' };
    }
    
    if (message.length > 10000) {
      return { valid: false, error: 'Nachricht zu lang (max 10.000 Zeichen)' };
    }
    
    // Sanitize: Entferne gefährliche Zeichen
    const sanitized = validator.escape(message.trim());
    
    return { valid: true, sanitized };
  }

  // Validiere Modell-Name
  static validateModel(model) {
    const allowedModels = [
      'gemini', 'claude', 'gpt4', 
      'opus', 'codex', 'moonshot', 'deepseek'
    ];
    
    if (!model) return { valid: true, model: 'gemini' }; // Default
    
    if (!allowedModels.includes(model)) {
      return { valid: false, error: 'Ungültiges Modell' };
    }
    
    return { valid: true, model };
  }

  // Prüfe auf Injection-Angriffe
  static detectInjection(input) {
    const suspicious = [
      /<script/i,
      /javascript:/i,
      /on\w+=/i,
      /SELECT\s+.*\s+FROM/i,
      /INSERT\s+INTO/i,
      /DELETE\s+FROM/i,
      /DROP\s+TABLE/i,
      /UNION\s+SELECT/i
    ];
    
    return suspicious.some(pattern => pattern.test(input));
  }
}

module.exports = ValidationService;
