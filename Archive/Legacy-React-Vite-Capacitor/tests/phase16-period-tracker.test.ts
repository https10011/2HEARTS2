/**
 * Phase 16 — Period Tracker Tests
 *
 * Tests the period data model, migration, repository, service,
 * cycle calculations, validation, and edge cases.
 */

import { describe, it } from 'node:test';
import assert from 'node:assert/strict';

import {
  type PeriodEntry,
  type FlowLevel,
  FLOW_LEVELS,
  PERIOD_ENTRY_COLUMNS,
  periodEntrySerializer,
  assertPeriodEntry,
  addDays,
  diffDays,
} from '../src/data/period/periodTypes.ts';
import { createMemoryAdapter, runMigrations } from './helpers.ts';
import { PeriodRepository } from '../src/repositories/periodRepository.ts';
import { PeriodService } from '../src/services/period/periodService.ts';

// ---------------------------------------------------------------------------
// Data model
// ---------------------------------------------------------------------------

describe('Period data model', () => {
  it('has correct flow levels', () => {
    assert.deepStrictEqual(FLOW_LEVELS, ['light', 'medium', 'heavy']);
  });

  it('assertPeriodEntry accepts valid entry', () => {
    const entry: PeriodEntry = {
      id: '550e8400-e29b-41d4-a716-446655440000',
      startDate: '2026-08-01',
      endDate: '2026-08-05',
      flowLevel: 'medium',
      note: null,
      profileId: 'owner-1',
      createdAt: '2026-08-01T00:00:00.000Z',
      updatedAt: '2026-08-01T00:00:00.000Z',
      deletedAt: null,
    };
    assert.doesNotThrow(() => assertPeriodEntry(entry));
  });

  it('assertPeriodEntry rejects invalid flow level', () => {
    const entry: PeriodEntry = {
      id: '550e8400-e29b-41d4-a716-446655440000',
      startDate: '2026-08-01',
      endDate: null,
      flowLevel: 'extreme' as FlowLevel,
      note: null,
      profileId: 'owner-1',
      createdAt: '2026-08-01T00:00:00.000Z',
      updatedAt: '2026-08-01T00:00:00.000Z',
      deletedAt: null,
    };
    assert.throws(() => assertPeriodEntry(entry), /flowLevel must be one of/);
  });

  it('addDays works correctly', () => {
    assert.equal(addDays('2026-08-01', 5), '2026-08-06');
    assert.equal(addDays('2026-08-31', 1), '2026-09-01');
    assert.equal(addDays('2026-12-31', 1), '2027-01-01');
  });

  it('diffDays works correctly', () => {
    assert.equal(diffDays('2026-08-01', '2026-08-06'), 5);
    assert.equal(diffDays('2026-08-06', '2026-08-01'), -5);
    assert.equal(diffDays('2026-08-01', '2026-08-01'), 0);
  });
});

// ---------------------------------------------------------------------------
// Serializer
// ---------------------------------------------------------------------------

describe('Period serializer', () => {
  it('has correct column count', () => {
    assert.equal(PERIOD_ENTRY_COLUMNS.length, 9);
  });

  it('round-trips through toParams/fromRow', () => {
    const entry: PeriodEntry = {
      id: '550e8400-e29b-41d4-a716-446655440000',
      startDate: '2026-08-01',
      endDate: '2026-08-05',
      flowLevel: 'heavy',
      note: 'Heavy flow',
      profileId: 'owner-1',
      createdAt: '2026-08-01T00:00:00.000Z',
      updatedAt: '2026-08-01T00:00:00.000Z',
      deletedAt: null,
    };

    const params = periodEntrySerializer.toParams(entry);
    const row: Record<string, unknown> = {};
    PERIOD_ENTRY_COLUMNS.forEach((col, i) => {
      row[col] = params[i];
    });

    const restored = periodEntrySerializer.fromRow(row as any);
    assert.equal(restored.id, entry.id);
    assert.equal(restored.startDate, entry.startDate);
    assert.equal(restored.endDate, entry.endDate);
    assert.equal(restored.flowLevel, entry.flowLevel);
    assert.equal(restored.note, entry.note);
  });
});

// ---------------------------------------------------------------------------
// Migration
// ---------------------------------------------------------------------------

describe('Period migration', () => {
  it('creates period_entries table', async () => {
    const adapter = await createMemoryAdapter();
    await runMigrations(adapter);
    const rows = await adapter.query('SELECT name FROM sqlite_master WHERE type="table" AND name="period_entries"');
    assert.equal(rows.length, 1);
  });

  it('creates period_settings table', async () => {
    const adapter = await createMemoryAdapter();
    await runMigrations(adapter);
    const rows = await adapter.query('SELECT name FROM sqlite_master WHERE type="table" AND name="period_settings"');
    assert.equal(rows.length, 1);
  });

  it('creates proper indexes', async () => {
    const adapter = await createMemoryAdapter();
    await runMigrations(adapter);
    const rows = await adapter.query(
      "SELECT name FROM sqlite_master WHERE type='index' AND tbl_name='period_entries'"
    );
    const indexNames = rows.map((r: any) => r.name);
    assert.ok(indexNames.includes('idx_period_entries_profile'));
    assert.ok(indexNames.includes('idx_period_entries_start'));
    assert.ok(indexNames.includes('idx_period_entries_deleted'));
  });
});

// ---------------------------------------------------------------------------
// Repository
// ---------------------------------------------------------------------------

describe('PeriodRepository', () => {
  it('create and retrieve an entry', async () => {
    const adapter = await createMemoryAdapter();
    await runMigrations(adapter);
    const repo = new PeriodRepository(adapter);

    const created = await repo.createEntry({
      startDate: '2026-08-01',
      endDate: '2026-08-05',
      flowLevel: 'medium',
      note: null,
      profileId: 'owner-1',
    });

    assert.ok(created.id);
    assert.equal(created.startDate, '2026-08-01');

    const retrieved = await repo.getEntryById(created.id);
    assert.ok(retrieved);
    assert.equal(retrieved.startDate, '2026-08-01');
  });

  it('list entries sorted by date descending', async () => {
    const adapter = await createMemoryAdapter();
    await runMigrations(adapter);
    const repo = new PeriodRepository(adapter);

    await repo.createEntry({ startDate: '2026-06-01', endDate: '2026-06-05', flowLevel: 'light', note: null, profileId: 'owner-1' });
    await repo.createEntry({ startDate: '2026-08-01', endDate: '2026-08-05', flowLevel: 'heavy', note: null, profileId: 'owner-1' });
    await repo.createEntry({ startDate: '2026-07-01', endDate: '2026-07-05', flowLevel: 'medium', note: null, profileId: 'owner-1' });

    const list = await repo.listEntries('owner-1');
    assert.equal(list.length, 3);
    assert.equal(list[0].startDate, '2026-08-01');
    assert.equal(list[1].startDate, '2026-07-01');
    assert.equal(list[2].startDate, '2026-06-01');
  });

  it('settings save and retrieve', async () => {
    const adapter = await createMemoryAdapter();
    await runMigrations(adapter);
    const repo = new PeriodRepository(adapter);

    const saved = await repo.saveSettings('owner-1', 30, 6);
    assert.equal(saved.cycleLengthDays, 30);
    assert.equal(saved.periodLengthDays, 6);

    const retrieved = await repo.getSettings('owner-1');
    assert.ok(retrieved);
    assert.equal(retrieved.cycleLengthDays, 30);
  });

  it('settings upsert works', async () => {
    const adapter = await createMemoryAdapter();
    await runMigrations(adapter);
    const repo = new PeriodRepository(adapter);

    await repo.saveSettings('owner-1', 28, 5);
    const updated = await repo.saveSettings('owner-1', 32, 4);
    assert.equal(updated.cycleLengthDays, 32);
    assert.equal(updated.periodLengthDays, 4);

    // Only one settings row per profile
    const settings = await adapter.query('SELECT COUNT(*) AS c FROM period_settings WHERE profile_id = ?', ['owner-1']);
    assert.equal((settings[0] as any).c, 1);
  });

  it('delete soft-deletes', async () => {
    const adapter = await createMemoryAdapter();
    await runMigrations(adapter);
    const repo = new PeriodRepository(adapter);

    const created = await repo.createEntry({
      startDate: '2026-08-01',
      endDate: null,
      flowLevel: 'light',
      note: null,
      profileId: 'owner-1',
    });

    const deleted = await repo.deleteEntry(created.id);
    assert.equal(deleted, true);

    const retrieved = await repo.getEntryById(created.id);
    assert.equal(retrieved, null);
  });
});

// ---------------------------------------------------------------------------
// Service
// ---------------------------------------------------------------------------

describe('PeriodService', () => {
  it('logPeriod validates and persists', async () => {
    const adapter = await createMemoryAdapter();
    await runMigrations(adapter);
    const repo = new PeriodRepository(adapter);
    const service = new PeriodService(repo);

    const entry = await service.logPeriod({
      startDate: '2026-08-01',
      endDate: '2026-08-05',
      flowLevel: 'medium',
      profileId: 'owner-1',
    });

    assert.ok(entry.id);
    assert.equal(entry.startDate, '2026-08-01');
  });

  it('logPeriod rejects end date before start', async () => {
    const adapter = await createMemoryAdapter();
    await runMigrations(adapter);
    const repo = new PeriodRepository(adapter);
    const service = new PeriodService(repo);

    await assert.rejects(
      service.logPeriod({
        startDate: '2026-08-05',
        endDate: '2026-08-01',
        flowLevel: 'medium',
        profileId: 'owner-1',
      }),
      (err: unknown) => {
        assert.ok(err instanceof Error);
        return true;
      },
    );
  });

  it('saveSettings validates range', async () => {
    const adapter = await createMemoryAdapter();
    await runMigrations(adapter);
    const repo = new PeriodRepository(adapter);
    const service = new PeriodService(repo);

    await assert.rejects(
      service.saveSettings('owner-1', 10, 5), // too short
      (err: unknown) => {
        assert.ok(err instanceof Error);
        return true;
      },
    );
  });

  it('getCycleInfo calculates correctly', async () => {
    const adapter = await createMemoryAdapter();
    await runMigrations(adapter);
    const repo = new PeriodRepository(adapter);
    const service = new PeriodService(repo);

    await service.logPeriod({
      startDate: '2026-07-01',
      endDate: '2026-07-05',
      flowLevel: 'medium',
      profileId: 'owner-1',
    });

    const info = await service.getCycleInfo('owner-1');
    assert.ok(info);
    assert.equal(info.cycleLength, 28);
    assert.ok(info.currentCycleDay > 0);
    assert.ok(info.nextPeriodDate > '2026-07-01');
  });

  it('getCycleInfo returns null with no entries', async () => {
    const adapter = await createMemoryAdapter();
    await runMigrations(adapter);
    const repo = new PeriodRepository(adapter);
    const service = new PeriodService(repo);

    const info = await service.getCycleInfo('owner-1');
    assert.equal(info, null);
  });

  it('getSummary computes averages', async () => {
    const adapter = await createMemoryAdapter();
    await runMigrations(adapter);
    const repo = new PeriodRepository(adapter);
    const service = new PeriodService(repo);

    await service.logPeriod({ startDate: '2026-06-01', endDate: '2026-06-05', flowLevel: 'medium', profileId: 'owner-1' });
    await service.logPeriod({ startDate: '2026-07-01', endDate: '2026-07-06', flowLevel: 'light', profileId: 'owner-1' });

    const summary = await service.getSummary('owner-1');
    assert.equal(summary.totalCycles, 2);
    assert.ok(summary.averageCycleLength);
    assert.ok(summary.averagePeriodDuration);
  });

  it('delete removes entry', async () => {
    const adapter = await createMemoryAdapter();
    await runMigrations(adapter);
    const repo = new PeriodRepository(adapter);
    const service = new PeriodService(repo);

    const created = await service.logPeriod({
      startDate: '2026-08-01',
      flowLevel: 'heavy',
      profileId: 'owner-1',
    });

    await service.deleteEntry(created.id);
    const result = await service.getEntryById(created.id);
    assert.equal(result, null);
  });
});

// ---------------------------------------------------------------------------
// Edge cases
// ---------------------------------------------------------------------------

describe('Period edge cases', () => {
  it('survives schema upgrade from v9 to v10', async () => {
    const adapter = await createMemoryAdapter();
    const { ALL_MIGRATIONS } = await import('../src/data/database/migrations/index.ts');
    const pre16 = ALL_MIGRATIONS.filter((m) => m.id <= 9);
    await runMigrations(adapter, pre16);

    await runMigrations(adapter, ALL_MIGRATIONS);

    const rows = await adapter.query('SELECT name FROM sqlite_master WHERE type="table" AND name="period_entries"');
    assert.equal(rows.length, 1);

    const settings = await adapter.query('SELECT name FROM sqlite_master WHERE type="table" AND name="period_settings"');
    assert.equal(settings.length, 1);
  });
});
