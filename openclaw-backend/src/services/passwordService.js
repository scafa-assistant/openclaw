// Sicherer Password Service mit bcrypt
const bcrypt = require('bcrypt');

const SALT_ROUNDS = 12;

class PasswordService {
  // Hash Passwort sicher
  static async hash(password) {
    return bcrypt.hash(password, SALT_ROUNDS);
  }

  // Vergleiche Passwort mit Hash
  static async compare(password, hash) {
    return bcrypt.compare(password, hash);
  }

  // Prüfe Passwort-Stärke
  static validateStrength(password) {
    const errors = [];
    
    if (password.length < 8) {
      errors.push('Mindestens 8 Zeichen');
    }
    if (!/[A-Z]/.test(password)) {
      errors.push('Großbuchstabe erforderlich');
    }
    if (!/[a-z]/.test(password)) {
      errors.push('Kleinbuchstabe erforderlich');
    }
    if (!/[0-9]/.test(password)) {
      errors.push('Zahl erforderlich');
    }
    if (!/[!@#$%^&*]/.test(password)) {
      errors.push('Sonderzeichen erforderlich (!@#$%^&*)');
    }
    
    return {
      valid: errors.length === 0,
      errors
    };
  }
}

module.exports = PasswordService;
