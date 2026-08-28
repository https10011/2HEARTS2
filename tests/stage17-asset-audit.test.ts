/**
 * Stage 17 — Branding + Asset Completeness Audit tests.
 *
 * Source-guard tests verifying that key screens have consistent branding
 * and decorative elements. No DOM, no sql.js, no mocks.
 */

import { describe, it } from 'node:test';
import assert from 'node:assert/strict';
import { readFileSync } from 'node:fs';
import { join } from 'node:path';

const ROOT = process.cwd();
const read = (rel: string) => readFileSync(join(ROOT, rel), 'utf8');

// ---------------------------------------------------------------------------
// Branding consistency
// ---------------------------------------------------------------------------

describe('BrandLogo usage consistency', () => {
  it('SplashScreen uses BrandLogo', () => {
    const src = read('src/features/onboarding/SplashScreen.tsx');
    assert.ok(src.includes('BrandLogo'), 'SplashScreen must use BrandLogo');
  });

  it('WelcomeScreen uses BrandLogo', () => {
    const src = read('src/features/onboarding/WelcomeScreen.tsx');
    assert.ok(src.includes('BrandLogo'), 'WelcomeScreen must use BrandLogo');
  });

  it('HomeScreen uses BrandLogo', () => {
    const src = read('src/features/app-shell/screens/HomeScreen.tsx');
    assert.ok(src.includes('BrandLogo'), 'HomeScreen must use BrandLogo');
  });

  it('BottomNav uses BrandLogo for center button', () => {
    const src = read('src/features/app-shell/BottomNav.tsx');
    assert.ok(src.includes('BrandLogo'), 'BottomNav must use BrandLogo');
  });

  it('AboutScreen uses BrandLogo', () => {
    const src = read('src/features/settings/AboutScreen.tsx');
    assert.ok(src.includes('BrandLogo'), 'AboutScreen must use BrandLogo');
  });

  it('AppLockGate uses BrandLogo', () => {
    const src = read('src/features/settings/AppLockGate.tsx');
    assert.ok(src.includes('BrandLogo'), 'AppLockGate must use BrandLogo');
  });

  it('main.tsx splash uses BrandLogo', () => {
    const src = read('src/main.tsx');
    assert.ok(src.includes('BrandLogo'), 'main.tsx splash must use BrandLogo');
  });
});

// ---------------------------------------------------------------------------
// Floral decoration consistency
// ---------------------------------------------------------------------------

describe('Floral decoration consistency across feature hubs', () => {
  it('HomeScreen has RoseLilyDecoration', () => {
    const src = read('src/features/app-shell/screens/HomeScreen.tsx');
    assert.ok(src.includes('RoseLilyDecoration'), 'HomeScreen must have floral');
  });

  it('UsScreen has RoseLilyDecoration', () => {
    const src = read('src/features/app-shell/screens/UsScreen.tsx');
    assert.ok(src.includes('RoseLilyDecoration'), 'UsScreen must have floral');
  });

  it('MoreScreen has RoseLilyDecoration', () => {
    const src = read('src/features/app-shell/screens/MoreScreen.tsx');
    assert.ok(src.includes('RoseLilyDecoration'), 'MoreScreen must have floral');
  });

  it('RelationshipCounterScreen has RoseLilyDecoration', () => {
    const src = read('src/features/app-shell/screens/RelationshipCounterScreen.tsx');
    assert.ok(src.includes('RoseLilyDecoration'), 'RelationshipCounterScreen must have floral');
  });

  it('MemoriesHome has RoseLilyDecoration', () => {
    const src = read('src/features/memories/MemoriesHome.tsx');
    assert.ok(src.includes('RoseLilyDecoration'), 'MemoriesHome must have floral');
  });

  it('NotesHome has RoseLilyDecoration', () => {
    const src = read('src/features/notes/NotesHome.tsx');
    assert.ok(src.includes('RoseLilyDecoration'), 'NotesHome must have floral');
  });

  it('TimelineHome has RoseLilyDecoration', () => {
    const src = read('src/features/timeline/TimelineHome.tsx');
    assert.ok(src.includes('RoseLilyDecoration'), 'TimelineHome must have floral');
  });

  it('RemindersHome has RoseLilyDecoration', () => {
    const src = read('src/features/reminders/RemindersHome.tsx');
    assert.ok(src.includes('RoseLilyDecoration'), 'RemindersHome must have floral');
  });

  it('PlacesHome has RoseLilyDecoration', () => {
    const src = read('src/features/places/PlacesHome.tsx');
    assert.ok(src.includes('RoseLilyDecoration'), 'PlacesHome must have floral');
  });

  it('MoodHome has RoseLilyDecoration', () => {
    const src = read('src/features/mood/MoodHome.tsx');
    assert.ok(src.includes('RoseLilyDecoration'), 'MoodHome must have floral');
  });

  it('GamesHubScreen has RoseLilyDecoration', () => {
    const src = read('src/features/app-shell/screens/GamesHubScreen.tsx');
    assert.ok(src.includes('RoseLilyDecoration'), 'GamesHubScreen must have floral');
  });

  it('VaultHome has RoseLilyDecoration', () => {
    const src = read('src/features/vault/VaultHome.tsx');
    assert.ok(src.includes('RoseLilyDecoration'), 'VaultHome must have floral');
  });

  it('SearchScreen has RoseLilyDecoration', () => {
    const src = read('src/features/app-shell/screens/SearchScreen.tsx');
    assert.ok(src.includes('RoseLilyDecoration'), 'SearchScreen must have floral');
  });

  it('NotificationCenter has RoseLilyDecoration', () => {
    const src = read('src/features/notifications/NotificationCenter.tsx');
    assert.ok(src.includes('RoseLilyDecoration'), 'NotificationCenter must have floral');
  });
});

// ---------------------------------------------------------------------------
// Period intentionally has no floral (private/medical)
// ---------------------------------------------------------------------------

describe('Period screen intentional design', () => {
  it('PeriodHome deliberately has no floral decoration', () => {
    const src = read('src/features/period/PeriodHome.tsx');
    assert.ok(
      !src.includes('RoseLilyDecoration'),
      'PeriodHome must NOT have floral (private/medical)',
    );
  });
});

// ---------------------------------------------------------------------------
// Decorative system uses centralized component
// ---------------------------------------------------------------------------

describe('No duplicate decoration systems', () => {
  it('features do not inline SVG decorations directly', () => {
    // Check a sample of feature screens for inline <svg> decorative elements
    const screens = [
      'src/features/memories/MemoriesHome.tsx',
      'src/features/notes/NotesHome.tsx',
      'src/features/timeline/TimelineHome.tsx',
      'src/features/mood/MoodHome.tsx',
    ];
    for (const screen of screens) {
      const src = read(screen);
      assert.ok(
        !src.includes('<svg') || src.includes('aria-hidden="true"'),
        `${screen} must not have visible inline SVG decorations`,
      );
    }
  });
});

// ---------------------------------------------------------------------------
// CouplePair consistency
// ---------------------------------------------------------------------------

describe('CouplePair usage', () => {
  it('CouplePair component exists', () => {
    const src = read('src/features/app-shell/couplePair.tsx');
    assert.ok(src.includes('export function CouplePair'), 'CouplePair must be exported');
  });

  it('UsScreen uses CouplePair', () => {
    const src = read('src/features/app-shell/screens/UsScreen.tsx');
    assert.ok(src.includes('CouplePair'), 'UsScreen must use CouplePair');
  });

  it('RelationshipCounterScreen uses CouplePair', () => {
    const src = read('src/features/app-shell/screens/RelationshipCounterScreen.tsx');
    assert.ok(src.includes('CouplePair'), 'RelationshipCounterScreen must use CouplePair');
  });
});
