// Retry Service mit Exponential Backoff
class RetryService {
  constructor() {
    this.defaultOptions = {
      maxRetries: 3,
      initialDelay: 1000,    // 1 Sekunde
      maxDelay: 30000,       // 30 Sekunden
      backoffMultiplier: 2,  // Verdopple jedes Mal
      retryableErrors: [
        'ECONNRESET',
        'ETIMEDOUT',
        'ECONNREFUSED',
        'ENOTFOUND',
        'EAI_AGAIN',
        'NETWORK_ERROR',
        'RATE_LIMITED'
      ]
    };
  }

  // Führe Funktion mit Retry aus
  async execute(fn, options = {}) {
    const opts = { ...this.defaultOptions, ...options };
    let lastError;
    
    for (let attempt = 0; attempt <= opts.maxRetries; attempt++) {
      try {
        const result = await fn();
        
        // Logge erfolgreichen Retry
        if (attempt > 0) {
          console.log(`[Retry] Success after ${attempt} retries`);
        }
        
        return {
          success: true,
          data: result,
          attempts: attempt + 1
        };
        
      } catch (error) {
        lastError = error;
        
        // Prüfe ob retryable
        if (!this.isRetryable(error, opts)) {
          throw error; // Nicht retryable, sofort fail
        }
        
        // Letzter Versuch fehlgeschlagen
        if (attempt === opts.maxRetries) {
          console.error(`[Retry] Failed after ${opts.maxRetries + 1} attempts:`, error.message);
          break;
        }
        
        // Berechne Delay
        const delay = this.calculateDelay(attempt, opts);
        
        console.log(`[Retry] Attempt ${attempt + 1} failed, retrying in ${delay}ms...`);
        await this.sleep(delay);
      }
    }
    
    return {
      success: false,
      error: lastError,
      attempts: opts.maxRetries + 1
    };
  }

  // Prüfe ob Fehler retryable ist
  isRetryable(error, opts) {
    // Prüfe Error Code
    if (error.code && opts.retryableErrors.includes(error.code)) {
      return true;
    }
    
    // Prüfe HTTP Status Codes
    if (error.status) {
      const retryableStatuses = [408, 429, 500, 502, 503, 504];
      return retryableStatuses.includes(error.status);
    }
    
    // Prüfe Error Message
    const retryablePatterns = [
      /timeout/i,
      /rate limit/i,
      /temporarily unavailable/i,
      /too many requests/i
    ];
    
    return retryablePatterns.some(pattern => 
      pattern.test(error.message || '')
    );
  }

  // Berechne Delay mit Exponential Backoff
  calculateDelay(attempt, opts) {
    const delay = opts.initialDelay * Math.pow(opts.backoffMultiplier, attempt);
    return Math.min(delay, opts.maxDelay);
  }

  // Sleep Helper
  sleep(ms) {
    return new Promise(resolve => setTimeout(resolve, ms));
  }

  // Circuit Breaker Pattern
  createCircuitBreaker(fn, options = {}) {
    const opts = {
      failureThreshold: 5,    // Nach 5 Fehlern öffnen
      resetTimeout: 60000,    // 1 Minute bis Reset
      ...options
    };
    
    let failures = 0;
    let lastFailureTime = null;
    let state = 'CLOSED'; // CLOSED, OPEN, HALF_OPEN
    
    return async (...args) => {
      // Prüfe ob Circuit offen
      if (state === 'OPEN') {
        if (Date.now() - lastFailureTime > opts.resetTimeout) {
          state = 'HALF_OPEN';
          console.log('[CircuitBreaker] Entering HALF_OPEN state');
        } else {
          throw new Error('Circuit breaker is OPEN');
        }
      }
      
      try {
        const result = await fn(...args);
        
        // Erfolg - schließe Circuit
        if (state === 'HALF_OPEN') {
          state = 'CLOSED';
          failures = 0;
          console.log('[CircuitBreaker] Circuit CLOSED');
        }
        
        return result;
        
      } catch (error) {
        failures++;
        lastFailureTime = Date.now();
        
        // Öffne Circuit bei zu vielen Fehlern
        if (failures >= opts.failureThreshold) {
          state = 'OPEN';
          console.error(`[CircuitBreaker] Circuit OPEN after ${failures} failures`);
        }
        
        throw error;
      }
    };
  }
}

module.exports = new RetryService();
