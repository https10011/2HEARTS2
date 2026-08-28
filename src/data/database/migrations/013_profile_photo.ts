/**
 * Migration 013 — Profile Photo (Stage 4).
 *
 * Adds `photo_ref` to the `profiles` table so each person can have a local
 * profile image. The value stores a MediaAsset.id reference (foreign-key
 * semantics enforced at the application layer, not SQL) or NULL when no
 * photo is set.
 *
 * Existing profiles continue to work — photo_ref defaults to NULL and
 * the avatar fallback (initials) remains the default presentation.
 */

import type { Migration } from './types.ts';

export const profilePhotoMigration: Migration = {
  id: 13,
  name: 'profile-photo',
  up: async (tx) => {
    await tx.exec(`
      ALTER TABLE profiles ADD COLUMN photo_ref TEXT DEFAULT NULL;
    `);
  },
};
