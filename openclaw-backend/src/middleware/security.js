// Security Middleware & Utilities für OpenClaw Backend

/**
 * API Key Validator - Prüft ob Keys gültig format sind
 */
function validateApiKey(provider, key) {
  const patterns = {
    google: /^AIza[\w-]{35}$/,
    anthropic: /^sk-ant-[\w-]{40,}$/,
    openai: /^sk-[\w-]{40,}$/,
    moonshot: /^sk-[\w-]{40,}$/,
    deepseek: /^sk-[\w-]{40,}$/,
    mistral: /^[\w-]{40,}$/
  };
  
  if (!patterns[provider]) return false;
  return patterns[provider].test(key);
}

/**
 * Request Logger - Für Debugging und Monitoring
 */
function requestLogger(req, res, next) {
  const start = Date.now();
  
  res.on('finish', () => {
    const duration = Date.now() - start;
    console.log(`[${new Date().toISOString()}] ${req.method} ${req.path} - ${res.statusCode} - ${duration}ms`);
  });
  
  next();
}

/**
 * Token Blacklist - Für Logout-Funktionalität
 */
class TokenBlacklist {
  constructor() {
    this.blacklist = new Set();
    // Cleanup alle 24 Stunden
    setInterval(() => this.cleanup(), 24 * 60 * 60 * 1000);
  }
  
  add(token) {
    try {
      const decoded = jwt.decode(token);
      if (decoded && decoded.exp) {
        this.blacklist.add({ token, exp: decoded.exp });
      }
    } catch (e) {
      console.error('[Blacklist Error]', e);
    }
  }
  
  has(token) {
    return Array.from(this.blacklist).some(item => item.token === token);
  }
  
  cleanup() {
    const now = Math.floor(Date.now() / 1000);
    this.blacklist = new Set(
      Array.from(this.blacklist).filter(item => item.exp > now)
    );
    console.log('[Blacklist] Cleanup completed');
  }
}

/**
 * Cache für API-Antworten (simple In-Memory)
 */
class ResponseCache {
  constructor(maxSize = 100, ttlMs = 5 * 60 * 1000) {
    this.cache = new Map();
    this.maxSize = maxSize;
    this.ttlMs = ttlMs;
  }
  
  get(key) {
    const item = this.cache.get(key);
    if (!item) return null;
    
    if (Date.now() > item.expiry) {
      this.cache.delete(key);
      return null;
    }
    
    return item.value;
  }
  
  set(key, value) {
    // LRU eviction wenn voll
    if (this.cache.size >= this.maxSize) {
      const firstKey = this.cache.keys().next().value;
      this.cache.delete(firstKey);
    }
    
    this.cache.set(key, {
      value,
      expiry: Date.now() + this.ttlMs
    });
  }
  
  clear() {
    this.cache.clear();
  }
}

/**
 * Retry Logic mit Exponential Backoff
 */
async function withRetry(fn, maxRetries = 3, baseDelay = 1000) {
  let lastError;
  
  for (let i = 0; i < maxRetries; i++) {
    try {
      return await fn();
    } catch (error) {
      lastError = error;
      
      // Nicht bei allen Errors retryen
      if (error.status === 401 || error.status === 403 || error.status === 400) {
        throw error;
      }
      
      if (i < maxRetries - 1) {
        const delay = baseDelay * Math.pow(2, i);
        console.log(`[Retry] Attempt ${i + 1}/${maxRetries}, waiting ${delay}ms...`);
        await new Promise(resolve => setTimeout(resolve, delay));
      }
    }
  }
  
  throw lastError;
}

/**
 * Performance Monitor
 */
class PerformanceMonitor {
  constructor() {
    this.metrics = {
      requests: 0,
      errors: 0,
      avgResponseTime: 0,
      startTime: Date.now()
    };
  }
  
  recordRequest(duration, error = false) {
    this.metrics.requests++;
    if (error) this.metrics.errors++;
    
    // Rolling average
    this.metrics.avgResponseTime = 
      (this.metrics.avgResponseTime * (this.metrics.requests - 1) + duration) / 
      this.metrics.requests;
  }
  
  getStats() {
    const uptime = Date.now() - this.metrics.startTime;
    return {
      ...this.metrics,
      uptime: Math.floor(uptime / 1000),
      errorRate: this.metrics.requests > 0 
        ? (this.metrics.errors / this.metrics.requests * 100).toFixed(2) + '%'
        : '0%'
    };
  }
}

module.exports = {
  validateApiKey,
  requestLogger,
  TokenBlacklist,
  ResponseCache,
  withRetry,
  PerformanceMonitor
};
