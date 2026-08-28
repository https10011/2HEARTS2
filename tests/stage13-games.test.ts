/**
 * Stage 13 — Games presentation helpers tests.
 *
 * Pure-function tests for gamesPresentation.ts: game personality,
 * scoring messages, accuracy labels, efficiency labels, score display.
 * No DOM, no sql.js, no mocks.
 */

import { describe, it } from 'node:test';
import assert from 'node:assert/strict';
import {
  getGamePersonality,
  accuracyPercent,
  accuracyLabel,
  efficiencyLabel,
  scoreDisplay,
  itemCountText,
  roundProgress,
  roundResultMessage,
  streakText,
  levelUpMessage,
} from '../src/features/games/gamesPresentation.ts';

describe('getGamePersonality', () => {
  it('returns personality for known couple game types', () => {
    const p = getGamePersonality('who-knows-who-better');
    assert.equal(p.category, 'couple');
    assert.ok(p.accent.length > 0);
    assert.ok(p.vibe.length > 0);
    assert.ok(p.theme.length > 0);
  });

  it('returns personality for known casual game types', () => {
    const p = getGamePersonality('memory-match');
    assert.equal(p.category, 'casual');
    assert.equal(p.theme, 'memory');
  });

  it('returns fallback for unknown game types', () => {
    const p = getGamePersonality('unknown-game');
    assert.equal(p.category, 'couple');
    assert.ok(p.accent.length > 0);
  });

  it('has all 10 game types defined', () => {
    const types = [
      'who-knows-who-better', 'guess-my-answer', 'would-you-rather',
      'couple-trivia', 'this-or-that', 'finish-my-sentence',
      'memory-match', 'word-scramble', 'casual-trivia', 'riddle-room',
    ];
    for (const t of types) {
      const p = getGamePersonality(t);
      assert.ok(p.accent.length > 0, `Missing accent for ${t}`);
      assert.ok(p.vibe.length > 0, `Missing vibe for ${t}`);
    }
  });
});

describe('accuracyPercent', () => {
  it('calculates percentage correctly', () => {
    assert.equal(accuracyPercent(8, 10), 80);
    assert.equal(accuracyPercent(0, 10), 0);
    assert.equal(accuracyPercent(10, 10), 100);
  });

  it('returns 0 for zero total', () => {
    assert.equal(accuracyPercent(5, 0), 0);
  });

  it('rounds to nearest integer', () => {
    assert.equal(accuracyPercent(1, 3), 33);
    assert.equal(accuracyPercent(2, 3), 67);
  });
});

describe('accuracyLabel', () => {
  it('returns Outstanding for 90+', () => {
    assert.equal(accuracyLabel(95), 'Outstanding!');
    assert.equal(accuracyLabel(90), 'Outstanding!');
  });

  it('returns Well done for 75-89', () => {
    assert.equal(accuracyLabel(80), 'Well done!');
    assert.equal(accuracyLabel(75), 'Well done!');
  });

  it('returns Good effort for 60-74', () => {
    assert.equal(accuracyLabel(65), 'Good effort!');
  });

  it('returns Not bad for 40-59', () => {
    assert.equal(accuracyLabel(50), 'Not bad!');
  });

  it('returns Keep practicing for < 40', () => {
    assert.equal(accuracyLabel(20), 'Keep practicing!');
  });
});

describe('efficiencyLabel', () => {
  it('returns Incredible memory for 80%+ efficiency', () => {
    assert.equal(efficiencyLabel(8, 8), 'Incredible memory!');
    assert.equal(efficiencyLabel(8, 10), 'Incredible memory!');
  });

  it('returns Great recall for 60-79%', () => {
    assert.equal(efficiencyLabel(6, 10), 'Great recall!');
  });

  it('returns Nice work for 40-59%', () => {
    assert.equal(efficiencyLabel(4, 10), 'Nice work!');
  });

  it('returns Practice makes perfect for < 40%', () => {
    assert.equal(efficiencyLabel(2, 10), 'Practice makes perfect!');
  });

  it('returns fallback for zero pairs', () => {
    assert.equal(efficiencyLabel(0, 0), 'Keep trying!');
  });
});

describe('scoreDisplay', () => {
  it('formats score as X / Y', () => {
    assert.equal(scoreDisplay(5, 10), '5 / 10');
    assert.equal(scoreDisplay(0, 8), '0 / 8');
    assert.equal(scoreDisplay(10, 10), '10 / 10');
  });
});

describe('itemCountText', () => {
  it('returns singular for 1', () => {
    assert.equal(itemCountText(1, 'questions'), '1 question');
    assert.equal(itemCountText(1, 'pairs'), '1 pair');
  });

  it('returns plural for 0 and >1', () => {
    assert.equal(itemCountText(0, 'questions'), '0 questions');
    assert.equal(itemCountText(5, 'pairs'), '5 pairs');
  });
});

describe('roundProgress', () => {
  it('calculates progress percentage', () => {
    assert.equal(roundProgress(5, 10), 50);
    assert.equal(roundProgress(0, 10), 0);
    assert.equal(roundProgress(10, 10), 100);
  });

  it('caps at 100', () => {
    assert.equal(roundProgress(15, 10), 100);
  });

  it('returns 0 for zero total', () => {
    assert.equal(roundProgress(5, 0), 0);
  });
});

describe('roundResultMessage', () => {
  it('returns match message for matched', () => {
    assert.equal(roundResultMessage(true), 'You matched!');
  });

  it('returns no-match message for unmatched', () => {
    assert.equal(roundResultMessage(false), 'No match');
  });
});

describe('streakText', () => {
  it('returns empty for zero', () => {
    assert.equal(streakText(0), '');
  });

  it('returns streak label', () => {
    assert.equal(streakText(3), '3 streak');
    assert.equal(streakText(1), '1 streak');
  });
});

describe('levelUpMessage', () => {
  it('returns appropriate message for high accuracy', () => {
    assert.equal(levelUpMessage(95), 'Brilliant performance!');
  });

  it('returns appropriate message for medium accuracy', () => {
    assert.equal(levelUpMessage(75), 'Well played!');
  });

  it('returns appropriate message for low accuracy', () => {
    assert.equal(levelUpMessage(30), 'You completed the level!');
  });
});
