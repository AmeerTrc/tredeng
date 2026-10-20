import assert from 'assert';
import bcrypt from 'bcryptjs';
import { DatabaseService } from '../services/storage/databaseService';
import { CryptoService } from '../services/cryptoService';

async function runTests() {
  console.log('--- Running Phase 2 Verification Tests ---');

  const db = DatabaseService.getInstance();

  // Test 1: User Registration & Persistence
  const testEmail = `test_${Date.now()}@example.com`;
  const password = 'StrongPassword123!';
  const hashedPassword = await bcrypt.hash(password, 10);

  const createdUser = db.createUser(testEmail, hashedPassword);
  assert.ok(createdUser.id, 'User ID should be generated');
  assert.strictEqual(createdUser.email, testEmail);

  // Verify retrieval
  const fetchedUser = db.getUserByEmail(testEmail);
  assert.ok(fetchedUser, 'User should be found in persistent database');
  assert.strictEqual(fetchedUser!.id, createdUser.id);

  // Test 2: Password Verification (Bcrypt)
  const isMatch = await bcrypt.compare(password, fetchedUser!.passwordHash);
  assert.strictEqual(isMatch, true, 'Correct password must match hash');

  const isWrongMatch = await bcrypt.compare('WrongPassword999', fetchedUser!.passwordHash);
  assert.strictEqual(isWrongMatch, false, 'Wrong password must NOT match hash');

  // Test 3: Encryption & Decryption (AES-256-GCM)
  const sampleSecret = 'binance_secret_key_abcdef1234567890';
  const encrypted = CryptoService.encrypt(sampleSecret);
  assert.ok(encrypted.encryptedText, 'Ciphertext must exist');
  assert.ok(encrypted.iv, 'IV must exist');
  assert.ok(encrypted.tag, 'Auth Tag must exist');

  const decrypted = CryptoService.decrypt(encrypted);
  assert.strictEqual(decrypted, sampleSecret, 'Decrypted text must match original secret');

  // Test 4: Save & Retrieve User Isolated Exchange Keys
  db.saveExchangeKeys(createdUser.id, 'binance', encrypted, encrypted);
  const keys = db.getExchangeKeys(createdUser.id);
  assert.ok(keys, 'Exchange keys should exist for user');
  assert.strictEqual(keys!.userId, createdUser.id);

  // Test 5: Kill Switch Persistence
  assert.strictEqual(db.isKillSwitchActive(createdUser.id), false, 'Default kill switch should be false');
  db.setKillSwitch(createdUser.id, true);
  assert.strictEqual(db.isKillSwitchActive(createdUser.id), true, 'Kill switch should persist as active');
  db.setKillSwitch(createdUser.id, false);
  assert.strictEqual(db.isKillSwitchActive(createdUser.id), false, 'Kill switch should toggle back to inactive');

  console.log('✅ All Phase 2 Core Security & Database Unit Tests Passed Successfully!');
}

runTests().catch(err => {
  console.error('❌ Test Failed:', err);
  process.exit(1);
});
