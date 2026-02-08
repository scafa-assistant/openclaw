// Auto Test Account für sofortiges Testen
// Fügt einen Test-User bei App-Start automatisch hinzu

const TEST_ACCOUNTS = [
  { email: 'demo1@openclaw.test', password: 'demo123', name: 'Demo User 1' },
  { email: 'demo2@openclaw.test', password: 'demo123', name: 'Demo User 2' },
  { email: 'gast@openclaw.test', password: 'gast123', name: 'Gast Account' }
];

// Auto-login middleware
function autoTestAccountMiddleware(req, res, next) {
  // Wenn ?demo=true in URL, automatisch einloggen
  if (req.query.demo === 'true') {
    const testUser = TEST_ACCOUNTS[0];
    req.demoUser = testUser;
    console.log(`[AUTO-LOGIN] Demo-Account: ${testUser.email}`);
  }
  next();
}

// Test-Account erstellen beim Server-Start
function seedTestAccounts(users, messages) {
  console.log('🌱 Seeding Test Accounts...');
  
  TEST_ACCOUNTS.forEach(account => {
    if (!users.has(account.email)) {
      const user = {
        id: 'test-' + Date.now() + '-' + Math.random().toString(36).substr(2, 9),
        email: account.email,
        password: account.password,
        name: account.name,
        tier: 'FREE',
        createdAt: new Date()
      };
      users.set(account.email, user);
      messages.set(user.id, []);
      console.log(`  ✅ Created: ${account.email} / ${account.password}`);
    }
  });
  
  console.log(`📝 ${TEST_ACCOUNTS.length} Test-Accounts bereit`);
  console.log('🧪 Test: curl http://127.0.0.1:3000/api/v1/auth/test-accounts');
}

module.exports = { TEST_ACCOUNTS, autoTestAccountMiddleware, seedTestAccounts };
