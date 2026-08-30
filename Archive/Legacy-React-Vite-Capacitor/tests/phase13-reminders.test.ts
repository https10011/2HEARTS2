/**
 * Phase 13 — Reminders tests.
 *
 * Tests the complete reminder system: data model, migration, repository,
 * service, recurrence calculations, notification coordination, and edge cases.
 */

import { describe, it } from 'node:test';
import assert from 'node:assert/strict';

import {
  type Reminder,
  type ReminderRecurrence,
  type ReminderStatus,
  REMINDER_RECURRENCES,
  REMINDER_STATUSES,
  nextOccurrence,
  formatReminderDateTime,
  assertReminder,
  reminderSerializer,
  REMINDER_COLUMNS,
} from '../src/data/reminder/reminderTypes.ts';
import {
  createMemoryAdapter,
  runMigrations,
} from './helpers.ts';
import { ReminderRepository } from '../src/repositories/reminderRepository.ts';
import { ReminderService } from '../src/services/reminder/reminderService.ts';
import { AppError } from '../src/services/errors/appError.ts';

// ---------------------------------------------------------------------------
// Data model
// ---------------------------------------------------------------------------

describe('Reminder data model', () => {
  it('has correct recurrence values', () => {
    assert.deepStrictEqual(REMINDER_RECURRENCES, ['none', 'daily', 'weekly', 'monthly', 'yearly']);
  });

  it('has correct status values', () => {
    assert.deepStrictEqual(REMINDER_STATUSES, ['active', 'completed', 'dismissed', 'missed']);
  });

  it('assertReminder accepts valid reminder', () => {
    const reminder: Reminder = {
      id: '550e8400-e29b-41d4-a716-446655440000',
      title: 'Test Reminder',
      description: null,
      scheduledDate: '2026-08-20',
      scheduledTime: '10:00',
      recurrence: 'none',
      status: 'active',
      notificationOwnerRef: null,
      notificationEnabled: true,
      createdAt: '2026-08-20T00:00:00.000Z',
      updatedAt: '2026-08-20T00:00:00.000Z',
      deletedAt: null,
    };
    assert.doesNotThrow(() => assertReminder(reminder));
  });

  it('assertReminder rejects empty title', () => {
    const reminder: Reminder = {
      id: '550e8400-e29b-41d4-a716-446655440000',
      title: '',
      description: null,
      scheduledDate: '2026-08-20',
      scheduledTime: '10:00',
      recurrence: 'none',
      status: 'active',
      notificationOwnerRef: null,
      notificationEnabled: true,
      createdAt: '2026-08-20T00:00:00.000Z',
      updatedAt: '2026-08-20T00:00:00.000Z',
      deletedAt: null,
    };
    assert.throws(() => assertReminder(reminder), /title must not be empty/);
  });

  it('assertReminder rejects invalid date format', () => {
    const reminder: Reminder = {
      id: '550e8400-e29b-41d4-a716-446655440000',
      title: 'Test',
      description: null,
      scheduledDate: 'not-a-date',
      scheduledTime: '10:00',
      recurrence: 'none',
      status: 'active',
      notificationOwnerRef: null,
      notificationEnabled: true,
      createdAt: '2026-08-20T00:00:00.000Z',
      updatedAt: '2026-08-20T00:00:00.000Z',
      deletedAt: null,
    };
    assert.throws(() => assertReminder(reminder), /scheduledDate must be a yyyy-mm-dd/);
  });

  it('assertReminder rejects invalid time format', () => {
    const reminder: Reminder = {
      id: '550e8400-e29b-41d4-a716-446655440000',
      title: 'Test',
      description: null,
      scheduledDate: '2026-08-20',
      scheduledTime: '10:00:00',
      recurrence: 'none',
      status: 'active',
      notificationOwnerRef: null,
      notificationEnabled: true,
      createdAt: '2026-08-20T00:00:00.000Z',
      updatedAt: '2026-08-20T00:00:00.000Z',
      deletedAt: null,
    };
    assert.throws(() => assertReminder(reminder), /scheduledTime must be HH:mm/);
  });
});

// ---------------------------------------------------------------------------
// Serializer
// ---------------------------------------------------------------------------

describe('Reminder serializer', () => {
  it('has correct column count', () => {
    assert.equal(REMINDER_COLUMNS.length, 12);
  });

  it('round-trips through toParams/fromRow', () => {
    const reminder: Reminder = {
      id: '550e8400-e29b-41d4-a716-446655440000',
      title: 'Test Reminder',
      description: 'Some notes',
      scheduledDate: '2026-08-20',
      scheduledTime: '10:30',
      recurrence: 'weekly',
      status: 'active',
      notificationOwnerRef: 'reminder-123',
      notificationEnabled: true,
      createdAt: '2026-08-20T00:00:00.000Z',
      updatedAt: '2026-08-20T00:00:00.000Z',
      deletedAt: null,
    };

    const params = reminderSerializer.toParams(reminder);
    const row: Record<string, unknown> = {};
    REMINDER_COLUMNS.forEach((col, i) => {
      row[col] = params[i];
    });

    const restored = reminderSerializer.fromRow(row as any);
    assert.equal(restored.id, reminder.id);
    assert.equal(restored.title, reminder.title);
    assert.equal(restored.description, reminder.description);
    assert.equal(restored.scheduledDate, reminder.scheduledDate);
    assert.equal(restored.scheduledTime, reminder.scheduledTime);
    assert.equal(restored.recurrence, reminder.recurrence);
    assert.equal(restored.status, reminder.status);
    assert.equal(restored.notificationOwnerRef, reminder.notificationOwnerRef);
    assert.equal(restored.notificationEnabled, reminder.notificationEnabled);
  });
});

// ---------------------------------------------------------------------------
// Recurrence calculations
// ---------------------------------------------------------------------------

describe('Recurrence calculations', () => {
  it('nextOccurrence returns null for none', () => {
    assert.equal(nextOccurrence('2026-08-20', 'none'), null);
  });

  it('daily recurrence advances by 1 day', () => {
    assert.equal(nextOccurrence('2026-08-20', 'daily'), '2026-08-21');
  });

  it('weekly recurrence advances by 7 days', () => {
    assert.equal(nextOccurrence('2026-08-20', 'weekly'), '2026-08-27');
  });

  it('monthly recurrence advances by 1 month', () => {
    assert.equal(nextOccurrence('2026-08-20', 'monthly'), '2026-09-20');
  });

  it('yearly recurrence advances by 1 year', () => {
    assert.equal(nextOccurrence('2026-08-20', 'yearly'), '2027-08-20');
  });

  it('monthly handles month-end overflow', () => {
    assert.equal(nextOccurrence('2026-01-31', 'monthly'), '2026-02-28');
  });

  it('daily handles month boundary', () => {
    assert.equal(nextOccurrence('2026-08-31', 'daily'), '2026-09-01');
  });

  it('weekly handles month boundary', () => {
    assert.equal(nextOccurrence('2026-08-28', 'weekly'), '2026-09-04');
  });
});

// ---------------------------------------------------------------------------
// Format helpers
// ---------------------------------------------------------------------------

describe('formatReminderDateTime', () => {
  it('formats date and time correctly', () => {
    assert.equal(formatReminderDateTime('2026-08-20', '14:30'), '08/20/2026 at 14:30');
  });

  it('formats with zero-padded values', () => {
    assert.equal(formatReminderDateTime('2026-01-05', '09:05'), '01/05/2026 at 09:05');
  });
});

// ---------------------------------------------------------------------------
// Repository
// ---------------------------------------------------------------------------

describe('ReminderRepository', () => {
  it('create and retrieve a reminder', async () => {
    const adapter = await createMemoryAdapter();
    await runMigrations(adapter);
    const repo = new ReminderRepository(adapter);

    const created = await repo.create({
      title: 'Test Reminder',
      description: 'Notes here',
      scheduledDate: '2026-08-20',
      scheduledTime: '10:00',
      recurrence: 'none',
      status: 'active',
      notificationOwnerRef: null,
      notificationEnabled: true,
    });

    assert.ok(created.id);
    assert.equal(created.title, 'Test Reminder');
    assert.equal(created.description, 'Notes here');
    assert.equal(created.scheduledDate, '2026-08-20');
    assert.equal(created.status, 'active');

    const retrieved = await repo.getById(created.id);
    assert.ok(retrieved);
    assert.equal(retrieved.title, 'Test Reminder');
  });

  it('list returns all non-deleted reminders', async () => {
    const adapter = await createMemoryAdapter();
    await runMigrations(adapter);
    const repo = new ReminderRepository(adapter);

    await repo.create({
      title: 'First',
      description: null,
      scheduledDate: '2026-08-20',
      scheduledTime: '10:00',
      recurrence: 'none',
      status: 'active',
      notificationOwnerRef: null,
      notificationEnabled: true,
    });
    await repo.create({
      title: 'Second',
      description: null,
      scheduledDate: '2026-08-21',
      scheduledTime: '11:00',
      recurrence: 'daily',
      status: 'active',
      notificationOwnerRef: null,
      notificationEnabled: false,
    });

    const list = await repo.list();
    assert.equal(list.length, 2);
    assert.equal(list[0].title, 'First');
    assert.equal(list[1].title, 'Second');
  });

  it('update modifies fields', async () => {
    const adapter = await createMemoryAdapter();
    await runMigrations(adapter);
    const repo = new ReminderRepository(adapter);

    const created = await repo.create({
      title: 'Original',
      description: null,
      scheduledDate: '2026-08-20',
      scheduledTime: '10:00',
      recurrence: 'none',
      status: 'active',
      notificationOwnerRef: null,
      notificationEnabled: true,
    });

    const updated = await repo.update(created.id, { title: 'Updated Title' });
    assert.ok(updated);
    assert.equal(updated.title, 'Updated Title');
    assert.equal(updated.scheduledDate, '2026-08-20'); // unchanged
  });

  it('delete soft-deletes', async () => {
    const adapter = await createMemoryAdapter();
    await runMigrations(adapter);
    const repo = new ReminderRepository(adapter);

    const created = await repo.create({
      title: 'To Delete',
      description: null,
      scheduledDate: '2026-08-20',
      scheduledTime: '10:00',
      recurrence: 'none',
      status: 'active',
      notificationOwnerRef: null,
      notificationEnabled: true,
    });

    const deleted = await repo.delete(created.id);
    assert.equal(deleted, true);

    const retrieved = await repo.getById(created.id);
    assert.equal(retrieved, null);
  });

  it('listUpcoming returns only future active reminders', async () => {
    const adapter = await createMemoryAdapter();
    await runMigrations(adapter);
    const repo = new ReminderRepository(adapter);

    await repo.create({
      title: 'Active',
      description: null,
      scheduledDate: '2099-12-31',
      scheduledTime: '10:00',
      recurrence: 'none',
      status: 'active',
      notificationOwnerRef: null,
      notificationEnabled: true,
    });
    await repo.create({
      title: 'Completed',
      description: null,
      scheduledDate: '2099-12-31',
      scheduledTime: '10:00',
      recurrence: 'none',
      status: 'completed',
      notificationOwnerRef: null,
      notificationEnabled: true,
    });

    const upcoming = await repo.listUpcoming();
    assert.equal(upcoming.length, 1);
    assert.equal(upcoming[0].title, 'Active');
  });

  it('count returns correct number', async () => {
    const adapter = await createMemoryAdapter();
    await runMigrations(adapter);
    const repo = new ReminderRepository(adapter);

    assert.equal(await repo.count(), 0);

    await repo.create({
      title: 'One',
      description: null,
      scheduledDate: '2026-08-20',
      scheduledTime: '10:00',
      recurrence: 'none',
      status: 'active',
      notificationOwnerRef: null,
      notificationEnabled: true,
    });
    await repo.create({
      title: 'Two',
      description: null,
      scheduledDate: '2026-08-21',
      scheduledTime: '10:00',
      recurrence: 'none',
      status: 'active',
      notificationOwnerRef: null,
      notificationEnabled: true,
    });

    assert.equal(await repo.count(), 2);
  });
});

// ---------------------------------------------------------------------------
// Service
// ---------------------------------------------------------------------------

describe('ReminderService', () => {
  it('create validates and persists', async () => {
    const adapter = await createMemoryAdapter();
    await runMigrations(adapter);
    const repo = new ReminderRepository(adapter);
    const service = new ReminderService(repo, null);

    const created = await service.create({
      title: 'Service Reminder',
      description: 'Test notes',
      scheduledDate: '2026-08-20',
      scheduledTime: '14:30',
      recurrence: 'daily',
      notificationEnabled: false,
    });

    assert.ok(created.id);
    assert.equal(created.title, 'Service Reminder');
    assert.equal(created.recurrence, 'daily');
    assert.equal(created.notificationEnabled, false);
  });

  it('create rejects empty title', async () => {
    const adapter = await createMemoryAdapter();
    await runMigrations(adapter);
    const repo = new ReminderRepository(adapter);
    const service = new ReminderService(repo, null);

    await assert.rejects(
      service.create({
        title: '',
        scheduledDate: '2026-08-20',
        scheduledTime: '10:00',
        recurrence: 'none',
        notificationEnabled: false,
      }),
      (err: unknown) => {
        assert.ok(err instanceof AppError);
        assert.ok(err.userMessage.toLowerCase().includes('title'));
        return true;
      },
    );
  });

  it('create rejects invalid date', async () => {
    const adapter = await createMemoryAdapter();
    await runMigrations(adapter);
    const repo = new ReminderRepository(adapter);
    const service = new ReminderService(repo, null);

    await assert.rejects(
      service.create({
        title: 'Test',
        scheduledDate: 'not-a-date',
        scheduledTime: '10:00',
        recurrence: 'none',
        notificationEnabled: false,
      }),
      (err: unknown) => {
        assert.ok(err instanceof AppError);
        assert.ok(err.userMessage.toLowerCase().includes('date'));
        return true;
      },
    );
  });

  it('update validates and persists', async () => {
    const adapter = await createMemoryAdapter();
    await runMigrations(adapter);
    const repo = new ReminderRepository(adapter);
    const service = new ReminderService(repo, null);

    const created = await service.create({
      title: 'Original',
      scheduledDate: '2026-08-20',
      scheduledTime: '10:00',
      recurrence: 'none',
      notificationEnabled: false,
    });

    const updated = await service.update(created.id, { title: 'Updated' });
    assert.equal(updated.title, 'Updated');
  });

  it('update rejects non-existent reminder', async () => {
    const adapter = await createMemoryAdapter();
    await runMigrations(adapter);
    const repo = new ReminderRepository(adapter);
    const service = new ReminderService(repo, null);

    await assert.rejects(
      service.update('non-existent-id', { title: 'Test' }),
      (err: unknown) => {
        assert.ok(err instanceof AppError);
        assert.ok(err.code.includes('not-found'));
        return true;
      },
    );
  });

  it('delete removes reminder', async () => {
    const adapter = await createMemoryAdapter();
    await runMigrations(adapter);
    const repo = new ReminderRepository(adapter);
    const service = new ReminderService(repo, null);

    const created = await service.create({
      title: 'To Delete',
      scheduledDate: '2026-08-20',
      scheduledTime: '10:00',
      recurrence: 'none',
      notificationEnabled: false,
    });

    await service.delete(created.id);
    const result = await service.getById(created.id);
    assert.equal(result, null);
  });

  it('complete transitions to completed status', async () => {
    const adapter = await createMemoryAdapter();
    await runMigrations(adapter);
    const repo = new ReminderRepository(adapter);
    const service = new ReminderService(repo, null);

    const created = await service.create({
      title: 'Complete Me',
      scheduledDate: '2026-08-20',
      scheduledTime: '10:00',
      recurrence: 'none',
      notificationEnabled: false,
    });

    const completed = await service.complete(created.id);
    assert.equal(completed.status, 'completed');
  });

  it('complete on recurring reminder advances date', async () => {
    const adapter = await createMemoryAdapter();
    await runMigrations(adapter);
    const repo = new ReminderRepository(adapter);
    const service = new ReminderService(repo, null);

    const created = await service.create({
      title: 'Daily Reminder',
      scheduledDate: '2026-08-20',
      scheduledTime: '10:00',
      recurrence: 'daily',
      notificationEnabled: false,
    });

    const next = await service.complete(created.id);
    assert.equal(next.scheduledDate, '2026-08-21');
    assert.equal(next.status, 'active'); // stays active for recurring
  });

  it('dismiss transitions to dismissed status', async () => {
    const adapter = await createMemoryAdapter();
    await runMigrations(adapter);
    const repo = new ReminderRepository(adapter);
    const service = new ReminderService(repo, null);

    const created = await service.create({
      title: 'Dismiss Me',
      scheduledDate: '2026-08-20',
      scheduledTime: '10:00',
      recurrence: 'none',
      notificationEnabled: false,
    });

    const dismissed = await service.dismiss(created.id);
    assert.equal(dismissed.status, 'dismissed');
  });
});

// ---------------------------------------------------------------------------
// Migration
// ---------------------------------------------------------------------------

describe('Reminders migration', () => {
  it('creates reminders table on fresh database', async () => {
    const adapter = await createMemoryAdapter();
    await runMigrations(adapter);

    // Should be able to query the reminders table
    const rows = await adapter.query('SELECT name FROM sqlite_master WHERE type="table" AND name="reminders"');
    assert.equal(rows.length, 1);
  });

  it('creates proper indexes', async () => {
    const adapter = await createMemoryAdapter();
    await runMigrations(adapter);

    const rows = await adapter.query(
      "SELECT name FROM sqlite_master WHERE type='index' AND tbl_name='reminders'"
    );
    const indexNames = rows.map((r: any) => r.name);
    assert.ok(indexNames.includes('idx_reminders_date'));
    assert.ok(indexNames.includes('idx_reminders_status'));
    assert.ok(indexNames.includes('idx_reminders_deleted'));
    assert.ok(indexNames.includes('idx_reminders_updated'));
    assert.ok(indexNames.includes('idx_reminders_notification_owner'));
  });
});
