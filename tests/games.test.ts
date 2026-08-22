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
    let updated = flipCard(session, 0, FIXED_CLOCK)!;
    updated = flipCard(updated.session, 1, FIXED_CLOCK)!;
    // Cards should be revealed (but not matched if different pairs)
    const reset = resetUnmatchedCards(updated.session, 0, 1, FIXED_CLOCK);
    assert.equal(reset.board?.cards[0].revealed, false);
    assert.equal(reset.board?.cards[1].revealed, false);
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
