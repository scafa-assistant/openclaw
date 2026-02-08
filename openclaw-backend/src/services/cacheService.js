// Response Caching Service für teure API-Calls
class CacheService {
  constructor() {
    this.cache = new Map();
    this.stats = {
      hits: 0,
      misses: 0,
      evictions: 0
    };
    
    // Cache-Einstellungen
    this.ttl = {
      default: 5 * 60 * 1000,    // 5 Minuten
      factual: 60 * 60 * 1000,    // 1 Stunde für Fakten
      weather: 10 * 60 * 1000,    // 10 Min für Wetter
      code: 30 * 60 * 1000        // 30 Min für Code
    };
    
    this.maxSize = 1000; // Max 1000 Einträge
    
    // Cleanup alle 10 Minuten
    setInterval(() => this.cleanup(), 10 * 60 * 1000);
  }

  // Generiere Cache-Key
  generateKey(query, model, userId = 'anonymous') {
    // Hash der Query für konsistenten Key
    const crypto = require('crypto');
    const hash = crypto
      .createHash('md5')
      .update(`${query}:${model}:${userId}`)
      .digest('hex');
    return hash;
  }

  // Prüfe ob im Cache
  get(key) {
    const entry = this.cache.get(key);
    
    if (!entry) {
      this.stats.misses++;
      return null;
    }
    
    // Prüfe ob abgelaufen
    if (Date.now() > entry.expiresAt) {
      this.cache.delete(key);
      this.stats.evictions++;
      this.stats.misses++;
      return null;
    }
    
    this.stats.hits++;
    return entry.data;
  }

  // Speichere im Cache
  set(key, data, type = 'default') {
    // Entferne ältesten Eintrag wenn voll
    if (this.cache.size >= this.maxSize) {
      const oldestKey = this.cache.keys().next().value;
      this.cache.delete(oldestKey);
      this.stats.evictions++;
    }
    
    const ttl = this.ttl[type] || this.ttl.default;
    
    this.cache.set(key, {
      data,
      expiresAt: Date.now() + ttl,
      createdAt: Date.now()
    });
  }

  // Cache-Typ basierend auf Query bestimmen
  getCacheType(query) {
    const lower = query.toLowerCase();
    
    if (/wetter|temperatur|weather/i.test(lower)) {
      return 'weather';
    }
    if (/wie spät|uhrzeit|datum|wann|was ist|wer ist|wo liegt/i.test(lower)) {
      return 'factual';
    }
    if (/code|function|programm|script/i.test(lower)) {
      return 'code';
    }
    
    return 'default';
  }

  // Cache invalidieren für User
  invalidateForUser(userId) {
    for (const [key, entry] of this.cache.entries()) {
      if (key.includes(userId)) {
        this.cache.delete(key);
        this.stats.evictions++;
      }
    }
  }

  // Aufräumen abgelaufener Einträge
  cleanup() {
    const now = Date.now();
    let cleaned = 0;
    
    for (const [key, entry] of this.cache.entries()) {
      if (now > entry.expiresAt) {
        this.cache.delete(key);
        cleaned++;
        this.stats.evictions++;
      }
    }
    
    console.log(`[Cache] Cleanup: ${cleaned} Einträge entfernt, ${this.cache.size} aktiv`);
    console.log(`[Cache] Stats: ${this.stats.hits} hits, ${this.stats.misses} misses (${this.getHitRate().toFixed(1)}%)`);
  }

  // Cache Hit Rate
  getHitRate() {
    const total = this.stats.hits + this.stats.misses;
    return total === 0 ? 0 : (this.stats.hits / total) * 100;
  }

  // Cache Stats
  getStats() {
    return {
      size: this.cache.size,
      maxSize: this.maxSize,
      hitRate: this.getHitRate(),
      hits: this.stats.hits,
      misses: this.stats.misses,
      evictions: this.stats.evictions
    };
  }
}

module.exports = new CacheService();
