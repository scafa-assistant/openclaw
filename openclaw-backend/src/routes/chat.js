const express = require('express');
const { v4: uuidv4 } = require('uuid');
const ChatMessage = require('../models/ChatMessage');
const authMiddleware = require('../middleware/auth');
const llmService = require('../services/llmService');

const router = express.Router();

// Send message
router.post('/message', authMiddleware, async (req, res) => {
  try {
    const { message, model = 'gemini-2.5-flash', stream = false } = req.body;

    if (!message) {
      return res.status(400).json({ error: 'Message is required' });
    }

    // Save user message
    const userMessage = new ChatMessage({
      userId: req.userId,
      role: 'user',
      content: message
    });
    await userMessage.save();

    // Get LLM response
    let responseContent;
    let tokens = 0;
    
    try {
      const llmResponse = await llmService.sendMessage(message, model);
      responseContent = llmResponse.content;
      tokens = llmResponse.tokens || 0;
    } catch (error) {
      console.error('LLM Error:', error);
      responseContent = 'Entschuldigung, ich konnte keine Verbindung zum KI-Service herstellen. Bitte versuche es später erneut.';
    }

    // Save assistant message
    const assistantMessage = new ChatMessage({
      userId: req.userId,
      role: 'assistant',
      content: responseContent,
      model,
      tokens
    });
    await assistantMessage.save();

    res.json({
      id: assistantMessage._id.toString(),
      content: responseContent,
      model,
      tokens
    });
  } catch (error) {
    console.error('Chat error:', error);
    res.status(500).json({ error: 'Server error' });
  }
});

// Get chat history
router.get('/history', authMiddleware, async (req, res) => {
  try {
    const limit = parseInt(req.query.limit) || 50;
    
    const messages = await ChatMessage
      .find({ userId: req.userId })
      .sort({ timestamp: -1 })
      .limit(limit)
      .lean();

    // Format for response
    const formattedMessages = messages.map(msg => ({
      id: msg._id.toString(),
      role: msg.role,
      content: msg.content,
      model: msg.model,
      timestamp: msg.timestamp.getTime()
    }));

    res.json(formattedMessages);
  } catch (error) {
    console.error('History error:', error);
    res.status(500).json({ error: 'Server error' });
  }
});

module.exports = router;
