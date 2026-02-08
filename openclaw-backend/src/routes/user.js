const express = require('express');
const authMiddleware = require('../middleware/auth');

const router = express.Router();

// Get user profile
router.get('/profile', authMiddleware, async (req, res) => {
  res.json(req.user);
});

// Get user settings
router.get('/settings', authMiddleware, async (req, res) => {
  res.json(req.user.settings || {});
});

// Update user settings
router.put('/settings', authMiddleware, async (req, res) => {
  try {
    const { preferredModel, language, ttsEnabled } = req.body;
    
    req.user.settings = {
      ...req.user.settings,
      ...(preferredModel && { preferredModel }),
      ...(language && { language }),
      ...(typeof ttsEnabled === 'boolean' && { ttsEnabled })
    };
    
    await req.user.save();
    res.json(req.user.settings);
  } catch (error) {
    console.error('Settings error:', error);
    res.status(500).json({ error: 'Server error' });
  }
});

// Verify subscription (placeholder)
router.post('/subscription/verify', authMiddleware, async (req, res) => {
  // TODO: Implement subscription verification with payment provider
  res.json({ 
    valid: true, 
    tier: req.user.tier,
    expiresAt: null 
  });
});

module.exports = router;
