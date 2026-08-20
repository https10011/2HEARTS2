/**
 * Phase 9 — Timeline Tests
 *
 * Tests the timeline event data model, repository CRUD, service layer,
 * validation, migration, and route structure.
 */

import { describe, it, before, after } from 'node:test';
import assert from 'node:assert/strict';
import {
  initializeDatabase,
  finalizeDatabaseForTests,
  getDatabase,
} from '../src/data/database/connection.ts';
import { TimelineRepository } from '../src/repositories/timelineRepository.ts';
import { TimelineService } from '../src/services/timeline/timelineService.ts';
import {
  timelineSerializer,
  type TimelineEvent,
} from '../src/data/timeline/timelineTypes.ts';
import { RoutePath } from '../src/navigation/routes.ts';
import { PERSISTENCE_CONFIG } from '../src/config/persistence.ts';

const FIXED_CLOCK = () => new Date('2026-01-15T12:00:00Z');

// ---------------------------------------------------------------------------
// Route structure tests
// ---------------------------------------------------------------------------

describe('Phase 9 routes', () => {
  it('defines timeline routes', () => {
    assert.equal(RoutePath.appTimelineRoot, '/app/timeline');
    assert.equal(RoutePath.appTimelineAdd, '/app/timeline/add');
    assert.ok(RoutePath.appTimelineDetail.startsWith('/app/timeline/'));
    assert.ok(RoutePath.appTimelineEdit.startsWith('/app/timeline/'));
  });

  it('timeline routes follow app nesting convention', () => {
    assert.ok(RoutePath.appTimelineRoot.startsWith('/app/'));
    assert.ok(RoutePath.appTimelineAdd.startsWith('/app/'));
  });
});

// ---------------------------------------------------------------------------
// Migration tests
// ---------------------------------------------------------------------------

describe('Timeline migration', () => {
  before(async () => {
    await initializeDatabase();
  });

  after(async () => {
    await finalizeDatabaseForTests();
  });

  it('schema version is 6 after migration', () => {
    assert.equal(PERSISTENCE_CONFIG.schemaVersion, 8);
  });

  it('creates timeline_events table', async () => {
    const db = await getDatabase();
    const tables = await db.query<{ name: string }>(
      "SELECT name FROM sqlite_master WHERE type = 'table' AND name = 'timeline_events'",
    );
    assert.equal(tables.length, 1);
  });

  it('timeline_events table has expected columns', async () => {
    const db = await getDatabase();
    const columns = await db.query<{ name: string }>(
      "PRAGMA table_info(timeline_events)",
    );
    const colNames = columns.map((c) => c.name);
    assert.ok(colNames.includes('id'));
    assert.ok(colNames.includes('title'));
    assert.ok(colNames.includes('event_date'));
    assert.ok(colNames.includes('description'));
    assert.ok(colNames.includes('created_at'));
    assert.ok(colNames.includes('updated_at'));
    assert.ok(colNames.includes('deleted_at'));
  });

  it('timeline_events table has indexes', async () => {
    const db = await getDatabase();
    const indexes = await db.query<{ name: string }>(
      "SELECT name FROM sqlite_master WHERE type = 'index' AND tbl_name = 'timeline_events'",
    );
    const idxNames = indexes.map((i) => i.name);
    assert.ok(idxNames.includes('idx_timeline_events_date'));
    assert.ok(idxNames.includes('idx_timeline_events_deleted'));
    assert.ok(idxNames.includes('idx_timeline_events_updated'));
  });
});

// ---------------------------------------------------------------------------
// Serialization tests
// ---------------------------------------------------------------------------

describe('Timeline serialization', () => {
  it('timelineSerializer round-trips domain -> params -> row', () => {
    const event: TimelineEvent = {
      id: 'test-event-id',
      title: 'First Date',
      eventDate: '2024-06-15',
      description: 'Our first date at the coffee shop',
      createdAt: '2026-01-15T12:00:00.000Z',
      updatedAt: '2026-01-15T12:00:00.000Z',
      deletedAt: null,
    };

    const params = timelineSerializer.toParams(event);
    const row: Record<string, unknown> = {};
    timelineSerializer.columns.forEach((col, i) => {
      row[col] = params[i];
    });

    const deserialized = timelineSerializer.fromRow(row);
    assert.equal(deserialized.id, event.id);
    assert.equal(deserialized.title, event.title);
    assert.equal(deserialized.eventDate, event.eventDate);
    assert.equal(deserialized.description, event.description);
    assert.equal(deserialized.createdAt, event.createdAt);
    assert.equal(deserialized.updatedAt, event.updatedAt);
    assert.equal(deserialized.deletedAt, event.deletedAt);
  });

  it('serializer handles null description', () => {
    const row: Record<string, unknown> = {
      id: 'test-id',
      title: 'Event',
      event_date: '2024-01-01',
      description: null,
      created_at: '2026-01-15T12:00:00.000Z',
      updated_at: '2026-01-15T12:00:00.000Z',
      deleted_at: null,
    };
    const result = timelineSerializer.fromRow(row);
    assert.equal(result.description, null);
    assert.equal(result.title, 'Event');
  });
});

// ---------------------------------------------------------------------------
// Repository CRUD tests
// ---------------------------------------------------------------------------

describe('TimelineRepository CRUD', () => {
  let repo: TimelineRepository;

  before(async () => {
    await initializeDatabase();
    const db = await getDatabase();
    repo = new TimelineRepository(db, FIXED_CLOCK);
  });

  after(async () => {
    await finalizeDatabaseForTests();
  });

  it('creates a timeline event', async () => {
    const event = await repo.create({
      title: 'Got Engaged',
      eventDate: '2025-12-25',
      description: 'She said yes!',
      deletedAt: null,
    });
    assert.ok(event.id);
    assert.equal(event.title, 'Got Engaged');
    assert.equal(event.eventDate, '2025-12-25');
    assert.equal(event.description, 'She said yes!');
  });

  it('retrieves an event by id', async () => {
    const created = await repo.create({
      title: 'First Trip',
      eventDate: '2024-08-01',
      description: 'Beach vacation',
      deletedAt: null,
    });
    const found = await repo.getById(created.id);
    assert.ok(found);
    assert.equal(found.title, 'First Trip');
    assert.equal(found.eventDate, '2024-08-01');
  });

  it('lists events ordered by event_date ASC', async () => {
    // Create events in reverse chronological order
    await repo.create({ title: 'Later Event', eventDate: '2026-01-01', description: null, deletedAt: null });
    await repo.create({ title: 'Earlier Event', eventDate: '2023-01-01', description: null, deletedAt: null });
    const events = await repo.listEvents();
    assert.ok(events.length >= 2);
    // First event should have earliest date
    for (let i = 1; i < events.length; i++) {
      assert.ok(events[i - 1].eventDate <= events[i].eventDate);
    }
  });

  it('filters by date range', async () => {
    await repo.create({ title: 'In Range', eventDate: '2025-06-15', description: null, deletedAt: null });
    const inRange = await repo.listByDateRange('2025-01-01', '2025-12-31');
    assert.ok(inRange.length >= 1);
    assert.ok(inRange.every((e) => e.eventDate >= '2025-01-01' && e.eventDate <= '2025-12-31'));
  });

  it('searches events by title', async () => {
    await repo.create({
      title: 'Unique Title XYZ',
      eventDate: '2024-06-01',
      description: null,
      deletedAt: null,
    });
    const results = await repo.search('Unique Title XYZ');
    assert.ok(results.length >= 1);
    assert.ok(results.some((e) => e.title.includes('Unique Title XYZ')));
  });

  it('searches events by description', async () => {
    await repo.create({
      title: 'Another Event',
      eventDate: '2024-06-01',
      description: 'Contains the word purple elephant',
      deletedAt: null,
    });
    const results = await repo.search('purple elephant');
    assert.ok(results.length >= 1);
  });

  it('updates an event', async () => {
    const created = await repo.create({
      title: 'Original Title',
      eventDate: '2024-06-01',
      description: null,
      deletedAt: null,
    });
    const updated = await repo.update(created.id, { title: 'Updated Title' });
    assert.equal(updated.title, 'Updated Title');
    assert.ok(updated.updatedAt);
  });

  it('soft-deletes an event', async () => {
    const created = await repo.create({
      title: 'To Delete',
      eventDate: '2024-06-01',
      description: null,
      deletedAt: null,
    });
    const result = await repo.delete(created.id);
    assert.equal(result, true);
    const found = await repo.getById(created.id);
    assert.equal(found, null);
  });

  it('returns false when deleting non-existent event', async () => {
    const result = await repo.delete('non-existent-id');
    assert.equal(result, false);
  });

  it('counts active events', async () => {
    const before = await repo.count();
    await repo.create({
      title: 'Count Test',
      eventDate: '2024-06-01',
      description: null,
      deletedAt: null,
    });
    const after = await repo.count();
    assert.equal(after, before + 1);
  });

  it('excerpt handles null description', () => {
    const result = TimelineRepository.excerpt(null);
    assert.equal(result, '');
  });

  it('excerpt truncates long description', () => {
    const short = TimelineRepository.excerpt('Short text', 120);
    assert.equal(short, 'Short text');

    const long = TimelineRepository.excerpt('A'.repeat(200), 120);
    assert.equal(long.length, 121);
    assert.ok(long.endsWith('…'));
  });
});

// ---------------------------------------------------------------------------
// TimelineService tests
// ---------------------------------------------------------------------------

describe('TimelineService', () => {
  let service: TimelineService;

  before(async () => {
    await initializeDatabase();
    const db = await getDatabase();
    service = new TimelineService(db, FIXED_CLOCK);
  });

  after(async () => {
    await finalizeDatabaseForTests();
  });

  it('creates an event via service', async () => {
    const result = await service.createEvent({
      title: 'First Date',
      eventDate: '2024-06-15',
      description: 'A wonderful evening',
    });
    assert.ok(result.id);
    assert.equal(result.title, 'First Date');
    assert.equal(result.eventDate, '2024-06-15');
    assert.equal(result.description, 'A wonderful evening');
    assert.ok(typeof result.excerpt === 'string');
  });

  it('validates empty title', () => {
    const result = service.validateInput({ title: '', eventDate: '2024-06-15' });
    assert.equal(result.ok, false);
    assert.ok(result.errors.length > 0);
  });

  it('validates missing date', () => {
    const result = service.validateInput({ title: 'Test', eventDate: '' });
    assert.equal(result.ok, false);
  });

  it('validates invalid date format', () => {
    const result = service.validateInput({ title: 'Test', eventDate: 'not-a-date' });
    assert.equal(result.ok, false);
  });

  it('accepts valid input', () => {
    const result = service.validateInput({
      title: 'Valid Event',
      eventDate: '2024-06-15',
      description: 'Some description',
    });
    assert.equal(result.ok, true);
  });

  it('accepts input without description', () => {
    const result = service.validateInput({
      title: 'Event Without Desc',
      eventDate: '2024-06-15',
    });
    assert.equal(result.ok, true);
  });

  it('lists events via service', async () => {
    await service.createEvent({ title: 'List Event 1', eventDate: '2024-01-01' });
    await service.createEvent({ title: 'List Event 2', eventDate: '2024-02-01' });
    const events = await service.listEvents();
    assert.ok(events.length >= 2);
  });

  it('gets a single event via service', async () => {
    const created = await service.createEvent({ title: 'Get Service Event', eventDate: '2024-03-01' });
    const found = await service.getEvent(created.id);
    assert.equal(found.title, 'Get Service Event');
    assert.ok(typeof found.excerpt === 'string');
  });

  it('throws for missing event', async () => {
    try {
      await service.getEvent('non-existent');
      assert.fail('Should have thrown');
    } catch (err) {
      assert.ok(err instanceof Error);
      assert.ok(err.message.includes('not-found'));
    }
  });

  it('updates an event via service', async () => {
    const created = await service.createEvent({ title: 'Update Me', eventDate: '2024-04-01' });
    const updated = await service.updateEvent(created.id, { title: 'Updated!' });
    assert.equal(updated.title, 'Updated!');
  });

  it('deletes an event via service', async () => {
    const created = await service.createEvent({ title: 'Delete Me', eventDate: '2024-05-01' });
    const deleted = await service.deleteEvent(created.id);
    assert.equal(deleted, true);
    try {
      await service.getEvent(created.id);
      assert.fail('Should have thrown');
    } catch {
      // Expected
    }
  });

  it('handles delete of non-existent event gracefully', async () => {
    const result = await service.deleteEvent('non-existent');
    assert.equal(result, false);
  });

  it('gets count via service', async () => {
    const count = await service.getCount();
    assert.ok(typeof count === 'number');
    assert.ok(count >= 0);
  });

  it('events are sorted chronologically', async () => {
    await service.createEvent({ title: 'Future', eventDate: '2030-01-01' });
    await service.createEvent({ title: 'Past', eventDate: '2020-01-01' });
    const events = await service.listEvents();
    // Past should come before Future
    const pastIdx = events.findIndex((e) => e.title === 'Past');
    const futureIdx = events.findIndex((e) => e.title === 'Future');
    assert.ok(pastIdx < futureIdx);
  });
});

// ---------------------------------------------------------------------------
// Timeline edge cases
// ---------------------------------------------------------------------------

describe('Timeline edge cases', () => {
  let service: TimelineService;

  before(async () => {
    await initializeDatabase();
    const db = await getDatabase();
    service = new TimelineService(db, FIXED_CLOCK);
  });

  after(async () => {
    await finalizeDatabaseForTests();
  });

  it('handles events with empty description', async () => {
    const event = await service.createEvent({
      title: 'No Description',
      eventDate: '2024-06-01',
    });
    assert.equal(event.description, '');
    assert.equal(event.excerpt, '');
  });

  it('handles events with very long description', async () => {
    const longDesc = 'A'.repeat(1000);
    const event = await service.createEvent({
      title: 'Long Desc Event',
      eventDate: '2024-06-01',
      description: longDesc,
    });
    assert.equal(event.description.length, 1000);
    assert.ok(event.excerpt.length <= 121);
  });

  it('rejects title that is too long', () => {
    const result = service.validateInput({
      title: 'A'.repeat(201),
      eventDate: '2024-06-01',
    });
    assert.equal(result.ok, false);
  });

  it('rejects description that is too long', () => {
    const result = service.validateInput({
      title: 'Valid',
      eventDate: '2024-06-01',
      description: 'A'.repeat(5001),
    });
    assert.equal(result.ok, false);
  });

  it('handles event creation with null description', async () => {
    const event = await service.createEvent({
      title: 'Null Desc',
      eventDate: '2024-06-01',
      description: null,
    });
    assert.equal(event.description, '');
  });

  it('handles updating only description', async () => {
    const created = await service.createEvent({
      title: 'Update Desc Only',
      eventDate: '2024-06-01',
    });
    const updated = await service.updateEvent(created.id, {
      description: 'New description',
    });
    assert.equal(updated.description, 'New description');
    assert.equal(updated.title, 'Update Desc Only');
  });

  it('handles updating only date', async () => {
    const created = await service.createEvent({
      title: 'Update Date Only',
      eventDate: '2024-06-01',
    });
    const updated = await service.updateEvent(created.id, {
      eventDate: '2025-12-25',
    });
    assert.equal(updated.eventDate, '2025-12-25');
    assert.equal(updated.title, 'Update Date Only');
  });
});
