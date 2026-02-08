// Rate Limiting Service
class RateLimitService {
  constructor() {
    // Speichert Anfragen pro IP/User
    this.requests = new Map();
    this.windowMs = 60 * 1000; // 1 Minute
    this.maxRequests = {
      anonymous: 10,    // 10 Anfragen/Min für Gäste
      authenticated: 60, // 60 Anfragen/Min für eingeloggte User
      power: 120         // 120 Anfragen/Min für Power-User
    };
    
    // Aufräumen alle 5 Minuten
    setInterval(() => this.cleanup(), 5 * 60 * 1000);
  }

  // Prüfe ob Anfrage erlaubt
  async checkLimit(identifier, tier = 'anonymous') {
    const now = Date.now();
    const windowStart = now - this.windowMs;
    
    // Hole bestehende Anfragen
    let userRequests = this.requests.get(identifier) || [];
    
    // Entferne alte Anfragen außerhalb des Zeitfensters
    userRequests = userRequests.filter(time => time > windowStart);
    
    // Prüfe Limit
    const limit = this.maxRequests[tier] || this.maxRequests.anonymous;
    
    if (userRequests.length >= limit) {
      const oldestRequest = Math.min(...userRequests);
      const retryAfter = Math.ceil((oldestRequest + this.windowMs - now) / 1000);
      
      return {
        allowed: false,
        retryAfter,
        remaining: 0,
        limit
      };
    }
    
    // Füge neue Anfrage hinzu
    userRequests.push(now);
    this.requests.set(identifier, userRequests);
    
    return {
      allowed: true,
      remaining: limit - userRequests.length,
      limit
    };
  }

  // Aufräumen alter Einträge
  cleanup() {
    const now = Date.now();
    const windowStart = now - this.windowMs;
    
    for (const [identifier, requests] of this.requests.entries()) {
      const validRequests = requests.filter(time => time > windowStart);
      
      if (validRequests.length === 0) {
        this.requests.delete(identifier);
      } else {
        this.requests.set(identifier, validRequests);
      }
    }
    
    console.log(`[RateLimit] Cleanup complete. Active clients: ${this.requests.size}`);
  }

  // Middleware für Express
  middleware() {
    return async (req, res, next) => {
      // Identifiziere User (IP oder User-ID)
      const identifier = req.user?.userId || req.ip;
      const tier = req.user?.tier?.toLowerCase() || 'anonymous';
      
      const result = await this.checkLimit(identifier, tier);
      
      // Setze Rate-Limit Header
      res.setHeader('X-RateLimit-Limit', result.limit);
      res.setHeader('X-RateLimit-Remaining', result.remaining);
      
      if (!result.allowed) {
        res.setHeader('Retry-After', result.retryAfter);
        return res.status(429).json({
          error: 'Rate limit exceeded',
          retryAfter: result.retryAfter,
          message: `Zu viele Anfragen. Bitte warte ${result.retryAfter} Sekunden.`
        });
      }
      
      next();
    };
  }
}

module.exports = new RateLimitService();
