/**
 * Phase 4 relationship foundation tests.
 *
 * Real stack end to end: service → repositories → serializer → SQLite
 * (sql.js in Node, identical SQL to the native adapter). No mocks.
 */

import { test } from 'node:test';
import assert from 'node:assert';
import { openMigratedDb } from './helpers.ts';
import { RelationshipService } from '../src/services/relationship/relationshipService.ts';
import { CoupleRepository } from '../src/repositories/coupleRepository.ts';
import { ProfileRepository } from '../src/repositories/profileRepository.ts';
import { ImportantDateRepository } from '../src/repositories/importantDateRepository.ts';
import { AppError } from '../src/services/errors/appError.ts';
import { isValidId } from '../src/utils/ids.ts';
import { isValidIsoTimestamp } from '../src/utils/time.ts';
import { toLocalDateKey } from '../src/services/datetime/datetime.ts';

const FIXED_NOW = () => new Date('2026-08-19T10:00:00Z'); // deterministic clock

test('owner profile persists, obeys entity conventions, links the couple singleton', async () => {
  const db = await openMigratedDb();
  const service = new RelationshipService(db, FIXED_NOW);
  const profiles = new ProfileRepository(db, FIXED_NOW);

  const owner = await service.saveOwner({ displayName: '  Casey  ', birthDate: '1997-04-15' });
  assert.ok(isValidId(owner.id));
  assert.ok(isValidIsoTimestamp(owner.createdAt));
  assert.strictEqual(owner.displayName, 'Casey'); // normalized input
  assert.strictEqual(owner.birthDate, '1997-04-15');
  assert.strictEqual((await profiles.getOwner())?.id, owner.id);

  // Couple row created in the same operation, linking the owner.
  const couple = await new CoupleRepository(db, FIXED_NOW).get();
  assert.strictEqual(couple?.ownerProfileId, owner.id);
  assert.strictEqual(couple?.partnerProfileId, null);
  assert.ok(isValidId(couple!.id));
  await db.close();
});

test('re-saving the owner updates instead of duplicating (live role is unique)', async () => {
  const db = await openMigratedDb();
  const service = new RelationshipService(db, FIXED_NOW);

  const first = await service.saveOwner({ displayName: 'Casey' });
  const second = await service.saveOwner({ displayName: 'Casey K', birthDate: '1997-04-15' });
  assert.strictEqual(second.id, first.id);

  const all = await new ProfileRepository(db, FIXED_NOW).list();
  assert.strictEqual(all.length, 1);
  await db.close();
});

test('the SQL index rejects a second live owner even when the service layer is bypassed', async () => {
  const db = await openMigratedDb();
  const profiles = new ProfileRepository(db, FIXED_NOW);
  await profiles.create({ role: 'owner', displayName: 'A', birthDate: null, deletedAt: null });

  await assert.rejects(
    () => profiles.create({ role: 'owner', displayName: 'B', birthDate: null, deletedAt: null }),
  );
  // A tombstoned owner does not block a new one.
  const first = await profiles.getOwner();
  await profiles.delete(first!.id);
  const replacement = await profiles.create({ role: 'owner', displayName: 'A2', birthDate: null, deletedAt: null });
  assert.ok(replacement.id);
  await db.close();
});

test('partner profile is independent from the owner yet joins the same couple', async () => {
  const db = await openMigratedDb();
  const service = new RelationshipService(db, FIXED_NOW);

  const owner = await service.saveOwner({ displayName: 'Casey' });
  const partner = await service.savePartner({ displayName: 'Jordan', birthDate: '1996-11-02' });
  assert.notStrictEqual(partner.id, owner.id);

  const couple = await new CoupleRepository(db, FIXED_NOW).get();
  assert.strictEqual(couple?.ownerProfileId, owner.id);
  assert.strictEqual(couple?.partnerProfileId, partner.id);
  await db.close();
});

test('singleton couple row: second save preserves identity and applies changes', async () => {
  const db = await openMigratedDb();
  const couple = new CoupleRepository(db, FIXED_NOW);
  assert.strictEqual(await couple.get(), null);

  const first = await couple.save({ ownerProfileId: null, partnerProfileId: null, startDate: null });
  const clock = () => new Date('2026-08-20T10:00:00Z');
  const later = await new CoupleRepository(db, clock).save({
    ownerProfileId: first.ownerProfileId,
    partnerProfileId: first.partnerProfileId,
    startDate: '2019-06-01',
  });
  assert.strictEqual(later.id, first.id);
  assert.strictEqual(later.createdAt, first.createdAt);
  assert.ok(later.updatedAt > first.updatedAt);
  assert.strictEqual(later.startDate, '2019-06-01');
  await db.close();
});

test('invalid dates and names are rejected as typed validation errors (no writes)', async () => {
  const db = await openMigratedDb();
  const service = new RelationshipService(db, FIXED_NOW);

  await assert.rejects(
    () => service.saveOwner({ displayName: '   ' }),
    (error: unknown) => error instanceof AppError && (error as AppError).category === 'validation',
  );
  await assert.rejects(() => service.saveOwner({ displayName: 'Casey', birthDate: 'not-a-date' }), AppError);
  await assert.rejects(() => service.saveOwner({ displayName: 'Casey', birthDate: '2023-02-29' }), AppError);
  await assert.rejects(() => service.setStartDate('2024-13-01'), AppError);

  assert.strictEqual(await service.getOwner(), null); // nothing leaked through
  await db.close();
});

test('relationship summary computes age days and the next anniversary (leap rule included)', async () => {
  const db = await openMigratedDb();
  const service = new RelationshipService(db, FIXED_NOW);
  await service.saveOwner({ displayName: 'Casey' });
  await service.savePartner({ displayName: 'Jordan' });

  const summaryBefore = await service.getSummary();
  assert.strictEqual(summaryBefore.startDate, null);
  assert.strictEqual(summaryBefore.ageDays, null);
  assert.strictEqual(summaryBefore.nextAnniversary, null);
  assert.ok(summaryBefore.owner);
  assert.ok(summaryBefore.partner);

  await service.setStartDate('2020-02-29'); // leap day
  const summary = await service.getSummary();
  assert.strictEqual(summary.startDate, '2020-02-29');
  assert.strictEqual(summary.ageDays, 2363); // exact distance 2020-02-29 → 2026-08-19
  // 2027 is not a leap year → Feb 28, 2027 (the 2026 anniversaries are past).
  assert.strictEqual(summary.nextAnniversary, toLocalDateKey(new Date(2027, 1, 28)));
  assert.ok(summary.daysUntilNextAnniversary! > 0);
  await db.close();
});

test('important dates persist with recurrence and profile ownership', async () => {
  const db = await openMigratedDb();
  const service = new RelationshipService(db, FIXED_NOW);

  const anniversary = await service.addImportantDate({
    title: 'Our anniversary',
    date: '2020-02-29',
    recurrence: 'yearly',
  });
  assert.ok(isValidId(anniversary.id));
  assert.strictEqual(anniversary.profileId, null); // relationship-level

  // Unknown profile references are validation failures, never FK explosions.
  await assert.rejects(
    () => service.addImportantDate({ title: 'X', date: '2020-01-01', profileId: '018ff000-0000-4000-8000-000000000000' }),
    AppError,
  );
  const owner = await service.saveOwner({ displayName: 'Casey' });
  const birthday = await service.addImportantDate({
    title: 'Owner birthday',
    date: '1997-04-15',
    recurrence: 'yearly',
    profileId: owner.id,
  });

  const dates = await service.listImportantDates();
  assert.deepStrictEqual(dates.map((d) => d.title).sort(), ['Our anniversary', 'Owner birthday']);
  assert.strictEqual(await service.removeImportantDate(birthday.id), true);
  assert.strictEqual((await service.listImportantDates()).length, 1); // tombstoned
  assert.strictEqual(await service.removeImportantDate(birthday.id), false); // twice = false
  await db.close();
});

test('repository queries feed future reminder features (profile + recurrence)', async () => {
  const db = await openMigratedDb();
  const service = new RelationshipService(db, FIXED_NOW);
  const dates = new ImportantDateRepository(db, FIXED_NOW);
  const owner = await service.saveOwner({ displayName: 'Casey' });

  await dates.create({ title: 'One-off', date: '2026-12-24', recurrence: 'none', profileId: null, deletedAt: null });
  await dates.create({ title: 'Yearly', date: '2019-01-01', recurrence: 'yearly', profileId: owner.id, deletedAt: null });

  assert.deepStrictEqual((await dates.listForProfile(owner.id)).map((d) => d.title), ['Yearly']);
  assert.deepStrictEqual((await dates.listForProfile(null)).map((d) => d.title), ['One-off']);
  assert.deepStrictEqual((await dates.listRecurring()).map((d) => d.title), ['Yearly']);
  await db.close();
});
