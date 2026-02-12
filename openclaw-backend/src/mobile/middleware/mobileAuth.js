/**
 * Mobile Authentication Middleware
 * Validates API keys for mobile app requests
 */

const apiKeyService = require('../services/apiKeyService');
const rateLimitService = require('../../services/rateLimitService');

/**
 * Middleware to validate API key from header
 * Expected header: x-api-key: oc_live_xxxxxxxx
 */
const mobileAuthMiddleware = async (req, res, next) => {
  try {
    // Get API key from header
    const apiKey = req.headers['x-api-key'] || req.headers['X-API-Key'];
    
    if (!apiKey) {
      console.warn(`[MobileAuth] Missing API key from IP: ${req.ip}`);
      return res.status(401).json({
        error: 'Unauthorized',
        message: 'API key is required. Please provide x-api-key header.',
        code: 'MISSING_API_KEY'
      });
    }
    
    // Validate the key
    const validation = await apiKeyService.validateKey(apiKey);
    
    if (!validation) {
      console.warn(`[MobileAuth] Invalid API key attempt from IP: ${req.ip}`);
      return res.status(401).json({
        error: 'Unauthorized',
        message: 'Invalid API key. Please check your key or generate a new one.',
        code: 'INVALID_API_KEY'
      });
    }
    
    // Check for rate limit error
    if (validation.error === 'Rate limit exceeded') {
      console.warn(`[MobileAuth] Rate limit exceeded for user: ${validation.userId}`);
      return res.status(429).json({
        error: 'Rate Limit Exceeded',
        message: 'Too many requests. Please try again later.',
        retryAfter: validation.retryAfter,
        code: 'RATE_LIMIT_EXCEEDED'
      });
    }
    
    // Attach user info to request
    req.user = {
      userId: validation.userId,
      apiKey: apiKey,
      hashedKey: validation.hashedKey,
      metadata: validation.metadata,
      rateLimit: validation.rateLimit
    };
    
    // Log successful auth (in production: use proper logging)
    console.log(`[MobileAuth] Authenticated user: ${validation.userId}, IP: ${req.ip}`);
    
    // Continue to next middleware/route handler
    next();
    
  } catch (error) {
    console.error('[MobileAuth] Authentication error:', error);
    return res.status(500).json({
      error: 'Internal Server Error',
      message: 'Authentication failed. Please try again.',
      code: 'AUTH_ERROR'
    });
  }
};

/**
 * Optional authentication - doesn't fail if no key provided
 * Used for public endpoints that can benefit from user context
 */
const optionalMobileAuth = async (req, res, next) => {
  try {
    const apiKey = req.headers['x-api-key'] || req.headers['X-API-Key'];
    
    if (!apiKey) {
      req.user = null;
      return next();
    }
    
    const validation = await apiKeyService.validateKey(apiKey);
    
    if (validation && !validation.error) {
      req.user = {
        userId: validation.userId,
        apiKey: apiKey,
        hashedKey: validation.hashedKey,
        metadata: validation.metadata,
        rateLimit: validation.rateLimit
      };
    } else {
      req.user = null;
    }
    
    next();
    
  } catch (error) {
    console.error('[MobileAuth] Optional auth error:', error);
    req.user = null;
    next();
  }
};

/**
 * Admin authentication middleware
 * Checks for admin token or special admin API key
 */
const adminAuthMiddleware = async (req, res, next) => {
  try {
    const adminToken = req.headers['x-admin-token'];
    const expectedToken = process.env.ADMIN_TOKEN || 'oc_admin_secret_2026';
    
    if (!adminToken || adminToken !== expectedToken) {
      console.warn(`[MobileAuth] Unauthorized admin attempt from IP: ${req.ip}`);
      return res.status(403).json({
        error: 'Forbidden',
        message: 'Admin access required.',
        code: 'ADMIN_REQUIRED'
      });
    }
    
    req.isAdmin = true;
    next();
    
  } catch (error) {
    console.error('[MobileAuth] Admin auth error:', error);
    return res.status(500).json({
      error: 'Internal Server Error',
      message: 'Admin authentication failed.'
    });
  }
};

module.exports = {
  mobileAuthMiddleware,
  optionalMobileAuth,
  adminAuthMiddleware
};
