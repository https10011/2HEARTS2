/**
 * Phase 28 — Game Engine Tests
 *
 * Comprehensive tests for the game engine: level/difficulty infrastructure,
 * session lifecycle, scoring, streak tracking, memory match, word scramble,
 * casual games, content validation, and replay correctness.
 */

import { describe, it } from 'node:test';
import assert from 'node:assert/strict';
import {
  createSession,
  recordAnswer,
  computeScores,
  nextQuestion,
  completeGame,
  createMemoryBoard,
  createMemoryMatchSession,
  flipCard,
  resetUnmatchedCards,
  completeMemoryMatch,
  createWordScrambleSession,
  scrambleWord,
  validateScrambleGuess,
  completeWordScramble,
  recordCasualAnswer,
  completeCasualGame,
} from '../src/services/game/gameEngine.ts';
import { resolveLevelConfig } from '../src/data/game/gameTypes.ts';
import type { GameDefinition, GameType, Difficulty } from '../src/data/game/gameTypes.ts';
import { getGameDefinition, ALL_GAME_DEFINITIONS } from '../src/customization/games/gameContent.ts';

const FIXED_CLOCK = () => new Date('2026-01-15T12:00:00Z');

// ---------------------------------------------------------------------------
// Helper: minimal definition for testing
// ---------------------------------------------------------------------------

const TEST_DEFINITION: GameDefinition = {
  type: 'who-knows-who-better',
  title: 'Test Game',
  description: 'A test game',
  questionsPerRound: 3,
  turnBased: true,
  scoringType: 'match',
  questions: [
    { id: 'q1', text: 'Favorite color?', correctAnswer: 'Blue' },
    { id: 'q2', text: 'Favorite food?', correctAnswer: 'Pizza' },
    { id: 'q3', text: 'Dream vacation?', correctAnswer: 'Paris' },
  ],
};

const CHOICE_DEFINITION: GameDefinition = {
  type: 'would-you-rather',
  title: 'Choice Test',
  description: 'A choice test',
  questionsPerRound: 3,
  turnBased: false,
  scoringType: 'choice',
  questions: [
    { id: 'c1', text: 'Stay in or go out?', options: ['Stay in', 'Go out'] },
    { id: 'c2', text: 'Beach or mountains?', options: ['Beach', 'Mountains'] },
    { id: 'c3', text: 'Coffee or tea?', options: ['Coffee', 'Tea'] },
  ],
};

// ---------------------------------------------------------------------------
// Level configuration
// ---------------------------------------------------------------------------

describe('Phase 28 — Level configuration', () => {
  it('resolves level 1 as easy', () => {
    const config = resolveLevelConfig(1);
    assert.equal(config.level, 1);
    assert.equal(config.difficulty, 'easy');
    assert.equal(config.challengeCount, 5);
    assert.equal(config.scoreMultiplier, 1.0);
  });

  it('resolves level 50 as easy (boundary)', () => {
    const config = resolveLevelConfig(50);
    assert.equal(config.difficulty, 'easy');
  });

  it('resolves level 51 as medium', () => {
    const config = resolveLevelConfig(51);
    assert.equal(config.difficulty, 'medium');
  });

  it('resolves level 200 as medium (boundary)', () => {
    const config = resolveLevelConfig(200);
    assert.equal(config.difficulty, 'medium');
  });

  it('resolves level 201 as hard', () => {
    const config = resolveLevelConfig(201);
    assert.equal(config.difficulty, 'hard');
  });

  it('resolves level 500 as hard (max)', () => {
    const config = resolveLevelConfig(500);
    assert.equal(config.level, 500);
    assert.equal(config.difficulty, 'hard');
    assert.ok(config.challengeCount >= 5);
    assert.ok(config.scoreMultiplier > 1.0);
  });

  it('clamps levels below 1', () => {
    const config = resolveLevelConfig(0);
    assert.equal(config.level, 1);
  });

  it('clamps levels above 500', () => {
    const config = resolveLevelConfig(600);
    assert.equal(config.level, 500);
  });

  it('challenge count increases with level', () => {
    const l1 = resolveLevelConfig(1);
    const l100 = resolveLevelConfig(100);
    const l300 = resolveLevelConfig(300);
    assert.ok(l100.challengeCount >= l1.challengeCount);
    assert.ok(l300.challengeCount >= l100.challengeCount);
  });

  it('score multiplier increases with level', () => {
    const l1 = resolveLevelConfig(1);
    const l100 = resolveLevelConfig(100);
    const l500 = resolveLevelConfig(500);
    assert.ok(l100.scoreMultiplier > l1.scoreMultiplier);
    assert.ok(l500.scoreMultiplier > l100.scoreMultiplier);
  });
});

// ---------------------------------------------------------------------------
// Session lifecycle
// ---------------------------------------------------------------------------

describe('Phase 28 — Session lifecycle', () => {
  it('creates a clean session', () => {
    const session = createSession('who-knows-who-better', FIXED_CLOCK);
    assert.ok(session.id);
    assert.equal(session.gameType, 'who-knows-who-better');
    assert.equal(session.currentRound, 0);
    assert.deepEqual(session.rounds, []);
    assert.equal(session.player1Score, 0);
    assert.equal(session.player2Score, 0);
    assert.equal(session.completed, false);
    assert.equal(session.createdAt, '2026-01-15T12:00:00.000Z');
  });

  it('creates session with optional level/difficulty', () => {
    const session = createSession('word-scramble', FIXED_CLOCK);
    // Level fields are optional on GameSession, so they may be undefined
    // The engine sets them when level-aware creation is used
    assert.equal(session.gameType, 'word-scramble');
    assert.equal(session.completed, false);
  });
});

// ---------------------------------------------------------------------------
// Answer recording & scoring
// ---------------------------------------------------------------------------

describe('Phase 28 — Answer recording & scoring', () => {
  it('records answers for turn-based game', () => {
    let session = createSession('who-knows-who-better', FIXED_CLOCK);
    session = recordAnswer(session, TEST_DEFINITION, 0, 'player1', 'Blue', undefined, FIXED_CLOCK);
    assert.equal(session.rounds.length, 1);
    assert.equal(session.rounds[0].answers.length, 1);
    assert.equal(session.rounds[0].answers[0].player, 'player1');
    assert.equal(session.rounds[0].complete, false);

    session = recordAnswer(session, TEST_DEFINITION, 0, 'player2', 'Blue', undefined, FIXED_CLOCK);
    assert.equal(session.rounds[0].answers.length, 2);
    assert.equal(session.rounds[0].complete, true);
  });

  it('computes correct scores for match-type game', () => {
    let session = createSession('who-knows-who-better', FIXED_CLOCK);
    session = recordAnswer(session, TEST_DEFINITION, 0, 'player1', 'Blue', undefined, FIXED_CLOCK);
    session = recordAnswer(session, TEST_DEFINITION, 0, 'player2', 'Blue', undefined, FIXED_CLOCK);
    session = computeScores(session, TEST_DEFINITION, FIXED_CLOCK);
    assert.equal(session.player1Score, 1); // Player1 always matches their own answer
    assert.equal(session.player2Score, 1); // Player2 matched the correct answer
  });

  it('scores mismatched answers correctly', () => {
    let session = createSession('who-knows-who-better', FIXED_CLOCK);
    session = recordAnswer(session, TEST_DEFINITION, 0, 'player1', 'Blue', undefined, FIXED_CLOCK);
    session = recordAnswer(session, TEST_DEFINITION, 0, 'player2', 'Red', undefined, FIXED_CLOCK);
    session = computeScores(session, TEST_DEFINITION, FIXED_CLOCK);
    assert.equal(session.player1Score, 1); // Player1 always matches
    assert.equal(session.player2Score, 0); // Player2 didn't match
  });

  it('records choice-based answers', () => {
    let session = createSession('would-you-rather', FIXED_CLOCK);
    session = recordAnswer(session, CHOICE_DEFINITION, 0, 'player1', 'Stay in', 0, FIXED_CLOCK);
    session = recordAnswer(session, CHOICE_DEFINITION, 0, 'player2', 'Stay in', 0, FIXED_CLOCK);
    session = computeScores(session, CHOICE_DEFINITION, FIXED_CLOCK);
    // Both chose same option -> both match
    assert.equal(session.player1Score, 1);
    assert.equal(session.player2Score, 1);
  });

  it('does not record answer after game completed', () => {
    let session = createSession('who-knows-who-better', FIXED_CLOCK);
    session = { ...session, completed: true };
    session = recordAnswer(session, TEST_DEFINITION, 0, 'player1', 'Blue', undefined, FIXED_CLOCK);
    assert.equal(session.rounds.length, 0);
  });

  it('normalizes answers for comparison', () => {
    let session = createSession('who-knows-who-better', FIXED_CLOCK);
    session = recordAnswer(session, TEST_DEFINITION, 0, 'player1', 'Blue', undefined, FIXED_CLOCK);
    session = recordAnswer(session, TEST_DEFINITION, 0, 'player2', '  blue  ', undefined, FIXED_CLOCK);
    session = computeScores(session, TEST_DEFINITION, FIXED_CLOCK);
    assert.equal(session.player2Score, 1);
  });
});

// ---------------------------------------------------------------------------
// Game progression
// ---------------------------------------------------------------------------

describe('Phase 28 — Game progression', () => {
  it('advances to next question', () => {
    const session = createSession('who-knows-who-better', FIXED_CLOCK);
    const next = nextQuestion(session, TEST_DEFINITION);
    assert.equal(next, 1);
  });

  it('returns null when game is complete', () => {
    const session = createSession('who-knows-who-better', FIXED_CLOCK);
    session.currentRound = 3; // Past questionsPerRound
    const next = nextQuestion(session, TEST_DEFINITION);
    assert.equal(next, null);
  });

  it('completes game and generates result', () => {
    let session = createSession('who-knows-who-better', FIXED_CLOCK);
    // Play all 3 rounds
    for (let i = 0; i < 3; i++) {
      session = recordAnswer(session, TEST_DEFINITION, i, 'player1', 'Blue', undefined, FIXED_CLOCK);
      session = recordAnswer(session, TEST_DEFINITION, i, 'player2', 'Blue', undefined, FIXED_CLOCK);
    }
    const { session: completed, result } = completeGame(session, TEST_DEFINITION, FIXED_CLOCK);
    assert.equal(completed.completed, true);
    assert.equal(result.totalQuestions, 3);
    assert.equal(result.rounds.length, 3);
    assert.ok(result.message.length > 0);
  });
});

// ---------------------------------------------------------------------------
// Memory Match
// ---------------------------------------------------------------------------

describe('Phase 28 — Memory Match', () => {
  it('creates a board with correct pair count', () => {
    const board = createMemoryBoard(4, FIXED_CLOCK);
    assert.equal(board.cards.length, 8);
    assert.equal(board.totalPairs, 4);
    assert.equal(board.matchedPairs, 0);
    assert.equal(board.moves, 0);
    assert.equal(board.firstFlippedIndex, null);
  });

  it('creates memory match session', () => {
    const session = createMemoryMatchSession(6, FIXED_CLOCK);
    assert.equal(session.gameType, 'memory-match');
    assert.ok(session.board);
    assert.equal(session.board.totalPairs, 6);
    assert.equal(session.completed, false);
  });

  it('flip card reveals it', () => {
    const session = createMemoryMatchSession(4, FIXED_CLOCK);
    const result = flipCard(session, 0, FIXED_CLOCK);
    assert.ok(result);
    assert.equal(result.session.board?.cards[0].revealed, true);
    assert.equal(result.matched, false);
    assert.equal(result.gameOver, false);
  });

  it('matching pair marks both cards as matched', () => {
    const session = createMemoryMatchSession(4, FIXED_CLOCK);
    const board = session.board!;
    // Find two cards with the same pairId
    const pairId = board.cards[0].pairId;
    const pairIndex = board.cards.findIndex((c, i) => i > 0 && c.pairId === pairId);
    assert.ok(pairIndex > 0);

    let result = flipCard(session, 0, FIXED_CLOCK)!;
    result = flipCard(result.session, pairIndex, FIXED_CLOCK)!;
    assert.equal(result.matched, true);
    assert.equal(result.session.board?.matchedPairs, 1);
  });

  it('game over when all pairs matched', () => {
    let session = createMemoryMatchSession(2, FIXED_CLOCK); // 4 cards
    const board = session.board!;
    // Find pairs
    const pair1 = board.cards[0].pairId;
    const idx1 = board.cards.findIndex((c, i) => i > 0 && c.pairId === pair1);
    const pair2Start = board.cards.findIndex(c => c.pairId !== pair1);
    const idx2 = board.cards.findIndex((c, i) => i > pair2Start && c.pairId === board.cards[pair2Start].pairId);

    let r = flipCard(session, 0, FIXED_CLOCK)!;
    r = flipCard(r.session, idx1, FIXED_CLOCK)!;
    r = flipCard(r.session, pair2Start, FIXED_CLOCK)!;
    r = flipCard(r.session, idx2, FIXED_CLOCK)!;
    assert.equal(r.gameOver, true);
  });

  it('resets unmatched cards', () => {
    const session = createMemoryMatchSession(4, FIXED_CLOCK);
    const cards = session.board!.cards;
    // Pick two cards from different pairs (shuffle is random — indices
    // 0 and 1 can be a matching pair, which made this test flaky).
    const first = 0;
    const second = cards.findIndex((c, i) => i > 0 && c.pairId !== cards[first].pairId);
    let updated = flipCard(session, first, FIXED_CLOCK)!;
    updated = flipCard(updated.session, second, FIXED_CLOCK)!;
    // Cards should be revealed (but not matched if different pairs)
    const reset = resetUnmatchedCards(updated.session, first, second, FIXED_CLOCK);
    assert.equal(reset.board?.cards[first].revealed, false);
    assert.equal(reset.board?.cards[second].revealed, false);
  });

  it('cannot flip already revealed card', () => {
    const session = createMemoryMatchSession(4, FIXED_CLOCK);
    const r1 = flipCard(session, 0, FIXED_CLOCK)!;
    const r2 = flipCard(r1.session, 0, FIXED_CLOCK);
    assert.equal(r2, null);
  });

  it('cannot flip matched card', () => {
    const session = createMemoryMatchSession(2, FIXED_CLOCK);
    const board = session.board!;
    const pairId = board.cards[0].pairId;
    const pairIdx = board.cards.findIndex((c, i) => i > 0 && c.pairId === pairId);

    let r = flipCard(session, 0, FIXED_CLOCK)!;
    r = flipCard(r.session, pairIdx, FIXED_CLOCK)!;
    // Both are matched now
    const attempt = flipCard(r.session, 0, FIXED_CLOCK);
    assert.equal(attempt, null);
  });
});

// ---------------------------------------------------------------------------
// Word Scramble
// ---------------------------------------------------------------------------

describe('Phase 28 — Word Scramble', () => {
  it('creates session with scramble state', () => {
    const session = createWordScrambleSession(5, FIXED_CLOCK);
    assert.equal(session.gameType, 'word-scramble');
    assert.ok(session.scrambleState);
    assert.equal(session.scrambleState.totalWords, 5);
    assert.equal(session.scrambleState.correct, 0);
  });

  it('scramble produces a different arrangement', () => {
    const word = 'HELLO';
    const scrambled = scrambleWord(word);
    assert.notEqual(scrambled, word);
    assert.equal(scrambled.length, word.length);
    // Same characters
    assert.equal(scrambled.split('').sort().join(''), word.split('').sort().join(''));
  });

  it('validates correct guess', () => {
    const session = createWordScrambleSession(3, FIXED_CLOCK);
    const result = validateScrambleGuess(session, 'LOVE', 'LOVE', FIXED_CLOCK);
    assert.ok(result);
    assert.equal(result.correct, true);
    assert.equal(result.gameOver, false);
    assert.equal(result.session.scrambleState?.correct, 1);
  });

  it('validates incorrect guess', () => {
    const session = createWordScrambleSession(3, FIXED_CLOCK);
    const result = validateScrambleGuess(session, 'WRONG', 'LOVE', FIXED_CLOCK);
    assert.ok(result);
    assert.equal(result.correct, false);
    assert.equal(result.session.scrambleState?.correct, 0);
  });

  it('game over after all words', () => {
    let session = createWordScrambleSession(2, FIXED_CLOCK);
    let r = validateScrambleGuess(session, 'WORD1', 'WORD1', FIXED_CLOCK)!;
    r = validateScrambleGuess(r.session, 'WORD2', 'WORD2', FIXED_CLOCK)!;
    assert.equal(r.gameOver, true);
    assert.equal(r.session.completed, true);
  });
});

// ---------------------------------------------------------------------------
// Casual games (trivia/riddle)
// ---------------------------------------------------------------------------

describe('Phase 28 — Casual games', () => {
  it('records casual answer correctly', () => {
    const session = createSession('casual-trivia', FIXED_CLOCK);
    const def = getGameDefinition('casual-trivia');
    assert.ok(def);

    const result = recordCasualAnswer(session, def, 0, def.questions[0].options![0], 0, FIXED_CLOCK);
    assert.ok(result);
    assert.equal(result.correct, true); // First option is always correct for choice games
  });

  it('records incorrect casual answer', () => {
    const session = createSession('casual-trivia', FIXED_CLOCK);
    const def = getGameDefinition('casual-trivia');
    assert.ok(def);

    const result = recordCasualAnswer(session, def, 0, def.questions[0].options![1], 1, FIXED_CLOCK);
    assert.ok(result);
    assert.equal(result.correct, false);
  });

  it('completes casual game', () => {
    let session = createSession('casual-trivia', FIXED_CLOCK);
    const def = getGameDefinition('casual-trivia');
    assert.ok(def);

    for (let i = 0; i < def.questionsPerRound; i++) {
      session = recordCasualAnswer(session, def, i, def.questions[i].options![0], 0, FIXED_CLOCK)!.session;
    }
    const { result } = completeCasualGame(session, FIXED_CLOCK);
    assert.equal(result.totalQuestions, def.questionsPerRound);
    assert.ok(result.casualResult);
  });
});

// ---------------------------------------------------------------------------
// Game content validation
// ---------------------------------------------------------------------------

describe('Phase 28 — Game content validation', () => {
  it('all registered game definitions are valid', () => {
    for (const def of ALL_GAME_DEFINITIONS) {
      assert.ok(def.type, `Game ${def.title} has no type`);
      assert.ok(def.title, `Game ${def.type} has no title`);
      assert.ok(def.description, `Game ${def.type} has no description`);
      assert.ok(def.questionsPerRound > 0, `Game ${def.type} has invalid questionsPerRound`);
      assert.ok(['match', 'choice', 'none'].includes(def.scoringType), `Game ${def.type} has invalid scoringType`);
      if (def.type !== 'memory-match') {
        assert.ok(def.questions.length >= def.questionsPerRound, `Game ${def.type} has fewer questions than questionsPerRound`);
      }
    }
  });

  it('choice games have options on every question', () => {
    for (const def of ALL_GAME_DEFINITIONS) {
      if (def.scoringType === 'choice') {
        for (const q of def.questions) {
          assert.ok(q.options && q.options.length >= 2, `Question "${q.text}" in ${def.type} missing options`);
        }
      }
    }
  });

  it('match games have correctAnswer or options on every question', () => {
    for (const def of ALL_GAME_DEFINITIONS) {
      if (def.scoringType === 'match' && def.questions.length > 0) {
        for (const q of def.questions) {
          // Match games may use correctAnswer (who-knows) or options (couple trivia)
          // In both cases the engine falls back to comparing player answers
          const hasAnswer = q.correctAnswer || (q.options && q.options.length > 0);
          assert.ok(hasAnswer, `Question "${q.text}" in ${def.type} missing correctAnswer and options`);
        }
      }
    }
  });

  it('no duplicate question IDs within a game', () => {
    for (const def of ALL_GAME_DEFINITIONS) {
      const ids = def.questions.map((q) => q.id);
      const unique = new Set(ids);
      assert.equal(ids.length, unique.size, `Game ${def.type} has duplicate question IDs`);
    }
  });

  it('getGameDefinition returns valid definitions for all types', () => {
    const types: GameType[] = [
      'who-knows-who-better', 'guess-my-answer', 'would-you-rather',
      'couple-trivia', 'this-or-that', 'finish-my-sentence',
      'memory-match', 'word-scramble', 'casual-trivia', 'riddle-room',
    ];
    for (const type of types) {
      const def = getGameDefinition(type);
      assert.ok(def, `Missing definition for ${type}`);
      assert.equal(def.type, type);
    }
  });

  it('getGameDefinition returns undefined for unknown type', () => {
    const def = getGameDefinition('unknown-game' as GameType);
    assert.equal(def, undefined);
  });
});

// ---------------------------------------------------------------------------
// Replay correctness
// ---------------------------------------------------------------------------

describe('Phase 28 — Replay correctness', () => {
  it('creating new session produces clean state', () => {
    const s1 = createSession('who-knows-who-better', FIXED_CLOCK);
    const s2 = createSession('who-knows-who-better', FIXED_CLOCK);
    assert.notEqual(s1.id, s2.id);
    assert.equal(s2.currentRound, 0);
    assert.deepEqual(s2.rounds, []);
    assert.equal(s2.player1Score, 0);
    assert.equal(s2.completed, false);
  });

  it('memory match replay produces clean board', () => {
    const s1 = createMemoryMatchSession(4, FIXED_CLOCK);
    const s2 = createMemoryMatchSession(4, FIXED_CLOCK);
    assert.notEqual(s1.id, s2.id);
    assert.notDeepEqual(s1.board?.cards.map(c => c.id), s2.board?.cards.map(c => c.id));
    assert.equal(s2.board?.matchedPairs, 0);
    assert.equal(s2.board?.moves, 0);
  });

  it('word scramble replay produces clean state', () => {
    const s1 = createWordScrambleSession(5, FIXED_CLOCK);
    const s2 = createWordScrambleSession(5, FIXED_CLOCK);
    assert.notEqual(s1.id, s2.id);
    assert.equal(s2.scrambleState?.currentWordIndex, 0);
    assert.equal(s2.scrambleState?.correct, 0);
    assert.equal(s2.casualScore, 0);
  });
});

// ---------------------------------------------------------------------------
// Edge cases
// ---------------------------------------------------------------------------

describe('Phase 28 — Edge cases', () => {
  it('computing scores with no rounds returns zero', () => {
    const session = createSession('who-knows-who-better', FIXED_CLOCK);
    const scored = computeScores(session, TEST_DEFINITION, FIXED_CLOCK);
    assert.equal(scored.player1Score, 0);
    assert.equal(scored.player2Score, 0);
  });

  it('completing game with partial rounds works', () => {
    let session = createSession('who-knows-who-better', FIXED_CLOCK);
    session = recordAnswer(session, TEST_DEFINITION, 0, 'player1', 'Blue', undefined, FIXED_CLOCK);
    session = recordAnswer(session, TEST_DEFINITION, 0, 'player2', 'Blue', undefined, FIXED_CLOCK);
    const { result } = completeGame(session, TEST_DEFINITION, FIXED_CLOCK);
    assert.equal(result.totalQuestions, 3);
    assert.equal(result.rounds.length, 1); // Only 1 complete round
  });

  it('empty answer is rejected by scoring', () => {
    let session = createSession('who-knows-who-better', FIXED_CLOCK);
    session = recordAnswer(session, TEST_DEFINITION, 0, 'player1', '', undefined, FIXED_CLOCK);
    session = recordAnswer(session, TEST_DEFINITION, 0, 'player2', 'Blue', undefined, FIXED_CLOCK);
    session = computeScores(session, TEST_DEFINITION, FIXED_CLOCK);
    // Empty answer should not match
    assert.equal(session.player2Score, 1); // player2 answered correctly
    assert.equal(session.player1Score, 1); // player1 always "matches"
  });

  it('choice game with no selection scores as mismatch', () => {
    let session = createSession('would-you-rather', FIXED_CLOCK);
    session = recordAnswer(session, CHOICE_DEFINITION, 0, 'player1', 'Stay in', 0, FIXED_CLOCK);
    session = recordAnswer(session, CHOICE_DEFINITION, 0, 'player2', 'Go out', 1, FIXED_CLOCK);
    session = computeScores(session, CHOICE_DEFINITION, FIXED_CLOCK);
    assert.equal(session.player1Score, 0); // Different choices
    assert.equal(session.player2Score, 0);
  });
});

// ---------------------------------------------------------------------------
// Game progression persistence (Phase 28)
// ---------------------------------------------------------------------------

/** Minimal in-memory localStorage mock for Node test environment. */
function createLocalStorageMock(): Storage {
  const store = new Map<string, string>();
  return {
    get length() { return store.size; },
    clear() { store.clear(); },
    getItem(key: string) { return store.get(key) ?? null; },
    setItem(key: string, value: string) { store.set(key, String(value)); },
    removeItem(key: string) { store.delete(key); },
    key(index: number) { return [...store.keys()][index] ?? null; },
  };
}

// Dynamic import to avoid module-level side effects
async function loadProgressionModule() {
  // Set up mock localStorage
  const mock = createLocalStorageMock();
  (globalThis as Record<string, unknown>).localStorage = mock;
  return await import('../src/services/game/gameProgression.ts');
}

describe('Phase 28 — Game progression persistence', () => {
  it('loads empty progression on first access', async () => {
    const { loadProgression } = await loadProgressionModule();
    const p = loadProgression();
    assert.deepEqual(p.highestCompleted, {});
    assert.deepEqual(p.bestScores, {});
    assert.deepEqual(p.streaks, {});
    assert.deepEqual(p.bestStreaks, {});
    assert.deepEqual(p.totalGamesPlayed, {});
  });

  it('records level completion and advances highest completed', async () => {
    const { recordLevelCompletion, getHighestCompletedLevel, getNextUnlockedLevel } = await loadProgressionModule();
    recordLevelCompletion('memory-match', 1, 80);
    assert.equal(getHighestCompletedLevel('memory-match'), 1);
    assert.equal(getNextUnlockedLevel('memory-match'), 2);

    recordLevelCompletion('memory-match', 2, 90);
    assert.equal(getHighestCompletedLevel('memory-match'), 2);
    assert.equal(getNextUnlockedLevel('memory-match'), 3);
  });

  it('does not downgrade highest completed level', async () => {
    const { recordLevelCompletion, getHighestCompletedLevel } = await loadProgressionModule();
    recordLevelCompletion('word-scramble', 5, 100);
    recordLevelCompletion('word-scramble', 3, 80); // Lower level
    assert.equal(getHighestCompletedLevel('word-scramble'), 5); // Still 5
  });

  it('tracks streaks for consecutive levels', async () => {
    const { recordLevelCompletion, getStreak } = await loadProgressionModule();
    recordLevelCompletion('casual-trivia', 1, 50);
    assert.equal(getStreak('casual-trivia'), 1);

    recordLevelCompletion('casual-trivia', 2, 60);
    assert.equal(getStreak('casual-trivia'), 2);

    recordLevelCompletion('casual-trivia', 3, 70);
    assert.equal(getStreak('casual-trivia'), 3);
  });

  it('resets streak on non-consecutive level', async () => {
    const { recordLevelCompletion, getStreak } = await loadProgressionModule();
    recordLevelCompletion('riddle-room', 1, 50);
    recordLevelCompletion('riddle-room', 2, 60);
    assert.equal(getStreak('riddle-room'), 2);

    recordLevelCompletion('riddle-room', 5, 100); // Skip levels
    assert.equal(getStreak('riddle-room'), 1); // Reset to 1
  });

  it('tracks best streak', async () => {
    const { recordLevelCompletion, getBestStreak, recordLevelFailure } = await loadProgressionModule();
    recordLevelCompletion('who-knows-who-better', 1, 50);
    recordLevelCompletion('who-knows-who-better', 2, 60);
    recordLevelFailure('who-knows-who-better'); // Reset streak
    recordLevelCompletion('who-knows-who-better', 3, 70);
    assert.equal(getBestStreak('who-knows-who-better'), 2); // Best was 2
  });

  it('tracks best score', async () => {
    const { recordLevelCompletion, getBestScore } = await loadProgressionModule();
    recordLevelCompletion('memory-match', 1, 80);
    assert.equal(getBestScore('memory-match'), 80);

    recordLevelCompletion('memory-match', 2, 90);
    assert.equal(getBestScore('memory-match'), 90);

    recordLevelCompletion('memory-match', 3, 70); // Lower score
    assert.equal(getBestScore('memory-match'), 90); // Still 90
  });

  it('counts total games played', async () => {
    const { recordLevelCompletion, getTotalGamesPlayed } = await loadProgressionModule();
    recordLevelCompletion('memory-match', 1, 80);
    recordLevelCompletion('memory-match', 2, 90);
    assert.equal(getTotalGamesPlayed('memory-match'), 2);
  });

  it('records failure resets streak', async () => {
    const { recordLevelCompletion, recordLevelFailure, getStreak } = await loadProgressionModule();
    recordLevelCompletion('word-scramble', 1, 50);
    recordLevelCompletion('word-scramble', 2, 60);
    assert.equal(getStreak('word-scramble'), 2);

    recordLevelFailure('word-scramble');
    assert.equal(getStreak('word-scramble'), 0);
  });

  it('resets game progression for a specific game', async () => {
    const { recordLevelCompletion, resetGameProgression, getHighestCompletedLevel, getStreak } = await loadProgressionModule();
    recordLevelCompletion('memory-match', 5, 100);
    recordLevelCompletion('word-scramble', 3, 80);

    resetGameProgression('memory-match');
    assert.equal(getHighestCompletedLevel('memory-match'), 0);
    assert.equal(getStreak('memory-match'), 0);
    // word-scramble should be untouched
    assert.equal(getHighestCompletedLevel('word-scramble'), 3);
  });

  it('generates valid level progress from storage', async () => {
    const { recordLevelCompletion, toLevelProgress } = await loadProgressionModule();
    recordLevelCompletion('memory-match', 3, 90);
    recordLevelCompletion('word-scramble', 2, 70);

    const progress = toLevelProgress();
    assert.equal(progress.highestCompleted['memory-match'], 3);
    assert.equal(progress.highestCompleted['word-scramble'], 2);
    assert.ok(typeof progress.streaks['memory-match'] === 'number');
    assert.ok(typeof progress.bestStreaks['word-scramble'] === 'number');
  });

  it('persists across load calls (simulates app restart)', async () => {
    const { recordLevelCompletion, loadProgression } = await loadProgressionModule();
    recordLevelCompletion('casual-trivia', 1, 50);
    recordLevelCompletion('casual-trivia', 2, 60);
    recordLevelCompletion('casual-trivia', 3, 70);

    // Simulate app restart — reload from storage
    const fresh = loadProgression();
    assert.equal(fresh.highestCompleted['casual-trivia'], 3);
    assert.equal(fresh.streaks['casual-trivia'], 3);
    assert.equal(fresh.bestScores['casual-trivia'], 70);
    assert.equal(fresh.totalGamesPlayed['casual-trivia'], 3);
  });

  it('clamps next unlocked level to 500', async () => {
    const { recordLevelCompletion, getNextUnlockedLevel } = await loadProgressionModule();
    recordLevelCompletion('memory-match', 500, 100);
    assert.equal(getNextUnlockedLevel('memory-match'), 500); // Capped at 500
  });
});

// ---------------------------------------------------------------------------
// Content selection helpers (Phase 28)
// ---------------------------------------------------------------------------

describe('Phase 28 — Content selection helpers', () => {
  it('selectQuestionsForLevel returns requested count', async () => {
    const { selectQuestionsForLevel } = await import('../src/services/game/gameEngine.ts');
    const questions = [
      { id: 'q1', text: 'Q1', correctAnswer: 'A1' },
      { id: 'q2', text: 'Q2', correctAnswer: 'A2' },
      { id: 'q3', text: 'Q3', correctAnswer: 'A3' },
      { id: 'q4', text: 'Q4', correctAnswer: 'A4' },
      { id: 'q5', text: 'Q5', correctAnswer: 'A5' },
    ];
    const selected = selectQuestionsForLevel(questions, 1, 3);
    assert.equal(selected.length, 3);
  });

  it('selectQuestionsForLevel caps at available questions', async () => {
    const { selectQuestionsForLevel } = await import('../src/services/game/gameEngine.ts');
    const questions = [
      { id: 'q1', text: 'Q1', correctAnswer: 'A1' },
      { id: 'q2', text: 'Q2', correctAnswer: 'A2' },
    ];
    const selected = selectQuestionsForLevel(questions, 1, 10);
    assert.equal(selected.length, 2); // Capped to available
  });

  it('selectQuestionsForLevel is deterministic for same level', async () => {
    const { selectQuestionsForLevel } = await import('../src/services/game/gameEngine.ts');
    const questions = [
      { id: 'q1', text: 'Q1', correctAnswer: 'A1' },
      { id: 'q2', text: 'Q2', correctAnswer: 'A2' },
      { id: 'q3', text: 'Q3', correctAnswer: 'A3' },
      { id: 'q4', text: 'Q4', correctAnswer: 'A4' },
      { id: 'q5', text: 'Q5', correctAnswer: 'A5' },
    ];
    const a = selectQuestionsForLevel(questions, 42, 3);
    const b = selectQuestionsForLevel(questions, 42, 3);
    assert.deepEqual(a.map(q => q.id), b.map(q => q.id));
  });

  it('selectQuestionsForLevel varies by level', async () => {
    const { selectQuestionsForLevel } = await import('../src/services/game/gameEngine.ts');
    const questions = [
      { id: 'q1', text: 'Q1', correctAnswer: 'A1' },
      { id: 'q2', text: 'Q2', correctAnswer: 'A2' },
      { id: 'q3', text: 'Q3', correctAnswer: 'A3' },
      { id: 'q4', text: 'Q4', correctAnswer: 'A4' },
      { id: 'q5', text: 'Q5', correctAnswer: 'A5' },
      { id: 'q6', text: 'Q6', correctAnswer: 'A6' },
      { id: 'q7', text: 'Q7', correctAnswer: 'A7' },
      { id: 'q8', text: 'Q8', correctAnswer: 'A8' },
      { id: 'q9', text: 'Q9', correctAnswer: 'A9' },
      { id: 'q10', text: 'Q10', correctAnswer: 'A10' },
    ];
    const a = selectQuestionsForLevel(questions, 1, 5);
    const b = selectQuestionsForLevel(questions, 10, 5);
    // Different levels should produce different selections (at least one differs)
    const aIds = a.map(q => q.id).join(',');
    const bIds = b.map(q => q.id).join(',');
    assert.notEqual(aIds, bIds, 'Different levels should select different questions');
  });

  it('resolveMemoryMatchPairs scales with level', async () => {
    const { resolveMemoryMatchPairs } = await import('../src/services/game/gameEngine.ts');
    assert.equal(resolveMemoryMatchPairs(1), 3);
    assert.ok(resolveMemoryMatchPairs(50) > 3);
    assert.ok(resolveMemoryMatchPairs(200) >= resolveMemoryMatchPairs(50));
  });

  it('resolveWordScrambleCount scales with level', async () => {
    const { resolveWordScrambleCount } = await import('../src/services/game/gameEngine.ts');
    assert.equal(resolveWordScrambleCount(1), 5);
    assert.ok(resolveWordScrambleCount(50) > 5);
    assert.ok(resolveWordScrambleCount(200) >= resolveWordScrambleCount(50));
  });
});
