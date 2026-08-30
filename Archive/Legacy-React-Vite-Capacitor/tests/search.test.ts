import { test } from 'node:test';
import assert from 'node:assert';
import { normalizeQuery, normalizeSearchText } from '../src/services/search/normalize.ts';
import { SearchEngine, scoreCandidate, type SearchProvider } from '../src/services/search/searchEngine.ts';

test('normalizeQuery folds case, diacritics, and whitespace', () => {
  const q = normalizeQuery('  Café   CÚPULA  ');
  assert.ok(q);
  assert.strictEqual(q.text, 'cafe cupula');
  assert.deepStrictEqual(q.tokens, ['cafe', 'cupula']);
  assert.strictEqual(normalizeQuery(''), null);
  assert.strictEqual(normalizeQuery('   '), null);
  assert.strictEqual(normalizeQuery(null), null);
});

test('normalizeQuery caps length and never returns empty', () => {
  const long = normalizeQuery('x'.repeat(500));
  assert.ok(long);
  assert.strictEqual(long.text.length, 200);
});

test('normalizeSearchText mirrors query rules for indexed fields', () => {
  assert.strictEqual(normalizeSearchText(' Héllo   World '), 'hello world');
  assert.strictEqual(normalizeSearchText(undefined), '');
});

test('scoreCandidate ranks exact > prefix > word-initial > substring > body', () => {
  const query = normalizeQuery('anniversary') as NonNullable<ReturnType<typeof normalizeQuery>>;
  const exact = scoreCandidate(query, 'Anniversary');
  const prefix = scoreCandidate(query, 'Anniversary dinner');
  const wordInitial = scoreCandidate(query, 'Our anniversary');
  const substring = scoreCandidate(query, 'Preanniversary');
  const bodyOnly = scoreCandidate(query, 'Plan', 'celebrate our anniversary soon');
  const noMatch = scoreCandidate(query, 'Nothing here');
  assert.ok(exact > prefix && prefix > wordInitial && wordInitial > substring && substring > bodyOnly && bodyOnly > 0);
  assert.strictEqual(noMatch, 0);
});

test('every query token must match somewhere (AND semantics)', () => {
  const query = normalizeQuery('beach trip') as NonNullable<ReturnType<typeof normalizeQuery>>;
  assert.ok(scoreCandidate(query, 'Beach', 'a long trip') > 0);
  assert.strictEqual(scoreCandidate(query, 'Beach day'), 0); // 'trip' missing
});

function fakeProvider(kind: string, results: Array<{ id: string; title: string; updatedAt: string }>): SearchProvider {
  return {
    kind,
    search: async (query) =>
      results
        .map((r) => ({ ...r, kind, score: scoreCandidate(query, r.title), snippet: undefined }))
        .filter((r) => r.score > 0),
  };
}

test('engine merges providers, ranks deterministically, isolates failures', async () => {
  const engine = new SearchEngine();
  engine.registerProvider(fakeProvider('note', [
    { id: 'b', title: 'Trip ideas', updatedAt: '2024-01-02T00:00:00.000Z' },
    { id: 'a', title: 'Trip ideas', updatedAt: '2024-01-05T00:00:00.000Z' },
  ]));
  engine.registerProvider(fakeProvider('memory', [
    { id: 'c', title: 'Trip to Lisbon', updatedAt: '2024-01-03T00:00:00.000Z' },
  ]));
  const failing: SearchProvider = {
    kind: 'broken',
    search: async () => {
      throw new Error('feature blew up');
    },
  };
  engine.registerProvider(failing);

  const results = await engine.search('trip');
  assert.strictEqual(results.matches.length, 3);
  // All three score as prefix matches; ties break by recency desc (a > c > b).
  assert.strictEqual(results.matches[0].id, 'a');
  assert.strictEqual(results.matches[1].id, 'c');
  assert.strictEqual(results.matches[2].id, 'b');
});

test('empty/invalid queries return empty results without touching providers', async () => {
  const engine = new SearchEngine();
  let called = 0;
  engine.registerProvider({
    kind: 'probe',
    search: async () => {
      called += 1;
      return [];
    },
  });
  assert.deepStrictEqual((await engine.search('   ')).matches, []);
  assert.deepStrictEqual((await engine.search('')).matches, []);
  assert.strictEqual(called, 0);
});

test('provider registration is deduped by kind', () => {
  const engine = new SearchEngine();
  engine.registerProvider(fakeProvider('note', []));
  engine.registerProvider(fakeProvider('note', []));
  assert.deepStrictEqual(engine.registeredKinds(), ['note']);
});
