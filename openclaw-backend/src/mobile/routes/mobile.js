/**
 * Mobile API Routes
 * Main API endpoints for the OpenClaw mobile app
 */

const express = require('express');
const { v4: uuidv4 } = require('uuid');
const fs = require('fs').promises;
const path = require('path');

const apiKeyService = require('../services/apiKeyService');
const { mobileAuthMiddleware, adminAuthMiddleware } = require('../middleware/mobileAuth');
const SmartLLMRouter = require('../../services/smartLLMRouter');
const cacheService = require('../../services/cacheService');

const router = express.Router();
const llmRouter = new SmartLLMRouter();

// Base directory for customer files
const CUSTOMERS_DIR = path.join(process.cwd(), '..', '..', 'kunden');

/**
 * Ensure customer directory exists
 */
async function ensureCustomerDir(userId) {
  const customerDir = path.join(CUSTOMERS_DIR, userId);
  const subdirs = ['commands', 'websites', 'files', 'exports'];
  
  try {
    await fs.mkdir(customerDir, { recursive: true });
    for (const subdir of subdirs) {
      await fs.mkdir(path.join(customerDir, subdir), { recursive: true });
    }
    return customerDir;
  } catch (error) {
    console.error('[MobileRoutes] Error creating customer dir:', error);
    throw error;
  }
}

/**
 * POST /api/mobile/register
 * Register a new user and generate API key
 */
router.post('/register', async (req, res) => {
  try {
    const { name, email, description } = req.body;
    
    // Generate new API key
    const result = await apiKeyService.generateKey(null, {
      name: name || 'Mobile App',
      description: description || 'Generated via mobile API',
      email: email || null
    });
    
    // Create customer directory
    await ensureCustomerDir(result.userId);
    
    // Create user config file
    const configPath = path.join(CUSTOMERS_DIR, result.userId, 'config.json');
    await fs.writeFile(configPath, JSON.stringify({
      userId: result.userId,
      createdAt: result.createdAt,
      name: name || 'Anonymous',
      email: email || null,
      settings: {
        defaultModel: 'gemini-2.5-flash',
        notifications: true,
        autoSave: true
      }
    }, null, 2));
    
    console.log(`[MobileRoutes] New user registered: ${result.userId}`);
    
    res.status(201).json({
      success: true,
      message: 'User registered successfully',
      data: {
        apiKey: result.apiKey,
        userId: result.userId,
        createdAt: result.createdAt,
        warning: 'Store this API key safely - it will not be shown again!'
      }
    });
    
  } catch (error) {
    console.error('[MobileRoutes] Registration error:', error);
    res.status(500).json({
      success: false,
      error: 'Registration failed',
      message: error.message
    });
  }
});

/**
 * POST /api/mobile/auth
 * Validate API key and return user info
 */
router.post('/auth', mobileAuthMiddleware, async (req, res) => {
  try {
    const { userId, metadata, rateLimit } = req.user;
    
    // Update API key usage
    apiKeyService.updateUsageStats(req.user.hashedKey, { lastCommand: 'auth' });
    
    res.json({
      success: true,
      message: 'Authentication successful',
      data: {
        userId: userId,
        metadata: metadata,
        rateLimit: rateLimit,
        serverTime: new Date().toISOString()
      }
    });
    
  } catch (error) {
    console.error('[MobileRoutes] Auth error:', error);
    res.status(500).json({
      success: false,
      error: 'Authentication check failed'
    });
  }
});

/**
 * POST /api/mobile/command
 * Main endpoint to receive commands from mobile app
 */
router.post('/command', mobileAuthMiddleware, async (req, res) => {
  const commandId = uuidv4();
  const startTime = Date.now();
  
  try {
    const { command, context = {}, model = 'gemini-2.5-flash' } = req.body;
    const { userId, hashedKey } = req.user;
    
    // Validate input
    if (!command || typeof command !== 'string') {
      return res.status(400).json({
        success: false,
        error: 'Command is required',
        commandId
      });
    }
    
    console.log(`[MobileRoutes] Command received: "${command.substring(0, 50)}..." from user: ${userId}`);
    
    // Save command to history
    const customerDir = await ensureCustomerDir(userId);
    const commandFile = path.join(customerDir, 'commands', `${commandId}.json`);
    
    const commandData = {
      commandId,
      userId,
      command,
      context: {
        source: context.source || 'mobile',
        device: context.device || 'unknown',
        timestamp: new Date().toISOString(),
        ...context
      },
      status: 'processing',
      createdAt: new Date().toISOString()
    };
    
    await fs.writeFile(commandFile, JSON.stringify(commandData, null, 2));
    
    // Check cache for similar commands
    const cacheKey = `command:${userId}:${Buffer.from(command).toString('base64')}`;
    const cached = cacheService.get(cacheKey);
    
    if (cached) {
      console.log(`[MobileRoutes] Cache hit for command: ${commandId}`);
      
      // Update command file
      commandData.status = 'completed';
      commandData.completedAt = new Date().toISOString();
      commandData.result = cached;
      commandData.fromCache = true;
      await fs.writeFile(commandFile, JSON.stringify(commandData, null, 2));
      
      // Update stats
      apiKeyService.updateUsageStats(hashedKey, { lastCommand: command });
      
      return res.json({
        success: true,
        commandId,
        result: cached,
        fromCache: true,
        processingTime: Date.now() - startTime
      });
    }
    
    // Process command with LLM Router
    let result;
    try {
      // Route to appropriate model/service
      const llmResult = await llmRouter.route(command, {
        userId,
        model,
        context: commandData.context
      });
      
      result = {
        response: llmResult.content || llmResult,
        model: llmResult.model || model,
        tokens: llmResult.tokens || 0,
        actions: llmResult.actions || []
      };
      
      // Handle special commands (website building, etc.)
      if (command.toLowerCase().includes('website') || command.toLowerCase().includes('webseite')) {
        result.type = 'website';
        result.outputPath = path.join('kunden', userId, 'websites', commandId);
        // Website generation would happen here
      }
      
      if (command.toLowerCase().includes('timer') || command.toLowerCase().includes('wecker')) {
        result.type = 'timer';
        result.systemAction = 'timer';
      }
      
    } catch (error) {
      console.error('[MobileRoutes] LLM processing error:', error);
      result = {
        response: 'Entschuldigung, ich konnte den Befehl nicht verarbeiten. Bitte versuche es erneut.',
        error: error.message,
        type: 'error'
      };
    }
    
    // Save result
    commandData.status = 'completed';
    commandData.completedAt = new Date().toISOString();
    commandData.result = result;
    await fs.writeFile(commandFile, JSON.stringify(commandData, null, 2));
    
    // Cache result (5 minutes for mobile commands)
    cacheService.set(cacheKey, result, 300);
    
    // Update usage stats
    apiKeyService.updateUsageStats(hashedKey, {
      lastCommand: command,
      tokens: result.tokens || 0
    });
    
    const processingTime = Date.now() - startTime;
    console.log(`[MobileRoutes] Command completed: ${commandId} in ${processingTime}ms`);
    
    res.json({
      success: true,
      commandId,
      result,
      processingTime,
      userId
    });
    
  } catch (error) {
    console.error('[MobileRoutes] Command error:', error);
    res.status(500).json({
      success: false,
      error: 'Command processing failed',
      message: error.message,
      commandId
    });
  }
});

/**
 * GET /api/mobile/status
 * Check server status and connectivity
 */
router.get('/status', async (req, res) => {
  try {
    const status = {
      status: 'online',
      timestamp: new Date().toISOString(),
      version: '1.0.0-mobile',
      features: {
        commands: true,
        websocket: false, // Will be enabled later
        cache: cacheService.getStats(),
        activeUsers: apiKeyService.getActiveKeysCount()
      }
    };
    
    res.json(status);
    
  } catch (error) {
    console.error('[MobileRoutes] Status error:', error);
    res.status(500).json({
      status: 'error',
      message: 'Could not retrieve status'
    });
  }
});

/**
 * GET /api/mobile/history
 * Get command history for authenticated user
 */
router.get('/history', mobileAuthMiddleware, async (req, res) => {
  try {
    const { userId } = req.user;
    const limit = parseInt(req.query.limit) || 20;
    
    const customerDir = path.join(CUSTOMERS_DIR, userId, 'commands');
    
    try {
      const files = await fs.readdir(customerDir);
      const commands = [];
      
      // Read last N command files
      const sortedFiles = files
        .filter(f => f.endsWith('.json'))
        .sort((a, b) => b.localeCompare(a))
        .slice(0, limit);
      
      for (const file of sortedFiles) {
        try {
          const content = await fs.readFile(path.join(customerDir, file), 'utf8');
          const command = JSON.parse(content);
          commands.push({
            commandId: command.commandId,
            command: command.command,
            status: command.status,
            createdAt: command.createdAt,
            result: command.result ? {
              type: command.result.type,
              response: command.result.response?.substring(0, 200) // Truncate
            } : null
          });
        } catch (e) {
          // Skip invalid files
        }
      }
      
      res.json({
        success: true,
        userId,
        commands,
        total: files.length
      });
      
    } catch (error) {
      // No commands yet
      res.json({
        success: true,
        userId,
        commands: [],
        total: 0
      });
    }
    
  } catch (error) {
    console.error('[MobileRoutes] History error:', error);
    res.status(500).json({
      success: false,
      error: 'Could not retrieve history'
    });
  }
});

/**
 * GET /api/mobile/stats (Admin only)
 * Get system statistics
 */
router.get('/stats', adminAuthMiddleware, async (req, res) => {
  try {
    res.json({
      success: true,
      stats: {
        activeKeys: apiKeyService.getActiveKeysCount(),
        cache: cacheService.getStats(),
        uptime: process.uptime(),
        memory: process.memoryUsage()
      }
    });
  } catch (error) {
    console.error('[MobileRoutes] Stats error:', error);
    res.status(500).json({
      success: false,
      error: 'Could not retrieve stats'
    });
  }
});

module.exports = router;
