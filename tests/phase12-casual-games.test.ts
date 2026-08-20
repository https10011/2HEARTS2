/**
 * Phase 12 — Fun / Casual Games tests.
 *
 * Tests the shared game engine extensions for casual games:
 * Memory Match, Word Scramble, casual trivia, and Riddle Room.
 * Verifies the engine is genuinely reusable and games don't duplicate logic.
 */

import { describe, it } from 'node:test';
import assert from 'node:assert/strict';

import {
  createMemoryMatchSession,
  flipCard,
  resetUnmatchedCards,
  completeMemoryMatch,
  createMemoryBoard,
  createWordScrambleSession,
  scrambleWord,
  validateScrambleGuess,
  completeWordScramble,
  recordCasualAnswer,
  completeCasualGame,
  createSession,
} from '../src/services/game/gameEngine.ts';
import {
  ALL_GAME_DEFINITIONS,
  CASUAL_GAME_DEFINITIONS,
  COUPLE_GAME_DEFINITIONS,
  getGameDefinition,
  getGameCategory,
} from '../src/customization/games/gameContent.ts';

// ---------------------------------------------------------------------------
// Shared framework
// ---------------------------------------------------------------------------

describe('Shared game framework reuse', () => {
  it('COUPLE_GAME_DEFINITIONS has 6 entries', () => {
    assert.equal(COUPLE_GAME_DEFINITIONS.length, 6);
  });

  it('CASUAL_GAME_DEFINITIONS has 4 entries', () => {
    assert.equal(CASUAL_GAME_DEFINITIONS.length, 4);
  });

  it('ALL_GAME_DEFINITIONS is union of couple + casual', () => {
    assert.equal(ALL_GAME_DEFINITIONS.length, 10);
    assert.equal(
      ALL_GAME_DEFINITIONS.length,
      COUPLE_GAME_DEFINITIONS.length + CASUAL_GAME_DEFINITIONS.length,
    );
  });

  it('getGameCategory returns correct categories', () => {
    assert.equal(getGameCategory('who-knows-who-better'), 'couple');
    assert.equal(getGameCategory('would-you-rather'), 'couple');
    assert.equal(getGameCategory('memory-match'), 'casual');
    assert.equal(getGameCategory('word-scramble'), 'casual');
    assert.equal(getGameCategory('casual-trivia'), 'casual');
    assert.equal(getGameCategory('riddle-room'), 'casual');
  });

  it('createSession works for any game type', () => {
    for (const def of ALL_GAME_DEFINITIONS) {
      const session = createSession(def.type);
      assert.equal(session.gameType, def.type);
      assert.equal(session.completed, false);
      assert.equal(session.rounds.length, 0);
    }
  });
});

// ---------------------------------------------------------------------------
// Memory Match
// ---------------------------------------------------------------------------

describe('Memory Match engine', () => {
  it('createMemoryBoard produces correct pair count', () => {
    const board = createMemoryBoard(6);
    assert.equal(board.totalPairs, 6);
    assert.equal(board.cards.length, 12);
    assert.equal(board.matchedPairs, 0);
    assert.equal(board.moves, 0);
  });

  it('each pair appears exactly twice', () => {
    const board = createMemoryBoard(8);
    const pairCounts = new Map<string, number>();
    for (const card of board.cards) {
      pairCounts.set(card.pairId, (pairCounts.get(card.pairId) ?? 0) + 1);
    }
    for (const [, count] of pairCounts) {
      assert.equal(count, 2);
    }
    assert.equal(pairCounts.size, 8);
  });

  it('flipCard reveals first card without match', () => {
    const session = createMemoryMatchSession(4);
    const result = flipCard(session, 0);
    assert.ok(result);
    assert.equal(result.matched, false);
    assert.equal(result.gameOver, false);
    assert.equal(result.session.board!.firstFlippedIndex, 0);
    assert.equal(result.session.board!.cards[0].revealed, true);
  });

  it('flipCard detects match when pair matches', () => {
    const session = createMemoryMatchSession(4);
    // Find a pair
    const board = session.board!;
    const pairId = board.cards[0].pairId;
    const secondIdx = board.cards.findIndex(
      (c, i) => i > 0 && c.pairId === pairId,
    );

    const r1 = flipCard(session, 0);
    assert.ok(r1);
    const r2 = flipCard(r1.session, secondIdx);
    assert.ok(r2);
    assert.equal(r2.matched, true);
    assert.equal(r2.session.board!.matchedPairs, 1);
  });

  it('flipCard handles non-match correctly', () => {
    const session = createMemoryMatchSession(4);
    const board = session.board!;
    // Find two cards with different pairIds
    const idx1 = board.cards.findIndex((c) => c.pairId !== board.cards[0].pairId);

    const r1 = flipCard(session, 0);
    assert.ok(r1);
    const r2 = flipCard(r1.session, idx1);
    assert.ok(r2);
    assert.equal(r2.matched, false);
    assert.equal(r2.session.board!.moves, 1);
    assert.equal(r2.session.board!.matchedPairs, 0);
  });

  it('resetUnmatchedCards hides revealed non-matched cards', () => {
    const session = createMemoryMatchSession(4);
    const board = session.board!;
    const idx1 = board.cards.findIndex((c) => c.pairId !== board.cards[0].pairId);

    const r1 = flipCard(session, 0);
    assert.ok(r1);
    const r2 = flipCard(r1.session, idx1);
    assert.ok(r2);

    const reset = resetUnmatchedCards(r2.session, 0, idx1);
    assert.equal(reset.board!.cards[0].revealed, false);
    assert.equal(reset.board!.cards[idx1].revealed, false);
  });

  it('cannot flip already revealed or matched cards', () => {
    const session = createMemoryMatchSession(4);
    const r1 = flipCard(session, 0);
    assert.ok(r1);
    // Try flipping same card again
    const r2 = flipCard(r1.session, 0);
    assert.equal(r2, null);
  });

  it('completes game when all pairs found', () => {
    const session = createMemoryMatchSession(2); // 4 cards, 2 pairs
    const board = session.board!;
    // Find the two pairs
    const pair1Idx = 0;
    const pair1MatchIdx = board.cards.findIndex(
      (c, i) => i > 0 && c.pairId === board.cards[pair1Idx].pairId,
    );
    const pair2Cards = board.cards.filter(
      (c, i) => i !== pair1Idx && i !== pair1MatchIdx,
    );
    const pair2Idx = board.cards.indexOf(pair2Cards[0]);
    const pair2MatchIdx = board.cards.indexOf(pair2Cards[1]);

    let s = session;
    const f1 = flipCard(s, pair1Idx);
    assert.ok(f1);
    s = f1.session;
    const f2 = flipCard(s, pair1MatchIdx);
    assert.ok(f2);
    s = f2.session;
    assert.equal(s.board!.matchedPairs, 1);

    const f3 = flipCard(s, pair2Idx);
    assert.ok(f3);
    s = f3.session;
    const f4 = flipCard(s, pair2MatchIdx);
    assert.ok(f4);
    assert.equal(f4.session.board!.matchedPairs, 2);
    assert.equal(f4.gameOver, true);
  });

  it('completeMemoryMatch generates result', () => {
    const session = createMemoryMatchSession(4);
    session.board!.moves = 10;
    session.board!.matchedPairs = 4;
    const { result } = completeMemoryMatch(session);
    assert.equal(result.gameType, 'memory-match');
    assert.equal(result.totalQuestions, 4);
    assert.ok(result.casualResult);
    assert.equal(result.casualResult.score, 4);
    assert.equal(result.casualResult.moves, 10);
  });
});

// ---------------------------------------------------------------------------
// Word Scramble
// ---------------------------------------------------------------------------

describe('Word Scramble engine', () => {
  it('scrambleWord produces a different arrangement', () => {
    const word = 'HELLO';
    // Run multiple times — at least one should be different
    let foundDifferent = false;
    for (let i = 0; i < 20; i++) {
      if (scrambleWord(word) !== word) {
        foundDifferent = true;
        break;
      }
    }
    assert.ok(foundDifferent, 'scrambleWord should produce a different arrangement');
  });

  it('scrambleWord preserves all characters', () => {
    const word = 'TEST';
    const scrambled = scrambleWord(word);
    const sortWord = word.split('').sort().join('');
    const sortScrambled = scrambled.split('').sort().join('');
    assert.equal(sortScrambled, sortWord);
  });

  it('createWordScrambleSession initializes correctly', () => {
    const session = createWordScrambleSession(10);
    assert.equal(session.gameType, 'word-scramble');
    assert.ok(session.scrambleState);
    assert.equal(session.scrambleState.totalWords, 10);
    assert.equal(session.scrambleState.correct, 0);
    assert.equal(session.completed, false);
  });

  it('validateScrambleGuess handles correct answer', () => {
    const session = createWordScrambleSession(5);
    const result = validateScrambleGuess(session, 'love', 'LOVE');
    assert.ok(result);
    assert.equal(result.correct, true);
    assert.equal(result.session.scrambleState!.correct, 1);
    assert.equal(result.session.casualScore, 1);
  });

  it('validateScrambleGuess handles case-insensitive match', () => {
    const session = createWordScrambleSession(5);
    const result = validateScrambleGuess(session, 'hello', 'HELLO');
    assert.ok(result);
    assert.equal(result.correct, true);
  });

  it('validateScrambleGuess handles wrong answer', () => {
    const session = createWordScrambleSession(5);
    const result = validateScrambleGuess(session, 'wrong', 'LOVE');
    assert.ok(result);
    assert.equal(result.correct, false);
    assert.equal(result.session.scrambleState!.correct, 0);
  });

  it('validateScrambleGuess completes after all words', () => {
    const session = createWordScrambleSession(3);
    let s = session;
    for (let i = 0; i < 3; i++) {
      const r = validateScrambleGuess(s, 'answer', 'ANSWER');
      assert.ok(r);
      s = r.session;
    }
    assert.equal(s.completed, true);
    assert.equal(s.scrambleState!.correct, 3);
  });

  it('completeWordScramble generates result', () => {
    const session = createWordScrambleSession(10);
    session.scrambleState!.correct = 7;
    session.casualScore = 7;
    const { result } = completeWordScramble(session);
    assert.equal(result.gameType, 'word-scramble');
    assert.ok(result.casualResult);
    assert.equal(result.casualResult.score, 7);
    assert.equal(result.casualResult.accuracy, 70);
  });
});

// ---------------------------------------------------------------------------
// Casual trivia / riddle room (single-player question games)
// ---------------------------------------------------------------------------

describe('Casual single-player game engine', () => {
  it('recordCasualAnswer correctly scores matching answer', () => {
    const def = getGameDefinition('casual-trivia')!;
    const session = createSession('casual-trivia');
    // First question has 'Paris' as correct option (index 0)
    const result = recordCasualAnswer(session, def, 0, 'Paris', 0);
    assert.ok(result);
    assert.equal(result.correct, true);
    assert.equal(result.session.casualScore, 1);
  });

  it('recordCasualAnswer scores incorrect option as wrong', () => {
    const def = getGameDefinition('casual-trivia')!;
    const session = createSession('casual-trivia');
    const result = recordCasualAnswer(session, def, 0, 'London', 1);
    assert.ok(result);
    assert.equal(result.correct, false);
    assert.equal(result.session.casualScore, 0);
  });

  it('recordCasualAnswer for riddle room (text input)', () => {
    const def = getGameDefinition('riddle-room')!;
    const session = createSession('riddle-room');
    // First riddle: answer is 'Artichoke'
    const result = recordCasualAnswer(session, def, 0, 'Artichoke');
    assert.ok(result);
    assert.equal(result.correct, true);
    assert.equal(result.session.casualScore, 1);
  });

  it('recordCasualAnswer for riddle room handles wrong answer', () => {
    const def = getGameDefinition('riddle-room')!;
    const session = createSession('riddle-room');
    const result = recordCasualAnswer(session, def, 0, 'wrong answer');
    assert.ok(result);
    assert.equal(result.correct, false);
    assert.equal(result.session.casualScore, 0);
  });

  it('casual game completes after all questions', () => {
    const def = getGameDefinition('casual-trivia')!;
    const session = createSession('casual-trivia');
    let s = session;
    for (let i = 0; i < def.questionsPerRound; i++) {
      const r = recordCasualAnswer(s, def, i, 'Paris', 0);
      assert.ok(r);
      s = r.session;
    }
    assert.equal(s.completed, true);
  });

  it('completeCasualGame generates result with correct stats', () => {
    const session = createSession('casual-trivia');
    session.casualScore = 8;
    session.casualMoves = 10;
    session.rounds = Array.from({ length: 10 }, (_, i) => ({
      questionIndex: i,
      question: { id: `q-${i}`, text: `Question ${i}`, options: ['A', 'B'] },
      answers: [{ questionId: `q-${i}`, player: 'player1' as const, answer: 'A', matched: i < 8 }],
      complete: true,
    }));
    const { result } = completeCasualGame(session);
    assert.equal(result.gameType, 'casual-trivia');
    assert.equal(result.player1Score, 8);
    assert.ok(result.casualResult);
    assert.equal(result.casualResult.accuracy, 80);
  });

  it('recordCasualAnswer returns null for completed session', () => {
    const def = getGameDefinition('casual-trivia')!;
    const session = createSession('casual-trivia');
    session.completed = true;
    const result = recordCasualAnswer(session, def, 0, 'Paris', 0);
    assert.equal(result, null);
  });
});

// ---------------------------------------------------------------------------
// Integration: Games Hub categories
// ---------------------------------------------------------------------------

describe('Games Hub integration', () => {
  it('couple games and casual games are correctly separated', () => {
    const coupleTypes = COUPLE_GAME_DEFINITIONS.map((g) => g.type);
    const casualTypes = CASUAL_GAME_DEFINITIONS.map((g) => g.type);

    // No overlap
    for (const t of coupleTypes) {
      assert.ok(!casualTypes.includes(t), `${t} should not be in casual`);
    }
    for (const t of casualTypes) {
      assert.ok(!coupleTypes.includes(t), `${t} should not be in couple`);
    }
  });

  it('all casual game definitions have unique types', () => {
    const types = CASUAL_GAME_DEFINITIONS.map((g) => g.type);
    const unique = new Set(types);
    assert.equal(unique.size, types.length);
  });

  it('Memory Match has board-based questionPerRound', () => {
    const def = getGameDefinition('memory-match')!;
    assert.equal(def.questionsPerRound, 8);
    assert.equal(def.questions.length, 0); // Board-based
  });

  it('Word Scramble has text questions with correctAnswer', () => {
    const def = getGameDefinition('word-scramble')!;
    assert.ok(def.questions.length > 0);
    for (const q of def.questions) {
      assert.ok(q.correctAnswer, `${q.id} missing correctAnswer`);
    }
  });

  it('Riddle Room has text questions with correctAnswer', () => {
    const def = getGameDefinition('riddle-room')!;
    assert.ok(def.questions.length > 0);
    for (const q of def.questions) {
      assert.ok(q.correctAnswer, `${q.id} missing correctAnswer`);
    }
  });

  it('Casual Trivia has multiple-choice questions', () => {
    const def = getGameDefinition('casual-trivia')!;
    assert.ok(def.questions.length > 0);
    for (const q of def.questions) {
      assert.ok(q.options && q.options.length >= 2, `${q.id} missing options`);
    }
  });
});
