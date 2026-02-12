/**
 * API Key Service for OpenClaw Mobile
 * Manages API key generation, validation, and rate limiting
 */

const { v4: uuidv4 } = require('uuid');
const bcrypt = require('bcrypt');
const rateLimitService = require('../../services/rateLimitService');

// Storage (in production: use Redis or Database)
const apiKeys = new Map(); // apiKeyHashed -> { userId, createdAt, lastUsed, isActive }
const userKeys = new Map(); // userId -> [apiKeyHashed]

class ApiKeyService {
  constructor() {
    this.SALT_ROUNDS = 10;
    this.KEY_PREFIX = 'oc_live_';
  }

  /**
   * Generate a new API key
   * @param {string} userId - Optional user ID (generated if not provided)
   * @param {Object} metadata - Optional metadata (name, description, etc.)
   * @returns {Object} { apiKey: string (full key, only shown once), userId: string }
   */
  async generateKey(userId = null, metadata = {}) {
    try {
      const finalUserId = userId || uuidv4();
      const keyId = uuidv4().replace(/-/g, '').substring(0, 16);
      const fullApiKey = `${this.KEY_PREFIX}${keyId}`;
      
      // Hash the key for storage (never store plain text!)
      const hashedKey = await bcrypt.hash(fullApiKey, this.SALT_ROUNDS);
      
      const keyData = {
        userId: finalUserId,
        hashedKey: hashedKey,
        createdAt: new Date(),
        lastUsed: null,
        isActive: true,
        metadata: {
          name: metadata.name || 'Default Key',
          description: metadata.description || '',
          ...metadata
        },
        usageStats: {
          totalRequests: 0,
          totalTokens: 0,
          lastCommand: null
        }
      };
      
      // Store hashed key
      apiKeys.set(hashedKey, keyData);
      
      // Track user -> keys mapping
      if (!userKeys.has(finalUserId)) {
        userKeys.set(finalUserId, []);
      }
      userKeys.get(finalUserId).push(hashedKey);
      
      console.log(`[ApiKeyService] Generated new API key for user: ${finalUserId}`);
      
      // Return the FULL key (only time it's shown!)
      return {
        apiKey: fullApiKey,
        userId: finalUserId,
        createdAt: keyData.createdAt
      };
      
    } catch (error) {
      console.error('[ApiKeyService] Error generating key:', error);
      throw new Error('Failed to generate API key');
    }
  }

  /**
   * Validate an API key
   * @param {string} apiKey - The API key to validate
   * @returns {Object|null} Key data if valid, null otherwise
   */
  async validateKey(apiKey) {
    try {
      if (!apiKey || typeof apiKey !== 'string') {
        return null;
      }
      
      // Check prefix
      if (!apiKey.startsWith(this.KEY_PREFIX)) {
        return null;
      }
      
      // Find matching key (iterate through stored hashes)
      for (const [hashedKey, keyData] of apiKeys.entries()) {
        if (keyData.isActive && await bcrypt.compare(apiKey, hashedKey)) {
          // Update last used
          keyData.lastUsed = new Date();
          
          // Check rate limit
          const rateLimit = await rateLimitService.checkLimit(keyData.userId, 'authenticated');
          if (!rateLimit.allowed) {
            console.warn(`[ApiKeyService] Rate limit exceeded for user: ${keyData.userId}`);
            return { error: 'Rate limit exceeded', retryAfter: rateLimit.retryAfter };
          }
          
          return {
            valid: true,
            userId: keyData.userId,
            hashedKey: hashedKey,
            metadata: keyData.metadata,
            createdAt: keyData.createdAt,
            rateLimit: {
              remaining: rateLimit.remaining,
              limit: rateLimit.limit
            }
          };
        }
      }
      
      return null;
      
    } catch (error) {
      console.error('[ApiKeyService] Error validating key:', error);
      return null;
    }
  }

  /**
   * Revoke an API key
   * @param {string} hashedKey - The hashed key to revoke
   * @param {string} userId - User ID for authorization
   * @returns {boolean} Success status
   */
  async revokeKey(hashedKey, userId) {
    try {
      const keyData = apiKeys.get(hashedKey);
      
      if (!keyData) {
        return false;
      }
      
      // Authorization check
      if (keyData.userId !== userId) {
        console.warn(`[ApiKeyService] Unauthorized revoke attempt for key by user: ${userId}`);
        return false;
      }
      
      keyData.isActive = false;
      keyData.revokedAt = new Date();
      
      console.log(`[ApiKeyService] Revoked key for user: ${userId}`);
      return true;
      
    } catch (error) {
      console.error('[ApiKeyService] Error revoking key:', error);
      return false;
    }
  }

  /**
   * Get API key statistics
   * @param {string} hashedKey - The hashed key
   * @returns {Object|null} Statistics
   */
  getKeyStats(hashedKey) {
    try {
      const keyData = apiKeys.get(hashedKey);
      
      if (!keyData) {
        return null;
      }
      
      return {
        userId: keyData.userId,
        createdAt: keyData.createdAt,
        lastUsed: keyData.lastUsed,
        isActive: keyData.isActive,
        metadata: keyData.metadata,
        usageStats: keyData.usageStats
      };
      
    } catch (error) {
      console.error('[ApiKeyService] Error getting stats:', error);
      return null;
    }
  }

  /**
   * Update usage statistics
   * @param {string} hashedKey - The hashed key
   * @param {Object} stats - Stats to update
   */
  updateUsageStats(hashedKey, stats) {
    try {
      const keyData = apiKeys.get(hashedKey);
      
      if (!keyData) {
        return;
      }
      
      keyData.usageStats.totalRequests++;
      if (stats.tokens) {
        keyData.usageStats.totalTokens += stats.tokens;
      }
      if (stats.lastCommand) {
        keyData.usageStats.lastCommand = stats.lastCommand;
      }
      
    } catch (error) {
      console.error('[ApiKeyService] Error updating stats:', error);
    }
  }

  /**
   * Get all keys for a user
   * @param {string} userId - User ID
   * @returns {Array} List of key statistics
   */
  getUserKeys(userId) {
    try {
      const hashedKeys = userKeys.get(userId) || [];
      return hashedKeys.map(hashedKey => this.getKeyStats(hashedKey)).filter(Boolean);
    } catch (error) {
      console.error('[ApiKeyService] Error getting user keys:', error);
      return [];
    }
  }

  /**
   * Get all active keys count (for admin)
   * @returns {number}
   */
  getActiveKeysCount() {
    let count = 0;
    for (const keyData of apiKeys.values()) {
      if (keyData.isActive) count++;
    }
    return count;
  }
}

// Singleton instance
const apiKeyService = new ApiKeyService();

module.exports = apiKeyService;
