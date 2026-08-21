/**
 * Phase 23 — Design-system centralization guards.
 *
 * These tests prove that:
 *  1. Every `var(--th-*)` token referenced anywhere in src is defined once in
 *     the single source of truth (src/theme/tokens.css).
 *  2. The official brand asset lives in ONE module (BrandLogo consumes it)
 *     — and features use that module instead of duplicating inline SVG logos.
 *  3. The 20 published Rose/Lily decorations are imported by exactly one
 *     module (decorations.tsx); features route through RoseLilyDecoration.
 *  4. Feature screens must not render emoji glyphs — icons come from the
 *     centralized Icon set (MasterPrompt §22).
 */

import test from 'node:test';
import assert from 'node:assert/strict';
import { readdirSync, readFileSync, existsSync } from 'node:fs';
import { join, relative } from 'node:path';

const ROOT = process.cwd();
const TOKENS_CSS = 'src/theme/tokens.css';

function listFiles(dir: string, exts: string[]): string[] {
  const out: string[] = [];
  if (!existsSync(dir)) return out;
  for (const entry of readdirSync(dir, { withFileTypes: true })) {
    const full = join(dir, entry.name);
    if (entry.isDirectory()) out.push(...listFiles(full, exts));
    else if (exts.some((ext) => full.endsWith(ext))) out.push(full);
  }
  return out;
}

function allVarTokens(file: string, text: string): string[] {
  const tokens: string[] = [];
  const re = /var\(\s*--th-[a-z0-9-]+/gi;
  let m: RegExpExecArray | null;
  while ((m = re.exec(text))) {
    tokens.push(m[0].replace(/var\(\s*/, ''));
  }
  return tokens;
}

// --------------------------------------------------------------------------- 1. Token integrity

test('token integrity: every var(--th-*) used anywhere resolves to a definition in tokens.css', () => {
  const tokensCss = readFileSync(TOKENS_CSS, 'utf8');

  const defined = new Set<string>();
  for (const m of tokensCss.matchAll(/(--th-[a-z0-9-]+)\s*:/gi)) {
    defined.add(m[1]);
  }

  const srcFiles = [
    ...listFiles(join(ROOT, 'src'), ['.ts', '.tsx', '.css']),
    ...listFiles(join(ROOT, 'tests'), ['.ts']),
  ];
  const missing: string[] = [];
  for (const file of srcFiles) {
    const text = readFileSync(file, 'utf8');
    for (const token of allVarTokens(file, text)) {
      if (!defined.has(token)) {
        missing.push(`${relative(ROOT, file)} → ${token}`);
      }
    }
  }
  assert.deepEqual(missing, [], 'tokens used but not defined in tokens.css:\n' + missing.join('\n'));

  // tokens.css is the ONLY allowed definition site for --th-* tokens
  const straySites: string[] = [];
  for (const file of srcFiles) {
    if (file === join(ROOT, TOKENS_CSS)) continue;
    const text = readFileSync(file, 'utf8');
    // tokens.ts (the typed mirror) is the documented exception — it must not
    // *define* --th-* properties either, only reference values.
    if (/--th-[a-z0-9-]+\s*:/i.test(text)) {
      straySites.push(relative(ROOT, file));
    }
  }
  assert.deepEqual(
    straySites,
    [],
    '--th-* token definitions found outside src/theme/tokens.css:\n' + straySites.join('\n'),
  );
});

// --------------------------------------------------------------------------- 2. Brand centralization

test('brand: features use the centralized BrandLogo (no duplicated inline brand SVG)', () => {
  const featureFiles = listFiles(join(ROOT, 'src', 'features'), ['.tsx']);
  const offenders: string[] = [];
  for (const file of featureFiles) {
    const text = readFileSync(file, 'utf8');
    // TwoHearts-branding markup should not exist <svg> blocks inside features.
    if (/<svg[\s>]/i.test(text)) {
      offenders.push(relative(ROOT, file));
    }
  }
  assert.deepEqual(offenders, [], 'files with inline <svg> brand/icon markup:\n' + offenders.join('\n'));
});

test('brand: BrandLogo is the single consumer of assets/branding SVGs', () => {
  const srcFiles = listFiles(join(ROOT, 'src'), ['.tsx', '.ts']);
  const consumers = srcFiles.filter((file) => {
    const text = readFileSync(file, 'utf8');
    return /['"]\.\/assets\/branding\/twohearts-logo\.svg['"]/i.test(text) ||
           /['"]@assets\/branding\/twohearts-logo\.svg['"]/i.test(text) ||
           /assets\/branding\/twohearts-logo\.svg/i.test(text);
  }).map((f) => relative(ROOT, f));
  assert.deepEqual(consumers, ['src/components/BrandLogo.tsx'], 'brand asset consumers: ' + consumers.join(', '));
});

// --------------------------------------------------------------------------- 3. Decorations centralization

test('decorations: RoseLilyDecoration is the only consumer of assets/decorations SVGs', () => {
  const srcFiles = listFiles(join(ROOT, 'src'), ['.tsx', '.ts']);
  const consumers = srcFiles.filter((file) => {
    const text = readFileSync(file, 'utf8');
    return /assets\/decorations\/rose-lily/i.test(text);
  }).map((f) => relative(ROOT, f));
  assert.deepEqual(consumers, ['src/components/decorations.tsx'], 'decoration asset consumers: ' + consumers.join(', '));
});

// --------------------------------------------------------------------------- 4. Icon system (no emoji glyphs in screens)

test('icons: feature screens do not use emoji glyphs as icons (MasterPrompt §22)', () => {
  const featureFiles = listFiles(join(ROOT, 'src', 'features'), ['.tsx']);
  const emojiPattern = /[\u{1F300}-\u{1FAFF}\u{1F000}-\u{1F7FF}\u{2600}-\u{27BF}\u{2B00}-\u{2B50}©®ℹ✓✔]/u;
  const offenders: string[] = [];
  for (const file of featureFiles) {
    const text = readFileSync(file, 'utf8');
    if (emojiPattern.test(text)) offenders.push(relative(ROOT, file));
  }
  assert.deepEqual(offenders, [], 'feature files using emoji literals:\n' + offenders.join('\n'));
});
