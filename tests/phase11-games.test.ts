/**
 * Phase 11 — Couple Mini-Games Tests
 *
 * Tests the game engine, game service, individual games, scoring,
 * session lifecycle, and verifies the engine is genuinely reusable.
 */

import { describe, it } from 'node:test';
import assert from 'node:assert/strict';
import { RoutePath } from '../src/navigation/routes.ts';
import {
  createSession,
  recordAnswer,
  computeScores,
  nextQuestion,
  completeGame,
} from '../src/services/game/gameEngine.ts';
import { GameService } from '../src/services/game/gameService.ts';
import {
  getGameDefinition,
  ALL_GAME_DEFINITIONS,
  WHO_KNOWS_WHO_BETTER,
  GUESS_MY_ANSWER,
  WOULD_YOU_RATHER,
  COUPLE_TRIVIA,
  THIS_OR_THAT,
  FINISH_MY_SENTENCE,
} from '../src/customization/games/gameContent.ts';
import type { GameType, GameSession, GameDefinition } from '../src/data/game/gameTypes.ts';

const FIXED_CLOCK = () => new Date('2026-01-15T12:00:00Z');

// ---------------------------------------------------------------------------
// Route structure
// ---------------------------------------------------------------------------

describe('Phase 11 routes', () => {
  it('defines game routes', () => {
    assert.ok(RoutePath.appGames);
    assert.ok(RoutePath.appGamesPlay);
    assert.ok(RoutePath.appGamesResults);
  });

  it('defines legacy game routes', () => {
    assert.ok(RoutePath.appGamesWhoKnows);
    assert.ok(RoutePath.appGamesWouldYouRather);
    assert.ok(RoutePath.appGamesTwentyQuestions);
    assert.ok(RoutePath.appGamesHowWell);
  });
});

// ---------------------------------------------------------------------------
// Game engine — core logic
// ---------------------------------------------------------------------------

describe('Game engine', () => {
  it('creates a session with unique id', () => {
    const s1 = createSession('would-you-rather', FIXED_CLOCK);
    const s2 = createSession('would-you-rather', FIXED_CLOCK);
    assert.ok(s1.id);
    assert.ok(s2.id);
    assert.notEqual(s1.id, s2.id);
    assert.equal(s1.gameType, 'would-you-rather');
    assert.equal(s1.completed, false);
    assert.equal(s1.player1Score, 0);
    assert.equal(s1.player2Score, 0);
  });

  it('records answers for both players', () => {
    let s = createSession('would-you-rather', FIXED_CLOCK);
    const def = getGameDefinition('would-you-rather')!;
    s = recordAnswer(s, def, 0, 'player1', 'Stay in', 0, FIXED_CLOCK);
    s = recordAnswer(s, def, 0, 'player2', 'Go out', 1, FIXED_CLOCK);
    const round = s.rounds.find((r) => r.questionIndex === 0);
    assert.ok(round);
    assert.equal(round.answers.length, 2);
    assert.equal(round.complete, true);
  });

  it('computes scores for match games', () => {
    let s = createSession('who-knows-who-better', FIXED_CLOCK);
    const def = getGameDefinition('who-knows-who-better')!;
    const q = def.questions[0];
    s = recordAnswer(s, def, 0, 'player1', q.correctAnswer!, undefined, FIXED_CLOCK);
    s = recordAnswer(s, def, 0, 'player2', q.correctAnswer!, undefined, FIXED_CLOCK);
    s = computeScores(s, def, FIXED_CLOCK);
    assert.equal(s.player1Score, 1);
    assert.equal(s.player2Score, 1);
  });

  it('computes scores for choice games', () => {
    let s = createSession('this-or-that', FIXED_CLOCK);
    const def = getGameDefinition('this-or-that')!;
    s = recordAnswer(s, def, 0, 'player1', 'Coffee', 0, FIXED_CLOCK);
    s = recordAnswer(s, def, 0, 'player2', 'Coffee', 0, FIXED_CLOCK);
    s = computeScores(s, def, FIXED_CLOCK);
    assert.equal(s.player1Score, 1);
    assert.equal(s.player2Score, 1);
  });

  it('scores mismatched choices as 0', () => {
    let s = createSession('this-or-that', FIXED_CLOCK);
    const def = getGameDefinition('this-or-that')!;
    s = recordAnswer(s, def, 0, 'player1', 'Coffee', 0, FIXED_CLOCK);
    s = recordAnswer(s, def, 0, 'player2', 'Tea', 1, FIXED_CLOCK);
    s = computeScores(s, def, FIXED_CLOCK);
    assert.equal(s.player1Score, 0);
    assert.equal(s.player2Score, 0);
  });

  it('advances to next question', () => {
    let s = createSession('would-you-rather', FIXED_CLOCK);
    const def = getGameDefinition('would-you-rather')!;
    assert.equal(s.currentRound, 0);
    const next = nextQuestion(s, def);
    assert.equal(next, 1);
  });

  it('returns null when game is complete', () => {
    const s: GameSession = {
      ...createSession('would-you-rather', FIXED_CLOCK),
      currentRound: 9,
    };
    const def = getGameDefinition('would-you-rather')!;
    const next = nextQuestion(s, def);
    assert.equal(next, null);
  });

  it('completes game and generates result', () => {
    let s = createSession('would-you-rather', FIXED_CLOCK);
    const def = getGameDefinition('would-you-rather')!;
    // Simulate some rounds
    for (let i = 0; i < 5; i++) {
      s = recordAnswer(s, def, i, 'player1', 'A', 0, FIXED_CLOCK);
      s = recordAnswer(s, def, i, 'player2', 'A', 0, FIXED_CLOCK);
    }
    const { result } = completeGame(s, def, FIXED_CLOCK);
    assert.equal(result.totalQuestions, 10);
    assert.equal(result.rounds.length, 5);
    assert.ok(result.message);
  });

  it('does not record answer on completed session', () => {
    const s: GameSession = { ...createSession('would-you-rather', FIXED_CLOCK), completed: true };
    const def = getGameDefinition('would-you-rather')!;
    const updated = recordAnswer(s, def, 0, 'player1', 'test', 0, FIXED_CLOCK);
    assert.equal(updated.rounds.length, 0);
  });
});

// ---------------------------------------------------------------------------
// Game content / definitions
// ---------------------------------------------------------------------------

describe('Game content', () => {
  it('has all 6 game definitions', () => {
    assert.equal(ALL_GAME_DEFINITIONS.length, 6);
  });

  it('getGameDefinition returns known games', () => {
    assert.ok(getGameDefinition('who-knows-who-better'));
    assert.ok(getGameDefinition('guess-my-answer'));
    assert.ok(getGameDefinition('would-you-rather'));
    assert.ok(getGameDefinition('couple-trivia'));
    assert.ok(getGameDefinition('this-or-that'));
    assert.ok(getGameDefinition('finish-my-sentence'));
  });

  it('returns undefined for unknown game type', () => {
    assert.equal(getGameDefinition('unknown-game' as GameType), undefined);
  });

  it('each game has questions', () => {
    for (const def of ALL_GAME_DEFINITIONS) {
      assert.ok(def.questions.length > 0, `${def.type} has no questions`);
      assert.ok(def.questionsPerRound > 0, `${def.type} has 0 questionsPerRound`);
    }
  });

  it('choice games have options', () => {
    const choiceGames: GameType[] = ['would-you-rather', 'this-or-that'];
    for (const type of choiceGames) {
      const def = getGameDefinition(type)!;
      for (const q of def.questions) {
        assert.ok(q.options && q.options.length >= 2, `${type}/${q.id} missing options`);
      }
    }
  });

  it('match games have correctAnswer', () => {
    const matchGames: GameType[] = ['who-knows-who-better', 'guess-my-answer'];
    for (const type of matchGames) {
      const def = getGameDefinition(type)!;
      for (const q of def.questions) {
        assert.ok(q.correctAnswer, `${type}/${q.id} missing correctAnswer`);
      }
    }
  });
});

// ---------------------------------------------------------------------------
// Game service
// ---------------------------------------------------------------------------

describe('GameService', () => {
  const service = new GameService(FIXED_CLOCK);

  it('starts a game', () => {
    const result = service.startGame('would-you-rather');
    assert.ok(result.session);
    assert.ok(result.definition);
    assert.equal(result.session.gameType, 'would-you-rather');
    assert.equal(result.session.completed, false);
  });

  it('throws for unknown game type', () => {
    assert.throws(() => service.startGame('unknown' as GameType));
  });

  it('records answers', () => {
    const { session: s1, definition } = service.startGame('this-or-that');
    const updated = service.answerQuestion(s1, 'this-or-that', 'player1', 'Coffee', 0);
    assert.ok(updated.session);
    assert.equal(updated.session.rounds.length, 1);
  });

  it('validates answers', () => {
    const result = service.validateAnswer('', 'would-you-rather');
    assert.equal(result.ok, false);
    assert.ok(result.errors.length > 0);
  });

  it('accepts valid answers', () => {
    const result = service.validateAnswer('Test answer', 'would-you-rather');
    assert.equal(result.ok, true);
  });

  it('rejects overly long answers', () => {
    const result = service.validateAnswer('A'.repeat(501), 'would-you-rather');
    assert.equal(result.ok, false);
  });

  it('gets current question', () => {
    const { session, definition } = service.startGame('couple-trivia');
    const q = service.getCurrentQuestion(session, 'couple-trivia');
    assert.ok(q);
    assert.equal(q.id, definition.questions[0].id);
  });
});

// ---------------------------------------------------------------------------
// Engine reusability — same engine for all games
// ---------------------------------------------------------------------------

describe('Engine reusability', () => {
  const service = new GameService(FIXED_CLOCK);

  it('who-knows-who-better uses the shared engine', () => {
    const { session } = service.startGame('who-knows-who-better');
    const def = service.getDefinition('who-knows-who-better');
    const q = def.questions[0];
    let s = service.answerQuestion(session, 'who-knows-who-better', 'player1', q.correctAnswer!);
    s = service.answerQuestion(s.session, 'who-knows-who-better', 'player2', q.correctAnswer!);
    assert.ok(s.session.rounds[0].complete);
  });

  it('would-you-rather uses the shared engine', () => {
    const { session } = service.startGame('would-you-rather');
    let s = service.answerQuestion(session, 'would-you-rather', 'player1', 'A', 0);
    s = service.answerQuestion(s.session, 'would-you-rather', 'player2', 'A', 0);
    assert.ok(s.session.rounds[0].complete);
  });

  it('this-or-that uses the shared engine', () => {
    const { session } = service.startGame('this-or-that');
    let s = service.answerQuestion(session, 'this-or-that', 'player1', 'Coffee', 0);
    s = service.answerQuestion(s.session, 'this-or-that', 'player2', 'Tea', 1);
    assert.ok(s.session.rounds[0].complete);
  });

  it('finish-my-sentence uses the shared engine', () => {
    const { session } = service.startGame('finish-my-sentence');
    let s = service.answerQuestion(session, 'finish-my-sentence', 'player1', 'Love');
    s = service.answerQuestion(s.session, 'finish-my-sentence', 'player2', 'Love');
    assert.ok(s.session.rounds[0].complete);
  });

  it('couple-trivia uses the shared engine', () => {
    const { session } = service.startGame('couple-trivia');
    let s = service.answerQuestion(session, 'couple-trivia', 'player1', 'A', 0);
    s = service.answerQuestion(s.session, 'couple-trivia', 'player2', 'A', 0);
    assert.ok(s.session.rounds[0].complete);
  });

  it('guess-my-answer uses the shared engine', () => {
    const { session } = service.startGame('guess-my-answer');
    const def = service.getDefinition('guess-my-answer');
    const q = def.questions[0];
    let s = service.answerQuestion(session, 'guess-my-answer', 'player1', q.correctAnswer!);
    s = service.answerQuestion(s.session, 'guess-my-answer', 'player2', q.correctAnswer!);
    assert.ok(s.session.rounds[0].complete);
  });
});
